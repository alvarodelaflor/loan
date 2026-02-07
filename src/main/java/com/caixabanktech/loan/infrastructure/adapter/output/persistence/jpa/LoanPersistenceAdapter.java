package com.caixabanktech.loan.infrastructure.adapter.output.persistence.jpa;

import com.caixabanktech.loan.domain.model.ApplicantIdentity;
import com.caixabanktech.loan.domain.model.LoanApplication;
import com.caixabanktech.loan.domain.model.LoanId;
import com.caixabanktech.loan.domain.port.out.LoanRepositoryPort;
import com.caixabanktech.loan.infrastructure.adapter.input.rest.mapper.LoanPersistenceMapper;
import com.caixabanktech.loan.infrastructure.adapter.output.persistence.jpa.spec.LoanSpecifications;
import jakarta.persistence.EntityManager;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class LoanPersistenceAdapter implements LoanRepositoryPort {

    private final LoanJpaRepository jpaRepository;
    private final EntityManager entityManager;
    private final LoanPersistenceMapper mapper;

    public LoanPersistenceAdapter(LoanJpaRepository jpaRepository, EntityManager entityManager, LoanPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.entityManager = entityManager;
        this.mapper = mapper;
    }

    @Override
    public LoanApplication save(LoanApplication loan) {
        LoanJpaEntity entity = mapper.toEntity(loan);
        // jpaRepository.save(entity) will enable AuditingEntityListener to set createdAt/modifiedAt
        LoanJpaEntity savedEntity = jpaRepository.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<LoanApplication> findById(LoanId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public List<LoanApplication> findAll() {
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<List<LoanApplication>> findHistory(LoanId id) {
        List<LoanJpaEntity> revisions = AuditReaderFactory.get(entityManager).createQuery()
                .forRevisionsOfEntity(LoanJpaEntity.class, true, true)
                .add(AuditEntity.id().eq(id.value()))
                .addOrder(AuditEntity.revisionNumber().asc())
                .getResultList();

        return revisions.stream()
                .filter(loan -> loan.getApplicantName() != null)
                .map(mapper::toDomain)
                .collect(Collectors.collectingAndThen(Collectors.toList(), Optional::of));
    }

    @Override
    public Optional<List<LoanApplication>> findByApplicantIdentity(ApplicantIdentity identity) {
        return jpaRepository.findByApplicantIdentity(identity.value()).stream()
                .map(mapper::toDomain)
                .collect(Collectors.collectingAndThen(Collectors.toList(), Optional::of));
    }

    @Override
    public Optional<List<LoanApplication>> findByCriteria(String identity, Instant startDate, Instant endDate) {
        Specification<LoanJpaEntity> spec = Specification
                .where(LoanSpecifications.hasIdentity(identity))
                .and(LoanSpecifications.createdBetween(startDate, endDate));

        return jpaRepository.findAll(spec).stream()
                .map(mapper::toDomain)
                .collect(Collectors.collectingAndThen(Collectors.toList(), Optional::of));
    }

    @Override
    public void deleteById(LoanId id) {
        jpaRepository.deleteById(id.value());
    }
}

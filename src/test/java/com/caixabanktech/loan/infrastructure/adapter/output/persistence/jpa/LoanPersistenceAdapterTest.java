package com.caixabanktech.loan.infrastructure.adapter.output.persistence.jpa;

import com.caixabanktech.loan.domain.model.ApplicantIdentity;
import com.caixabanktech.loan.domain.model.LoanAmount;
import com.caixabanktech.loan.domain.model.LoanApplication;
import com.caixabanktech.loan.domain.model.LoanId;
import com.caixabanktech.loan.domain.model.LoanStatus;
import com.caixabanktech.loan.infrastructure.adapter.input.rest.mapper.LoanPersistenceMapper;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.envers.query.AuditQueryCreator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@DisplayName("Persistence Adapter Tests: LoanPersistenceAdapter")
class LoanPersistenceAdapterTest {

    @Test
    @DisplayName("save should map domain to entity, persist, and return mapped domain")
    void savePersistsAndReturnsMappedDomain() {
        LoanJpaRepository jpaRepo = mock(LoanJpaRepository.class);
        EntityManager em = mock(EntityManager.class);
        LoanPersistenceMapper mapper = mock(LoanPersistenceMapper.class);
        LoanPersistenceAdapter adapter = new LoanPersistenceAdapter(jpaRepo, em, mapper);

        LoanApplication domain = sampleDomain(LoanStatus.PENDING);
        LoanJpaEntity entity = sampleEntity(domain.getId().value(), domain.getStatus().name());
        LoanJpaEntity savedEntity = sampleEntity(domain.getId().value(), domain.getStatus().name());
        LoanApplication mappedBack = domain;

        when(mapper.toEntity(domain)).thenReturn(entity);
        when(jpaRepo.save(entity)).thenReturn(savedEntity);
        when(mapper.toDomain(savedEntity)).thenReturn(mappedBack);

        LoanApplication result = adapter.save(domain);
        assertSame(mappedBack, result);
        verify(mapper).toEntity(domain);
        verify(jpaRepo).save(entity);
        verify(mapper).toDomain(savedEntity);
    }

    @Test
    @DisplayName("findById should query repository and map to domain")
    void findByIdMaps() {
        LoanJpaRepository jpaRepo = mock(LoanJpaRepository.class);
        EntityManager em = mock(EntityManager.class);
        LoanPersistenceMapper mapper = mock(LoanPersistenceMapper.class);
        LoanPersistenceAdapter adapter = new LoanPersistenceAdapter(jpaRepo, em, mapper);

        LoanId id = new LoanId(UUID.randomUUID());
        LoanJpaEntity entity = sampleEntity(id.value(), "PENDING");
        LoanApplication domain = sampleDomain(LoanStatus.PENDING);

        when(jpaRepo.findById(id.value())).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(domain);

        Optional<LoanApplication> result = adapter.findById(id);
        assertTrue(result.isPresent());
        assertSame(domain, result.get());
        verify(jpaRepo).findById(id.value());
        verify(mapper).toDomain(entity);
    }

    @Test
    @DisplayName("findAll should map all entities to domain list")
    void findAllMapsList() {
        LoanJpaRepository jpaRepo = mock(LoanJpaRepository.class);
        EntityManager em = mock(EntityManager.class);
        LoanPersistenceMapper mapper = mock(LoanPersistenceMapper.class);
        LoanPersistenceAdapter adapter = new LoanPersistenceAdapter(jpaRepo, em, mapper);

        LoanJpaEntity e1 = sampleEntity(UUID.randomUUID(), "PENDING");
        LoanJpaEntity e2 = sampleEntity(UUID.randomUUID(), "APPROVED");
        LoanApplication d1 = sampleDomain(LoanStatus.PENDING);
        LoanApplication d2 = sampleDomain(LoanStatus.APPROVED);

        when(jpaRepo.findAll()).thenReturn(List.of(e1, e2));
        when(mapper.toDomain(e1)).thenReturn(d1);
        when(mapper.toDomain(e2)).thenReturn(d2);

        List<LoanApplication> result = adapter.findAll();
        assertEquals(2, result.size());
        assertTrue(result.containsAll(List.of(d1, d2)));
        verify(jpaRepo).findAll();
        verify(mapper).toDomain(e1);
        verify(mapper).toDomain(e2);
    }

    @Test
    @DisplayName("findHistory should use Envers AuditReader and map revisions to domain")
    void findHistoryMapsRevisions() {
        LoanJpaRepository jpaRepo = mock(LoanJpaRepository.class);
        EntityManager em = mock(EntityManager.class);
        LoanPersistenceMapper mapper = mock(LoanPersistenceMapper.class);
        LoanPersistenceAdapter adapter = new LoanPersistenceAdapter(jpaRepo, em, mapper);

        LoanId id = new LoanId(UUID.randomUUID());
        LoanJpaEntity e1 = sampleEntity(id.value(), "PENDING");
        LoanJpaEntity e2 = sampleEntity(id.value(), "APPROVED");
        LoanApplication d1 = sampleDomain(LoanStatus.PENDING);
        LoanApplication d2 = sampleDomain(LoanStatus.APPROVED);

        AuditReader auditReader = mock(AuditReader.class);
        AuditQueryCreator queryCreator = mock(AuditQueryCreator.class);
        AuditQuery auditQuery = mock(AuditQuery.class);

        try (MockedStatic<AuditReaderFactory> mocked = Mockito.mockStatic(AuditReaderFactory.class)) {
            mocked.when(() -> AuditReaderFactory.get(em)).thenReturn(auditReader);
            when(auditReader.createQuery()).thenReturn(queryCreator);
            when(queryCreator.forRevisionsOfEntity(LoanJpaEntity.class, true, true)).thenReturn(auditQuery);
            // chain methods on auditQuery
            when(auditQuery.add(any())).thenReturn(auditQuery);
            when(auditQuery.addOrder(any())).thenReturn(auditQuery);
            when(auditQuery.getResultList()).thenReturn(List.of(e1, e2));

            when(mapper.toDomain(e1)).thenReturn(d1);
            when(mapper.toDomain(e2)).thenReturn(d2);

            List<LoanApplication> result = adapter.findHistory(id);
            assertEquals(2, result.size());
            assertTrue(result.containsAll(List.of(d1, d2)));

            verify(mapper).toDomain(e1);
            verify(mapper).toDomain(e2);
        }
    }

    @Test
    @DisplayName("findByApplicantIdentity should return Optional with mapped list")
    void findByApplicantIdentityReturnsOptionalList() {
        LoanJpaRepository jpaRepo = mock(LoanJpaRepository.class);
        EntityManager em = mock(EntityManager.class);
        LoanPersistenceMapper mapper = mock(LoanPersistenceMapper.class);
        LoanPersistenceAdapter adapter = new LoanPersistenceAdapter(jpaRepo, em, mapper);

        ApplicantIdentity identity = new ApplicantIdentity("12345678Z");
        LoanJpaEntity e = sampleEntity(UUID.randomUUID(), "PENDING");
        LoanApplication d = sampleDomain(LoanStatus.PENDING);

        when(jpaRepo.findByApplicantIdentity(identity.value())).thenReturn(List.of(e));
        when(mapper.toDomain(e)).thenReturn(d);

        Optional<List<LoanApplication>> result = adapter.findByApplicantIdentity(identity);
        assertTrue(result.isPresent());
        assertEquals(1, result.get().size());
        assertSame(d, result.get().get(0));
        verify(jpaRepo).findByApplicantIdentity(identity.value());
        verify(mapper).toDomain(e);
    }

    @Test
    @DisplayName("findByCriteria should build Specification, query repo, and map list")
    void findByCriteriaBuildsSpecAndMaps() {
        LoanJpaRepository jpaRepo = mock(LoanJpaRepository.class);
        EntityManager em = mock(EntityManager.class);
        LoanPersistenceMapper mapper = mock(LoanPersistenceMapper.class);
        LoanPersistenceAdapter adapter = new LoanPersistenceAdapter(jpaRepo, em, mapper);

        String identity = "12345678Z";
        Instant start = Instant.parse("2026-02-07T00:00:00Z");
        Instant end = Instant.parse("2026-02-08T00:00:00Z");

        LoanJpaEntity e = sampleEntity(UUID.randomUUID(), "APPROVED");
        LoanApplication d = sampleDomain(LoanStatus.APPROVED);

        when(jpaRepo.findAll(any(Specification.class))).thenReturn(List.of(e));
        when(mapper.toDomain(e)).thenReturn(d);

        Optional<List<LoanApplication>> result = adapter.findByCriteria(identity, start, end);
        assertTrue(result.isPresent());
        assertEquals(1, result.get().size());
        assertSame(d, result.get().get(0));
        verify(jpaRepo).findAll(any(Specification.class));
        verify(mapper).toDomain(e);
    }

    private LoanApplication sampleDomain(LoanStatus status) {
        return LoanApplication.builder()
                .id(new LoanId(UUID.randomUUID()))
                .applicantName("Alvaro de la Flor Bonilla")
                .applicantIdentity(new ApplicantIdentity("12345678Z"))
                .loanAmount(new LoanAmount(new BigDecimal("1998.03"), Currency.getInstance("EUR")))
                .createdAt(Instant.parse("2026-02-07T10:00:00Z"))
                .modifiedAt(Instant.parse("2026-02-07T11:00:00Z"))
                .status(status)
                .build();
    }

    private LoanJpaEntity sampleEntity(UUID id, String status) {
        LoanJpaEntity e = new LoanJpaEntity();
        e.setId(id);
        e.setApplicantName("Alvaro de la Flor Bonilla");
        e.setApplicantIdentity("12345678Z");
        e.setAmount(new BigDecimal("1998.03"));
        e.setCurrency("EUR");
        e.setStatus(status);
        e.setCreatedAt(Instant.parse("2026-02-07T10:00:00Z"));
        e.setModifiedAt(Instant.parse("2026-02-07T11:00:00Z"));
        return e;
    }
}


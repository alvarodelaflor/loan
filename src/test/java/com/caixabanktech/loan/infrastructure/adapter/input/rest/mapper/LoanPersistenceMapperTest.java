package com.caixabanktech.loan.infrastructure.adapter.input.rest.mapper;

import com.caixabanktech.loan.domain.model.ApplicantIdentity;
import com.caixabanktech.loan.domain.model.LoanAmount;
import com.caixabanktech.loan.domain.model.LoanApplication;
import com.caixabanktech.loan.domain.model.LoanId;
import com.caixabanktech.loan.domain.model.LoanStatus;
import com.caixabanktech.loan.infrastructure.adapter.output.persistence.jpa.LoanJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Mapper Tests: LoanPersistenceMapper")
class LoanPersistenceMapperTest {

    private final LoanPersistenceMapper mapper = Mappers.getMapper(LoanPersistenceMapper.class);

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

    private LoanJpaEntity sampleEntity(String status) {
        LoanJpaEntity e = new LoanJpaEntity();
        e.setId(UUID.randomUUID());
        e.setApplicantName("Alvaro de la Flor Bonilla");
        e.setApplicantIdentity("12345678Z");
        e.setAmount(new BigDecimal("1998.03"));
        e.setCurrency("EUR");
        e.setStatus(status);
        e.setCreatedAt(Instant.parse("2026-02-07T10:00:00Z"));
        e.setModifiedAt(Instant.parse("2026-02-07T11:00:00Z"));
        return e;
    }

    @Test
    @DisplayName("toEntity should map domain to JPA entity fields correctly")
    void toEntityMapsCorrectly() {
        LoanApplication domain = sampleDomain(LoanStatus.PENDING);
        LoanJpaEntity entity = mapper.toEntity(domain);

        assertEquals(domain.getId().value(), entity.getId());
        assertEquals(domain.getApplicantName(), entity.getApplicantName());
        assertEquals(domain.getApplicantIdentity().value(), entity.getApplicantIdentity());
        assertEquals(domain.getLoanAmount().amount(), entity.getAmount());
        assertEquals(domain.getLoanAmount().currency().getCurrencyCode(), entity.getCurrency());
        assertEquals(domain.getStatus().name(), entity.getStatus());
        // createdAt/modifiedAt are managed by JPA auditing, mapper does not set them
    }

    @Test
    @DisplayName("toDomain should map JPA entity to domain fields correctly")
    void toDomainMapsCorrectly() {
        LoanJpaEntity entity = sampleEntity("APPROVED");
        LoanApplication domain = mapper.toDomain(entity);

        assertEquals(entity.getId(), domain.getId().value());
        assertEquals(entity.getApplicantName(), domain.getApplicantName());
        assertEquals(entity.getApplicantIdentity(), domain.getApplicantIdentity().value());
        assertEquals(entity.getAmount().setScale(2), domain.getLoanAmount().amount());
        assertEquals(entity.getCurrency(), domain.getLoanAmount().currency().getCurrencyCode());
        assertEquals("APPROVED", domain.getStatus().name());
    }

    @Test
    @DisplayName("@Named helpers should construct domain value objects")
    void namedHelpersWork() {
        UUID id = UUID.randomUUID();
        LoanId loanId = mapper.mapToLoanId(id);
        assertEquals(id, loanId.value());

        ApplicantIdentity identity = mapper.mapToIdentity("12345678Z");
        assertEquals("12345678Z", identity.value());

        LoanJpaEntity e = sampleEntity("PENDING");
        LoanAmount amount = mapper.mapToLoanAmount(e);
        assertEquals(new BigDecimal("1998.03"), amount.amount());
        assertEquals(Currency.getInstance("EUR"), amount.currency());
    }
}


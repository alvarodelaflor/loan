package com.caixabanktech.loan.infrastructure.adapter.input.rest.mapper;

import com.caixabanktech.loan.domain.model.ApplicantIdentity;
import com.caixabanktech.loan.domain.model.LoanAmount;
import com.caixabanktech.loan.domain.model.LoanApplication;
import com.caixabanktech.loan.domain.model.LoanId;
import com.caixabanktech.loan.domain.model.LoanStatus;
import com.caixabanktech.loan.infrastructure.adapter.output.persistence.jpa.LoanJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Generated Mapper Tests: LoanPersistenceMapperImpl")
class LoanPersistenceMapperImplTest {

    private final LoanPersistenceMapperImpl mapper = new LoanPersistenceMapperImpl();

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
    @DisplayName("toEntity should return null when domain is null")
    void toEntityReturnsNullOnNullInput() {
        assertNull(mapper.toEntity(null));
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
        assertEquals(domain.getCreatedAt(), entity.getCreatedAt());
        assertEquals(domain.getModifiedAt(), entity.getModifiedAt());
        assertEquals(domain.getStatus().name(), entity.getStatus());
    }

    @Test
    @DisplayName("toDomain should return null when entity is null")
    void toDomainReturnsNullOnNullInput() {
        assertNull(mapper.toDomain(null));
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
        assertEquals(entity.getCreatedAt(), domain.getCreatedAt());
        assertEquals(entity.getModifiedAt(), domain.getModifiedAt());
    }

    @Test
    @DisplayName("toDomain should throw when status is null because domain requires non-null status")
    void toDomainThrowsWhenStatusNull() {
        LoanJpaEntity entity = sampleEntity(null);
        // Since the generated implementation does not set status when null, builder.build() will throw NPE
        assertThrows(NullPointerException.class, () -> mapper.toDomain(entity));
    }

    @Test
    @DisplayName("toDomain should throw NPE when applicantName is null (builder @NonNull)")
    void toDomainThrowsWhenApplicantNameNull() {
        LoanJpaEntity entity = sampleEntity("PENDING");
        entity.setApplicantName(null);
        assertThrows(NullPointerException.class, () -> mapper.toDomain(entity));
    }

    @Test
    @DisplayName("toDomain should throw InvalidDomainDataException when applicantIdentity is null")
    void toDomainThrowsWhenApplicantIdentityNull() {
        LoanJpaEntity entity = sampleEntity("PENDING");
        entity.setApplicantIdentity(null);
        assertThrows(com.caixabanktech.loan.domain.exception.InvalidDomainDataException.class, () -> mapper.toDomain(entity));
    }

    @Test
    @DisplayName("toDomain should throw InvalidDomainDataException when amount is null")
    void toDomainThrowsWhenAmountNull() {
        LoanJpaEntity entity = sampleEntity("PENDING");
        entity.setAmount(null);
        assertThrows(com.caixabanktech.loan.domain.exception.InvalidDomainDataException.class, () -> mapper.toDomain(entity));
    }

    @Test
    @DisplayName("toDomain should throw NullPointerException when currency is null")
    void toDomainThrowsWhenCurrencyNull() {
        LoanJpaEntity entity = sampleEntity("PENDING");
        entity.setCurrency(null);
        assertThrows(NullPointerException.class, () -> mapper.toDomain(entity));
    }

    @Test
    @DisplayName("toEntity should set id null when domain.getId() is null")
    void toEntitySetsNullIdWhenDomainIdNull() {
        LoanApplication domain = Mockito.mock(LoanApplication.class);
        Mockito.when(domain.getId()).thenReturn(null);
        Mockito.when(domain.getApplicantIdentity()).thenReturn(new ApplicantIdentity("12345678Z"));
        Mockito.when(domain.getLoanAmount()).thenReturn(new LoanAmount(new BigDecimal("10"), Currency.getInstance("EUR")));
        Mockito.when(domain.getApplicantName()).thenReturn("Name");
        Mockito.when(domain.getCreatedAt()).thenReturn(Instant.parse("2026-02-07T10:00:00Z"));
        Mockito.when(domain.getModifiedAt()).thenReturn(Instant.parse("2026-02-07T11:00:00Z"));
        Mockito.when(domain.getStatus()).thenReturn(LoanStatus.PENDING);

        LoanJpaEntity entity = mapper.toEntity(domain);
        assertNull(entity.getId());
    }

    @Test
    @DisplayName("toEntity should set applicantIdentity null when domain.getApplicantIdentity() is null")
    void toEntitySetsNullApplicantIdentityWhenDomainIdentityNull() {
        LoanApplication domain = Mockito.mock(LoanApplication.class);
        Mockito.when(domain.getId()).thenReturn(new LoanId(UUID.randomUUID()));
        Mockito.when(domain.getApplicantIdentity()).thenReturn(null);
        Mockito.when(domain.getLoanAmount()).thenReturn(new LoanAmount(new BigDecimal("10"), Currency.getInstance("EUR")));
        Mockito.when(domain.getApplicantName()).thenReturn("Name");
        Mockito.when(domain.getCreatedAt()).thenReturn(Instant.parse("2026-02-07T10:00:00Z"));
        Mockito.when(domain.getModifiedAt()).thenReturn(Instant.parse("2026-02-07T11:00:00Z"));
        Mockito.when(domain.getStatus()).thenReturn(LoanStatus.PENDING);

        LoanJpaEntity entity = mapper.toEntity(domain);
        assertNull(entity.getApplicantIdentity());
    }

    @Test
    @DisplayName("toEntity should set amount and currency null when domain.getLoanAmount() is null")
    void toEntitySetsNullAmountAndCurrencyWhenDomainLoanAmountNull() {
        LoanApplication domain = Mockito.mock(LoanApplication.class);
        Mockito.when(domain.getId()).thenReturn(new LoanId(UUID.randomUUID()));
        Mockito.when(domain.getApplicantIdentity()).thenReturn(new ApplicantIdentity("12345678Z"));
        Mockito.when(domain.getLoanAmount()).thenReturn(null);
        Mockito.when(domain.getApplicantName()).thenReturn("Name");
        Mockito.when(domain.getCreatedAt()).thenReturn(Instant.parse("2026-02-07T10:00:00Z"));
        Mockito.when(domain.getModifiedAt()).thenReturn(Instant.parse("2026-02-07T11:00:00Z"));
        Mockito.when(domain.getStatus()).thenReturn(LoanStatus.PENDING);

        LoanJpaEntity entity = mapper.toEntity(domain);
        assertNull(entity.getAmount());
        assertNull(entity.getCurrency());
    }

    @Test
    @DisplayName("toEntity should set currency null when loanAmount.currency() is null")
    void toEntitySetsNullCurrencyWhenLoanAmountCurrencyNull() {
        LoanApplication domain = Mockito.mock(LoanApplication.class);
        LoanAmount loanAmount = Mockito.mock(LoanAmount.class);
        Mockito.when(domain.getId()).thenReturn(new LoanId(UUID.randomUUID()));
        Mockito.when(domain.getApplicantIdentity()).thenReturn(new ApplicantIdentity("12345678Z"));
        Mockito.when(domain.getLoanAmount()).thenReturn(loanAmount);
        Mockito.when(loanAmount.amount()).thenReturn(new BigDecimal("10"));
        Mockito.when(loanAmount.currency()).thenReturn(null);
        Mockito.when(domain.getApplicantName()).thenReturn("Name");
        Mockito.when(domain.getCreatedAt()).thenReturn(Instant.parse("2026-02-07T10:00:00Z"));
        Mockito.when(domain.getModifiedAt()).thenReturn(Instant.parse("2026-02-07T11:00:00Z"));
        Mockito.when(domain.getStatus()).thenReturn(LoanStatus.PENDING);

        LoanJpaEntity entity = mapper.toEntity(domain);
        assertNull(entity.getCurrency());
        assertEquals(new BigDecimal("10"), entity.getAmount());
    }
}

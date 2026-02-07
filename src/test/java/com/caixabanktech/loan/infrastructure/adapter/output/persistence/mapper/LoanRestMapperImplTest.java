package com.caixabanktech.loan.infrastructure.adapter.output.persistence.mapper;

import com.caixabanktech.loan.domain.model.ApplicantIdentity;
import com.caixabanktech.loan.domain.model.LoanAmount;
import com.caixabanktech.loan.domain.model.LoanApplication;
import com.caixabanktech.loan.domain.model.LoanId;
import com.caixabanktech.loan.domain.model.LoanStatus;
import com.caixabanktech.loan.infrastructure.adapter.input.rest.dto.LoanResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Generated Mapper Tests: LoanRestMapperImpl")
class LoanRestMapperImplTest {

    private final LoanRestMapperImpl mapper = new LoanRestMapperImpl();

    private LoanApplication sampleDomain(LoanStatus status) {
        return LoanApplication.builder()
                .id(new LoanId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")))
                .applicantName("Alvaro de la Flor Bonilla")
                .applicantIdentity(new ApplicantIdentity("12345678Z"))
                .loanAmount(new LoanAmount(new BigDecimal("1998.03"), Currency.getInstance("EUR")))
                .createdAt(Instant.parse("2026-02-07T10:00:00Z"))
                .modifiedAt(Instant.parse("2026-02-07T11:00:00Z"))
                .status(status)
                .build();
    }

    @Test
    @DisplayName("toResponse should return null when domain is null")
    void toResponseReturnsNullOnNullInput() {
        assertNull(mapper.toResponse(null));
    }

    @Test
    @DisplayName("toResponse should map all domain fields correctly")
    void toResponseMapsCorrectly() {
        LoanApplication domain = sampleDomain(LoanStatus.APPROVED);

        LoanResponse response = mapper.toResponse(domain);

        assertEquals("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa", response.id());
        assertEquals(domain.getApplicantName(), response.applicantName());
        assertEquals(domain.getApplicantIdentity().value(), response.applicantIdentity());
        assertEquals(domain.getLoanAmount().amount(), response.loanAmount());
        assertEquals(domain.getLoanAmount().currency().getCurrencyCode(), response.currency());
        assertEquals(domain.getCreatedAt(), response.createdAt());
        assertEquals(domain.getModifiedAt(), response.modifiedAt());
        assertEquals(domain.getStatus().name(), response.status());
    }

    @Test
    @DisplayName("toResponse should handle nested nulls: id, applicantIdentity, loanAmount and currency")
    void toResponseHandlesNestedNulls() {
        LoanApplication domain = Mockito.mock(LoanApplication.class);
        // id null
        Mockito.when(domain.getId()).thenReturn(null);
        // applicantIdentity null
        Mockito.when(domain.getApplicantIdentity()).thenReturn(null);
        // loanAmount con amount y currency null
        LoanAmount loanAmount = Mockito.mock(LoanAmount.class);
        Mockito.when(loanAmount.amount()).thenReturn(null);
        Mockito.when(loanAmount.currency()).thenReturn(null);
        Mockito.when(domain.getLoanAmount()).thenReturn(loanAmount);
        Mockito.when(domain.getApplicantName()).thenReturn("Name");
        Mockito.when(domain.getCreatedAt()).thenReturn(Instant.parse("2026-02-07T10:00:00Z"));
        Mockito.when(domain.getModifiedAt()).thenReturn(Instant.parse("2026-02-07T11:00:00Z"));
        Mockito.when(domain.getStatus()).thenReturn(LoanStatus.PENDING);

        LoanResponse response = mapper.toResponse(domain);
        assertNull(response.id());
        assertNull(response.applicantIdentity());
        assertNull(response.loanAmount());
        assertNull(response.currency());
        assertEquals("Name", response.applicantName());
        assertEquals("PENDING", response.status());
    }

    @Test
    @DisplayName("toResponse should map correctly when loanAmount exists but currency is null")
    void toResponseHandlesNullCurrencyOnly() {
        LoanApplication domain = Mockito.mock(LoanApplication.class);
        LoanAmount loanAmount = Mockito.mock(LoanAmount.class);
        Mockito.when(domain.getId()).thenReturn(new LoanId(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")));
        Mockito.when(domain.getApplicantIdentity()).thenReturn(new ApplicantIdentity("87654321X"));
        Mockito.when(domain.getApplicantName()).thenReturn("Name2");
        Mockito.when(domain.getCreatedAt()).thenReturn(Instant.parse("2026-02-07T10:00:00Z"));
        Mockito.when(domain.getModifiedAt()).thenReturn(Instant.parse("2026-02-07T11:00:00Z"));
        Mockito.when(domain.getStatus()).thenReturn(LoanStatus.REJECTED);
        Mockito.when(domain.getLoanAmount()).thenReturn(loanAmount);
        Mockito.when(loanAmount.amount()).thenReturn(new BigDecimal("10.00"));
        Mockito.when(loanAmount.currency()).thenReturn(null);

        LoanResponse response = mapper.toResponse(domain);
        assertEquals("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb", response.id());
        assertEquals("87654321X", response.applicantIdentity());
        assertEquals(new BigDecimal("10.00"), response.loanAmount());
        assertNull(response.currency());
        assertEquals("REJECTED", response.status());
    }

    @Test
    @DisplayName("toResponse should handle loanAmount null: covers if (loanAmount == null) in currency and amount")
    void toResponseHandlesNullLoanAmountObject() {
        LoanApplication domain = Mockito.mock(LoanApplication.class);
        Mockito.when(domain.getId()).thenReturn(new LoanId(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc")));
        Mockito.when(domain.getApplicantIdentity()).thenReturn(new ApplicantIdentity("12345678Z"));
        Mockito.when(domain.getLoanAmount()).thenReturn(null);
        Mockito.when(domain.getApplicantName()).thenReturn("Name3");
        Mockito.when(domain.getCreatedAt()).thenReturn(Instant.parse("2026-02-07T10:00:00Z"));
        Mockito.when(domain.getModifiedAt()).thenReturn(Instant.parse("2026-02-07T11:00:00Z"));
        Mockito.when(domain.getStatus()).thenReturn(LoanStatus.PENDING);

        LoanResponse response = mapper.toResponse(domain);
        assertEquals("cccccccc-cccc-cccc-cccc-cccccccccccc", response.id());
        assertEquals("12345678Z", response.applicantIdentity());
        assertNull(response.loanAmount());
        assertNull(response.currency());
        assertEquals("Name3", response.applicantName());
        assertEquals("PENDING", response.status());
    }

    @Test
    @DisplayName("toResponse should throw NPE when status is null")
    void toResponseThrowsWhenStatusNull() {
        LoanApplication domain = Mockito.mock(LoanApplication.class);
        Mockito.when(domain.getId()).thenReturn(new LoanId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")));
        Mockito.when(domain.getApplicantIdentity()).thenReturn(new ApplicantIdentity("12345678Z"));
        Mockito.when(domain.getLoanAmount()).thenReturn(new LoanAmount(new BigDecimal("1.00"), Currency.getInstance("EUR")));
        Mockito.when(domain.getApplicantName()).thenReturn("Name4");
        Mockito.when(domain.getCreatedAt()).thenReturn(Instant.parse("2026-02-07T10:00:00Z"));
        Mockito.when(domain.getModifiedAt()).thenReturn(Instant.parse("2026-02-07T11:00:00Z"));
        Mockito.when(domain.getStatus()).thenReturn(null);
        assertThrows(NullPointerException.class, () -> mapper.toResponse(domain));
    }

    @Test
    @DisplayName("toResponseList should return null when the input list is null")
    void toResponseListReturnsNullOnNullInput() {
        assertNull(mapper.toResponseList(null));
    }

    @Test
    @DisplayName("toResponseList should map each element using toResponse")
    void toResponseListMapsAll() {
        List<LoanApplication> domains = Arrays.asList(
                sampleDomain(LoanStatus.PENDING),
                sampleDomain(LoanStatus.APPROVED)
        );

        List<LoanResponse> responses = mapper.toResponseList(domains);
        assertEquals(2, responses.size());
        assertEquals(domains.get(0).getStatus().name(), responses.get(0).status());
        assertEquals(domains.get(1).getStatus().name(), responses.get(1).status());
        assertEquals(domains.get(0).getApplicantName(), responses.get(0).applicantName());
    }
}

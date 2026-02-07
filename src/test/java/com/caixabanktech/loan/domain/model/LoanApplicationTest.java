package com.caixabanktech.loan.domain.model;

import com.caixabanktech.loan.domain.exception.InvalidDomainDataException;
import com.caixabanktech.loan.domain.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Domain Tests: Entity LoanApplication")
class LoanApplicationTest {

    @Test
    @DisplayName("A valid request with an initial status of PENDING must be created.")
    void shouldCreateValidRequest() {
        LoanApplication loanApplication = aLoanApplication().build();

        assertThat(loanApplication.getId()).isNotNull();
        assertThat(loanApplication.getStatus()).isEqualTo(LoanStatus.PENDING);
        assertThat(loanApplication.getCreatedAt()).isBeforeOrEqualTo(Instant.now());
        assertThat(loanApplication.getLoanAmount().amount()).isEqualTo(new BigDecimal("123.45"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-10", "-0.01"})
    @DisplayName("Amounts less than or equal to zero should not be allowed")
    void shouldRejectInvalidAmounts(String amountStr) {
        assertThatThrownBy(() -> aLoanApplication().loanAmount(new LoanAmount(new BigDecimal(amountStr), Currency.getInstance("EUR"))))
                .isInstanceOf(InvalidDomainDataException.class);
    }

    @Test
    @DisplayName("Nulls should not be allowed in the main constructor")
    void shouldRejectNulls() {
        assertThatThrownBy(LoanApplication.builder()::build)
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Transition from PENDING to APPROVED must be successful")
    void shouldApprovePending() {
        LoanApplication loanApplication = aLoanApplication().build();
        loanApplication.approve();
        assertThat(loanApplication.getStatus()).isEqualTo(LoanStatus.APPROVED);
    }

    @Test
    @DisplayName("Transition PENDING -> REJECTED must be successful")
    void shouldRejectPending() {
        LoanApplication loanApplication = aLoanApplication().build();
        loanApplication.reject();
        assertThat(loanApplication.getStatus()).isEqualTo(LoanStatus.REJECTED);
    }

    @Test
    @DisplayName("Transition APPROVED -> CANCELED must be successful")
    void shouldCancelApproved() {
        LoanApplication loanApplication = aLoanApplication()
                .status(LoanStatus.APPROVED)
                .build();
        loanApplication.cancel();
        assertThat(loanApplication.getStatus()).isEqualTo(LoanStatus.CANCELLED);
    }

    @ParameterizedTest
    @EnumSource(value = LoanStatus.class, names = {"APPROVED", "REJECTED", "CANCELLED"})
    @DisplayName("It cannot be approved if it is not PENDING")
    void shouldFailApproveIfNotPending(LoanStatus initialStatus) {
        LoanApplication loanApplication = aLoanApplication()
                .status(initialStatus)
                .build();
        assertThatThrownBy(loanApplication::approve)
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessage("Only PENDING -> APPROVED");
    }

    @ParameterizedTest
    @EnumSource(value = LoanStatus.class, names = {"APPROVED", "REJECTED", "CANCELLED"})
    @DisplayName("It cannot be rejected if it is not PENDING")
    void shouldFailRejectedIfNotPending(LoanStatus initialStatus) {
        LoanApplication loanApplication = aLoanApplication()
                .status(initialStatus)
                .build();
        assertThatThrownBy(loanApplication::reject)
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessage("Only PENDING -> REJECTED");
    }

    @ParameterizedTest
    @EnumSource(value = LoanStatus.class, names = {"PENDING", "REJECTED", "CANCELLED"})
    @DisplayName("Cancellation is not possible unless APPROVED.")
    void shouldFailCancelIfNotApproved(LoanStatus initialStatus) {
        LoanApplication loanApplication = aLoanApplication()
                .status(initialStatus)
                .build();
        assertThatThrownBy(loanApplication::cancel)
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessage("Only APPROVED -> CANCELLED");
    }

    @Test
    @DisplayName("Builder should throw NPE when id is null")
    void builderRejectsNullId() {
        assertThrows(NullPointerException.class, () -> LoanApplication.builder()
                .id(null)
                .applicantName("Name")
                .applicantIdentity(new ApplicantIdentity("12345678Z"))
                .loanAmount(new LoanAmount(new BigDecimal("10"), Currency.getInstance("EUR")))
                .createdAt(Instant.now())
                .modifiedAt(Instant.now())
                .status(LoanStatus.PENDING)
                .build());
    }

    @Test
    @DisplayName("Builder should throw NPE when applicantName is null")
    void builderRejectsNullApplicantName() {
        assertThrows(NullPointerException.class, () -> LoanApplication.builder()
                .id(new LoanId(UUID.randomUUID()))
                .applicantName(null)
                .applicantIdentity(new ApplicantIdentity("12345678Z"))
                .loanAmount(new LoanAmount(new BigDecimal("10"), Currency.getInstance("EUR")))
                .createdAt(Instant.now())
                .modifiedAt(Instant.now())
                .status(LoanStatus.PENDING)
                .build());
    }

    @Test
    @DisplayName("Builder should throw NPE when applicantIdentity is null")
    void builderRejectsNullApplicantIdentity() {
        assertThrows(NullPointerException.class, () -> LoanApplication.builder()
                .id(new LoanId(UUID.randomUUID()))
                .applicantName("Name")
                .applicantIdentity(null)
                .loanAmount(new LoanAmount(new BigDecimal("10"), Currency.getInstance("EUR")))
                .createdAt(Instant.now())
                .modifiedAt(Instant.now())
                .status(LoanStatus.PENDING)
                .build());
    }

    @Test
    @DisplayName("Builder should throw NPE when loanAmount is null")
    void builderRejectsNullLoanAmount() {
        assertThrows(NullPointerException.class, () -> LoanApplication.builder()
                .id(new LoanId(UUID.randomUUID()))
                .applicantName("Name")
                .applicantIdentity(new ApplicantIdentity("12345678Z"))
                .loanAmount(null)
                .createdAt(Instant.now())
                .modifiedAt(Instant.now())
                .status(LoanStatus.PENDING)
                .build());
    }

    @Test
    @DisplayName("Builder should throw NPE when createdAt is null")
    void builderRejectsNullCreatedAt() {
        assertThrows(NullPointerException.class, () -> LoanApplication.builder()
                .id(new LoanId(UUID.randomUUID()))
                .applicantName("Name")
                .applicantIdentity(new ApplicantIdentity("12345678Z"))
                .loanAmount(new LoanAmount(new BigDecimal("10"), Currency.getInstance("EUR")))
                .createdAt(null)
                .modifiedAt(Instant.now())
                .status(LoanStatus.PENDING)
                .build());
    }

    @Test
    @DisplayName("Builder should throw NPE when modifiedAt is null")
    void builderRejectsNullModifiedAt() {
        assertThrows(NullPointerException.class, () -> LoanApplication.builder()
                .id(new LoanId(UUID.randomUUID()))
                .applicantName("Name")
                .applicantIdentity(new ApplicantIdentity("12345678Z"))
                .loanAmount(new LoanAmount(new BigDecimal("10"), Currency.getInstance("EUR")))
                .createdAt(Instant.now())
                .modifiedAt(null)
                .status(LoanStatus.PENDING)
                .build());
    }

    @Test
    @DisplayName("Builder should throw NPE when status is null")
    void builderRejectsNullStatus() {
        assertThrows(NullPointerException.class, () -> LoanApplication.builder()
                .id(new LoanId(UUID.randomUUID()))
                .applicantName("Name")
                .applicantIdentity(new ApplicantIdentity("12345678Z"))
                .loanAmount(new LoanAmount(new BigDecimal("10"), Currency.getInstance("EUR")))
                .createdAt(Instant.now())
                .modifiedAt(Instant.now())
                .status(null)
                .build());
    }

    @Test
    @DisplayName("Builder should throw NPE when id is omitted")
    void builderRejectsOmittedId() {
        assertThrows(NullPointerException.class, () -> LoanApplication.builder()
                // .id omitted
                .applicantName("Name")
                .applicantIdentity(new ApplicantIdentity("12345678Z"))
                .loanAmount(new LoanAmount(new BigDecimal("10"), Currency.getInstance("EUR")))
                .createdAt(Instant.now())
                .modifiedAt(Instant.now())
                .status(LoanStatus.PENDING)
                .build());
    }

    @Test
    @DisplayName("Builder should throw NPE when applicantName is omitted")
    void builderRejectsOmittedApplicantName() {
        assertThrows(NullPointerException.class, () -> LoanApplication.builder()
                .id(new LoanId(UUID.randomUUID()))
                // .applicantName omitted
                .applicantIdentity(new ApplicantIdentity("12345678Z"))
                .loanAmount(new LoanAmount(new BigDecimal("10"), Currency.getInstance("EUR")))
                .createdAt(Instant.now())
                .modifiedAt(Instant.now())
                .status(LoanStatus.PENDING)
                .build());
    }

    @Test
    @DisplayName("Builder should throw NPE when applicantIdentity is omitted")
    void builderRejectsOmittedApplicantIdentity() {
        assertThrows(NullPointerException.class, () -> LoanApplication.builder()
                .id(new LoanId(UUID.randomUUID()))
                .applicantName("Name")
                // .applicantIdentity omitted
                .loanAmount(new LoanAmount(new BigDecimal("10"), Currency.getInstance("EUR")))
                .createdAt(Instant.now())
                .modifiedAt(Instant.now())
                .status(LoanStatus.PENDING)
                .build());
    }

    @Test
    @DisplayName("Builder should throw NPE when loanAmount is omitted")
    void builderRejectsOmittedLoanAmount() {
        assertThrows(NullPointerException.class, () -> LoanApplication.builder()
                .id(new LoanId(UUID.randomUUID()))
                .applicantName("Name")
                .applicantIdentity(new ApplicantIdentity("12345678Z"))
                // .loanAmount omitted
                .createdAt(Instant.now())
                .modifiedAt(Instant.now())
                .status(LoanStatus.PENDING)
                .build());
    }

    @Test
    @DisplayName("Builder should throw NPE when createdAt is omitted")
    void builderRejectsOmittedCreatedAt() {
        assertThrows(NullPointerException.class, () -> LoanApplication.builder()
                .id(new LoanId(UUID.randomUUID()))
                .applicantName("Name")
                .applicantIdentity(new ApplicantIdentity("12345678Z"))
                .loanAmount(new LoanAmount(new BigDecimal("10"), Currency.getInstance("EUR")))
                // .createdAt omitted
                .modifiedAt(Instant.now())
                .status(LoanStatus.PENDING)
                .build());
    }

    @Test
    @DisplayName("Builder should throw NPE when modifiedAt is omitted")
    void builderRejectsOmittedModifiedAt() {
        assertThrows(NullPointerException.class, () -> LoanApplication.builder()
                .id(new LoanId(UUID.randomUUID()))
                .applicantName("Name")
                .applicantIdentity(new ApplicantIdentity("12345678Z"))
                .loanAmount(new LoanAmount(new BigDecimal("10"), Currency.getInstance("EUR")))
                .createdAt(Instant.now())
                // .modifiedAt omitted
                .status(LoanStatus.PENDING)
                .build());
    }

    @Test
    @DisplayName("Builder should throw NPE when status is omitted")
    void builderRejectsOmittedStatus() {
        assertThrows(NullPointerException.class, () -> LoanApplication.builder()
                .id(new LoanId(UUID.randomUUID()))
                .applicantName("Name")
                .applicantIdentity(new ApplicantIdentity("12345678Z"))
                .loanAmount(new LoanAmount(new BigDecimal("10"), Currency.getInstance("EUR")))
                .createdAt(Instant.now())
                .modifiedAt(Instant.now())
                // .status omitted
                .build());
    }

    @Test
    @DisplayName("approve should return the same instance for fluent chaining")
    void approveReturnsSameInstance() {
        LoanApplication loanApplication = aLoanApplication().build();
        LoanApplication returned = loanApplication.approve();
        assertThat(returned).isSameAs(loanApplication);
        assertThat(loanApplication.getStatus()).isEqualTo(LoanStatus.APPROVED);
    }

    @Test
    @DisplayName("reject should return the same instance for fluent chaining")
    void rejectReturnsSameInstance() {
        LoanApplication loanApplication = aLoanApplication().build();
        LoanApplication returned = loanApplication.reject();
        assertThat(returned).isSameAs(loanApplication);
        assertThat(loanApplication.getStatus()).isEqualTo(LoanStatus.REJECTED);
    }

    @Test
    @DisplayName("cancel should return the same instance for fluent chaining")
    void cancelReturnsSameInstance() {
        LoanApplication loanApplication = aLoanApplication().status(LoanStatus.APPROVED).build();
        LoanApplication returned = loanApplication.cancel();
        assertThat(returned).isSameAs(loanApplication);
        assertThat(loanApplication.getStatus()).isEqualTo(LoanStatus.CANCELLED);
    }

    private LoanApplication.LoanApplicationBuilder aLoanApplication() {
        return LoanApplication.builder()
                .id(new LoanId(UUID.randomUUID()))
                .applicantName("Alvaro de la Flor Bonilla")
                .applicantIdentity(new ApplicantIdentity("12345678Z"))
                .loanAmount(new LoanAmount(new BigDecimal("123.45"), Currency.getInstance("EUR")))
                .createdAt(Instant.now())
                .modifiedAt(Instant.now())
                .status(LoanStatus.PENDING);
    }
}

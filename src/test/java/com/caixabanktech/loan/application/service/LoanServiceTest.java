package com.caixabanktech.loan.application.service;

import com.caixabanktech.loan.domain.exception.InvalidStateTransitionException;
import com.caixabanktech.loan.domain.exception.ResourceNotFoundException;
import com.caixabanktech.loan.domain.model.ApplicantIdentity;
import com.caixabanktech.loan.domain.model.LoanAmount;
import com.caixabanktech.loan.domain.model.LoanApplication;
import com.caixabanktech.loan.domain.model.LoanId;
import com.caixabanktech.loan.domain.model.LoanStatus;
import com.caixabanktech.loan.domain.port.in.CreateLoanCommand;
import com.caixabanktech.loan.domain.port.out.LoanRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Service Tests: Entity LoanService")
@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepositoryPort repositoryPort;

    private LoanApplicationService loanApplicationService;

    @BeforeEach
    void setUp() {
        loanApplicationService = new LoanApplicationService(repositoryPort);
    }

    @Test
    @DisplayName("createLoan should persist and return the created loan")
    void shouldCreateLoanSuccessfully() {
        CreateLoanCommand command = new CreateLoanCommand("Alvaro de la Flor Bonilla", BigDecimal.valueOf(1998.03), "EUR", "12345678Z");
        var loanApplication = createLoanApplication(LoanStatus.PENDING).build();
        when(repositoryPort.save(any(LoanApplication.class))).thenReturn(loanApplication);
        LoanApplication result = loanApplicationService.createLoan(command);

        assertThat(result.getApplicantName()).isEqualTo(command.applicantName());
    }

    @Test
    @DisplayName("approveLoan should throw ResourceNotFoundException when loan does not exist")
    void shouldThrowExceptionWhenUpdatingNonExistentLoan() {
        LoanId id = new LoanId(UUID.randomUUID());
        when(repositoryPort.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanApplicationService.approveLoan(id.value()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("approveLoan should update status to APPROVED and save")
    void shouldApproveStatusSuccessfully() {
        LoanId id = new LoanId(UUID.randomUUID());
        LoanApplication loan = createLoanApplication(LoanStatus.PENDING).build();

        when(repositoryPort.findById(id)).thenReturn(Optional.of(loan));
        when(repositoryPort.save(loan)).thenReturn(loan);

        LoanApplication result = loanApplicationService.approveLoan(id.value());

        assertThat(result.getStatus()).isEqualTo(LoanStatus.APPROVED);
        verify(repositoryPort).save(loan);
    }

    @Test
    @DisplayName("rejectLoan should update status to REJECTED and save")
    void shouldRejectStatusSuccessfully() {
        LoanId id = new LoanId(UUID.randomUUID());
        LoanApplication loan = createLoanApplication(LoanStatus.PENDING).build();

        when(repositoryPort.findById(id)).thenReturn(Optional.of(loan));
        when(repositoryPort.save(loan)).thenReturn(loan);

        LoanApplication result = loanApplicationService.rejectLoan(id.value());

        assertThat(result.getStatus()).isEqualTo(LoanStatus.REJECTED);
        verify(repositoryPort).save(loan);
    }

    @Test
    @DisplayName("cancelLoan should update status to CANCELLED from APPROVED and save")
    void shouldCancelStatusSuccessfully() {
        LoanId id = new LoanId(UUID.randomUUID());
        LoanApplication loan = createLoanApplication(LoanStatus.APPROVED).build();

        when(repositoryPort.findById(id)).thenReturn(Optional.of(loan));
        when(repositoryPort.save(loan)).thenReturn(loan);

        LoanApplication result = loanApplicationService.cancelLoan(id.value());

        assertThat(result.getStatus()).isEqualTo(LoanStatus.CANCELLED);
        verify(repositoryPort).save(loan);
    }

    @ParameterizedTest
    @EnumSource(value = LoanStatus.class, names = {"PENDING", "REJECTED", "CANCELLED"})
    @DisplayName("cancelLoan should throw InvalidStateTransitionException for unsupported transition")
    void shouldThrowExceptionForUnsupportedCancelTransitionInSwitch(LoanStatus status) {
        LoanId id = new LoanId(UUID.randomUUID());
        LoanApplication loan = LoanApplication.builder()
                .id(id)
                .applicantName("Alvaro de la Flor Bonilla")
                .applicantIdentity(new ApplicantIdentity("12345678Z"))
                .loanAmount(new LoanAmount(BigDecimal.TEN, Currency.getInstance("EUR")))
                .createdAt(Instant.now())
                .modifiedAt(Instant.now())
                .status(status)
                .build();
        when(repositoryPort.findById(id)).thenReturn(Optional.of(loan));

        assertThatThrownBy(() -> loanApplicationService.cancelLoan(id.value()))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("Only APPROVED -> CANCELLED");
    }

    @ParameterizedTest
    @EnumSource(value = LoanStatus.class, names = {"APPROVED", "REJECTED", "CANCELLED"})
    @DisplayName("approve should throw InvalidStateTransitionException for unsupported transition")
    void shouldThrowExceptionForUnsupportedApproveTransitionInSwitch(LoanStatus status) {
        LoanId id = new LoanId(UUID.randomUUID());
        LoanApplication loan = LoanApplication.builder()
                .id(id)
                .applicantName("Alvaro de la Flor Bonilla")
                .applicantIdentity(new ApplicantIdentity("12345678Z"))
                .loanAmount(new LoanAmount(BigDecimal.TEN, Currency.getInstance("EUR")))
                .createdAt(Instant.now())
                .modifiedAt(Instant.now())
                .status(status)
                .build();
        when(repositoryPort.findById(id)).thenReturn(Optional.of(loan));

        assertThatThrownBy(() -> loanApplicationService.approveLoan(id.value()))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("Only PENDING -> APPROVED");
    }

    @ParameterizedTest
    @EnumSource(value = LoanStatus.class, names = {"APPROVED", "REJECTED", "CANCELLED"})
    @DisplayName("reject should throw InvalidStateTransitionException for unsupported transition")
    void shouldThrowExceptionForUnsupportedRejectTransitionInSwitch(LoanStatus status) {
        LoanId id = new LoanId(UUID.randomUUID());
        LoanApplication loan = LoanApplication.builder()
                .id(id)
                .applicantName("Alvaro de la Flor Bonilla")
                .applicantIdentity(new ApplicantIdentity("12345678Z"))
                .loanAmount(new LoanAmount(BigDecimal.TEN, Currency.getInstance("EUR")))
                .createdAt(Instant.now())
                .modifiedAt(Instant.now())
                .status(status)
                .build();
        when(repositoryPort.findById(id)).thenReturn(Optional.of(loan));

        assertThatThrownBy(() -> loanApplicationService.rejectLoan(id.value()))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("Only PENDING -> REJECTED");
    }

    @Test
    @DisplayName("getLoan should return loan when exists")
    void getLoanShouldReturnLoan() {
        LoanId id = new LoanId(UUID.randomUUID());
        LoanApplication loan = createLoanApplication(LoanStatus.PENDING).id(id).build();
        when(repositoryPort.findById(id)).thenReturn(java.util.Optional.of(loan));

        LoanApplication result = loanApplicationService.getLoan(id.value());
        assertThat(result).isEqualTo(loan);
    }

    @Test
    @DisplayName("getLoan should throw ResourceNotFoundException when loan does not exist")
    void getLoanShouldThrowWhenNotFound() {
        LoanId id = new LoanId(UUID.randomUUID());
        when(repositoryPort.findById(id)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> loanApplicationService.getLoan(id.value()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Loan not found");
    }

    @Test
    @DisplayName("getLoanHistory should return history list from repository")
    void getLoanHistoryShouldReturnList() {
        LoanId id = new LoanId(UUID.randomUUID());
        List<LoanApplication> history = List.of(
                createLoanApplication(LoanStatus.PENDING).id(id).build(),
                createLoanApplication(LoanStatus.APPROVED).id(id).build()
        );
        when(repositoryPort.findHistory(id)).thenReturn(Optional.of(history));

        List<LoanApplication> result = loanApplicationService.getLoanHistory(id.value());
        assertThat(result).hasSize(2).containsAll(history);
    }

    @Test
    @DisplayName("getLoanHistory should throw when repository returns empty list")
    void getLoanHistoryShouldThrowWhenEmptyListReturned() {
        LoanId id = new LoanId(UUID.randomUUID());
        when(repositoryPort.findHistory(id)).thenReturn(Optional.of(List.of()));

        assertThatThrownBy(() -> loanApplicationService.getLoanHistory(id.value()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No loans found for uuid");
    }

    @Test
    @DisplayName("getLoansByIdentity should return list when found")
    void getLoansByIdentityShouldReturnList() {
        ApplicantIdentity identity = new ApplicantIdentity("12345678Z");
        List<LoanApplication> loans = List.of(createLoanApplication(LoanStatus.PENDING).build());
        when(repositoryPort.findByApplicantIdentity(identity)).thenReturn(java.util.Optional.of(loans));

        List<LoanApplication> result = loanApplicationService.getLoansByIdentity(identity);
        assertThat(result).hasSize(1).containsAll(loans);
    }

    @Test
    @DisplayName("getLoansByIdentity should throw when no loans found")
    void getLoansByIdentityShouldThrowWhenNotFound() {
        ApplicantIdentity identity = new ApplicantIdentity("12345678Z");
        when(repositoryPort.findByApplicantIdentity(identity)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> loanApplicationService.getLoansByIdentity(identity))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No loans found for applicant identity");
    }

    @Test
    @DisplayName("getLoansByIdentity should throw when repository returns empty list")
    void getLoansByIdentityShouldThrowWhenEmptyListReturned() {
        ApplicantIdentity identity = new ApplicantIdentity("12345678Z");
        when(repositoryPort.findByApplicantIdentity(identity)).thenReturn(Optional.of(List.of()));

        assertThatThrownBy(() -> loanApplicationService.getLoansByIdentity(identity))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No loans found for applicant identity");
    }

    @Test
    @DisplayName("searchLoans should return list when criteria matches")
    void searchLoansShouldReturnList() {
        String identity = "12345678Z";
        Instant start = Instant.now().minusSeconds(3600);
        Instant end = Instant.now();
        List<LoanApplication> loans = List.of(createLoanApplication(LoanStatus.PENDING).build());
        when(repositoryPort.findByCriteria(identity, start, end)).thenReturn(java.util.Optional.of(loans));

        List<LoanApplication> result = loanApplicationService.searchLoans(identity, start, end);
        assertThat(result).hasSize(1).containsAll(loans);
    }

    @Test
    @DisplayName("searchLoans should throw when no results match criteria")
    void searchLoansShouldThrowWhenNoResults() {
        String identity = "notfound";
        Instant start = Instant.now().minusSeconds(3600);
        Instant end = Instant.now();
        when(repositoryPort.findByCriteria(identity, start, end)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> loanApplicationService.searchLoans(identity, start, end))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No loans found matching criteria");
    }

    @Test
    @DisplayName("searchLoans should throw when repository returns empty list")
    void searchLoansShouldThrowWhenEmptyListReturned() {
        String identity = "12345678Z";
        Instant start = Instant.now().minusSeconds(3600);
        Instant end = Instant.now();
        when(repositoryPort.findByCriteria(identity, start, end)).thenReturn(Optional.of(List.of()));

        assertThatThrownBy(() -> loanApplicationService.searchLoans(identity, start, end))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No loans found matching criteria");
    }

    @Test
    @DisplayName("LoanApplication builder should build with all fields correctly")
    void loanApplicationBuilderBuildsAllFields() {
        LoanId id = new LoanId(UUID.randomUUID());
        String name = "Alvaro de la Flor Bonilla";
        ApplicantIdentity identity = new ApplicantIdentity("12345678Z");
        LoanAmount amount = new LoanAmount(BigDecimal.valueOf(1998.03), Currency.getInstance("EUR"));
        Instant created = Instant.parse("2026-02-07T10:00:00Z");
        Instant modified = Instant.parse("2026-02-07T11:00:00Z");
        LoanStatus status = LoanStatus.PENDING;

        LoanApplication loan = LoanApplication.builder()
                .id(id)
                .applicantName(name)
                .applicantIdentity(identity)
                .loanAmount(amount)
                .createdAt(created)
                .modifiedAt(modified)
                .status(status)
                .build();

        assertThat(loan.getId()).isEqualTo(id);
        assertThat(loan.getApplicantName()).isEqualTo(name);
        assertThat(loan.getApplicantIdentity()).isEqualTo(identity);
        assertThat(loan.getLoanAmount()).isEqualTo(amount);
        assertThat(loan.getCreatedAt()).isEqualTo(created);
        assertThat(loan.getModifiedAt()).isEqualTo(modified);
        assertThat(loan.getStatus()).isEqualTo(status);
    }

    @Test
    @DisplayName("LoanApplication builder should allow changing status and fields")
    void loanApplicationBuilderAllowsChanges() {
        LoanApplication base = createLoanApplication(LoanStatus.PENDING).build();
        LoanStatus newStatus = LoanStatus.APPROVED;
        String applicantName = "Alvaro de la Flor Bonilla";
        ApplicantIdentity newIdentity = new ApplicantIdentity("98765432M");
        LoanAmount newAmount = new LoanAmount(BigDecimal.valueOf(5000.50), Currency.getInstance("EUR"));
        Instant newCreated = Instant.parse("2026-02-01T00:00:00Z");
        Instant newModified = Instant.parse("2026-02-02T00:00:00Z");

        LoanApplication updated = LoanApplication.builder()
                .id(base.getId())
                .applicantName(applicantName)
                .applicantIdentity(newIdentity)
                .loanAmount(newAmount)
                .createdAt(newCreated)
                .modifiedAt(newModified)
                .status(newStatus)
                .build();

        assertThat(updated.getStatus()).isEqualTo(newStatus);
        assertThat(updated.getApplicantName()).isEqualTo(applicantName);
        assertThat(updated.getApplicantIdentity()).isEqualTo(newIdentity);
        assertThat(updated.getLoanAmount()).isEqualTo(newAmount);
        assertThat(updated.getCreatedAt()).isEqualTo(newCreated);
        assertThat(updated.getModifiedAt()).isEqualTo(newModified);
    }

    private LoanApplication.LoanApplicationBuilder createLoanApplication(LoanStatus status) {
        return LoanApplication.builder()
                .id(new LoanId(UUID.randomUUID()))
                .applicantName("Alvaro de la Flor Bonilla")
                .applicantIdentity(new ApplicantIdentity("12345678Z"))
                .loanAmount(new LoanAmount(BigDecimal.TEN, Currency.getInstance("EUR")))
                .createdAt(Instant.now())
                .modifiedAt(Instant.now())
                .status(status);
    }

    @Test
    @DisplayName("deleteLoan should delegate to repository deleteById")
    void deleteLoanDelegatesToRepository() {
        var loan = createLoanApplication(LoanStatus.PENDING).build();
        var loanId = loan.getId();
        when(repositoryPort.findById(loanId)).thenReturn(Optional.of(loan));

        loanApplicationService.deleteLoan(loanId.value());

        verify(repositoryPort).deleteById(loanId);
    }

    @Test
    @DisplayName("deleteLoan throws ResourceNotFoundException when loan does not exist")
    void deleteLoanThrowsWhenNotFound() {
        var loanId = new LoanId(UUID.randomUUID());
        when(repositoryPort.findById(loanId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> loanApplicationService.deleteLoan(loanId.value()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Loan not found");
    }
}
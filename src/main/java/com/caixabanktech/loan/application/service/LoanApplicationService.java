package com.caixabanktech.loan.application.service;

import com.caixabanktech.loan.domain.model.ApplicantIdentity;
import com.caixabanktech.loan.domain.model.LoanAmount;
import com.caixabanktech.loan.domain.model.LoanApplication;
import com.caixabanktech.loan.domain.model.LoanId;
import com.caixabanktech.loan.domain.model.LoanStatus;
import com.caixabanktech.loan.domain.port.in.CreateLoanCommand;
import com.caixabanktech.loan.domain.port.in.CreateLoanUseCase;
import com.caixabanktech.loan.domain.port.in.ModifyLoanStatusUseCase;
import com.caixabanktech.loan.domain.port.in.RetrieveLoanUseCase;
import com.caixabanktech.loan.domain.port.out.LoanRepositoryPort;
import com.caixabanktech.loan.domain.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.Currency;

@Service
@Transactional
public class LoanApplicationService implements CreateLoanUseCase, ModifyLoanStatusUseCase, RetrieveLoanUseCase {

    private final LoanRepositoryPort loanRepository;

    public LoanApplicationService(LoanRepositoryPort loanRepository) {
        this.loanRepository = loanRepository;
    }

    @Override
    public LoanApplication createLoan(CreateLoanCommand command) {
        LoanApplication loanApplication = LoanApplication.builder()
                .id(new LoanId(UUID.randomUUID()))
                .applicantName(command.applicantName())
                .applicantIdentity(new ApplicantIdentity(command.applicantIdentity()))
                .loanAmount(new LoanAmount(command.amount(), Currency.getInstance(command.currency())))
                .createdAt(Instant.now())
                .modifiedAt(Instant.now())
                .status(LoanStatus.PENDING)
                .build();
        return loanRepository.save(loanApplication);
    }

    @Override
    public LoanApplication approveLoan(UUID id) {
        return loanRepository.save(getLoanOrThrow(id).approve());
    }

    @Override
    public LoanApplication rejectLoan(UUID id) {
        return loanRepository.save(getLoanOrThrow(id).reject());
    }

    @Override
    public LoanApplication cancelLoan(UUID id) {
        return loanRepository.save(getLoanOrThrow(id).cancel());
    }

    @Override
    @Transactional(readOnly = true)
    public LoanApplication getLoan(UUID id) { return getLoanOrThrow(id); }

    @Override
    @Transactional(readOnly = true)
    public List<LoanApplication> getLoanHistory(UUID id) { return loanRepository.findHistory(new LoanId(id)); }

    private LoanApplication getLoanOrThrow(UUID uuid) {
        return loanRepository.findById(new LoanId(uuid))
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + uuid));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoanApplication> getLoansByIdentity(ApplicantIdentity applicantIdentity) {
        List<LoanApplication> results = loanRepository.findByApplicantIdentity(applicantIdentity)
                .orElseThrow(() -> buildCriteriaMessage(applicantIdentity));
        if (results.isEmpty()) {
            throw buildCriteriaMessage(applicantIdentity);
        }
        return results;
    }

    private ResourceNotFoundException buildCriteriaMessage(ApplicantIdentity applicantIdentity) {
        return new ResourceNotFoundException("No loans found for applicant identity: " + applicantIdentity.value());
    }

    @Override
    public List<LoanApplication> searchLoans(String identity, Instant startDate, Instant endDate) {
        List<LoanApplication> results = loanRepository.findByCriteria(identity, startDate, endDate)
                .orElseThrow(() -> buildCriteriaMessage(identity, startDate, endDate));
        if (results.isEmpty()) {
            throw buildCriteriaMessage(identity, startDate, endDate);
        }
        return results;
    }

    private ResourceNotFoundException buildCriteriaMessage(String identity, Instant startDate, Instant endDate) {
        return new ResourceNotFoundException("No loans found matching criteria: identity=" + identity + ", startDate=" + startDate + ", endDate=" + endDate);
    }

    @Override
    public void deleteLoan(UUID id) {
        loanRepository.deleteById(new LoanId(id));
    }
}

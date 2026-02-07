package com.caixabanktech.loan.domain.port.out;

import com.caixabanktech.loan.domain.model.ApplicantIdentity;
import com.caixabanktech.loan.domain.model.LoanApplication;
import com.caixabanktech.loan.domain.model.LoanId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface LoanRepositoryPort {
    LoanApplication save(LoanApplication loan);
    Optional<LoanApplication> findById(LoanId id);
    List<LoanApplication> findAll();
    List<LoanApplication> findHistory(LoanId id);
    Optional<List<LoanApplication>> findByApplicantIdentity(ApplicantIdentity identity);
    Optional<List<LoanApplication>> findByCriteria(String identity, Instant startDate, Instant endDate);
    void deleteById(LoanId id);
}

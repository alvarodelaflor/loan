package com.caixabanktech.loan.infrastructure.adapter.output.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface LoanJpaRepository extends JpaRepository<LoanJpaEntity, UUID>, JpaSpecificationExecutor<LoanJpaEntity> {

    List<LoanJpaEntity> findByApplicantIdentity(String applicantIdentity);
}

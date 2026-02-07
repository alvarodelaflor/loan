package com.caixabanktech.loan.infrastructure.adapter.output.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "LOAN_APPLICATIONS")
@EntityListeners(AuditingEntityListener.class)
@Audited
@Data
public class LoanJpaEntity {
    @Id
    @Column(name = "ID", columnDefinition = "RAW(16)")
    private UUID id;

    @Column(nullable = false) private String applicantName;
    @Column(nullable = false) private String applicantIdentity;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal amount;
    @Column(nullable = false, length = 3) private String currency;
    @Column(nullable = false) private String status;

    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "MODIFIED_AT")
    private Instant modifiedAt;
}

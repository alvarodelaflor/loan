package com.caixabanktech.loan.infrastructure.adapter.output.persistence.jpa.spec;

import com.caixabanktech.loan.infrastructure.adapter.output.persistence.jpa.LoanJpaEntity;
import org.springframework.data.jpa.domain.Specification;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class LoanSpecifications {

    public static Specification<LoanJpaEntity> hasIdentity(String identity) {
        return (root, query, cb) -> identity == null? null : cb.equal(root.get("applicantIdentity"), identity);
    }

    public static Specification<LoanJpaEntity> createdBetween(Instant start, Instant end) {
        return (root, query, cb) -> {
            Instant startTruncated = (start != null) ? start.truncatedTo(ChronoUnit.SECONDS) : null;
            Instant endNextSecond = (end != null) ? end.truncatedTo(ChronoUnit.SECONDS).plusSeconds(1) : null;

            if (startTruncated == null && endNextSecond == null) {
                return null;
            }

            if (startTruncated != null && endNextSecond != null) {
                return cb.and(
                        cb.greaterThanOrEqualTo(root.get("createdAt"), startTruncated),
                        cb.lessThan(root.get("createdAt"), endNextSecond)
                );
            }

            if (startTruncated != null) {
                return cb.greaterThanOrEqualTo(root.get("createdAt"), startTruncated);
            }

            return cb.lessThan(root.get("createdAt"), endNextSecond);
        };
    }
}

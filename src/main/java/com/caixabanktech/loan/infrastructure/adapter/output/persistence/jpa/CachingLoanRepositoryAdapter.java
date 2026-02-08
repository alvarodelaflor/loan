package com.caixabanktech.loan.infrastructure.adapter.output.persistence.jpa;

import com.caixabanktech.loan.domain.model.ApplicantIdentity;
import com.caixabanktech.loan.domain.model.LoanApplication;
import com.caixabanktech.loan.domain.model.LoanId;
import com.caixabanktech.loan.domain.port.out.LoanRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@Primary
public class CachingLoanRepositoryAdapter implements LoanRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(CachingLoanRepositoryAdapter.class);
    private final LoanRepositoryPort delegate;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final long CACHE_TTL = 10; // Time-to-live for cache entries in minutes
    private static final TimeUnit CACHE_TTLUNIT = TimeUnit.MINUTES;

    public CachingLoanRepositoryAdapter(@Qualifier("loanPersistenceAdapter") LoanRepositoryPort delegate, RedisTemplate<String, Object> redisTemplate) {
        this.delegate = delegate;
        this.redisTemplate = redisTemplate;
    }

    private String getCacheKey(LoanId id) {
        return "loan:" + id.value();
    }

    private String getIdentityCacheKey(String identity) {
        return "loan:identity:" + identity;
    }

    @Override
    public Optional<LoanApplication> findById(LoanId id) {
        String key = getCacheKey(id);
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached instanceof LoanApplication cachedLoan) {
                log.info("Cache hit for key: {}", key);
                return Optional.of(cachedLoan);
            }
            log.info("Cache miss for key: {}", key);
        } catch (Exception e) {
            log.warn("Error reading from Redis cache for key {}. Proceeding to database.", key, e);
        }

        Optional<LoanApplication> loanFromDb = delegate.findById(id);
        loanFromDb.ifPresent(loan -> {
            try {
                redisTemplate.opsForValue().set(key, loan, CACHE_TTL, CACHE_TTLUNIT);
            } catch (Exception e) {
                log.warn("Error writing to Redis cache for key {}.", key, e);
            }
        });
        return loanFromDb;
    }

    @Override
    public LoanApplication save(LoanApplication loan) {
        LoanApplication savedLoan = delegate.save(loan);
        try {
            // Update individual cache
            String key = getCacheKey(savedLoan.getId());
            redisTemplate.opsForValue().set(key, savedLoan, CACHE_TTL, CACHE_TTLUNIT);

            // Invalidate identity list cache to ensure consistency
            String identityKey = getIdentityCacheKey(savedLoan.getApplicantIdentity().value());
            redisTemplate.delete(identityKey);
        } catch (Exception e) {
            log.warn("Error updating cache for loan {}.", savedLoan.getId().value(), e);
        }
        return savedLoan;
    }

    @Override
    public void deleteById(LoanId id) {
        Optional<LoanApplication> loan = findById(id);
        delegate.deleteById(id);
        try {
            redisTemplate.delete(getCacheKey(id));
            loan.ifPresent(l -> redisTemplate.delete(getIdentityCacheKey(l.getApplicantIdentity().value())));
        } catch (Exception e) {
            log.warn("Error deleting from cache for key {}.", getCacheKey(id), e);
        }
    }

    @Override
    public List<LoanApplication> findAll() {
        return delegate.findAll();
    }

    @Override
    public Optional<List<LoanApplication>> findHistory(LoanId id) {
        return delegate.findHistory(id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<List<LoanApplication>> findByApplicantIdentity(ApplicantIdentity identity) {
        String key = getIdentityCacheKey(identity.value());
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached instanceof List) {
                log.info("Cache hit for identity: {}", identity.value());
                return Optional.of((List<LoanApplication>) cached);
            }
            log.info("Cache miss for identity: {}", identity.value());
        } catch (Exception e) {
            log.warn("Error reading from Redis cache for identity {}. Proceeding to database.", identity.value(), e);
        }

        Optional<List<LoanApplication>> results = delegate.findByApplicantIdentity(identity);
        results.ifPresent(list -> {
            try {
                redisTemplate.opsForValue().set(key, list, CACHE_TTL, CACHE_TTLUNIT);
            } catch (Exception e) {
                log.warn("Error writing to Redis cache for identity {}.", identity.value(), e);
            }
        });
        return results;
    }

    @Override
    public Optional<List<LoanApplication>> findByCriteria(String identity, Instant startDate, Instant endDate) {
        return delegate.findByCriteria(identity, startDate, endDate);
    }
}

package com.caixabanktech.loan.infrastructure.adapter.output.persistence.jpa;

import com.caixabanktech.loan.domain.model.ApplicantIdentity;
import com.caixabanktech.loan.domain.model.LoanAmount;
import com.caixabanktech.loan.domain.model.LoanApplication;
import com.caixabanktech.loan.domain.model.LoanId;
import com.caixabanktech.loan.domain.model.LoanStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CachingLoanRepositoryAdapter Tests")
class CachingLoanRepositoryAdapterTest {

    @Mock
    private LoanPersistenceAdapter delegate;

    @Mock
    private RedisTemplate<String, LoanApplication> redisTemplate;

    @Mock
    private ValueOperations<String, LoanApplication> valueOperations;

    @InjectMocks
    private CachingLoanRepositoryAdapter cachingAdapter;

    private LoanId loanId;
    private String cacheKey;
    private LoanApplication loanApplication;

    @BeforeEach
    void setUp() {
        loanApplication = aLoanApplication().build();
        loanId = loanApplication.getId();
        cacheKey = "loan:" + loanId.value();
        // Mock the opsForValue() call to return our mocked ValueOperations
        // Use lenient() because not all tests will use opsForValue()
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("findById should return from cache on cache hit")
    void shouldReturnFromCacheOnCacheHit() {
        when(valueOperations.get(cacheKey)).thenReturn(loanApplication);

        Optional<LoanApplication> result = cachingAdapter.findById(loanId);

        assertThat(result).isPresent().contains(loanApplication);
        verify(delegate, never()).findById(any());
    }

    @Test
    @DisplayName("findById should fetch from delegate and cache on cache miss")
    void shouldFetchFromDelegateAndCacheOnCacheMiss() {
        when(valueOperations.get(cacheKey)).thenReturn(null);
        when(delegate.findById(loanId)).thenReturn(Optional.of(loanApplication));

        Optional<LoanApplication> result = cachingAdapter.findById(loanId);

        assertThat(result).isPresent().contains(loanApplication);
        verify(delegate).findById(loanId);
        verify(valueOperations).set(cacheKey, loanApplication, 10, TimeUnit.MINUTES);
    }

    @Test
    @DisplayName("findById should return empty and not cache when not found in delegate")
    void shouldReturnEmptyAndNotCacheWhenNotFoundInDelegate() {
        when(valueOperations.get(cacheKey)).thenReturn(null);
        when(delegate.findById(loanId)).thenReturn(Optional.empty());

        Optional<LoanApplication> result = cachingAdapter.findById(loanId);

        assertThat(result).isNotPresent();
        verify(delegate).findById(loanId);
        verify(valueOperations, never()).set(anyString(), any(), anyLong(), any());
    }

    @Test
    @DisplayName("findById should fallback to delegate on Redis read error")
    void shouldFallbackToDelegateOnRedisReadError() {
        when(valueOperations.get(cacheKey)).thenThrow(new RuntimeException("Redis down"));
        when(delegate.findById(loanId)).thenReturn(Optional.of(loanApplication));

        Optional<LoanApplication> result = cachingAdapter.findById(loanId);

        assertThat(result).isPresent().contains(loanApplication);
        verify(delegate).findById(loanId);
    }

    @Test
    @DisplayName("findById should not fail on Redis write error")
    void shouldNotFailOnRedisWriteError() {
        when(valueOperations.get(cacheKey)).thenReturn(null);
        when(delegate.findById(loanId)).thenReturn(Optional.of(loanApplication));
        doThrow(new RuntimeException("Redis down")).when(valueOperations).set(cacheKey, loanApplication, 10, TimeUnit.MINUTES);

        Optional<LoanApplication> result = cachingAdapter.findById(loanId);

        assertThat(result).isPresent().contains(loanApplication);
        verify(delegate).findById(loanId);
    }

    @Test
    @DisplayName("save should call delegate and update cache")
    void shouldCallDelegateAndUpdateCache() {
        when(delegate.save(loanApplication)).thenReturn(loanApplication);

        LoanApplication result = cachingAdapter.save(loanApplication);

        assertThat(result).isEqualTo(loanApplication);
        verify(delegate).save(loanApplication);
        verify(valueOperations).set(cacheKey, loanApplication, 10, TimeUnit.MINUTES);
    }

    @Test
    @DisplayName("save should not fail on Redis update error")
    void shouldNotFailOnRedisUpdateError() {
        when(delegate.save(loanApplication)).thenReturn(loanApplication);
        doThrow(new RuntimeException("Redis down")).when(valueOperations).set(cacheKey, loanApplication, 10, TimeUnit.MINUTES);

        LoanApplication result = cachingAdapter.save(loanApplication);

        assertThat(result).isEqualTo(loanApplication);
        verify(delegate).save(loanApplication);
    }

    @Test
    @DisplayName("deleteById should call delegate and evict from cache")
    void shouldCallDelegateAndEvictFromCache() {
        cachingAdapter.deleteById(loanId);

        verify(delegate).deleteById(loanId);
        verify(redisTemplate).delete(cacheKey);
    }

    @Test
    @DisplayName("deleteById should not fail on Redis delete error")
    void shouldNotFailOnRedisDeleteError() {
        doThrow(new RuntimeException("Redis down")).when(redisTemplate).delete(cacheKey);

        cachingAdapter.deleteById(loanId);

        verify(delegate).deleteById(loanId);
    }

    @Test
    @DisplayName("Delegated methods should call delegate")
    void shouldCallDelegateForOtherMethods() {
        cachingAdapter.findAll();
        verify(delegate).findAll();

        cachingAdapter.findHistory(loanId);
        verify(delegate).findHistory(loanId);

        cachingAdapter.findByApplicantIdentity(null);
        verify(delegate).findByApplicantIdentity(null);

        cachingAdapter.findByCriteria(null, null, null);
        verify(delegate).findByCriteria(null, null, null);
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

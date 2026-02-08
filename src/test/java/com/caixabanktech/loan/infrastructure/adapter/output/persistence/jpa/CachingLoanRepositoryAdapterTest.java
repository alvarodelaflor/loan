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
import java.util.List;
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
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

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
    @DisplayName("save should call delegate, update individual cache and evict identity and history caches")
    void shouldCallDelegateAndUpdateCache() {
        String identityKey = "loan:identity:" + loanApplication.getApplicantIdentity().value();
        String historyKey = "loan:history:" + loanApplication.getId().value();
        when(delegate.save(loanApplication)).thenReturn(loanApplication);

        LoanApplication result = cachingAdapter.save(loanApplication);

        assertThat(result).isEqualTo(loanApplication);
        verify(delegate).save(loanApplication);
        verify(valueOperations).set(cacheKey, loanApplication, 10, TimeUnit.MINUTES);
        verify(redisTemplate).delete(identityKey);
        verify(redisTemplate).delete(historyKey);
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
    @DisplayName("deleteById should call delegate and evict both caches")
    void shouldCallDelegateAndEvictFromCache() {
        when(valueOperations.get(cacheKey)).thenReturn(loanApplication);
        String identityKey = "loan:identity:" + loanApplication.getApplicantIdentity().value();
        String historyKey = "loan:history:" + loanApplication.getId().value();

        cachingAdapter.deleteById(loanId);

        verify(delegate).deleteById(loanId);
        verify(redisTemplate).delete(cacheKey);
        verify(redisTemplate).delete(identityKey);
        verify(redisTemplate).delete(historyKey);
    }

    @Test
    @DisplayName("deleteById should not fail on Redis delete error")
    void shouldNotFailOnRedisDeleteError() {
        when(valueOperations.get(cacheKey)).thenReturn(loanApplication);
        doThrow(new RuntimeException("Redis down")).when(redisTemplate).delete(cacheKey);

        cachingAdapter.deleteById(loanId);

        verify(delegate).deleteById(loanId);
    }

    @Test
    @DisplayName("findByApplicantIdentity should return from cache on cache hit")
    void shouldReturnFromIdentityCacheOnHit() {
        ApplicantIdentity identity = loanApplication.getApplicantIdentity();
        String identityKey = "loan:identity:" + identity.value();
        List<LoanApplication> loans = List.of(loanApplication);
        when(valueOperations.get(identityKey)).thenReturn(loans);

        Optional<List<LoanApplication>> result = cachingAdapter.findByApplicantIdentity(identity);

        assertThat(result).isPresent().contains(loans);
        verify(delegate, never()).findByApplicantIdentity(any());
    }

    @Test
    @DisplayName("findByApplicantIdentity should return empty and not cache when not found in delegate")
    void shouldReturnEmptyAndNotCacheWhenNotFoundInDelegateForIdentity() {
        ApplicantIdentity identity = loanApplication.getApplicantIdentity();
        String identityKey = "loan:identity:" + identity.value();
        when(valueOperations.get(identityKey)).thenReturn(null);
        when(delegate.findByApplicantIdentity(identity)).thenReturn(Optional.empty());

        Optional<List<LoanApplication>> result = cachingAdapter.findByApplicantIdentity(identity);

        assertThat(result).isNotPresent();
        verify(delegate).findByApplicantIdentity(identity);
        verify(valueOperations, never()).set(anyString(), any(), anyLong(), any());
    }

    @Test
    @DisplayName("findByApplicantIdentity should fallback to delegate on Redis read error")
    void shouldFallbackToDelegateOnRedisReadErrorForIdentity() {
        ApplicantIdentity identity = loanApplication.getApplicantIdentity();
        String identityKey = "loan:identity:" + identity.value();
        List<LoanApplication> loans = List.of(loanApplication);
        when(valueOperations.get(identityKey)).thenThrow(new RuntimeException("Redis down"));
        when(delegate.findByApplicantIdentity(identity)).thenReturn(Optional.of(loans));

        Optional<List<LoanApplication>> result = cachingAdapter.findByApplicantIdentity(identity);

        assertThat(result).isPresent().contains(loans);
        verify(delegate).findByApplicantIdentity(identity);
    }

    @Test
    @DisplayName("findByApplicantIdentity should not fail on Redis write error")
    void shouldNotFailOnRedisWriteErrorForIdentity() {
        ApplicantIdentity identity = loanApplication.getApplicantIdentity();
        String identityKey = "loan:identity:" + identity.value();
        List<LoanApplication> loans = List.of(loanApplication);
        when(valueOperations.get(identityKey)).thenReturn(null);
        when(delegate.findByApplicantIdentity(identity)).thenReturn(Optional.of(loans));
        doThrow(new RuntimeException("Redis down")).when(valueOperations).set(identityKey, loans, 10, TimeUnit.MINUTES);

        Optional<List<LoanApplication>> result = cachingAdapter.findByApplicantIdentity(identity);

        assertThat(result).isPresent().contains(loans);
        verify(delegate).findByApplicantIdentity(identity);
    }

    @Test
    @DisplayName("findByApplicantIdentity should fetch from delegate and cache on miss")
    void shouldFetchFromDelegateAndCacheIdentityOnMiss() {
        ApplicantIdentity identity = loanApplication.getApplicantIdentity();
        String identityKey = "loan:identity:" + identity.value();
        List<LoanApplication> loans = List.of(loanApplication);
        when(valueOperations.get(identityKey)).thenReturn(null);
        when(delegate.findByApplicantIdentity(identity)).thenReturn(Optional.of(loans));

        Optional<List<LoanApplication>> result = cachingAdapter.findByApplicantIdentity(identity);

        assertThat(result).isPresent().contains(loans);
        verify(delegate).findByApplicantIdentity(identity);
        verify(valueOperations).set(identityKey, loans, 10, TimeUnit.MINUTES);
    }

    @Test
    @DisplayName("findHistory should return from cache on cache hit")
    void shouldReturnFromHistoryCacheOnHit() {
        String historyKey = "loan:history:" + loanId.value();
        List<LoanApplication> history = List.of(loanApplication);
        when(valueOperations.get(historyKey)).thenReturn(history);

        Optional<List<LoanApplication>> result = cachingAdapter.findHistory(loanId);

        assertThat(result).isPresent().contains(history);
        verify(delegate, never()).findHistory(any());
    }

    @Test
    @DisplayName("findHistory should fetch from delegate and cache on miss")
    void shouldFetchFromDelegateAndCacheHistoryOnMiss() {
        String historyKey = "loan:history:" + loanId.value();
        List<LoanApplication> history = List.of(loanApplication);
        when(valueOperations.get(historyKey)).thenReturn(null);
        when(delegate.findHistory(loanId)).thenReturn(Optional.of(history));

        Optional<List<LoanApplication>> result = cachingAdapter.findHistory(loanId);

        assertThat(result).isPresent().contains(history);
        verify(delegate).findHistory(loanId);
        verify(valueOperations).set(historyKey, history, 10, TimeUnit.MINUTES);
    }

    @Test
    @DisplayName("findHistory should return empty and not cache when not found in delegate")
    void shouldReturnEmptyAndNotCacheWhenNotFoundInDelegateForHistory() {
        String historyKey = "loan:history:" + loanId.value();
        when(valueOperations.get(historyKey)).thenReturn(null);
        when(delegate.findHistory(loanId)).thenReturn(Optional.empty());

        Optional<List<LoanApplication>> result = cachingAdapter.findHistory(loanId);

        assertThat(result).isNotPresent();
        verify(delegate).findHistory(loanId);
        verify(valueOperations, never()).set(anyString(), any(), anyLong(), any());
    }

    @Test
    @DisplayName("findHistory should fallback to delegate on Redis read error")
    void shouldFallbackToDelegateOnRedisReadErrorForHistory() {
        String historyKey = "loan:history:" + loanId.value();
        List<LoanApplication> history = List.of(loanApplication);
        when(valueOperations.get(historyKey)).thenThrow(new RuntimeException("Redis down"));
        when(delegate.findHistory(loanId)).thenReturn(Optional.of(history));

        Optional<List<LoanApplication>> result = cachingAdapter.findHistory(loanId);

        assertThat(result).isPresent().contains(history);
        verify(delegate).findHistory(loanId);
    }

    @Test
    @DisplayName("findHistory should not fail on Redis write error")
    void shouldNotFailOnRedisWriteErrorForHistory() {
        String historyKey = "loan:history:" + loanId.value();
        List<LoanApplication> history = List.of(loanApplication);
        when(valueOperations.get(historyKey)).thenReturn(null);
        when(delegate.findHistory(loanId)).thenReturn(Optional.of(history));
        doThrow(new RuntimeException("Redis down")).when(valueOperations).set(historyKey, history, 10, TimeUnit.MINUTES);

        Optional<List<LoanApplication>> result = cachingAdapter.findHistory(loanId);

        assertThat(result).isPresent().contains(history);
        verify(delegate).findHistory(loanId);
    }

    @Test
    @DisplayName("Delegated methods should call delegate")
    void shouldCallDelegateForOtherMethods() {
        cachingAdapter.findAll();
        verify(delegate).findAll();


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

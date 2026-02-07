package com.caixabanktech.loan.infrastructure.adapter.output.persistence.jpa.spec;

import com.caixabanktech.loan.infrastructure.adapter.output.persistence.jpa.LoanJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JPA Specifications Tests: LoanSpecifications")
class LoanSpecificationsTest {

    @Test
    @DisplayName("Constructor should be instantiable (coverage)")
    void constructorIsAccessible() {
        assertNotNull(new LoanSpecifications());
    }

    @Test
    @DisplayName("hasIdentity returns null predicate when identity is null")
    void hasIdentityReturnsNullWhenIdentityNull() {
        Root<LoanJpaEntity> root = Mockito.mock(Root.class);
        CriteriaQuery<?> query = Mockito.mock(CriteriaQuery.class);
        CriteriaBuilder cb = Mockito.mock(CriteriaBuilder.class);

        Predicate result = LoanSpecifications.hasIdentity(null).toPredicate(root, query, cb);
        assertNull(result);
    }

    @Test
    @DisplayName("hasIdentity returns equal predicate when identity is provided")
    void hasIdentityReturnsEqualPredicate() {
        Root<LoanJpaEntity> root = Mockito.mock(Root.class);
        CriteriaQuery<?> query = Mockito.mock(CriteriaQuery.class);
        CriteriaBuilder cb = Mockito.mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        Path<String> identityPath = (Path<String>) Mockito.mock(Path.class);
        Mockito.when(root.<String>get("applicantIdentity")).thenReturn(identityPath);
        Predicate expected = Mockito.mock(Predicate.class);
        Mockito.when(cb.equal(identityPath, "12345678Z")).thenReturn(expected);

        Predicate result = LoanSpecifications.hasIdentity("12345678Z").toPredicate(root, query, cb);
        assertSame(expected, result);
    }

    @Test
    @DisplayName("createdBetween returns null when both start and end are null")
    void createdBetweenReturnsNullWhenBothNull() {
        Root<LoanJpaEntity> root = Mockito.mock(Root.class);
        CriteriaQuery<?> query = Mockito.mock(CriteriaQuery.class);
        CriteriaBuilder cb = Mockito.mock(CriteriaBuilder.class);

        Predicate result = LoanSpecifications.createdBetween(null, null).toPredicate(root, query, cb);
        assertNull(result);
    }

    @Test
    @DisplayName("createdBetween returns between predicates logic when start and end are provided")
    void createdBetweenReturnsCombinedPredicates() {
        Root<LoanJpaEntity> root = Mockito.mock(Root.class);
        CriteriaQuery<?> query = Mockito.mock(CriteriaQuery.class);
        CriteriaBuilder cb = Mockito.mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        Path<Instant> createdAtPath = (Path<Instant>) Mockito.mock(Path.class);
        Mockito.when(root.<Instant>get("createdAt")).thenReturn(createdAtPath);

        Instant start = Instant.parse("2026-02-07T10:00:00.123Z");
        Instant end = Instant.parse("2026-02-07T12:00:00.456Z");

        Instant startTruncated = start.truncatedTo(ChronoUnit.SECONDS);
        Instant endNextSecond = end.truncatedTo(ChronoUnit.SECONDS).plusSeconds(1);

        Predicate gePredicate = Mockito.mock(Predicate.class);
        Predicate ltPredicate = Mockito.mock(Predicate.class);
        Predicate andPredicate = Mockito.mock(Predicate.class);

        Mockito.when(cb.greaterThanOrEqualTo(createdAtPath, startTruncated)).thenReturn(gePredicate);
        Mockito.when(cb.lessThan(createdAtPath, endNextSecond)).thenReturn(ltPredicate);
        Mockito.when(cb.and(gePredicate, ltPredicate)).thenReturn(andPredicate);

        Predicate result = LoanSpecifications.createdBetween(start, end).toPredicate(root, query, cb);
        assertSame(andPredicate, result);
    }

    @Test
    @DisplayName("createdBetween returns greaterThanOrEqualTo when only start is provided")
    void createdBetweenReturnsOnlyStartPredicate() {
        Root<LoanJpaEntity> root = Mockito.mock(Root.class);
        CriteriaQuery<?> query = Mockito.mock(CriteriaQuery.class);
        CriteriaBuilder cb = Mockito.mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        Path<Instant> createdAtPath = (Path<Instant>) Mockito.mock(Path.class);
        Mockito.when(root.<Instant>get("createdAt")).thenReturn(createdAtPath);

        Instant start = Instant.parse("2026-02-07T10:00:00.999Z");
        Instant startTruncated = start.truncatedTo(ChronoUnit.SECONDS);

        Predicate gePredicate = Mockito.mock(Predicate.class);
        Mockito.when(cb.greaterThanOrEqualTo(createdAtPath, startTruncated)).thenReturn(gePredicate);

        Predicate result = LoanSpecifications.createdBetween(start, null).toPredicate(root, query, cb);
        assertSame(gePredicate, result);
    }

    @Test
    @DisplayName("createdBetween returns lessThan when only end is provided")
    void createdBetweenReturnsOnlyEndPredicate() {
        Root<LoanJpaEntity> root = Mockito.mock(Root.class);
        CriteriaQuery<?> query = Mockito.mock(CriteriaQuery.class);
        CriteriaBuilder cb = Mockito.mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        Path<Instant> createdAtPath = (Path<Instant>) Mockito.mock(Path.class);
        Mockito.when(root.<Instant>get("createdAt")).thenReturn(createdAtPath);

        Instant end = Instant.parse("2026-02-07T12:00:00.888Z");
        Instant endNextSecond = end.truncatedTo(ChronoUnit.SECONDS).plusSeconds(1);

        Predicate ltPredicate = Mockito.mock(Predicate.class);
        Mockito.when(cb.lessThan(createdAtPath, endNextSecond)).thenReturn(ltPredicate);

        Predicate result = LoanSpecifications.createdBetween(null, end).toPredicate(root, query, cb);
        assertSame(ltPredicate, result);
    }
}

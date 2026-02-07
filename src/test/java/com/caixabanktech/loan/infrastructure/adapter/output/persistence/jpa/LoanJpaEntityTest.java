package com.caixabanktech.loan.infrastructure.adapter.output.persistence.jpa;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("JPA Specifications Tests: LoanJpaEntity")
class LoanJpaEntityTest {

    private LoanJpaEntity newEntity(UUID id) {
        LoanJpaEntity e = new LoanJpaEntity();
        e.setId(id);
        e.setApplicantName("Alvaro de la Flor Bonilla");
        e.setApplicantIdentity("12345678Z");
        e.setAmount(new BigDecimal("100.00"));
        e.setCurrency("EUR");
        e.setStatus("PENDING");
        e.setCreatedAt(Instant.parse("2026-02-07T10:00:00Z"));
        e.setModifiedAt(Instant.parse("2026-02-07T11:00:00Z"));
        return e;
    }

    @Test
    @DisplayName("Getters and setters should read and write values correctly")
    void gettersAndSettersWork() {
        UUID id = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        LoanJpaEntity e = newEntity(id);

        assertEquals(id, e.getId());
        assertEquals("Alvaro de la Flor Bonilla", e.getApplicantName());
        assertEquals("12345678Z", e.getApplicantIdentity());
        assertEquals(new BigDecimal("100.00"), e.getAmount());
        assertEquals("EUR", e.getCurrency());
        assertEquals("PENDING", e.getStatus());
        assertEquals(Instant.parse("2026-02-07T10:00:00Z"), e.getCreatedAt());
        assertEquals(Instant.parse("2026-02-07T11:00:00Z"), e.getModifiedAt());

        // mutate
        e.setApplicantName("Javier de la Flor Bonilla");
        e.setApplicantIdentity("87654321X");
        e.setAmount(new BigDecimal("200.50"));
        e.setCurrency("USD");
        e.setStatus("APPROVED");
        e.setCreatedAt(Instant.parse("2026-02-07T12:00:00Z"));
        e.setModifiedAt(Instant.parse("2026-02-07T13:00:00Z"));

        assertEquals("Javier de la Flor Bonilla", e.getApplicantName());
        assertEquals("87654321X", e.getApplicantIdentity());
        assertEquals(new BigDecimal("200.50"), e.getAmount());
        assertEquals("USD", e.getCurrency());
        assertEquals("APPROVED", e.getStatus());
        assertEquals(Instant.parse("2026-02-07T12:00:00Z"), e.getCreatedAt());
        assertEquals(Instant.parse("2026-02-07T13:00:00Z"), e.getModifiedAt());
    }

    @Test
    @DisplayName("equals should follow reflexive, symmetric, transitive and null/other class rules")
    void equalsContracts() {
        UUID id = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        LoanJpaEntity a = newEntity(id);
        LoanJpaEntity b = newEntity(id);
        LoanJpaEntity c = newEntity(id);

        // reflexive
        assertEquals(a, a);
        // symmetric
        assertEquals(a, b);
        assertEquals(b, a);
        // transitive
        assertEquals(a, b);
        assertEquals(b, c);
        assertEquals(a, c);
        // null
        assertNotEquals(a, null);
        // other class
        assertNotEquals(a, new Object());

        // change a field to break equality
        b.setStatus("REJECTED");
        assertNotEquals(a, b);

        // handle null fields: set some fields to null in one entity
        LoanJpaEntity n1 = new LoanJpaEntity();
        LoanJpaEntity n2 = new LoanJpaEntity();
        n1.setId(null);
        n2.setId(null);
        assertEquals(n1, n2);
        n2.setApplicantName("X");
        assertNotEquals(n1, n2);
    }

    @Test
    @DisplayName("equals should differ when only id changes")
    void equalsDiffersOnId() {
        LoanJpaEntity a = newEntity(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        LoanJpaEntity b = newEntity(UUID.fromString("00000000-0000-0000-0000-000000000002"));
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals should differ when only applicantName changes")
    void equalsDiffersOnApplicantName() {
        LoanJpaEntity a = newEntity(UUID.fromString("00000000-0000-0000-0000-000000000003"));
        LoanJpaEntity b = newEntity(a.getId());
        b.setApplicantName("Other");
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals should differ when only applicantIdentity changes")
    void equalsDiffersOnApplicantIdentity() {
        LoanJpaEntity a = newEntity(UUID.fromString("00000000-0000-0000-0000-000000000004"));
        LoanJpaEntity b = newEntity(a.getId());
        b.setApplicantIdentity("99999999Z");
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals should differ when only amount changes")
    void equalsDiffersOnAmount() {
        LoanJpaEntity a = newEntity(UUID.fromString("00000000-0000-0000-0000-000000000005"));
        LoanJpaEntity b = newEntity(a.getId());
        b.setAmount(new BigDecimal("999.99"));
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals should differ when only currency changes")
    void equalsDiffersOnCurrency() {
        LoanJpaEntity a = newEntity(UUID.fromString("00000000-0000-0000-0000-000000000006"));
        LoanJpaEntity b = newEntity(a.getId());
        b.setCurrency("GBP");
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals should differ when only status changes")
    void equalsDiffersOnStatus() {
        LoanJpaEntity a = newEntity(UUID.fromString("00000000-0000-0000-0000-000000000007"));
        LoanJpaEntity b = newEntity(a.getId());
        b.setStatus("REJECTED");
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals should differ when only createdAt changes")
    void equalsDiffersOnCreatedAt() {
        LoanJpaEntity a = newEntity(UUID.fromString("00000000-0000-0000-0000-000000000008"));
        LoanJpaEntity b = newEntity(a.getId());
        b.setCreatedAt(Instant.parse("2026-02-07T12:34:56Z"));
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals should differ when only modifiedAt changes")
    void equalsDiffersOnModifiedAt() {
        LoanJpaEntity a = newEntity(UUID.fromString("00000000-0000-0000-0000-000000000009"));
        LoanJpaEntity b = newEntity(a.getId());
        b.setModifiedAt(Instant.parse("2026-02-07T12:34:57Z"));
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("equals should differ when one field is null and the other is non-null (id)")
    void equalsDiffersOnNullVsNonNullId() {
        LoanJpaEntity a = newEntity(UUID.fromString("ffffffff-ffff-ffff-ffff-fffffffffff3"));
        LoanJpaEntity b = newEntity(a.getId());
        a.setId(null);
        assertNotEquals(a, b);
    }
    @Test
    @DisplayName("equals should consider nulls per field")
    void equalsWithNullFieldsPerField() {
        LoanJpaEntity base = newEntity(UUID.fromString("00000000-0000-0000-0000-00000000000a"));
        // id null vs null
        LoanJpaEntity idNull1 = newEntity(null);
        LoanJpaEntity idNull2 = newEntity(null);
        assertEquals(idNull1, idNull2);
        // applicantName null equality
        LoanJpaEntity nameNull1 = newEntity(base.getId()); nameNull1.setApplicantName(null);
        LoanJpaEntity nameNull2 = newEntity(base.getId()); nameNull2.setApplicantName(null);
        assertEquals(nameNull1, nameNull2);
        // applicantIdentity null equality
        LoanJpaEntity identityNull1 = newEntity(base.getId()); identityNull1.setApplicantIdentity(null);
        LoanJpaEntity identityNull2 = newEntity(base.getId()); identityNull2.setApplicantIdentity(null);
        assertEquals(identityNull1, identityNull2);
        // amount null equality
        LoanJpaEntity amountNull1 = newEntity(base.getId()); amountNull1.setAmount(null);
        LoanJpaEntity amountNull2 = newEntity(base.getId()); amountNull2.setAmount(null);
        assertEquals(amountNull1, amountNull2);
        // currency null equality
        LoanJpaEntity currencyNull1 = newEntity(base.getId()); currencyNull1.setCurrency(null);
        LoanJpaEntity currencyNull2 = newEntity(base.getId()); currencyNull2.setCurrency(null);
        assertEquals(currencyNull1, currencyNull2);
        // status null equality
        LoanJpaEntity statusNull1 = newEntity(base.getId()); statusNull1.setStatus(null);
        LoanJpaEntity statusNull2 = newEntity(base.getId()); statusNull2.setStatus(null);
        assertEquals(statusNull1, statusNull2);
        // createdAt null equality
        LoanJpaEntity createdNull1 = newEntity(base.getId()); createdNull1.setCreatedAt(null);
        LoanJpaEntity createdNull2 = newEntity(base.getId()); createdNull2.setCreatedAt(null);
        assertEquals(createdNull1, createdNull2);
        // modifiedAt null equality
        LoanJpaEntity modifiedNull1 = newEntity(base.getId()); modifiedNull1.setModifiedAt(null);
        LoanJpaEntity modifiedNull2 = newEntity(base.getId()); modifiedNull2.setModifiedAt(null);
        assertEquals(modifiedNull1, modifiedNull2);
    }

    @Test
    @DisplayName("hashCode should work with nulls")
    void hashCodeWithNulls() {
        LoanJpaEntity e1 = new LoanJpaEntity();
        LoanJpaEntity e2 = new LoanJpaEntity();
        assertEquals(e1.hashCode(), e2.hashCode());
        e1.setApplicantName("A");
        assertNotEquals(e1.hashCode(), e2.hashCode());
        e2.setApplicantName("A");
        assertEquals(e1.hashCode(), e2.hashCode());
    }

    @Test
    @DisplayName("toString should handle nulls")
    void toStringWithNulls() {
        LoanJpaEntity e = new LoanJpaEntity();
        String s = e.toString();
        assertNotNull(s);
        assertTrue(s.contains("LoanJpaEntity"));
    }

    // Helper subclass to exercise Lombok canEqual branch
    static class LoanJpaEntitySubclass extends LoanJpaEntity {
        @Override
        public boolean canEqual(Object other) {
            return false; // Force canEqual to return false
        }
    }

    @Test
    @DisplayName("EqualsVerifier should validate equals and hashCode contracts for LoanJpaEntity")
    void equalsVerifierCoversAllBranches() {
        EqualsVerifier.forClass(LoanJpaEntity.class)
                .suppress(Warning.NONFINAL_FIELDS,
                          Warning.SURROGATE_KEY,
                          Warning.ALL_FIELDS_SHOULD_BE_USED,
                          Warning.STRICT_HASHCODE,
                          Warning.BIGDECIMAL_EQUALITY)
                .withPrefabValues(UUID.class,
                        UUID.fromString("11111111-1111-1111-1111-111111111111"),
                        UUID.fromString("22222222-2222-2222-2222-222222222222"))
                .verify();
    }

    @Test
    @DisplayName("equals should return false when canEqual returns false")
    void equalsReturnsFalseWhenCanEqualFalse() {
        LoanJpaEntitySubclass sub1 = new LoanJpaEntitySubclass();
        sub1.setId(UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee"));
        sub1.setApplicantName("Alvaro de la Flor Bonilla");
        sub1.setApplicantIdentity("12345678Z");
        sub1.setAmount(new BigDecimal("100.00"));
        sub1.setCurrency("EUR");
        sub1.setStatus("PENDING");
        sub1.setCreatedAt(Instant.parse("2026-02-07T10:00:00Z"));
        sub1.setModifiedAt(Instant.parse("2026-02-07T11:00:00Z"));

        LoanJpaEntitySubclass sub2 = new LoanJpaEntitySubclass();
        sub2.setId(sub1.getId());
        sub2.setApplicantName(sub1.getApplicantName());
        sub2.setApplicantIdentity(sub1.getApplicantIdentity());
        sub2.setAmount(sub1.getAmount());
        sub2.setCurrency(sub1.getCurrency());
        sub2.setStatus(sub1.getStatus());
        sub2.setCreatedAt(sub1.getCreatedAt());
        sub2.setModifiedAt(sub1.getModifiedAt());

        // Since canEqual returns false, equals should return false even if fields are equal
        assertNotEquals(sub1, sub2);
    }
}

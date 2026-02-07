package com.caixabanktech.loan.infrastructure.adapter.output.persistence.jpa;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JPA Entity Tests: AuditRevisionEntity")
class AuditRevisionEntityTest {

    @Test
    @DisplayName("Getter and Setter should work for id and timestamp")
    void gettersAndSettersWork() {
        AuditRevisionEntity entity = new AuditRevisionEntity();
        entity.setId(123);
        entity.setTimestamp(1700000000000L);

        assertEquals(123, entity.getId());
        assertEquals(1700000000000L, entity.getTimestamp());
    }

    @Test
    @DisplayName("equals and hashCode should be based on fields (Lombok @Data)")
    void equalsAndHashCode() {
        AuditRevisionEntity e1 = new AuditRevisionEntity();
        e1.setId(1);
        e1.setTimestamp(100L);

        AuditRevisionEntity e2 = new AuditRevisionEntity();
        e2.setId(1);
        e2.setTimestamp(100L);

        AuditRevisionEntity e3 = new AuditRevisionEntity();
        e3.setId(2);
        e3.setTimestamp(200L);

        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
        assertNotEquals(e1, e3);
    }

    @Test
    @DisplayName("toString should include class name and fields")
    void toStringContainsFields() {
        AuditRevisionEntity entity = new AuditRevisionEntity();
        entity.setId(42);
        entity.setTimestamp(777L);
        String s = entity.toString();
        assertTrue(s.contains("AuditRevisionEntity"));
        assertTrue(s.contains("id=42"));
        assertTrue(s.contains("timestamp=777"));
    }

    @Test
    @DisplayName("equals should return false when comparing with different type (canEqual behavior)")
    void equalsReturnsFalseForDifferentType() {
        AuditRevisionEntity e1 = new AuditRevisionEntity();
        e1.setId(1);
        e1.setTimestamp(100L);

        Object otherType = new Object();
        assertNotEquals(e1, otherType);

        AuditRevisionEntity e2 = new AuditRevisionEntity();
        e2.setId(1);
        e2.setTimestamp(100L);
        assertEquals(e1, e2);
        assertEquals(e2, e1);
    }

    @Test
    @DisplayName("equals should be reflexive (object equals itself)")
    void equalsReflexive() {
        AuditRevisionEntity e1 = new AuditRevisionEntity();
        e1.setId(1);
        e1.setTimestamp(100L);
        assertEquals(e1, e1);
    }

    @Test
    @DisplayName("equals should return false when comparing with null")
    void equalsReturnsFalseForNull() {
        AuditRevisionEntity e1 = new AuditRevisionEntity();
        e1.setId(1);
        e1.setTimestamp(100L);
        assertNotEquals(e1, null);
    }

    @Test
    @DisplayName("equals should return false when ids differ")
    void equalsReturnsFalseWhenIdDiffers() {
        AuditRevisionEntity e1 = new AuditRevisionEntity();
        e1.setId(1);
        e1.setTimestamp(100L);
        AuditRevisionEntity e2 = new AuditRevisionEntity();
        e2.setId(2);
        e2.setTimestamp(100L);
        assertNotEquals(e1, e2);
    }

    @Test
    @DisplayName("equals should return false when timestamps differ")
    void equalsReturnsFalseWhenTimestampDiffers() {
        AuditRevisionEntity e1 = new AuditRevisionEntity();
        e1.setId(1);
        e1.setTimestamp(100L);
        AuditRevisionEntity e2 = new AuditRevisionEntity();
        e2.setId(1);
        e2.setTimestamp(200L);
        assertNotEquals(e1, e2);
    }

    static class AuditRevisionEntitySubclass extends AuditRevisionEntity {
        @Override
        public boolean canEqual(Object other) {
            return false;
        }
    }

    @Test
    @DisplayName("equals should return false when canEqual in other returns false")
    void equalsReturnsFalseWhenOtherCanEqualIsFalse() {
        AuditRevisionEntity base = new AuditRevisionEntity();
        base.setId(1);
        base.setTimestamp(100L);

        AuditRevisionEntitySubclass other = new AuditRevisionEntitySubclass();
        other.setId(1);
        other.setTimestamp(100L);

        assertFalse(base.equals(other));
    }
}

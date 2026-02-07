package com.caixabanktech.loan.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Domain Tests: Entity LoanId")
class LoanIdTest {

    @Test
    @DisplayName("Constructor should accept non-null UUID and expose value")
    void constructorAcceptsNonNull() {
        UUID uuid = UUID.randomUUID();
        LoanId id = new LoanId(uuid);
        assertEquals(uuid, id.value());
    }

    @Test
    @DisplayName("Constructor should throw IllegalArgumentException when UUID is null")
    void constructorRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> new LoanId(null));
    }

    @Test
    @DisplayName("Record equality and hashCode should be based on UUID value")
    void equalityAndHashCode() {
        UUID uuid = UUID.randomUUID();
        LoanId id1 = new LoanId(uuid);
        LoanId id2 = new LoanId(uuid);
        LoanId id3 = new LoanId(UUID.randomUUID());

        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
        assertNotEquals(id1, id3);
    }

    @Test
    @DisplayName("toString should include the UUID value")
    void toStringContainsValue() {
        UUID uuid = UUID.randomUUID();
        LoanId id = new LoanId(uuid);
        String s = id.toString();
        assertTrue(s.contains(uuid.toString()));
    }
}


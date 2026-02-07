package com.caixabanktech.loan.domain.model;

import com.caixabanktech.loan.domain.exception.InvalidDomainDataException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Domain Tests: Entity LoanAmount")
class LoanAmountTest {

    @Test
    @DisplayName("Constructor should set scale to 2 using HALF_EVEN")
    void constructorSetsScaleHalfEven() {
        LoanAmount amount = new LoanAmount(new BigDecimal("1998.035"), Currency.getInstance("EUR"));
        assertEquals(new BigDecimal("1998.04"), amount.amount());
        assertEquals(Currency.getInstance("EUR"), amount.currency());
        assertEquals(2, amount.amount().scale());
    }

    @ParameterizedTest
    @DisplayName("Positive amounts should be accepted")
    @ValueSource(strings = {"0.01", "1", "100", "12345.67"})
    void positiveAmountsAccepted(String value) {
        LoanAmount amount = new LoanAmount(new BigDecimal(value), Currency.getInstance("USD"));
        assertTrue(amount.amount().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("Null amount should throw InvalidDomainDataException")
    void nullAmountThrows() {
        assertThrows(InvalidDomainDataException.class, () -> new LoanAmount(null, Currency.getInstance("EUR")));
    }

    @ParameterizedTest
    @DisplayName("Zero or negative amount should throw InvalidDomainDataException")
    @ValueSource(strings = {"0", "-0.01", "-100"})
    void nonPositiveAmountThrows(String value) {
        assertThrows(InvalidDomainDataException.class, () -> new LoanAmount(new BigDecimal(value), Currency.getInstance("EUR")));
    }

    @Test
    @DisplayName("Null currency should throw InvalidDomainDataException")
    void nullCurrencyThrows() {
        assertThrows(InvalidDomainDataException.class, () -> new LoanAmount(new BigDecimal("10"), null));
    }

    @Test
    @DisplayName("Record equality should compare amount and currency")
    void recordEqualityWorks() {
        LoanAmount a1 = new LoanAmount(new BigDecimal("10.01"), Currency.getInstance("EUR"));
        LoanAmount a2 = new LoanAmount(new BigDecimal("10.01"), Currency.getInstance("EUR"));
        LoanAmount a3 = new LoanAmount(new BigDecimal("10.01"), Currency.getInstance("USD"));
        assertEquals(a1, a2);
        assertNotEquals(a2, a3);
        assertEquals(a2.hashCode(), a1.hashCode());
    }
}

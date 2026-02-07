package com.caixabanktech.loan.domain.model;

import com.caixabanktech.loan.domain.exception.InvalidDomainDataException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Domain Tests: Entity ApplicantIdentity")
@ExtendWith(MockitoExtension.class)
class ApplicantIdentityTest {

    @Test
    @DisplayName("Constructor should normalize to uppercase and trim")
    void constructorNormalizesInput() {
        ApplicantIdentity id = new ApplicantIdentity("  x1234567l  ");
        assertEquals("X1234567L", id.value());
    }

    @ParameterizedTest
    @DisplayName("Valid DNI should be accepted")
    @ValueSource(strings = {"00000000T", "12345678Z", "98765432M"})
    void validDniAccepted(String dni) {
        ApplicantIdentity id = new ApplicantIdentity(dni);
        assertEquals(dni, id.value());
    }

    @ParameterizedTest
    @DisplayName("Valid NIE should be accepted")
    @ValueSource(strings = {"X1234567L", "Y5261900C", "X0807007Y"})
    void validNieAccepted(String nie) {
        ApplicantIdentity id = new ApplicantIdentity(nie);
        assertEquals(nie, id.value());
    }

    @ParameterizedTest
    @DisplayName("Invalid format should throw InvalidDomainDataException")
    @ValueSource(strings = {"", " ", "1234567Z", "A2345678Z", "123456789Z", "X12345678L", "X1234567LL"})
    void invalidFormatThrows(String input) {
        assertThrows(InvalidDomainDataException.class, () -> new ApplicantIdentity(input));
    }

    @ParameterizedTest
    @DisplayName("Wrong checksum should throw InvalidDomainDataException")
    @ValueSource(strings = {"00000000A", "12345678A", "X1234567A", "Z7654321A"})
    void wrongChecksumThrows(String input) {
        assertThrows(InvalidDomainDataException.class, () -> new ApplicantIdentity(input));
    }

    @Test
    @DisplayName("Null value should throw InvalidDomainDataException")
    void nullValueThrows() {
        assertThrows(InvalidDomainDataException.class, () -> new ApplicantIdentity(null));
    }

    @Test
    @DisplayName("Lowercase valid input should be normalized and accepted")
    void lowercaseInputAccepted() {
        ApplicantIdentity id = new ApplicantIdentity("x1234567l");
        assertEquals("X1234567L", id.value());
    }
}

package com.caixabanktech.loan.domain.model;

import com.caixabanktech.loan.domain.exception.InvalidDomainDataException;
import java.util.regex.Pattern;

public record ApplicantIdentity(String value) {
    private static final Pattern DNI_NIE_PATTERN = Pattern.compile("^[XYZ0-9][0-9]{7}[A-Z]$");
    private static final String CONTROL_LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";

    public ApplicantIdentity {
        if (value == null || value.isBlank()) {
            throw new InvalidDomainDataException("Identification is mandatory.");
        }
        String normalized = value.toUpperCase().trim();
        if (!isValidChecksum(normalized)) {
            throw new InvalidDomainDataException("DNI/NIE not valid: " + value);
        }
        value = normalized;
    }

    private static boolean isValidChecksum(String value) {
        if (!DNI_NIE_PATTERN.matcher(value).matches()) return false;

        String numericPart = value.substring(0, 8)
                .replace("X", "0")
                .replace("Y", "1")
                .replace("Z", "2");

        int number = Integer.parseInt(numericPart);
        char expectedLetter = CONTROL_LETTERS.charAt(number % 23);
        return expectedLetter == value.charAt(8);
    }
}

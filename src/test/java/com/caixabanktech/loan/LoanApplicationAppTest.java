package com.caixabanktech.loan;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("LoanApplicationApp")
class LoanApplicationAppTest {
    @Test
    @DisplayName("main method throws UnsatisfiedDependencyException without configured JPA repositories")
    void mainFailsWithUnsatisfiedDependencies() {
        System.setProperty("spring.main.web-application-type", "none");
        System.setProperty("spring.autoconfigure.exclude",
                String.join(",",
                        "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
                        "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration",
                        "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
                )
        );
        try {
            assertThrows(org.springframework.beans.factory.UnsatisfiedDependencyException.class,
                    () -> LoanApplicationApp.main(new String[]{}));
        } finally {
            System.clearProperty("spring.main.web-application-type");
            System.clearProperty("spring.autoconfigure.exclude");
        }
    }
}

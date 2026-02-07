package com.caixabanktech.loan;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@OpenAPIDefinition(info = @Info(title = "Loan API v1", version = "1.0", description = "Temporary Audit Loan API"))
public class LoanApplicationApp {

	public static void main(String[] args) {
		SpringApplication.run(LoanApplicationApp.class, args);
	}
}

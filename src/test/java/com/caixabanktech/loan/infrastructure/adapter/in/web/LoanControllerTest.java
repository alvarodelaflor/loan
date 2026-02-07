package com.caixabanktech.loan.infrastructure.adapter.in.web;

import com.caixabanktech.loan.domain.model.ApplicantIdentity;
import com.caixabanktech.loan.domain.model.LoanAmount;
import com.caixabanktech.loan.domain.model.LoanApplication;
import com.caixabanktech.loan.domain.model.LoanId;
import com.caixabanktech.loan.domain.model.LoanStatus;
import com.caixabanktech.loan.domain.port.in.CreateLoanCommand;
import com.caixabanktech.loan.domain.port.in.CreateLoanUseCase;
import com.caixabanktech.loan.domain.port.in.ModifyLoanStatusUseCase;
import com.caixabanktech.loan.domain.port.in.RetrieveLoanUseCase;
import com.caixabanktech.loan.infrastructure.adapter.input.rest.LoanController;
import com.caixabanktech.loan.infrastructure.adapter.input.rest.dto.LoanResponse;
import com.caixabanktech.loan.infrastructure.adapter.output.persistence.mapper.LoanRestMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Controller Tests: Entity LoanController")
@WebMvcTest(LoanController.class)
@AutoConfigureMockMvc
class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private CreateLoanUseCase createUseCase;
    @MockBean private ModifyLoanStatusUseCase modifyStatusUseCase;
    @MockBean private RetrieveLoanUseCase retrieveUseCase;
    @MockBean private LoanRestMapper loanRestMapper;
    @MockBean private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    private static final String PATH = "/api/v1/loans";
    private static final String SEARCH_PATH = PATH + "/%s";
    private static final String HISTORY_PATH = PATH + "/%s/history";
    private static final String STATUS_PATH = PATH + "/%s/status";
    private static final String APPLICATION_PATH = PATH + "/search/%s";
    private static final String SEARCH_CRITERIA_PATH = PATH + "/search/criteria";

    private LoanApplication sampleLoan(LoanStatus status) {
        return LoanApplication.builder()
                .id(new LoanId(UUID.randomUUID()))
                .applicantName("Alvaro de la Flor Bonilla")
                .applicantIdentity(new ApplicantIdentity("12345678Z"))
                .loanAmount(new LoanAmount(BigDecimal.valueOf(1998.03), Currency.getInstance("EUR")))
                .createdAt(Instant.parse("2026-02-07T10:00:00Z"))
                .modifiedAt(Instant.parse("2026-02-07T11:00:00Z"))
                .status(status)
                .build();
    }

    private LoanResponse sampleResponse(LoanApplication loan) {
        return new LoanResponse(
                loan.getId().value().toString(),
                loan.getApplicantName(),
                loan.getApplicantIdentity().value(),
                loan.getLoanAmount().amount(),
                loan.getLoanAmount().currency().getCurrencyCode(),
                loan.getCreatedAt(),
                loan.getModifiedAt(),
                loan.getStatus().name()
        );
    }

    @Test
    @DisplayName("POST /api/v1/loans should create and return 201")
    void shouldCreateLoanAndReturn201() throws Exception {
        LoanApplication loan = sampleLoan(LoanStatus.PENDING);
        CreateLoanCommand command = new CreateLoanCommand(
                loan.getApplicantName(),
                loan.getLoanAmount().amount(),
                loan.getLoanAmount().currency().getCurrencyCode(),
                loan.getApplicantIdentity().value()
        );
        when(createUseCase.createLoan(command)).thenReturn(loan);
        when(loanRestMapper.toResponse(loan)).thenReturn(sampleResponse(loan));

        String body = """
            {
              "applicantName": "Alvaro de la Flor Bonilla",
              "amount": 1998.03,
              "currency": "EUR",
              "identityDocument": "12345678Z"
            }
            """;

        mockMvc.perform(MockMvcRequestBuilders.post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.applicantName").value(loan.getApplicantName()))
                .andExpect(jsonPath("$.applicantIdentity").value(loan.getApplicantIdentity().value()))
                .andExpect(jsonPath("$.loanAmount").value(loan.getLoanAmount().amount()))
                .andExpect(jsonPath("$.currency").value(loan.getLoanAmount().currency().getCurrencyCode()))
                .andExpect(jsonPath("$.status").value(loan.getStatus().name()));
    }

    @Test
    @DisplayName("GET /api/v1/loans/{id} should return loan 200")
    void shouldGetLoanById() throws Exception {
        LoanApplication loan = sampleLoan(LoanStatus.PENDING);
        UUID id = loan.getId().value();
        when(retrieveUseCase.getLoan(id)).thenReturn(loan);
        when(loanRestMapper.toResponse(loan)).thenReturn(sampleResponse(loan));

        mockMvc.perform(MockMvcRequestBuilders.get(SEARCH_PATH.formatted(loan.getId().value())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.status").value(LoanStatus.PENDING.name()));
    }

    @Test
    @DisplayName("GET /api/v1/loans/{id}/history should return list 200")
    void shouldGetLoanHistory() throws Exception {
        LoanApplication l1 = sampleLoan(LoanStatus.PENDING);
        LoanApplication l2 = sampleLoan(LoanStatus.APPROVED);
        UUID id = l1.getId().value();
        when(retrieveUseCase.getLoanHistory(id)).thenReturn(List.of(l1, l2));
        when(loanRestMapper.toResponse(l1)).thenReturn(sampleResponse(l1));
        when(loanRestMapper.toResponse(l2)).thenReturn(sampleResponse(l2));
        when(loanRestMapper.toResponseList(List.of(l1, l2))).thenAnswer(inv -> {
            List<LoanApplication> loans = inv.getArgument(0);
            return loans.stream().map(this::sampleResponse).toList();
        });

        mockMvc.perform(MockMvcRequestBuilders.get(HISTORY_PATH.formatted(id)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value(LoanStatus.PENDING.name()))
                .andExpect(jsonPath("$[1].status").value(LoanStatus.APPROVED.name()));
    }

    @Test
    @DisplayName("PATCH /api/v1/loans/{id}/status APPROVED returns 204")
    void shouldUpdateStatusApproved() throws Exception {
        UUID id = UUID.randomUUID();
        when(modifyStatusUseCase.approveLoan(id)).thenReturn(sampleLoan(LoanStatus.APPROVED));

        mockMvc.perform(MockMvcRequestBuilders.patch(STATUS_PATH.formatted(id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"%s\"}".formatted(LoanStatus.APPROVED.name())))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("PATCH /api/v1/loans/{id}/status REJECTED returns 204")
    void shouldUpdateStatusRejected() throws Exception {
        UUID id = UUID.randomUUID();
        when(modifyStatusUseCase.rejectLoan(id)).thenReturn(sampleLoan(LoanStatus.REJECTED));

        mockMvc.perform(MockMvcRequestBuilders.patch(STATUS_PATH.formatted(id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"%s\"}".formatted(LoanStatus.REJECTED.name())))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("PATCH /api/v1/loans/{id}/status CANCELLED returns 204")
    void shouldUpdateStatusCancelled() throws Exception {
        UUID id = UUID.randomUUID();
        when(modifyStatusUseCase.cancelLoan(id)).thenReturn(sampleLoan(LoanStatus.CANCELLED));

        mockMvc.perform(MockMvcRequestBuilders.patch(STATUS_PATH.formatted(id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"%s\"}".formatted(LoanStatus.CANCELLED.name())))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("PATCH /api/v1/loans/{id}/status PENDING returns 400")
    void shouldRejectPendingTransition() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(MockMvcRequestBuilders.patch(STATUS_PATH.formatted(id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"%s\"}".formatted(LoanStatus.PENDING.name())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/v1/loans/{id}/status UNKNOWN returns 400")
    void shouldRejectUnknownTransition() throws Exception {
        UUID id = UUID.randomUUID();
        mockMvc.perform(MockMvcRequestBuilders.patch(STATUS_PATH.formatted(id))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"UNKNOWN\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/loans/search/{identity} should return list 200")
    void shouldSearchByIdentity() throws Exception {
        ApplicantIdentity identity = new ApplicantIdentity("12345678Z");
        List<LoanApplication> loans = List.of(sampleLoan(LoanStatus.PENDING));
        when(retrieveUseCase.getLoansByIdentity(identity)).thenReturn(loans);
        when(loanRestMapper.toResponseList(loans)).thenReturn(loans.stream().map(this::sampleResponse).toList());

        mockMvc.perform(MockMvcRequestBuilders.get(APPLICATION_PATH.formatted(identity.value())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].applicantIdentity").value(identity.value()));
    }

    @Test
    @DisplayName("GET /api/v1/loans/search/criteria should return list 200")
    void shouldSearchByCriteria() throws Exception {
        var sampleLoan = sampleLoan(LoanStatus.PENDING);
        List<LoanApplication> loans = List.of(sampleLoan);
        when(retrieveUseCase.searchLoans(sampleLoan.getApplicantName(), sampleLoan.getCreatedAt(), sampleLoan.getModifiedAt())).thenReturn(loans);
        when(loanRestMapper.toResponseList(loans)).thenReturn(loans.stream().map(this::sampleResponse).toList());

        mockMvc.perform(MockMvcRequestBuilders.get(SEARCH_CRITERIA_PATH)
                        .param("applicantIdentity", sampleLoan.getApplicantName())
                        .param("startDate", sampleLoan.getCreatedAt().toString())
                        .param("endDate", sampleLoan.getModifiedAt().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value(LoanStatus.PENDING.name()));
    }

    @Test
    @DisplayName("DELETE /api/v1/loans/{id} should return 204")
    void shouldDeleteLoan() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(MockMvcRequestBuilders.delete(PATH + "/" + id))
                .andExpect(status().isNoContent());
    }
}

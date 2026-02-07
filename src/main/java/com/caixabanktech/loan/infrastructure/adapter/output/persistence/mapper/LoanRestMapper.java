package com.caixabanktech.loan.infrastructure.adapter.output.persistence.mapper;

import com.caixabanktech.loan.domain.model.LoanApplication;
import com.caixabanktech.loan.infrastructure.adapter.input.rest.dto.LoanResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LoanRestMapper {

    @Mapping(target = "id", source = "domain.id.value")
    @Mapping(target = "applicantIdentity", source = "domain.applicantIdentity.value")
    @Mapping(target = "loanAmount", source = "domain.loanAmount.amount")
    @Mapping(target = "currency", source = "domain.loanAmount.currency.currencyCode")
    @Mapping(target = "status", expression = "java(domain.getStatus().name())")
    LoanResponse toResponse(LoanApplication domain);

    List<LoanResponse> toResponseList(List<LoanApplication> domains);
}

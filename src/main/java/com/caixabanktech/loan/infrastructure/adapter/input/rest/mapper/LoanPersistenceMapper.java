package com.caixabanktech.loan.infrastructure.adapter.input.rest.mapper;

import com.caixabanktech.loan.domain.model.ApplicantIdentity;
import com.caixabanktech.loan.domain.model.LoanAmount;
import com.caixabanktech.loan.domain.model.LoanApplication;
import com.caixabanktech.loan.domain.model.LoanId;
import com.caixabanktech.loan.infrastructure.adapter.output.persistence.jpa.LoanJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.UUID;

@Mapper(
        componentModel = "spring",
        builder = @org.mapstruct.Builder(buildMethod = "build")
)
public interface LoanPersistenceMapper {

    @Mapping(target = "id", source = "domain.id.value")
    @Mapping(target = "applicantIdentity", source = "domain.applicantIdentity.value")
    @Mapping(target = "amount", source = "domain.loanAmount.amount")
    @Mapping(target = "currency", source = "domain.loanAmount.currency.currencyCode")
    @Mapping(target = "status", expression = "java(domain.getStatus().name())")
    LoanJpaEntity toEntity(LoanApplication domain);

    @Mapping(target = "id", source = "entity.id", qualifiedByName = "mapToLoanId")
    @Mapping(target = "applicantIdentity", source = "entity.applicantIdentity", qualifiedByName = "mapToIdentity")
    @Mapping(target = "loanAmount", expression = "java(mapToLoanAmount(entity))")
    LoanApplication toDomain(LoanJpaEntity entity);

    @Named("mapToLoanId")
    default LoanId mapToLoanId(UUID id) { return new LoanId(id); }

    @Named("mapToIdentity")
    default ApplicantIdentity mapToIdentity(String val) { return new ApplicantIdentity(val); }

    default LoanAmount mapToLoanAmount(LoanJpaEntity entity) {
        return new LoanAmount(entity.getAmount(), java.util.Currency.getInstance(entity.getCurrency()));
    }
}

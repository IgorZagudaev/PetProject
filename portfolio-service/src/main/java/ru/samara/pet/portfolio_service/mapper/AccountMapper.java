package ru.samara.pet.portfolio_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.samara.pet.portfolio_service.model.Account;
import ru.samara.pet.portfolio_service.model.dto.AccountResponse;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    @Mapping(source = "id", target = "id")
    @Mapping(source = "userId", target = "userId")
    @Mapping(source = "createdAt", target = "createdAt")
    AccountResponse toResponse(Account account);

}

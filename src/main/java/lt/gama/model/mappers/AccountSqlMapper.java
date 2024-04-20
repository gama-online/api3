package lt.gama.model.mappers;

import lt.gama.model.dto.entities.AccountDto;
import lt.gama.model.dto.entities.CompanyDto;
import lt.gama.model.sql.system.AccountSql;
import lt.gama.model.sql.system.CompanySql;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {UtilsMapper.class}, componentModel = "spring")
public interface AccountSqlMapper extends IBaseMapper<AccountDto, AccountSql> {

    @Override
    AccountDto toDto(AccountSql entity);

    @Override
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "salt", ignore = true)
    AccountSql toEntity(AccountDto dto);

    @Mapping(target = "otherAccountsList", ignore = true)
    CompanyDto toDto(CompanySql entity);

    @Mapping(target = "otherAccountsList", ignore = true)
    CompanySql toEntity(CompanyDto dto);
}

package lt.gama.model.mappers;

import lt.gama.model.dto.entities.BankAccountDto;
import lt.gama.model.sql.entities.BankAccountSql;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {UtilsMapper.class}, componentModel = "spring")
public abstract class BankAccountSqlMapper implements IBaseMapper<BankAccountDto, BankAccountSql> {

    @Override
    @Mapping(target = "remainders", ignore = true)
    public abstract BankAccountDto toDto(BankAccountSql entity);

    @Override
    @Mapping(target = "remainders", ignore = true)
    public abstract BankAccountSql toEntity(BankAccountDto dto);
}

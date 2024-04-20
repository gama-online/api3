package lt.gama.model.mappers;

import lt.gama.model.dto.entities.DebtNowDto;
import lt.gama.model.sql.entities.DebtNowSql;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {UtilsMapper.class, CounterpartySqlMapper.class}, componentModel = "spring")
public abstract class DebtNowSqlMapper
        extends DebtSqlMapper<DebtNowDto, DebtNowSql> implements IBaseMapper<DebtNowDto, DebtNowSql> {

    @Override
    @Mapping(target = "counterparty", ignore = true)
    public abstract DebtNowDto toDto(DebtNowSql entity);

    @Override
    @Mapping(target = "counterparty", ignore = true)
    public abstract DebtNowSql toEntity(DebtNowDto dto);
}
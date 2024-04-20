package lt.gama.model.mappers;

import lt.gama.model.dto.entities.DebtHistoryDto;
import lt.gama.model.sql.entities.DebtHistorySql;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {UtilsMapper.class, CounterpartySqlMapper.class}, componentModel = "spring")
public abstract class DebtHistorySqlMapper
        extends DebtSqlMapper<DebtHistoryDto, DebtHistorySql> implements IBaseMapper<DebtHistoryDto, DebtHistorySql> {

    @Override
    @Mapping(target = "counterparty", ignore = true)
    public abstract DebtHistoryDto toDto(DebtHistorySql entity);

    @Override
    @Mapping(target = "counterparty", ignore = true)
    public abstract DebtHistorySql toEntity(DebtHistoryDto dto);
}

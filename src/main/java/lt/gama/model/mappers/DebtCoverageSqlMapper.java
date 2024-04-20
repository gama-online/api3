package lt.gama.model.mappers;

import lt.gama.model.dto.entities.DebtCoverageDto;
import lt.gama.model.sql.entities.DebtCoverageSql;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {UtilsMapper.class, CounterpartySqlMapper.class}, componentModel = "spring")
public abstract class DebtCoverageSqlMapper
        extends DebtSqlMapper<DebtCoverageDto, DebtCoverageSql> implements IBaseMapper<DebtCoverageDto, DebtCoverageSql> {

    @Override
    @Mapping(target = "counterparty", ignore = true)
    public abstract DebtCoverageDto toDto(DebtCoverageSql entity);

    @Override
    @Mapping(target = "counterparty", ignore = true)
    public abstract DebtCoverageSql toEntity(DebtCoverageDto dto);
}

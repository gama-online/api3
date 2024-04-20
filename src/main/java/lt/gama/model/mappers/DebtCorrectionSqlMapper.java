package lt.gama.model.mappers;

import lt.gama.model.dto.documents.DebtCorrectionDto;
import lt.gama.model.sql.documents.DebtCorrectionSql;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {UtilsMapper.class, EmployeeSqlMapper.class, CounterpartySqlMapper.class}, componentModel = "spring")
public abstract class DebtCorrectionSqlMapper extends BaseDocumentSqlMapper<DebtCorrectionDto, DebtCorrectionSql> {

    @Override
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "sum", ignore = true)
    @Mapping(target = "baseSum", ignore = true)
    public abstract DebtCorrectionDto toDto(DebtCorrectionSql entity);

    @Override
    @Mapping(target = "counterparty", ignore = true)
    public abstract DebtCorrectionSql toEntity(DebtCorrectionDto dto);
}

package lt.gama.model.mappers;

import lt.gama.model.dto.documents.EmployeeOperationDto;
import lt.gama.model.sql.documents.EmployeeOperationSql;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {UtilsMapper.class, EmployeeSqlMapper.class, CounterpartySqlMapper.class}, componentModel = "spring")
public abstract class EmployeeOperationSqlMapper extends BaseDocumentSqlMapper<EmployeeOperationDto, EmployeeOperationSql> {

    @Override
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "sum", ignore = true)
    @Mapping(target = "baseSum", ignore = true)
    public abstract EmployeeOperationDto toDto(EmployeeOperationSql entity);

    @Override
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "employee", ignore = true)
    public abstract EmployeeOperationSql toEntity(EmployeeOperationDto dto);
}

package lt.gama.model.mappers;

import lt.gama.helpers.Validators;
import lt.gama.model.dto.documents.CashOperationDto;
import lt.gama.model.dto.entities.CashDto;
import lt.gama.model.sql.documents.CashOperationSql;
import lt.gama.model.sql.entities.CashSql;
import lt.gama.model.type.enums.DBType;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(uses = {UtilsMapper.class, CashSqlMapper.class, EmployeeSqlMapper.class, CounterpartySqlMapper.class}, componentModel = "spring")
public abstract class CashOperationSqlMapper extends BaseDocumentSqlMapper<CashOperationDto, CashOperationSql> {

    @Override
    @Mapping(target = "cash", ignore = true)
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "sum", ignore = true)
    @Mapping(target = "baseSum", ignore = true)
    public abstract CashOperationDto toDto(CashOperationSql entity);

    @Override
    @Mapping(target = "cash", ignore = true)
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "employee", ignore = true)
    public abstract CashOperationSql toEntity(CashOperationDto dto);

    @AfterMapping
    void afterToEntity(CashOperationDto src, @MappingTarget CashOperationSql target) {
        if (Validators.isValid(src.getCash())) {
            target.setCash(entityManager.getReference(CashSql.class, src.getCash().getId()));
        } else {
            target.setCash(null);
        }
    }

    @AfterMapping
    void afterToDto(CashOperationSql src, @MappingTarget CashOperationDto target) {
        if (Validators.isValid(src.getCash())) {
            target.setCash(entityManager.getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(src, "cash")
                    ? new CashDto(src.getCash())
                    : new CashDto(src.getCash().getId(), DBType.POSTGRESQL));
        } else {
            target.setCash(null);
        }
    }
}

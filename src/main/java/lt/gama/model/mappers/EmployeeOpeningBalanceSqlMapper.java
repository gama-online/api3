package lt.gama.model.mappers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.helpers.Validators;
import lt.gama.model.dto.documents.EmployeeOpeningBalanceDto;
import lt.gama.model.dto.documents.items.EmployeeBalanceDto;
import lt.gama.model.sql.documents.EmployeeOpeningBalanceSql;
import lt.gama.model.sql.documents.items.EmployeeOpeningBalanceEmployeeSql;
import lt.gama.model.sql.entities.EmployeeSql;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(uses = {UtilsMapper.class, EmployeeSqlMapper.class}, componentModel = "spring")
public abstract class EmployeeOpeningBalanceSqlMapper implements IBaseMapper<EmployeeOpeningBalanceDto, EmployeeOpeningBalanceSql> {

    @PersistenceContext
    protected EntityManager entityManager;


    @Override
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "employee", ignore = true)
    public abstract EmployeeOpeningBalanceDto toDto(EmployeeOpeningBalanceSql entity);

    @Override
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "employee", ignore = true)
    public abstract EmployeeOpeningBalanceSql toEntity(EmployeeOpeningBalanceDto dto);

    @AfterMapping
    public void afterToEntity(EmployeeOpeningBalanceDto src, @MappingTarget EmployeeOpeningBalanceSql target) {
        if (target.getEmployees() != null) {
            target.getEmployees().forEach(cp -> cp.setParent(target));
        }
    }

    public abstract List<EmployeeBalanceDto> toBalanceDtoList(List<EmployeeOpeningBalanceEmployeeSql> entity);

    public abstract List<EmployeeOpeningBalanceEmployeeSql> toBalanceEntityList(List<EmployeeBalanceDto> dto);

    @Mapping(target = "sum", ignore = true)
    @Mapping(target = "baseSum", ignore = true)
    @Mapping(target = "baseFixSum", ignore = true)
    public abstract EmployeeBalanceDto toBalanceDto(EmployeeOpeningBalanceEmployeeSql entity);

    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "employee", ignore = true)
    public abstract EmployeeOpeningBalanceEmployeeSql toBalanceEntity(EmployeeBalanceDto dto);

    @AfterMapping
    void afterToEntity(EmployeeBalanceDto src, @MappingTarget EmployeeOpeningBalanceEmployeeSql target) {
        if (Validators.isValid(src.getEmployee())) {
            target.setEmployee(entityManager.getReference(EmployeeSql.class, src.getEmployee().getId()));
        } else {
            target.setEmployee(null);
        }
    }
}

package lt.gama.model.mappers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.helpers.Validators;
import lt.gama.model.dto.documents.EmployeeRateInfluenceDto;
import lt.gama.model.dto.documents.items.EmployeeBalanceDto;
import lt.gama.model.sql.documents.EmployeeRateInfluenceSql;
import lt.gama.model.sql.documents.items.EmployeeRateInfluenceMoneyBalanceSql;
import lt.gama.model.sql.entities.EmployeeSql;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(uses = {UtilsMapper.class, EmployeeSqlMapper.class}, componentModel = "spring")
public abstract class EmployeeRateInfluenceSqlMapper implements IBaseMapper<EmployeeRateInfluenceDto, EmployeeRateInfluenceSql> {

    @PersistenceContext
    protected EntityManager entityManager;


    @Override
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "itemsFinished", ignore = true)
    public abstract EmployeeRateInfluenceDto toDto(EmployeeRateInfluenceSql entity);

    @Override
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "itemsFinished", ignore = true)
    public abstract EmployeeRateInfluenceSql toEntity(EmployeeRateInfluenceDto dto);

    @AfterMapping
    void afterToEntity(EmployeeRateInfluenceDto src, @MappingTarget EmployeeRateInfluenceSql target) {
        if (target.getAccounts() != null) {
            target.getAccounts().forEach(account -> account.setParent(target));
        }
    }

    public abstract List<EmployeeBalanceDto> toEmployeeBalanceDtoList(List<EmployeeRateInfluenceMoneyBalanceSql> entity);

    public abstract List<EmployeeRateInfluenceMoneyBalanceSql> toEmployeeBalanceEntityList(List<EmployeeBalanceDto> dto);

    @Mapping(target = "sum", ignore = true)
    @Mapping(target = "baseSum", ignore = true)
    @Mapping(target = "baseFixSum", ignore = true)
    public abstract EmployeeBalanceDto toEmployeeBalanceDto(EmployeeRateInfluenceMoneyBalanceSql entity);

    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "employee", ignore = true)
    public abstract EmployeeRateInfluenceMoneyBalanceSql toEmployeeRateInfluenceBalanceEntity(EmployeeBalanceDto dto);

    @AfterMapping
    void afterToEntity(EmployeeBalanceDto src, @MappingTarget EmployeeRateInfluenceMoneyBalanceSql target) {
        if (Validators.isValid(src.getEmployee())) {
            target.setEmployee(entityManager.getReference(EmployeeSql.class, src.getEmployee().getId()));
        } else {
            target.setEmployee(null);
        }
    }
}

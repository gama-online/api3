package lt.gama.model.mappers;

import lt.gama.model.dto.documents.DebtOpeningBalanceDto;
import lt.gama.model.dto.documents.items.DebtBalanceDto;
import lt.gama.model.sql.documents.DebtOpeningBalanceSql;
import lt.gama.model.sql.documents.items.DebtOpeningBalanceCounterpartySql;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(uses = {UtilsMapper.class, EmployeeSqlMapper.class, CounterpartySqlMapper.class}, componentModel = "spring")
public interface DebtOpeningBalanceSqlMapper extends IBaseMapper<DebtOpeningBalanceDto, DebtOpeningBalanceSql> {

    @Override
    DebtOpeningBalanceDto toDto(DebtOpeningBalanceSql entity);

    @Override
    DebtOpeningBalanceSql toEntity(DebtOpeningBalanceDto dto);

    @AfterMapping
    default void afterToEntity(DebtOpeningBalanceDto src, @MappingTarget DebtOpeningBalanceSql target) {
        if (target.getCounterparties() != null) {
            target.getCounterparties().forEach(cp -> cp.setParent(target));
        }
    }

    List<DebtBalanceDto> toBalanceDtoList(List<DebtOpeningBalanceCounterpartySql> entity);

    List<DebtOpeningBalanceCounterpartySql> toBalanceEntityList(List<DebtBalanceDto> dto);

    @Mapping(target = "sum", ignore = true)
    @Mapping(target = "baseSum", ignore = true)
    @Mapping(target = "baseFixSum", ignore = true)
    DebtBalanceDto toBalanceDto(DebtOpeningBalanceCounterpartySql entity);

    @Mapping(target = "parent", ignore = true)
    DebtOpeningBalanceCounterpartySql toBalanceEntity(DebtBalanceDto dto);
}

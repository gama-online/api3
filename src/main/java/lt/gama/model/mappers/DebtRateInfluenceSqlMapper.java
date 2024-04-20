package lt.gama.model.mappers;

import lt.gama.model.dto.documents.DebtRateInfluenceDto;
import lt.gama.model.dto.documents.items.DebtBalanceDto;
import lt.gama.model.sql.documents.DebtRateInfluenceSql;
import lt.gama.model.sql.documents.items.DebtRateInfluenceMoneyBalanceSql;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(uses = {UtilsMapper.class, EmployeeSqlMapper.class, CounterpartySqlMapper.class}, componentModel = "spring")
public abstract class DebtRateInfluenceSqlMapper extends BaseDocumentSqlMapper<DebtRateInfluenceDto, DebtRateInfluenceSql> {

    @Override
    @Mapping(target = "itemsFinished", ignore = true)
    public abstract DebtRateInfluenceDto toDto(DebtRateInfluenceSql entity);

    @Override
    @Mapping(target = "itemsFinished", ignore = true)
    public abstract DebtRateInfluenceSql toEntity(DebtRateInfluenceDto dto);

    @AfterMapping
    void afterToEntity(DebtRateInfluenceDto src, @MappingTarget DebtRateInfluenceSql target) {
        if (target.getAccounts() != null) {
            target.getAccounts().forEach(account -> account.setParent(target));
        }
    }

    public abstract List<DebtBalanceDto> toBalanceDtoList(List<DebtRateInfluenceMoneyBalanceSql> entity);

    public abstract List<DebtRateInfluenceMoneyBalanceSql> toBalanceEntityList(List<DebtBalanceDto> dto);

    @Mapping(target = "sum", ignore = true)
    @Mapping(target = "baseSum", ignore = true)
    @Mapping(target = "baseFixSum", ignore = true)
    public abstract DebtBalanceDto toDebtRateInfluenceBalanceDto(DebtRateInfluenceMoneyBalanceSql entity);

    @Mapping(target = "parent", ignore = true)
    public abstract DebtRateInfluenceMoneyBalanceSql toBalanceEntity(DebtBalanceDto dto);
}

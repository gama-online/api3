package lt.gama.model.mappers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.helpers.Validators;
import lt.gama.model.dto.documents.CashRateInfluenceDto;
import lt.gama.model.dto.documents.items.CashBalanceDto;
import lt.gama.model.sql.documents.CashRateInfluenceSql;
import lt.gama.model.sql.documents.items.CashRateInfluenceMoneyBalanceSql;
import lt.gama.model.sql.entities.CashSql;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(uses = {UtilsMapper.class, CashSqlMapper.class}, componentModel = "spring")
public abstract class CashRateInfluenceSqlMapper implements IBaseMapper<CashRateInfluenceDto, CashRateInfluenceSql> {

    @PersistenceContext
    protected EntityManager entityManager;


    @Override
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "itemsFinished", ignore = true)
    public abstract CashRateInfluenceDto toDto(CashRateInfluenceSql entity);

    @Override
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "itemsFinished", ignore = true)
    public abstract CashRateInfluenceSql toEntity(CashRateInfluenceDto dto);

    @AfterMapping
    void afterToEntity(CashRateInfluenceDto src, @MappingTarget CashRateInfluenceSql target) {
        if (target.getAccounts() != null) {
            target.getAccounts().forEach(account -> account.setParent(target));
        }
    }

    public abstract List<CashBalanceDto> toBalanceDtoList(List<CashRateInfluenceMoneyBalanceSql> entity);

    public abstract List<CashRateInfluenceMoneyBalanceSql> toBalanceEntityList(List<CashBalanceDto> dto);

    @Mapping(target = "sum", ignore = true)
    @Mapping(target = "baseSum", ignore = true)
    @Mapping(target = "baseFixSum", ignore = true)
    public abstract CashBalanceDto toBalanceDto(CashRateInfluenceMoneyBalanceSql entity);

    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "cash", ignore = true)
    public abstract CashRateInfluenceMoneyBalanceSql toBalanceEntity(CashBalanceDto dto);

    @AfterMapping
    void afterToEntity(CashBalanceDto src, @MappingTarget CashRateInfluenceMoneyBalanceSql target) {
        if (Validators.isValid(src.getCash())) {
            target.setCash(entityManager.getReference(CashSql.class, src.getCash().getId()));
        } else {
            target.setCash(null);
        }
    }
}

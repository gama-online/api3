package lt.gama.model.mappers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.helpers.Validators;
import lt.gama.model.dto.documents.BankRateInfluenceDto;
import lt.gama.model.dto.documents.items.BankAccountBalanceDto;
import lt.gama.model.sql.documents.BankRateInfluenceSql;
import lt.gama.model.sql.documents.items.BankRateInfluenceMoneyBalanceSql;
import lt.gama.model.sql.entities.BankAccountSql;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(uses = {UtilsMapper.class, BankAccountSqlMapper.class}, componentModel = "spring")
public abstract class BankRateInfluenceSqlMapper implements IBaseMapper<BankRateInfluenceDto, BankRateInfluenceSql> {

    @PersistenceContext
    protected EntityManager entityManager;


    @Override
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "itemsFinished", ignore = true)
    public abstract BankRateInfluenceDto toDto(BankRateInfluenceSql entity);

    @Override
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "itemsFinished", ignore = true)
    public abstract BankRateInfluenceSql toEntity(BankRateInfluenceDto dto);

    @AfterMapping
    void afterToEntity(BankRateInfluenceDto src, @MappingTarget BankRateInfluenceSql target) {
        if (target.getAccounts() != null) {
            target.getAccounts().forEach(account -> account.setParent(target));
        }
    }

    public abstract List<BankAccountBalanceDto> toBalanceDtoList(List<BankRateInfluenceMoneyBalanceSql> entity);

    public abstract List<BankRateInfluenceMoneyBalanceSql> toBalanceEntityList(List<BankAccountBalanceDto> dto);

    @Mapping(target = "sum", ignore = true)
    @Mapping(target = "baseSum", ignore = true)
    @Mapping(target = "baseFixSum", ignore = true)
    @Mapping(target = "account", ignore = true)
    public abstract BankAccountBalanceDto toBalanceDto(BankRateInfluenceMoneyBalanceSql entity);

    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "bankAccount", ignore = true)
    public abstract BankRateInfluenceMoneyBalanceSql toBalanceEntity(BankAccountBalanceDto dto);

    @AfterMapping
    void afterToEntity(BankAccountBalanceDto src, @MappingTarget BankRateInfluenceMoneyBalanceSql target) {
        if (Validators.isValid(src.getBankAccount())) {
            target.setBankAccount(entityManager.getReference(BankAccountSql.class, src.getBankAccount().getId()));
        } else {
            target.setBankAccount(null);
        }
    }
}

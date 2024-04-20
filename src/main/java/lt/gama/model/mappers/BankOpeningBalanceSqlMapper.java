package lt.gama.model.mappers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.helpers.Validators;
import lt.gama.model.dto.documents.BankOpeningBalanceDto;
import lt.gama.model.dto.documents.items.BankAccountBalanceDto;
import lt.gama.model.sql.documents.BankOpeningBalanceSql;
import lt.gama.model.sql.documents.items.BankOpeningBalanceBankSql;
import lt.gama.model.sql.entities.BankAccountSql;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(uses = {UtilsMapper.class, BankAccountSqlMapper.class}, componentModel = "spring")
public abstract class BankOpeningBalanceSqlMapper implements IBaseMapper<BankOpeningBalanceDto, BankOpeningBalanceSql> {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "employee", ignore = true)
    public abstract BankOpeningBalanceDto toDto(BankOpeningBalanceSql entity);

    @Override
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "employee", ignore = true)
    public abstract BankOpeningBalanceSql toEntity(BankOpeningBalanceDto dto);

    @AfterMapping
    public void afterToEntity(BankOpeningBalanceDto src, @MappingTarget BankOpeningBalanceSql target) {
        if (target.getBankAccounts() != null) {
            target.getBankAccounts().forEach(cp -> cp.setParent(target));
        }
    }

    public abstract List<BankAccountBalanceDto> toBankOpeningBalanceDtoList(List<BankOpeningBalanceBankSql> entity);

    public abstract List<BankOpeningBalanceBankSql> toBankOpeningBalanceEntityList(List<BankAccountBalanceDto> dto);

    @Mapping(target = "sum", ignore = true)
    @Mapping(target = "baseSum", ignore = true)
    @Mapping(target = "baseFixSum", ignore = true)
    @Mapping(target = "account", ignore = true)
    public abstract BankAccountBalanceDto toBankOpeningBalanceDto(BankOpeningBalanceBankSql entity);

    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "bankAccount", ignore = true)
    public abstract BankOpeningBalanceBankSql toBankOpeningBalanceEntity(BankAccountBalanceDto dto);

    @AfterMapping
    public void afterToEntity(BankAccountBalanceDto src, @MappingTarget BankOpeningBalanceBankSql target) {
        if (Validators.isValid(src.getBankAccount())) {
            target.setBankAccount(entityManager.getReference(BankAccountSql.class, src.getBankAccount().getId()));
        } else {
            target.setBankAccount(null);
        }
    }
}

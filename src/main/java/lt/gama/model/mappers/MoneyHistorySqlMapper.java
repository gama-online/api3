package lt.gama.model.mappers;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.helpers.Validators;
import lt.gama.model.dto.entities.*;
import lt.gama.model.sql.entities.*;
import lt.gama.model.type.enums.DBType;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


@Mapper(uses = {UtilsMapper.class}, componentModel = "spring")
public abstract class MoneyHistorySqlMapper implements IBaseMapper<MoneyHistoryDto, MoneyHistorySql> {

    @PersistenceContext
    protected EntityManager entityManager;

    @Override
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "cash", ignore = true)
    @Mapping(target = "bankAccount", ignore = true)
    @Mapping(target = "bankAccount2", ignore = true)
    abstract public MoneyHistoryDto toDto(MoneyHistorySql entity);

    @Override
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "cash", ignore = true)
    @Mapping(target = "bankAccount", ignore = true)
    @Mapping(target = "bankAccount2", ignore = true)
    abstract public MoneyHistorySql toEntity(MoneyHistoryDto dto);

    @AfterMapping
    void afterToEntity(MoneyHistoryDto src, @MappingTarget MoneyHistorySql target) {
        if (Validators.isValid(src.getCounterparty())) {
            target.setCounterparty(entityManager.getReference(CounterpartySql.class, src.getCounterparty().getId()));
        } else {
            target.setCounterparty(null);
        }
        if (Validators.isValid(src.getEmployee())) {
            target.setEmployee(entityManager.getReference(EmployeeSql.class, src.getEmployee().getId()));
        } else {
            target.setEmployee(null);
        }
        if (Validators.isValid(src.getCash())) {
            target.setCash(entityManager.getReference(CashSql.class, src.getCash().getId()));
        } else {
            target.setCash(null);
        }
        if (Validators.isValid(src.getBankAccount())) {
            target.setBankAccount(entityManager.getReference(BankAccountSql.class, src.getBankAccount().getId()));
        } else {
            target.setBankAccount(null);
        }
        if (Validators.isValid(src.getBankAccount2())) {
            target.setBankAccount2(entityManager.getReference(BankAccountSql.class, src.getBankAccount2().getId()));
        } else {
            target.setBankAccount2(null);
        }
    }

    @AfterMapping
    void afterToDto(MoneyHistorySql src, @MappingTarget MoneyHistoryDto target) {
        if (Validators.isValid(src.getCounterparty())) {
            target.setCounterparty(entityManager.getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(src, "counterparty")
                    ? new CounterpartyDto(src.getCounterparty())
                    : new CounterpartyDto(src.getCounterparty().getId(), DBType.POSTGRESQL));
        } else {
            target.setCounterparty(null);
        }
        if (Validators.isValid(src.getEmployee())) {
            target.setEmployee(entityManager.getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(src, "employee")
                    ? new EmployeeDto(src.getEmployee())
                    : new EmployeeDto(src.getEmployee().getId(), DBType.POSTGRESQL));
        } else {
            target.setEmployee(null);
        }
        if (Validators.isValid(src.getCash())) {
            target.setCash(entityManager.getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(src, "cash")
                    ? new CashDto(src.getCash())
                    : new CashDto(src.getCash().getId(), DBType.POSTGRESQL));
        } else {
            target.setCash(null);
        }
        if (Validators.isValid(src.getBankAccount())) {
            target.setBankAccount(entityManager.getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(src, "bankAccount")
                    ? new BankAccountDto(src.getBankAccount())
                    : new BankAccountDto(src.getBankAccount().getId(), DBType.POSTGRESQL));
        } else {
            target.setBankAccount(null);
        }
        if (Validators.isValid(src.getBankAccount2())) {
            target.setBankAccount2(entityManager.getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(src, "bankAccount2")
                    ? new BankAccountDto(src.getBankAccount2())
                    : new BankAccountDto(src.getBankAccount2().getId(), DBType.POSTGRESQL));
        } else {
            target.setBankAccount2(null);
        }
    }
}

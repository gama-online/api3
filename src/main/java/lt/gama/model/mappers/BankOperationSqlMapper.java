package lt.gama.model.mappers;

import lt.gama.helpers.StringHelper;
import lt.gama.helpers.Validators;
import lt.gama.model.dto.documents.BankOperationDto;
import lt.gama.model.dto.entities.BankAccountDto;
import lt.gama.model.sql.documents.BankOperationSql;
import lt.gama.model.sql.entities.BankAccountSql;
import lt.gama.model.type.enums.DBType;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(uses = {UtilsMapper.class, BankAccountSqlMapper.class, EmployeeSqlMapper.class, CounterpartySqlMapper.class}, componentModel = "spring")
public abstract class BankOperationSqlMapper extends BaseDocumentSqlMapper<BankOperationDto, BankOperationSql> {

    @Override
    @Mapping(target = "bankAccount", ignore = true)
    @Mapping(target = "bankAccount2", ignore = true)
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "sum", ignore = true)
    @Mapping(target = "baseSum", ignore = true)
    @Mapping(target = "account", ignore = true)
    public abstract BankOperationDto toDto(BankOperationSql entity);

    @Override
    @Mapping(target = "bankAccount", ignore = true)
    @Mapping(target = "bankAccount2", ignore = true)
    @Mapping(target = "counterparty", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "otherAccount", ignore = true)
    public abstract BankOperationSql toEntity(BankOperationDto dto);

    @AfterMapping
    void afterToEntity(BankOperationDto src, @MappingTarget BankOperationSql target) {
        if (Validators.isValid(src.getBankAccount())) {
            target.setBankAccount(entityManager.getReference(BankAccountSql.class, src.getBankAccount().getId()));
        } else {
            target.setBankAccount(null);
        }
        if (Validators.isValid(src.getBankAccount2())) {
            target.setBankAccount2(entityManager.getReference(BankAccountSql.class, src.getBankAccount2().getId()));
        } else if (src.getBankAccount2() != null && StringHelper.hasValue(src.getBankAccount2().getAccount())) {
            target.setOtherAccount(src.getBankAccount2().getAccount());
        } else {
            target.setBankAccount2(null);
            target.setOtherAccount(null);
        }
    }

    @AfterMapping
    void afterToDto(BankOperationSql src, @MappingTarget BankOperationDto target) {
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
        } else if (StringHelper.hasValue(src.getOtherAccount())) {
            target.setBankAccount2(new BankAccountDto());
            target.getBankAccount2().setAccount(src.getOtherAccount());
        } else {
            target.setBankAccount2(null);
        }
    }
}

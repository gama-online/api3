package lt.gama.model.mappers;

import lt.gama.helpers.BooleanUtils;
import lt.gama.model.dto.entities.*;
import lt.gama.model.type.doc.*;
import lt.gama.model.type.gl.GLDC;
import lt.gama.model.type.gl.GLDCActive;
import lt.gama.model.type.gl.GLOperationAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public abstract class UtilsMapper {

    public UUID mapString2Uuid(String value) {
        return value != null ? UUID.fromString(value) : null;
    }

    public String mapUuid2String(UUID value) {
        return value != null ? value.toString() : null;
    }

    public Boolean boolean2Boolean(Boolean value) {
        return BooleanUtils.isTrue(value);
    }

    abstract public GLOperationAccount cloneGLOperationAccount(GLOperationAccount src);

    abstract public GLDCActive cloneGLDCActive(GLDCActive src);

    abstract public GLDC cloneGLDC(GLDC src);


    @IgnoreEntityFields
    @Mapping(target = "foreignId", ignore = true)
    @Mapping(target = "labels", ignore = true)
    @Mapping(target = "note", ignore = true)
    @Mapping(target = "credit", ignore = true)
    @Mapping(target = "discount", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "debts", ignore = true)
    @Mapping(target = "usedCurrencies", ignore = true)
    @Mapping(target = "debtsNow", ignore = true)
    @Mapping(target = "debtTypeImport", ignore = true)
    abstract public CounterpartyDto counterpartyDoc2Dto(DocCounterparty counterparty);

    @Mapping(target = "debtType", ignore = true)
    abstract public DocCounterparty counterpartyDto2Doc(CounterpartyDto counterparty);

    @IgnoreEntityFields
    @Mapping(target = "foreignId", ignore = true)
    @Mapping(target = "labels", ignore = true)
    @Mapping(target = "moneyAccount", ignore = true)
    @Mapping(target = "usedCurrencies", ignore = true)
    @Mapping(target = "remainder", ignore = true)
    @Mapping(target = "contacts", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "banks", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "remainders", ignore = true)
    @Mapping(target = "unionPermissions", ignore = true)
    abstract public EmployeeDto employeeDoc2Dto(DocEmployee employee);

    abstract public DocEmployee employeeDto2Doc(EmployeeDto employee);


    @IgnoreEntityFields
    @Mapping(target = "foreignId", ignore = true)
    @Mapping(target = "labels", ignore = true)
    @Mapping(target = "moneyAccount", ignore = true)
    @Mapping(target = "usedCurrencies", ignore = true)
    @Mapping(target = "remainder", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "invoice", ignore = true)
    @Mapping(target = "cards", ignore = true)
    @Mapping(target = "remainders", ignore = true)
    abstract public BankAccountDto bankAccountDoc2Dto(DocBankAccount employee);

    abstract public DocBankAccount bankAccountDto2Doc(BankAccountDto employee);


    @IgnoreEntityFields
    @Mapping(target = "foreignId", ignore = true)
    @Mapping(target = "labels", ignore = true)
    @Mapping(target = "moneyAccount", ignore = true)
    @Mapping(target = "usedCurrencies", ignore = true)
    @Mapping(target = "remainder", ignore = true)
    @Mapping(target = "remainders", ignore = true)
    abstract public CashDto cashDoc2Dto(DocCash cash);

    abstract public DocCash cashDto2Doc(CashDto cash);

    @IgnoreEntityFields
    @Mapping(target = "location", ignore = true)
    @Mapping(target = "storekeeper", ignore = true)
    @Mapping(target = "closed", ignore = true)
    @Mapping(target = "foreignId", ignore = true)
    @Mapping(target = "labels", ignore = true)
    abstract public WarehouseDto warehouseDoc2Dto(DocWarehouse warehouse);

    abstract public DocWarehouse warehouseDto2Doc(WarehouseDto warehouse);
}

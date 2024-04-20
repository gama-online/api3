package lt.gama.tasks;

import lt.gama.api.response.TaskResponse;
import lt.gama.helpers.GamaMoneyUtils;
import lt.gama.helpers.StringHelper;
import lt.gama.helpers.Validators;
import lt.gama.impexp.entity.ISO20022Record;
import lt.gama.model.dto.documents.BankOperationDto;
import lt.gama.model.dto.entities.BankAccountDto;
import lt.gama.model.dto.entities.CounterpartyDto;
import lt.gama.model.dto.entities.EmployeeDto;
import lt.gama.model.mappers.BankAccountSqlMapper;
import lt.gama.model.mappers.BankOperationSqlMapper;
import lt.gama.model.sql.documents.BankOperationSql;
import lt.gama.model.sql.documents.BankOperationSql_;
import lt.gama.model.sql.entities.BankAccountSql;
import lt.gama.model.type.enums.DebtType;
import lt.gama.service.CurrencyService;
import lt.gama.service.DocumentService;
import lt.gama.service.GLOperationsService;
import lt.gama.service.ex.rt.GamaException;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import static lt.gama.ConstWorkers.IMPORT_QUEUE;


/**
 * gama-online
 * Created by valdas on 2018-06-27.
 */
public class ImportBankTask extends BaseDeferredTask {

    @Serial
    private static final long serialVersionUID = -1L;

    @Autowired
    transient protected DocumentService documentService;

    @Autowired
    transient protected GLOperationsService glOperationsService;

    @Autowired
    transient protected BankAccountSqlMapper bankAccountSqlMapper;

    @Autowired
    transient protected BankOperationSqlMapper bankOperationSqlMapper;

    @Autowired protected CurrencyService currencyService;


    private final long bankAccountId;
    private final List<ISO20022Record> items;
    private int nr;
    private int imported;


    public ImportBankTask(long companyId, long bankAccountId, List<ISO20022Record> items, int nr) {
        super(companyId, IMPORT_QUEUE);
        this.bankAccountId = bankAccountId;
        this.items = items;
        this.nr = nr;
    }

    @Override
    public void execute() {
        if (items == null || items.size() == 0) return;

        final BankAccountDto bankAccount;
        try {
            Validators.checkNotNull(auth.getSettings(), "No company settings");
            bankAccount = bankAccountSqlMapper.toDto(dbServiceSQL.getAndCheck(BankAccountSql.class, bankAccountId));
        } catch (Exception e) {
            log.error(className + ": " + e.getMessage(), e);
            return;
        }

        List<String> warnings = new ArrayList<>();
        for (ISO20022Record item : items) {
            this.nr++;
            try {
                dbServiceSQL.executeInTransaction(entityManager -> this.importLine(item, bankAccount, warnings));
            } catch (GamaException e) {
                log.error(className + ": " + e.getMessage(), e);
            }
        }

        log.info(className + ": - Bank operations import - " +
                        "records=" + (nr - 1) +
                        " imported=" + imported +
                        " warnings=" + warnings.size() +
                        " company=" + getCompanyId());
        finish(TaskResponse.success().withWarnings(warnings));
    }

    void importLine(ISO20022Record item, BankAccountDto bankAccount, List<String> warnings) {
        BankOperationDto bankOperation = null;
        try {
            bankOperation = makeBankOperation(item, bankAccount);
            if (bankOperation == null) return; // do nothing if document imported already

            if (item.getDetail().isLinked()) {
                bankOperation.setEmployee(new EmployeeDto(item.getDetail().getEmployee()));
                if (item.getDetail().getCounterparty() != null) {
                    bankOperation.setCounterparty(new CounterpartyDto(item.getDetail().getCounterparty()));
                    bankOperation.setDebtType(
                            item.getDetail().getCounterparty().getDebtType() != null
                                    ? item.getDetail().getCounterparty().getDebtType()
                                    : GamaMoneyUtils.isNegative(bankOperation.getAmount()) ? DebtType.VENDOR : DebtType.CUSTOMER);
                }
                bankOperation.setBankAccount2(new BankAccountDto(item.getDetail().getAccount2()));
            }
            currencyService.checkBaseMoneyDocumentExchange(bankOperation.getDate(), bankOperation);

            BankOperationSql entity = dbServiceSQL.saveWithCounter(bankOperationSqlMapper.toEntity(bankOperation));
            if (item.isFinish()) {
                entity = documentService.finish(entity, true).getDocument();
            } else {
                glOperationsService.finishBankOperation(bankAccount.getMoneyAccount(),
                        bankOperationSqlMapper.toDto(entity), null, false);
            }
            bankOperation = bankOperationSqlMapper.toDto(entity);

            imported++;

        } catch (Exception e) {
            log.error(className + ": " + e.getMessage(), e);
            if (bankOperation != null) {
                String msg = makeWarning(nr, bankOperation, item, e.getMessage());
                log.warn(msg);
                warnings.add(msg);
            } else {
                log.error(item.toString());
            }
            throw new GamaException(e.getMessage(), e);
        }
    }

    private String makeWarning(int no, BankOperationDto bankOperation, ISO20022Record item, String msg) {
        return no + ": Date: " + bankOperation.getDate().toString() +
                (StringHelper.hasValue(item.getDetail().getOpNumber()) ? (", Op.Number: " + item.getDetail().getOpNumber()) : "") +
                (StringHelper.hasValue(item.getDetail().getPartyIBAN()) ? (", Party IBAN: " + item.getDetail().getPartyIBAN()) : "") +
                (StringHelper.hasValue(item.getDetail().getPartyName()) ? (", Party Name: " + item.getDetail().getPartyName()) : "") +
                (Validators.isValid(bankOperation.getEmployee()) ? (", Employee: " + bankOperation.getEmployee().getName()) : "") +
                (Validators.isValid(bankOperation.getCounterparty()) ? (", Party: " + bankOperation.getCounterparty().getName()) : "") +
                (Validators.isValid(bankOperation.getBankAccount()) ? (", Bank account: " + bankOperation.getBankAccount().getAccount()) : "") +
                (Validators.isValid(bankOperation.getBankAccount()) && bankOperation.getBankAccount().getBank() != null ? (", Bank: " + bankOperation.getBankAccount().getBank().getName()) : "") +
                (StringHelper.hasValue(bankOperation.getNote()) ? (", Note: " + bankOperation.getNote()) : "") +
                (bankOperation.getAmount() != null ? ", " + bankOperation.getAmount() : "") +
                ", ERROR: " + msg;
    }

    private BankOperationDto makeBankOperation(ISO20022Record item, BankAccountDto bankAccount) {
        if (item.getUuid() != null) {
            // check if operation is not imported already
            int count = ((Number) entityManager.createQuery(
                    "SELECT COUNT(a) FROM " + BankOperationSql.class.getName() + " a" +
                            " WHERE " + BankOperationSql_.UUID + " = :uuid" +
                            " AND " + BankOperationSql_.COMPANY_ID + " = :companyId" +
                            " AND archive IS NOT true")
                    .setParameter("companyId", auth.getCompanyId())
                    .setParameter("uuid", item.getUuid())
                    .getSingleResult()).intValue();
            if (count > 0) return null;
        }

        BankOperationDto bankOperation = new BankOperationDto();
        bankOperation.setUuid(item.getUuid());
        bankOperation.setNoDebt(item.getNoDebt());
        bankOperation.setDate(item.getEntry().getBookingDate().toLocalDate());

        String opNumber = item.getDetail().getOpNumber();
        if (StringHelper.isEmpty(opNumber) || "NOTPROVIDED".equals(opNumber)) {
            bankOperation.setAutoNumber(true);
        } else {
            bankOperation.setNumber(opNumber);
        }
        bankOperation.setBankAccount(new BankAccountDto(bankAccount));
        bankOperation.setCashOperation(item.getEntry().isCash());
        if (GamaMoneyUtils.isNonZero(item.getEntry().getCredit())) {
            bankOperation.setAmount(item.getEntry().getCredit());
        } else if (GamaMoneyUtils.isNonZero(item.getEntry().getDebit())) {
            bankOperation.setAmount(item.getEntry().getDebit().negated());
        }
        bankOperation.setNote(item.getDetail().getNote());
        bankOperation.setPaymentCode(item.getDetail().getPaymentCode());
        return bankOperation;
    }


    @Override
    public String toString() {
        return "bankAccountId=" + bankAccountId +
                ", nr=" + nr +
                ", imported=" + imported +
                ' ' + super.toString();
    }
}

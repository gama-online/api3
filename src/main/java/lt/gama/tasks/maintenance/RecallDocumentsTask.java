package lt.gama.tasks.maintenance;

import lt.gama.model.i.ICompany;
import lt.gama.model.sql.base.BaseCompanySql;
import lt.gama.model.sql.base.BaseDocumentSql;
import lt.gama.model.sql.documents.*;
import lt.gama.model.sql.documents.items.*;
import lt.gama.model.sql.entities.*;
import lt.gama.tasks.BaseDeferredTask;

import java.io.Serial;

public class RecallDocumentsTask extends BaseDeferredTask {

    @Serial
    private static final long serialVersionUID = -1L;

    public RecallDocumentsTask(long companyId) {
        super(companyId);
    }

    @Override
    public void execute() {
        try {
            // recall base documents - finished, finishedGL
            recallBaseDocuments();

            // recall gl
            // GLOpeningBalanceSql recallBaseDocuments()
            recallDoubleEntry();

            // recall inventory documents

            // inventory documents
            recallInventoryDocuments(InventoryOpeningBalanceSql.class);
            recallInventoryDocuments(PurchaseSql.class);
            recallInventoryDocuments(InvoiceSql.class);
            recallInventoryDocuments(TransProdSql.class);
            recallInventoryDocuments(InventorySql.class);
            // EstimateSql recallBaseDocuments()
            // OrderSql recallBaseDocuments()
            // RecipeSql recallBaseDocuments()

            // inventory documents parts
            recallInventoryDocumentParts(InventoryOpeningBalancePartSql.class);
            recallInventoryDocumentParts(PurchasePartSql.class);
            recallInventoryDocumentParts(InvoiceBasePartSql.class);
            recallInventoryDocumentParts(TransProdPartSql.class);
            recallInventoryDocumentParts(InventoryPartSql.class);
            recallInventoryDocumentParts(RecipePartSql.class);
            recallInventoryDocumentParts(EstimateBasePartSql.class);
            // OrderPartSql nothing to recall

            clearPart();

            deleteAll(InventoryHistorySql.class);
            deleteAll(InventoryNowSql.class);

            // recall debt documents

            // debt documents
            recallDebtDocuments(DebtOpeningBalanceSql.class);
            recallDebtDocuments(DebtCorrectionSql.class);
            // DebtRateInfluenceSql recallBaseDocuments()

            // debt documents items
            recallDocumentItems(DebtOpeningBalanceCounterpartySql.class);
            // DebtCorrectionSql - no items
            recallDocumentItems(DebtRateInfluenceMoneyBalanceSql.class);

            clearCounterparty();

            deleteAll(DebtHistorySql.class);
            deleteAll(DebtNowSql.class);
            deleteAll(DebtCoverageSql.class);

            // recall money documents

            // bank documents
            // BankOpeningBalanceSql recallBaseDocuments()
            recallDocuments(BankOperationSql.class);
            // BankRateInfluenceSql recallBaseDocuments()

            // bank documents items
            recallDocumentItems(BankOpeningBalanceBankSql.class);
            // BankAccountSql nothing to recall
            recallDocumentItems(BankRateInfluenceMoneyBalanceSql.class);

            // cash documents
            // CashOpeningBalanceSql recallBaseDocuments()
            recallDocuments(CashOperationSql.class);
            // CashRateInfluenceSql recallBaseDocuments()

            // cash documents items
            recallDocumentItems(CashOpeningBalanceCashSql.class);
            // CashSql nothing to recall
            recallDocumentItems(CashRateInfluenceMoneyBalanceSql.class);

            clearEmployee();

            deleteAll(MoneyHistorySql.class);

            // employee documents
            // EmployeeOpeningBalanceSql recallBaseDocuments()
            recallDocuments(EmployeeOperationSql.class);
            // EmployeeRateInfluenceSql recallBaseDocuments()

            // employee documents items
            recallDocumentItems(EmployeeOpeningBalanceEmployeeSql.class);
            // no items in EmployeeOperationSql
            recallDocumentItems(EmployeeRateInfluenceMoneyBalanceSql.class);

            // recall salary documents

            // salary documents
            // SalarySql recallBaseDocuments()

            // salary documents items - no relation with salary document
            recallDocumentItems(EmployeeChargeSql.class);

        } catch (Exception e) {
            log.error(className + ": " + e.getMessage(), e);
        }
    }

    private void recallBaseDocuments() {
        int recallCount = dbServiceSQL.executeAndReturnInTransaction(entityManager ->
                entityManager.createQuery("UPDATE " + BaseDocumentSql.class.getName() + " a" +
                                " SET a.finishedGL = false, a.finished = false" +
                                " WHERE a.companyId = :companyId")
                        .setParameter("companyId", getCompanyId())
                        .executeUpdate());
        log.info(className + " - " + BaseDocumentSql.class.getSimpleName() + ": " + recallCount + " records recalled");
    }

    private void recallDoubleEntry() {
        int recallCount = dbServiceSQL.executeAndReturnInTransaction(entityManager ->
                entityManager.createQuery("UPDATE " + DoubleEntrySql.class.getName() + " a" +
                                " SET a.finishedGL = false" +
                                " WHERE a.companyId = :companyId")
                        .setParameter("companyId", getCompanyId())
                        .executeUpdate());

        log.info(className + " - " + DoubleEntrySql.class.getSimpleName() + ": " + recallCount + " records recalled");
    }

    private void recallInventoryDocuments(Class<?> type) {
        int recallCount = dbServiceSQL.executeAndReturnInTransaction(entityManager ->
                entityManager.createQuery("UPDATE " + type.getName() + " a SET" +
                                (type.isAssignableFrom(TransProdSql.class)
                                        ? " a.finishedPartsFrom = false, finishedPartsTo = false"
                                        : " a.finishedParts = false") +
                                (type.isAssignableFrom(PurchaseSql.class) || type.isAssignableFrom(InvoiceSql.class)
                                        ? ", a.finishedDebt = false"
                                        : "") +
                                " WHERE a.companyId = :companyId")
                        .setParameter("companyId", getCompanyId())
                        .executeUpdate());
        log.info(className + " - " + type.getSimpleName() + ": " + recallCount + " records recalled");
    }

    private void recallInventoryDocumentParts(Class<?> type) {
        int recallCount = dbServiceSQL.executeAndReturnInTransaction(entityManager ->
                entityManager.createQuery("UPDATE " + type.getName() + " a" +
                                " SET a.finished = false" +
                                (type.isAssignableFrom(EstimateBasePartSql.class)
                                        ? ""
                                        : ", a.costInfo = null") +
                                " WHERE a.companyId = :companyId")
                        .setParameter("companyId", getCompanyId())
                        .executeUpdate());
        log.info(className + " - " + type.getSimpleName() + ": " + recallCount + " records recalled");
    }

    private void clearPart() {
        int recallCount = dbServiceSQL.executeAndReturnInTransaction(entityManager ->
                entityManager.createQuery("UPDATE " + PartSql.class.getName() + " a" +
                                " SET a.quantityTotal = null, a.costTotal.amount = null, a.costTotal.currency = null, a.remainder = null" +
                                " WHERE a.companyId = :companyId")
                        .setParameter("companyId", getCompanyId())
                        .executeUpdate());
        log.info(className + " - " + GLOpeningBalanceSql.class.getSimpleName() + ": " + recallCount + " records recalled");
    }

    private void recallDebtDocuments(Class<?> type) {
        int recallCount = dbServiceSQL.executeAndReturnInTransaction(entityManager ->
                entityManager.createQuery("UPDATE " + type.getName() + " a " +
                                " SET a.finishedDebt = false" +
                                " WHERE a.companyId = :companyId")
                        .setParameter("companyId", getCompanyId())
                        .executeUpdate());
        log.info(className + " - " + type.getSimpleName() + ": " + recallCount + " records recalled");
    }

    private void clearCounterparty() {
        int recallCount = dbServiceSQL.executeAndReturnInTransaction(entityManager ->
                entityManager.createQuery("UPDATE " + CounterpartySql.class.getName() + " a" +
                                " SET a.debts = jsonb('{}')" +
                                " WHERE a.companyId = :companyId")
                        .setParameter("companyId", getCompanyId())
                        .executeUpdate());
        log.info(className + " - " + CounterpartySql.class.getSimpleName() + ": " + recallCount + " records recalled");
    }

    private void clearEmployee() {
        int recallCount = dbServiceSQL.executeAndReturnInTransaction(entityManager ->
                entityManager.createQuery("UPDATE " + EmployeeSql.class.getName() + " a" +
                                " SET a.remainder = jsonb('{}')" +
                                " WHERE a.companyId = :companyId")
                        .setParameter("companyId", getCompanyId())
                        .executeUpdate());
        log.info(className + " - " + EmployeeSql.class.getSimpleName() + ": " + recallCount + " records recalled");
    }

    private void recallDocuments(Class<?> type) {
        int recallCount = dbServiceSQL.executeAndReturnInTransaction(entityManager ->
                entityManager.createQuery("UPDATE " + type.getName() + " a " +
                                " SET a.finishedMoney = null, a.finishedDebt = false" +
                                " WHERE a.companyId = :companyId")
                        .setParameter("companyId", getCompanyId())
                        .executeUpdate());
        log.info(className + " - " + type.getSimpleName() + ": " + recallCount + " records recalled");
    }

    private void recallDocumentItems(Class<?> type) {
        int recallCount = dbServiceSQL.executeAndReturnInTransaction(entityManager ->
                entityManager.createQuery("UPDATE " + type.getName() + " a" +
                                " SET a.finished = false" +
                                " WHERE a.companyId = :companyId")
                        .setParameter("companyId", getCompanyId())
                        .executeUpdate());
        log.info(className + " - " + type.getSimpleName() + ": " + recallCount + " records recalled");
    }

    private void deleteAll(Class<? extends ICompany> entityClass) {
        if (BaseCompanySql .class.isAssignableFrom(entityClass)) {
            log.info(className + " - deleting " + entityClass.getSimpleName());
            //noinspection unchecked
            dbServiceSQL.deleteAll((Class<? extends BaseCompanySql>) entityClass);
        } else {
            log.error(entityClass.getSimpleName() + " not ICompany");
        }
    }
}

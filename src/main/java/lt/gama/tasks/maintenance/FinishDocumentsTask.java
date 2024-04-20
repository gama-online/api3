package lt.gama.tasks.maintenance;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.BooleanUtils;
import lt.gama.helpers.RequestTimeoutChecker;
import lt.gama.helpers.StringHelper;
import lt.gama.helpers.Validators;
import lt.gama.impexp.LinkBase;
import lt.gama.model.i.base.IBaseCompany;
import lt.gama.model.i.base.IBaseDocument;
import lt.gama.model.i.base.IBaseEntity;
import lt.gama.model.sql.base.BaseDocumentSql;
import lt.gama.model.sql.base.BaseNumberDocumentSql;
import lt.gama.model.sql.base.BaseNumberDocumentSql_;
import lt.gama.model.sql.documents.*;
import lt.gama.service.CheckRequestTimeoutService;
import lt.gama.tasks.BaseDeferredTask;
import org.hibernate.jpa.QueryHints;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serial;
import java.time.LocalDate;
import java.util.List;

public class FinishDocumentsTask extends BaseDeferredTask {

    @Serial
    private static final long serialVersionUID = -1L;

    @Autowired
    transient protected CheckRequestTimeoutService checkRequestTimeoutService;

//TODO spring migration
//    @Autowired
//    transient protected MapLink mapLink;


    private final LocalDate dateFrom;
    private final LocalDate dateTo;
    private String cursor;
    private final boolean opening;
    private final int documentTypeIndex;
    private int finishCount;


    public FinishDocumentsTask(long companyId, boolean opening, LocalDate dateFrom, LocalDate dateTo) {
        super(companyId);
        this.opening = opening;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.documentTypeIndex = 0;
    }

    public FinishDocumentsTask(long companyId, boolean opening, LocalDate dateFrom, LocalDate dateTo, int documentTypeIndex, String cursor, int finishCount) {
        super(companyId);
        this.opening = opening;
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        Validators.checkArgument(documentTypeIndex >= 0 && documentTypeIndex < getDocTypes(opening).size(), "Wrong document type index: " + documentTypeIndex);
        this.documentTypeIndex = documentTypeIndex;
        this.cursor = cursor;
        this.finishCount = finishCount;
    }

    @Override
    public void execute() {
        try {
            // check if done
            if (dateFrom.isAfter(dateTo)) return;
            dbServiceSQL.executeInTransaction(entityManager ->
                    proceedDocuments(opening, dateFrom, dateTo, documentTypeIndex, cursor, finishCount));

        } catch (Exception e) {
            log.error(className + ": " + e.getMessage(), e);
        }
    }

    // and special cases GLOpeningBalance and DoubleEntry
    private static final List<Class<? extends IBaseCompany>> _DocOpeningTypes = List.of(
            // any order
            BankOpeningBalanceSql.class,
            CashOpeningBalanceSql.class,
            EmployeeOpeningBalanceSql.class,
            InventoryOpeningBalanceSql.class,
            DebtOpeningBalanceSql.class,
            GLOpeningBalanceSql.class
    );
    private static final List<Class<? extends IBaseCompany>> _DocOperationsTypes = List.of(
            // strict order
            PurchaseSql.class,
            TransProdSql.class,
            InvoiceSql.class,
            InventorySql.class,

            // any order
            EmployeeOperationSql.class,
            BankOperationSql.class,
            CashOperationSql.class,
            DebtCorrectionSql.class,

            EmployeeRateInfluenceSql.class,
            BankRateInfluenceSql.class,
            CashRateInfluenceSql.class,
            DebtRateInfluenceSql.class,

            DoubleEntrySql.class
    );

    private static List<Class<? extends IBaseCompany>> getDocTypes(boolean opening) {
        return opening ? _DocOpeningTypes : _DocOperationsTypes;
    }

    private void proceedDocuments(boolean opening, LocalDate dateFrom, LocalDate dateTo,
                          int index, String cursor, int finishCount) {

        RequestTimeoutChecker requestTimeoutChecker = checkRequestTimeoutService.init();

        LocalDate operationDate = opening ? dateFrom.plusDays(-1) : dateFrom;

        while (index < getDocTypes(opening).size() && !dateFrom.isAfter(dateTo)) {

            Class<? extends IBaseCompany> entityClass = getDocTypes(opening).get(index);

            if (BaseNumberDocumentSql.class.isAssignableFrom(entityClass)) {

                int count = 100;
                int skip = StringHelper.hasValue(cursor) ? Integer.parseInt(cursor) : 0;

                List<BaseNumberDocumentSql> docs = entityManager.createQuery(
                                "SELECT a FROM " + entityClass.getName() + " a" +
                                        " WHERE " + BaseNumberDocumentSql_.COMPANY_ID + " = :companyId" +
                                        " AND " + BaseNumberDocumentSql_.DATE + " >= :dateFrom" +
                                        " AND " + BaseNumberDocumentSql_.DATE + " < :dateTo" +
                                        " ORDER BY " + BaseNumberDocumentSql_.DATE +
                                        ", " + BaseNumberDocumentSql_.NUMBER +
                                        ", " + BaseNumberDocumentSql_.ID,
                                BaseNumberDocumentSql.class)
                        .setParameter("companyId", auth.getCompanyId())
                        .setParameter("dateFrom", operationDate)
                        .setParameter("dateTo", dateTo.plusDays(1))
                        .setMaxResults(count)
                        .setFirstResult(skip)
                        .setHint(QueryHints.HINT_READONLY, true)
                        .getResultList();


                for (BaseNumberDocumentSql document : docs) {
                    count--;
                    finishDocument(document);
                    finishCount++;
                    skip++;

                    if (requestTimeoutChecker.isTimeout()) {
                        // if timeout - start a new task
                        count = 0;
                        break;
                    }
                }

                if (count == 0) {
                    log.info("Start a new finishing task at " + finishCount);
                    taskQueueService.queueTask(new FinishDocumentsTask(getCompanyId(), opening, dateFrom, dateTo, index, String.valueOf(skip), finishCount));
                    return;
                }

                cursor = null;
                if (++index >= getDocTypes(opening).size()) {
                    opening = false;
                    index = 0;
                    operationDate = operationDate.plusDays(1);
                    dateFrom = operationDate;
                }
            }

        }

        log.info(className + ": Documents " + finishCount + " records are finished");
    }

    private void finishDocument(IBaseEntity document)  {

        if (BooleanUtils.isTrue(document.getArchive())) return;

        if (document instanceof DoubleEntrySql) {
            if (BooleanUtils.isTrue(((DoubleEntrySql) document).getFinishedGL())) return;
        } else if (document instanceof GLOpeningBalanceSql) {
            if (BooleanUtils.isTrue(((GLOpeningBalanceSql) document).getFinishedGL())) return;
        } else if (document instanceof BaseDocumentSql) {
            if (((BaseDocumentSql) document).isFullyFinished()) return;
        }

        @SuppressWarnings("unchecked")
        LinkBase<IBaseCompany> link = null; //TODO spring migration: mapLink.getMap(document.getClass());
        if (link != null) {
            try {
                if (document instanceof IBaseDocument d)
                    link.finish(d.getId());
            } catch (Exception e) {
                log.error(e.toString());
            }
        } else {
            log.error("No LinkBase for class " + document.getClass().getSimpleName());
        }
    }
}

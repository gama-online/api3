package lt.gama.tasks;

import lt.gama.ConstWorkers;
import lt.gama.helpers.DateUtils;
import lt.gama.model.sql.system.CompanySql;
import lt.gama.service.AccountingService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serial;
import java.time.LocalDate;

import static lt.gama.ConstWorkers.IMPORT_QUEUE;

public class SubscriptionsInvoicingCompanyTask extends BaseDeferredTask {

    @Serial
    private static final long serialVersionUID = -1L;


    private static final int MAX_RETRY_COUNT = 5;

    @Autowired
    transient protected AccountingService accountingService;


    final private boolean debug;

    public SubscriptionsInvoicingCompanyTask(long companyId) {
        this(companyId, false, 0);
    }

    public SubscriptionsInvoicingCompanyTask(long companyId, boolean debug, int retryNumber) {
        super(companyId, IMPORT_QUEUE, retryNumber);
        this.debug = debug;
    }

    @Override
    public void execute() {
        LocalDate date = DateUtils.date().withDayOfMonth(1).minusDays(1);
        log.info(className + ": Invoicing company " + getCompanyId() + " on " + date);
        try {
            dbServiceSQL.executeInTransaction(entityManager -> {
                generateInvoice(date);
                log.info(className + ": Successfully Invoiced company " + getCompanyId() + " on " + date);
            });
        } catch (Exception e) {
            if (e instanceof jakarta.persistence.RollbackException || e.getCause() instanceof jakarta.persistence.RollbackException) {
                if (getRetryNumber() < MAX_RETRY_COUNT) {
                    taskQueueService.queueTask(new SubscriptionsInvoicingCompanyTask(getCompanyId(), debug, getRetryNumber() + 1), 3);
                } else {
                    log.error(className + ": " + e.getMessage(), e);
                    CompanySql subscriberCompany = dbServiceSQL.getById(CompanySql.class, getCompanyId());
                    accountingService.sendInvoicingErrorAdminEMail("Invoicing error for company " + subscriberCompany.getName() +
                            " (" + subscriberCompany.getId() + ")" + "\n" + e.getMessage());
                }
            } else {
                log.info(className + ": " + e.getMessage(), e);
            }
        }
    }

    // need separate method for testing
    public void generateInvoice(LocalDate date) {
        accountingService.invoicingCompany(date, getCompanyId(), debug);
    }
}

package lt.gama.tasks;

import lt.gama.helpers.DateUtils;
import lt.gama.service.AccountingService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serial;

public class SubscriptionsCountCompanyTask extends BaseDeferredTask {

    @Serial
    private static final long serialVersionUID = -1L;


    @Autowired
    transient protected AccountingService accountingService;


    public SubscriptionsCountCompanyTask(long companyId) {
        super(companyId);
    }

    @Override
    public void execute() {
        try {
            accountingService.subscriptionsUpdate(getCompanyId(), DateUtils.date());
            log.info("Subscriptions updated for company " + getCompanyId());
        } catch (Exception e) {
            log.error(className + ": " + e.getMessage(), e);
        }
    }
}

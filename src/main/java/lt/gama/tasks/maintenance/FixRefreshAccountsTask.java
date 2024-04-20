package lt.gama.tasks.maintenance;

import lt.gama.service.AdminService;
import lt.gama.tasks.BaseDeferredTask;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serial;

public class FixRefreshAccountsTask  extends BaseDeferredTask {

    @Serial
    private static final long serialVersionUID = -1L;


    @Autowired
    transient protected AdminService adminService;


    public FixRefreshAccountsTask(long companyId) {
        super(companyId);
    }

    @Override
    public void execute() {
        try {
            adminService.refreshCompanyAccounts(getCompanyId());
            log.info(className + ": companyId=" + getCompanyId() + " accounts refreshed");
        } catch (Exception e) {
            log.error(className + ": " + e.getMessage(), e);
        }
    }
}

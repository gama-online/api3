package lt.gama.tasks;


import lt.gama.model.sql.documents.*;
import lt.gama.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serial;

public class DeleteCompanyDocumentsTask extends BaseDeferredTask {

    @Serial
    private static final long serialVersionUID = -1L;

    @Autowired
    transient protected AdminService adminService;

    public DeleteCompanyDocumentsTask(long companyId) {
        super(companyId);
    }

    @Override
    public void execute() {
        adminService.deleteAll(GLOpeningBalanceSql.class, getCompanyId());
        adminService.deleteAll(DoubleEntrySql.class, getCompanyId());
        adminService.deleteAll(EmployeeOpeningBalanceSql.class, getCompanyId());
        adminService.deleteAll(EmployeeOperationSql.class, getCompanyId());
        adminService.deleteAll(EmployeeRateInfluenceSql.class, getCompanyId());
        adminService.deleteAll(BankOpeningBalanceSql.class, getCompanyId());
        adminService.deleteAll(BankOperationSql.class, getCompanyId());
        adminService.deleteAll(BankRateInfluenceSql.class, getCompanyId());
        adminService.deleteAll(CashOpeningBalanceSql.class, getCompanyId());
        adminService.deleteAll(CashOperationSql.class, getCompanyId());
        adminService.deleteAll(CashRateInfluenceSql.class, getCompanyId());
        adminService.deleteAll(DebtCorrectionSql.class, getCompanyId());
        adminService.deleteAll(DebtOpeningBalanceSql.class, getCompanyId());
        adminService.deleteAll(DebtRateInfluenceSql.class, getCompanyId());
        adminService.deleteAll(EstimateSql.class, getCompanyId());
        adminService.deleteAll(InventorySql.class, getCompanyId());
        adminService.deleteAll(InvoiceSql.class, getCompanyId());
        adminService.deleteAll(OrderSql.class, getCompanyId());
        adminService.deleteAll(PurchaseSql.class, getCompanyId());
        adminService.deleteAll(TransProdSql.class, getCompanyId());
    }
}

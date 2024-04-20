package lt.gama.impexp.link;

import lt.gama.auth.impl.Auth;
import lt.gama.helpers.CollectionsHelper;
import lt.gama.helpers.Validators;
import lt.gama.impexp.LinkBase;
import lt.gama.model.dto.documents.EmployeeOpeningBalanceDto;
import lt.gama.model.mappers.EmployeeOpeningBalanceSqlMapper;
import lt.gama.model.sql.entities.EmployeeSql;
import lt.gama.model.type.Exchange;
import lt.gama.service.CurrencyService;
import lt.gama.service.DBServiceSQL;
import lt.gama.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;


public class LinkEmployeeOpeningBalanceDto implements LinkBase<EmployeeOpeningBalanceDto> {

    @Autowired
    private DBServiceSQL dbServiceSQL;

    @Autowired
    private EmployeeOpeningBalanceSqlMapper employeeOpeningBalanceSqlMapper;

    @Autowired
    private Auth auth;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private EmployeeService employeeService;


    @Override
    public EmployeeOpeningBalanceDto resolve(EmployeeOpeningBalanceDto document) {
        if (CollectionsHelper.hasValue(document.getEmployees())) {
            document.getEmployees().forEach(account -> {
                LinkHelper.link(account.getEmployee(), auth.getCompanyId(), EmployeeSql.class, dbServiceSQL);
                Exchange exchange = Validators.checkNotNull(currencyService.currencyExchange(
                        Validators.checkNotNull(auth.getSettings(), "No company settings"),
                        account.getExchange(), document.getDate()), "No exchange");
                account.setExchange(exchange);
            });
        }
        return document;
    }

    @Override
    public void finish(long documentId) {
        employeeService.finishEmployeeOpeningBalance(documentId);
    }
}

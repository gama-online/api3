package lt.gama.impexp.link;

import lt.gama.auth.impl.Auth;
import lt.gama.helpers.CollectionsHelper;
import lt.gama.helpers.Validators;
import lt.gama.impexp.LinkBase;
import lt.gama.model.dto.documents.EmployeeRateInfluenceDto;
import lt.gama.model.mappers.EmployeeRateInfluenceSqlMapper;
import lt.gama.model.sql.entities.EmployeeSql;
import lt.gama.model.type.Exchange;
import lt.gama.service.CurrencyService;
import lt.gama.service.DBServiceSQL;
import lt.gama.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;


public class LinkEmployeeRateInfluenceDto implements LinkBase<EmployeeRateInfluenceDto> {

    @Autowired
    private DBServiceSQL dbServiceSQL;

    @Autowired
    private EmployeeRateInfluenceSqlMapper employeeRateInfluenceSqlMapper;

    @Autowired
    private Auth auth;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private EmployeeService employeeService;


    @Override
    public EmployeeRateInfluenceDto resolve(EmployeeRateInfluenceDto document) {
        if (CollectionsHelper.hasValue(document.getAccounts())) {
            document.getAccounts().forEach(account -> {
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
        employeeService.finishEmployeeRateInfluence(documentId, true);
    }
}

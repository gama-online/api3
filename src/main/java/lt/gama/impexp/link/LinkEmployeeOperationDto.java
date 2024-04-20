package lt.gama.impexp.link;

import lt.gama.auth.impl.Auth;
import lt.gama.helpers.Validators;
import lt.gama.impexp.LinkBase;
import lt.gama.model.dto.documents.EmployeeOperationDto;
import lt.gama.model.mappers.EmployeeOperationSqlMapper;
import lt.gama.model.sql.entities.CounterpartySql;
import lt.gama.model.sql.entities.EmployeeSql;
import lt.gama.model.type.Exchange;
import lt.gama.service.CurrencyService;
import lt.gama.service.DBServiceSQL;
import lt.gama.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;


public class LinkEmployeeOperationDto implements LinkBase<EmployeeOperationDto> {

    @Autowired
    private DBServiceSQL dbServiceSQL;

    @Autowired
    private Auth auth;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private EmployeeOperationSqlMapper employeeOperationSqlMapper;

    @Autowired
    private EmployeeService employeeService;


    @Override
    public EmployeeOperationDto resolve(EmployeeOperationDto document) {
        LinkHelper.link(document.getEmployee(), auth.getCompanyId(), EmployeeSql.class, dbServiceSQL);
        LinkHelper.link(document.getCounterparty(), auth.getCompanyId(), CounterpartySql.class, dbServiceSQL);
        Exchange exchange = Validators.checkNotNull(currencyService.currencyExchange(
                Validators.checkNotNull(auth.getSettings(), "No company settings"),
                document.getExchange(), document.getDate()), "No exchange");
        document.setExchange(exchange);
        return document;
    }

    @Override
    public void finish(long documentId) {
        employeeService.finishEmployeeOperation(documentId, true);
    }
}

package lt.gama.impexp.link;

import lt.gama.auth.impl.Auth;
import lt.gama.helpers.Validators;
import lt.gama.impexp.LinkBase;
import lt.gama.model.dto.documents.BankOperationDto;
import lt.gama.model.mappers.BankOperationSqlMapper;
import lt.gama.model.sql.entities.BankAccountSql;
import lt.gama.model.sql.entities.CounterpartySql;
import lt.gama.model.sql.entities.EmployeeSql;
import lt.gama.model.type.Exchange;
import lt.gama.service.BankService;
import lt.gama.service.CurrencyService;
import lt.gama.service.DBServiceSQL;
import org.springframework.beans.factory.annotation.Autowired;


public class LinkBankOperationDto implements LinkBase<BankOperationDto> {

    @Autowired
    private DBServiceSQL dbServiceSQL;

    @Autowired
    private Auth auth;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private BankOperationSqlMapper bankOperationSqlMapper;

    @Autowired
    private BankService bankService;


    @Override
    public BankOperationDto resolve(BankOperationDto document) {
        LinkHelper.link(document.getBankAccount(), auth.getCompanyId(), BankAccountSql.class, dbServiceSQL);
        LinkHelper.link(document.getBankAccount2(), auth.getCompanyId(), BankAccountSql.class, dbServiceSQL);
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
        bankService.finishBankOperation(documentId, true);
    }
}

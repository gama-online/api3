package lt.gama.impexp.link;

import lt.gama.auth.impl.Auth;
import lt.gama.helpers.CollectionsHelper;
import lt.gama.helpers.Validators;
import lt.gama.impexp.LinkBase;
import lt.gama.model.dto.documents.CashOpeningBalanceDto;
import lt.gama.model.mappers.CashOpeningBalanceSqlMapper;
import lt.gama.model.sql.entities.CashSql;
import lt.gama.model.type.Exchange;
import lt.gama.service.CashService;
import lt.gama.service.CurrencyService;
import lt.gama.service.DBServiceSQL;
import org.springframework.beans.factory.annotation.Autowired;


public class LinkCashOpeningBalanceDto implements LinkBase<CashOpeningBalanceDto> {

    @Autowired
    private DBServiceSQL dbServiceSQL;

    @Autowired
    private CashOpeningBalanceSqlMapper cashOpeningBalanceSqlMapper;

    @Autowired
    private Auth auth;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private CashService cashService;


    @Override
    public CashOpeningBalanceDto resolve(CashOpeningBalanceDto document) {
        if (CollectionsHelper.hasValue(document.getCashes())) {
            document.getCashes().forEach(account -> {
                LinkHelper.link(account.getCash(), auth.getCompanyId(), CashSql.class, dbServiceSQL);
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
        cashService.finishCashOpeningBalance(documentId);
    }
}

package lt.gama.impexp.link;

import lt.gama.auth.impl.Auth;
import lt.gama.helpers.CollectionsHelper;
import lt.gama.helpers.Validators;
import lt.gama.impexp.LinkBase;
import lt.gama.model.dto.documents.BankOpeningBalanceDto;
import lt.gama.model.mappers.BankOpeningBalanceSqlMapper;
import lt.gama.model.sql.entities.BankAccountSql;
import lt.gama.model.type.Exchange;
import lt.gama.service.BankService;
import lt.gama.service.CurrencyService;
import lt.gama.service.DBServiceSQL;
import org.springframework.beans.factory.annotation.Autowired;


public class LinkBankOpeningBalanceDto implements LinkBase<BankOpeningBalanceDto> {

    @Autowired
    private DBServiceSQL dbServiceSQL;

    @Autowired
    private BankOpeningBalanceSqlMapper bankOpeningBalanceSqlMapper;

    @Autowired
    private Auth auth;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private BankService bankService;


    @Override
    public BankOpeningBalanceDto resolve(BankOpeningBalanceDto document) {
        if (CollectionsHelper.hasValue(document.getBankAccounts())) {
            document.getBankAccounts().forEach(account -> {
                LinkHelper.link(account.getBankAccount(), auth.getCompanyId(), BankAccountSql.class, dbServiceSQL);
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
        bankService.finishBankOpeningBalance(documentId);
    }
}

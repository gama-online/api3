package lt.gama.impexp.link;

import lt.gama.auth.impl.Auth;
import lt.gama.helpers.CollectionsHelper;
import lt.gama.helpers.Validators;
import lt.gama.impexp.LinkBase;
import lt.gama.model.dto.documents.BankRateInfluenceDto;
import lt.gama.model.mappers.BankRateInfluenceSqlMapper;
import lt.gama.model.sql.entities.BankAccountSql;
import lt.gama.model.type.Exchange;
import lt.gama.service.BankService;
import lt.gama.service.CurrencyService;
import lt.gama.service.DBServiceSQL;
import org.springframework.beans.factory.annotation.Autowired;


public class LinkBankRateInfluenceDto implements LinkBase<BankRateInfluenceDto> {

    @Autowired
    private DBServiceSQL dbServiceSQL;

    @Autowired
    private BankRateInfluenceSqlMapper bankRateInfluenceSqlMapper;

    @Autowired
    private Auth auth;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private BankService bankService;


    @Override
    public BankRateInfluenceDto resolve(BankRateInfluenceDto document) {
        if (CollectionsHelper.hasValue(document.getAccounts())) {
            document.getAccounts().forEach(account -> {
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
        bankService.finishBankRateInfluence(documentId, true);
    }
}

package lt.gama.impexp.link;

import lt.gama.auth.impl.Auth;
import lt.gama.helpers.CollectionsHelper;
import lt.gama.helpers.Validators;
import lt.gama.impexp.LinkBase;
import lt.gama.model.dto.documents.CashRateInfluenceDto;
import lt.gama.model.mappers.CashRateInfluenceSqlMapper;
import lt.gama.model.sql.entities.CashSql;
import lt.gama.model.type.Exchange;
import lt.gama.service.CashService;
import lt.gama.service.CurrencyService;
import lt.gama.service.DBServiceSQL;
import org.springframework.beans.factory.annotation.Autowired;


public class LinkCashRateInfluenceDto implements LinkBase<CashRateInfluenceDto> {

    @Autowired
    private DBServiceSQL dbServiceSQL;

    @Autowired
    private CashRateInfluenceSqlMapper cashRateInfluenceSqlMapper;

    @Autowired
    private Auth auth;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private CashService cashService;


    @Override
    public CashRateInfluenceDto resolve(CashRateInfluenceDto document) {
        if (CollectionsHelper.hasValue(document.getAccounts())) {
            document.getAccounts().forEach(account -> {
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
        cashService.finishCashRateInfluence(documentId, true);
    }
}

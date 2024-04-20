package lt.gama.impexp.link;

import lt.gama.auth.impl.Auth;
import lt.gama.helpers.CollectionsHelper;
import lt.gama.helpers.Validators;
import lt.gama.impexp.LinkBase;
import lt.gama.model.dto.documents.DebtRateInfluenceDto;
import lt.gama.model.sql.entities.CounterpartySql;
import lt.gama.model.type.Exchange;
import lt.gama.service.CurrencyService;
import lt.gama.service.DBServiceSQL;
import lt.gama.service.DebtService;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * gama-online
 * Created by valdas on 2018-09-21.
 */
public class LinkDebtRateInfluenceDto implements LinkBase<DebtRateInfluenceDto> {

    @Autowired
    private Auth auth;

    @Autowired
    private DBServiceSQL dbServiceSQL;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private DebtService debtService;


    @Override
    public DebtRateInfluenceDto resolve(DebtRateInfluenceDto document) {
        if (CollectionsHelper.hasValue(document.getAccounts())) {
            document.getAccounts().forEach(account -> {
                LinkHelper.link(account.getCounterparty(), auth.getCompanyId(), CounterpartySql.class, dbServiceSQL);
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
        debtService.finishRateInfluence(documentId, true);
    }
}

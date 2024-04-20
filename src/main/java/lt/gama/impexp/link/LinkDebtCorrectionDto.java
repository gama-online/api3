package lt.gama.impexp.link;

import lt.gama.auth.impl.Auth;
import lt.gama.helpers.Validators;
import lt.gama.impexp.LinkBase;
import lt.gama.model.dto.documents.DebtCorrectionDto;
import lt.gama.model.sql.entities.CounterpartySql;
import lt.gama.model.type.Exchange;
import lt.gama.service.CurrencyService;
import lt.gama.service.DBServiceSQL;
import lt.gama.service.DebtService;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Gama
 * Created by valdas on 15-06-15.
 */
public class LinkDebtCorrectionDto implements LinkBase<DebtCorrectionDto> {

    @Autowired
    private DBServiceSQL dbServiceSQL;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private Auth auth;

    @Autowired
    private DebtService debtService;


    @Override
    public DebtCorrectionDto resolve(DebtCorrectionDto document) {
        LinkHelper.link(document.getCounterparty(), auth.getCompanyId(), CounterpartySql.class, dbServiceSQL);
        Exchange exchange = Validators.checkNotNull(currencyService.currencyExchange(
                Validators.checkNotNull(auth.getSettings(), "No company settings"),
                document.getExchange(), document.getDate()), "No exchange");
        document.setExchange(exchange);
        return document;
    }

    @Override
    public void finish(long documentId) {
        debtService.finishDebtCorrection(documentId, true);
    }
}

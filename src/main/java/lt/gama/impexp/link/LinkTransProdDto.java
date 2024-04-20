package lt.gama.impexp.link;

import lt.gama.auth.impl.Auth;
import lt.gama.helpers.Validators;
import lt.gama.impexp.LinkBase;
import lt.gama.model.dto.documents.TransProdDto;
import lt.gama.model.dto.documents.items.PartFromDto;
import lt.gama.model.dto.documents.items.PartToDto;
import lt.gama.model.mappers.TransProdSqlMapper;
import lt.gama.model.sql.entities.EmployeeSql;
import lt.gama.model.sql.entities.PartSql;
import lt.gama.model.sql.entities.WarehouseSql;
import lt.gama.model.type.Exchange;
import lt.gama.service.CurrencyService;
import lt.gama.service.DBServiceSQL;
import lt.gama.service.TradeService;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Gama
 * Created by valdas on 15-07-19.
 */
public class LinkTransProdDto implements LinkBase<TransProdDto> {

    @Autowired
    private DBServiceSQL dbServiceSQL;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private Auth auth;

    @Autowired
    private TransProdSqlMapper transportationSqlMapper;

    @Autowired
    private TradeService tradeService;


    @Override
    public TransProdDto resolve(TransProdDto document) {
        final long companyId = auth.getCompanyId();
        LinkHelper.link(document.getEmployee(), companyId, EmployeeSql.class, dbServiceSQL);
        LinkHelper.link(document.getWarehouseFrom(), companyId, WarehouseSql.class, dbServiceSQL);
        if (document.getPartsFrom() != null && document.getPartsFrom().size() > 0) {
            for (PartFromDto part : document.getPartsFrom()) {
                LinkHelper.link(part, companyId, PartSql.class, dbServiceSQL);
                part.setCompanyId(companyId);
            }
        }
        LinkHelper.link(document.getWarehouseTo(), companyId, WarehouseSql.class, dbServiceSQL);
        if (document.getPartsTo() != null && document.getPartsTo().size() > 0) {
            for (PartToDto part : document.getPartsTo()) {
                LinkHelper.link(part, companyId, PartSql.class, dbServiceSQL);
                part.setCompanyId(companyId);
            }
        }
        Exchange exchange = Validators.checkNotNull(currencyService.currencyExchange(
                Validators.checkNotNull(auth.getSettings(), "No company settings"),
                document.getExchange(), document.getDate()), "No exchange");
        document.setExchange(exchange);
        return document;
    }

    @Override
    public void finish(long documentId) {
        tradeService.finishTransProd(documentId, true);
    }
}

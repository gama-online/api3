package lt.gama.impexp.link;

import lt.gama.auth.impl.Auth;
import lt.gama.helpers.CollectionsHelper;
import lt.gama.helpers.Validators;
import lt.gama.impexp.LinkBase;
import lt.gama.model.dto.documents.InventoryDto;
import lt.gama.model.dto.documents.items.PartInventoryDto;
import lt.gama.model.mappers.InventorySqlMapper;
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
public class LinkInventoryDto implements LinkBase<InventoryDto> {

    @Autowired
    private DBServiceSQL dbServiceSQL;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private Auth auth;

    @Autowired
    private InventorySqlMapper inventorySqlMapper;

    @Autowired
    private TradeService tradeService;


    @Override
    public InventoryDto resolve(InventoryDto document) {
        final long companyId = auth.getCompanyId();
        LinkHelper.link(document.getEmployee(), companyId, EmployeeSql.class, dbServiceSQL);
        LinkHelper.link(document.getWarehouse(), companyId, WarehouseSql.class, dbServiceSQL);
        if (CollectionsHelper.hasValue(document.getParts())) {
            for (PartInventoryDto part : document.getParts()) {
                LinkHelper.link(part, companyId, PartSql.class, dbServiceSQL);
                LinkHelper.link(part.getWarehouse(), companyId, WarehouseSql.class, dbServiceSQL);
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
        tradeService.runFinishInventoryTask(documentId, true);
    }
}

package lt.gama.impexp.link;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.CollectionsHelper;
import lt.gama.helpers.Validators;
import lt.gama.impexp.LinkBase;
import lt.gama.model.dto.documents.EstimateDto;
import lt.gama.model.dto.entities.WarehouseDto;
import lt.gama.model.mappers.EstimateSqlMapper;
import lt.gama.model.sql.entities.EmployeeSql;
import lt.gama.model.sql.entities.PartSql;
import lt.gama.model.type.Exchange;
import lt.gama.service.CurrencyService;
import lt.gama.service.DBServiceSQL;
import lt.gama.service.InventoryCheckService;
import lt.gama.service.TradeService;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Gama
 * Created by valdas on 15-08-18.
 */
public class LinkEstimateDto implements LinkBase<EstimateDto> {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private DBServiceSQL dbServiceSQL;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private Auth auth;

    @Autowired
    private EstimateSqlMapper estimateSqlMapper;

    @Autowired
    private InventoryCheckService inventoryCheckService;

    @Autowired
    private TradeService tradeService;


    @Override
    public EstimateDto resolve(EstimateDto document) {
        final long companyId = auth.getCompanyId();
        LinkHelper.link(document.getEmployee(), companyId, EmployeeSql.class, dbServiceSQL);
        LinkHelper.linkCounterparty(document.getCounterparty(), companyId, dbServiceSQL, entityManager);
        LinkHelper.link(document.getWarehouse(), companyId, WarehouseDto.class, dbServiceSQL);
        CollectionsHelper.streamOf(document.getParts()).forEach(part -> {
            LinkHelper.link(part, companyId, PartSql.class, dbServiceSQL);
            LinkHelper.link(part.getWarehouse(), companyId, WarehouseDto.class, dbServiceSQL);
            CollectionsHelper.streamOf(part.getParts()).forEach(partPart -> {
                LinkHelper.link(partPart, companyId, PartSql.class, dbServiceSQL);
                LinkHelper.link(part.getWarehouse(), companyId, WarehouseDto.class, dbServiceSQL);
            });
        });
        Exchange exchange = Validators.checkNotNull(currencyService.currencyExchange(
                Validators.checkNotNull(auth.getSettings(), "No company settings"),
                document.getExchange(), document.getDate()), "No exchange");
        document.setExchange(exchange);

        inventoryCheckService.checkPartLinkUuids(document.getParts());
        inventoryCheckService.checkPartUuids(document.getParts(), false);

        return document;
    }

    @Override
    public void finish(long documentId) {
        tradeService.finishEstimate(documentId);
    }
}

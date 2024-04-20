package lt.gama.impexp.link;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.CollectionsHelper;
import lt.gama.helpers.GamaMoneyUtils;
import lt.gama.helpers.Validators;
import lt.gama.impexp.LinkBase;
import lt.gama.model.dto.documents.PurchaseDto;
import lt.gama.model.dto.documents.items.PartPurchaseDto;
import lt.gama.model.mappers.PurchaseSqlMapper;
import lt.gama.model.sql.documents.PurchaseSql;
import lt.gama.model.sql.entities.EmployeeSql;
import lt.gama.model.sql.entities.PartSql;
import lt.gama.model.sql.entities.WarehouseSql;
import lt.gama.model.type.Exchange;
import lt.gama.model.type.enums.DebtType;
import lt.gama.service.CurrencyService;
import lt.gama.service.DBServiceSQL;
import lt.gama.service.TradeService;
import lt.gama.service.ex.rt.GamaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.invoke.MethodHandles;

/**
 * Gama
 * Created by valdas on 15-06-17.
 */
public class LinkPurchaseDto implements LinkBase<PurchaseDto> {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private DBServiceSQL dbServiceSQL;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private TradeService tradeService;

    @Autowired
    private Auth auth;

    @Autowired
    private PurchaseSqlMapper purchaseSqlMapper;


    @Override
    public PurchaseDto resolve(PurchaseDto document) {
        final long companyId = auth.getCompanyId();

        LinkHelper.link(document.getEmployee(), companyId, EmployeeSql.class, dbServiceSQL);
        LinkHelper.linkCounterparty(document.getCounterparty(), companyId, dbServiceSQL, entityManager);

        LinkHelper.link(document.getWarehouse(), companyId, WarehouseSql.class, dbServiceSQL);
        if (CollectionsHelper.hasValue(document.getParts())) {
            for (PartPurchaseDto part : document.getParts()) {
                LinkHelper.link(part, companyId, PartSql.class, dbServiceSQL);
                LinkHelper.link(part.getWarehouse(), companyId, WarehouseSql.class, dbServiceSQL);
                LinkHelper.link(part.getDocReturn(), companyId, PurchaseSql.class, dbServiceSQL);
                part.setCompanyId(companyId);
            }
        }
        if (Validators.isValid(document.getCounterparty()) && document.getDebtType() == null) {
            document.setDebtType(DebtType.VENDOR);
        }
        Exchange exchange = Validators.checkNotNull(currencyService.currencyExchange(
                Validators.checkNotNull(auth.getSettings(), "No company settings"),
                document.getExchange(), document.getDate()), "No exchange");
        document.setExchange(exchange);

        if (GamaMoneyUtils.isZero(document.getTotal())) {
            document.setTotal(GamaMoneyUtils.add(document.getSubtotal(), document.getTaxTotal()));
        }

        try {
            PurchaseSql entity = purchaseSqlMapper.toEntity(document);
            tradeService.prepareSavePurchaseSQL(entity);
            document = purchaseSqlMapper.toDto(entity);

        } catch (GamaException e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }

        return document;
    }

    @Override
    public void finish(long documentId) {
        tradeService.finishPurchase(documentId, true);
    }
}

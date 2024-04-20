package lt.gama.impexp.link;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.CollectionsHelper;
import lt.gama.helpers.GamaMoneyUtils;
import lt.gama.helpers.Validators;
import lt.gama.impexp.LinkBase;
import lt.gama.model.dto.documents.InvoiceDto;
import lt.gama.model.dto.documents.items.PartInvoiceDto;
import lt.gama.model.dto.documents.items.PartInvoiceSubpartDto;
import lt.gama.model.dto.entities.BankAccountDto;
import lt.gama.model.dto.entities.WarehouseDto;
import lt.gama.model.mappers.InvoiceSqlMapper;
import lt.gama.model.sql.documents.InvoiceSql;
import lt.gama.model.sql.documents.items.InvoicePartSql;
import lt.gama.model.sql.entities.BankAccountSql;
import lt.gama.model.sql.entities.EmployeeSql;
import lt.gama.model.sql.entities.PartSql;
import lt.gama.model.sql.entities.WarehouseSql;
import lt.gama.model.type.Exchange;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.model.type.auth.VATRatesDate;
import lt.gama.model.type.enums.DebtType;
import lt.gama.model.type.part.VATRate;
import lt.gama.service.*;
import lt.gama.service.ex.rt.GamaException;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Gama
 * Created by valdas on 15-07-17.
 */
public class LinkInvoiceDto implements LinkBase<InvoiceDto> {

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
    private InventoryService inventoryService;

    @Autowired
    private Auth auth;

    @Autowired
    private InvoiceSqlMapper invoiceSqlMapper;

    @Autowired
    private InventoryCheckService inventoryCheckService;


    @Override
    public InvoiceDto resolve(InvoiceDto document) {
        final long companyId = auth.getCompanyId();
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");

        LinkHelper.link(document.getEmployee(), companyId, EmployeeSql.class, dbServiceSQL);
        LinkHelper.linkCounterparty(document.getCounterparty(), companyId, dbServiceSQL, entityManager);

        LinkHelper.link(document.getWarehouse(), companyId, WarehouseSql.class, dbServiceSQL);
        WarehouseDto warehouse = Validators.isValid(companySettings.getWarehouse())
                ? new WarehouseDto(companySettings.getWarehouse().getId(), companySettings.getWarehouse().getDb())
                : null;
        if (!Validators.isValid(document.getWarehouse())) document.setWarehouse(warehouse);

        LinkHelper.link(document.getAccount(), companyId, BankAccountSql.class, dbServiceSQL);
        if (!Validators.isValid(document.getAccount())) document.setAccount(Validators.isValid(companySettings.getAccount())
                ? new BankAccountDto(companySettings.getAccount().getId(), companySettings.getAccount().getDb())
                : null);

        if (CollectionsHelper.hasValue(document.getParts())) {
            VATRatesDate vatRatesDate = null;
            for (PartInvoiceDto part : document.getParts()) {
                LinkHelper.link(part, companyId, PartSql.class, dbServiceSQL);
                LinkHelper.link(part.getWarehouse(), companyId, WarehouseSql.class, dbServiceSQL);
                if (!Validators.isValid(part.getWarehouse())) part.setWarehouse(warehouse);

                if (part.isTaxable() && part.getVat() == null) {
                    VATRate vat = null;
                    if (vatRatesDate == null) {
                        vatRatesDate = dbServiceSQL.getVATRateDate(companySettings.getCountry(), document.getDate());
                    }
                    if (vatRatesDate != null && CollectionsHelper.hasValue(vatRatesDate.getRates())) {
                        BigDecimal rate = NumberUtils.toScaledBigDecimal(part.getVatRate(), 2, RoundingMode.HALF_UP);
                        vat = vatRatesDate.getRates().stream()
                                .filter(e -> Objects.equals(rate, NumberUtils.toScaledBigDecimal(e.getRate(), 2, RoundingMode.HALF_UP)))
                                .findFirst()
                                .orElse(null);
                    }

                    if (vat != null) {
                        part.setVat(vat);
                        part.setVatRateCode(vat.getCode());
                    }
                }

                LinkHelper.link(part.getDocReturn(), companyId, InvoiceSql.class, dbServiceSQL);
                if (part.getParts() != null && part.getParts().size() > 0) {
                    for (PartInvoiceSubpartDto partPart : part.getParts()) {
                        LinkHelper.link(partPart.getWarehouse(), companyId, WarehouseSql.class, dbServiceSQL);
                        if (!Validators.isValid(partPart.getWarehouse())) partPart.setWarehouse(warehouse);

                        LinkHelper.link(partPart, companyId, PartSql.class, dbServiceSQL);
                    }
                }
            }
        }
        if (Validators.isValid(document.getCounterparty()) && document.getDebtType() == null) {
            document.setDebtType(DebtType.CUSTOMER);
        }
        Exchange exchange = Validators.checkNotNull(currencyService.currencyExchange(companySettings,
                document.getExchange(), document.getDate()), "No exchange");
        document.setExchange(exchange);

        if (GamaMoneyUtils.isZero(document.getTotal())) {
            document.setTotal(GamaMoneyUtils.add(document.getSubtotal(), document.getTaxTotal()));
        }

        try {
            inventoryCheckService.checkPartLinkUuids(document.getParts());
            InvoiceSql entity = invoiceSqlMapper.toEntity(document);
            tradeService.prepareSaveInvoiceSQL(true, true, false, entity);
            // assign parts DiscountedTotal if null
            CollectionsHelper.streamOf(entity.getParts())
                    .filter(p -> p instanceof InvoicePartSql)
                    .map(InvoicePartSql.class::cast)
                    .filter(part -> part.getDiscountedTotal() == null)
                    .forEach(part -> part.setDiscountedTotal(part.getTotal()));
            inventoryService.calculateVatCodeTotalsSQL(entity);
            document = invoiceSqlMapper.toDto(entity);

        } catch (GamaException e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }

        return document;
    }

    @Override
    public void finish(long documentId) {
        tradeService.finishInvoice(documentId, true);
    }
}

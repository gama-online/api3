package lt.gama.service.sync.woocommerce.task;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.EntityManager;
import lt.gama.api.response.TaskResponse;
import lt.gama.helpers.*;
import lt.gama.model.sql.entities.PartSql;
import lt.gama.model.sql.entities.PartSql_;
import lt.gama.model.sql.system.CountryVatRateSql;
import lt.gama.model.type.auth.VATRatesDate;
import lt.gama.model.type.enums.PartType;
import lt.gama.model.type.gl.GLDC;
import lt.gama.model.type.part.VATRate;
import lt.gama.service.TranslationService;
import lt.gama.service.ex.rt.GamaException;
import lt.gama.service.sync.SyncHttpService;
import lt.gama.service.sync.woocommerce.model.WooProduct;
import lt.gama.service.sync.woocommerce.model.WooProductVariation;
import lt.gama.service.sync.woocommerce.model.WooTax;
import lt.gama.tasks.BigDataDeferredTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serial;
import java.lang.invoke.MethodHandles;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static lt.gama.service.sync.i.ISyncWoocommerceService.API_3_SUFFIX;
import static lt.gama.service.sync.i.ISyncWoocommerceService.STD_TAX_CLASS;

public class SyncWooProductsTask extends BigDataDeferredTask<SyncWooData> {

    @Serial
    private static final long serialVersionUID = -1L;

    int TAX_PAGE_SIZE = 100;
    int PRODUCTS_PAGE_SIZE = 100;

    @Autowired
    transient protected SyncHttpService syncHttpService;


    transient private String api;
    transient private String authorizationHeader;
    transient private SyncWooData taskData;


    enum Step {
        TAXES,
        PRODUCTS
    }

    private final LocalDateTime date;
    private final int page;
    private final Step step;


    public SyncWooProductsTask(String token, long companyId, LocalDateTime date) {
        this(token, companyId, null, date, 1, Step.TAXES);
    }

    public SyncWooProductsTask(String token, long companyId, String fileName, LocalDateTime date, int page, Step step) {
        super(token, companyId, fileName);
        this.date = date != null ? date : DateUtils.now().minusYears(100);
        this.page = page;
        this.step = step;
    }

    @Override
    public void execute() {
        var companySettings = auth.getSettings();
        Validators.checkNotNull(companySettings.getSync(), "No sync settings");
        if (BooleanUtils.isNotTrue(companySettings.getSync().getSyncActive())) {
            log.info(className + ": Sync is not active");
            finish(TaskResponse.success());
            return;
        }
        api = companySettings.getSync().getUrl().endsWith("/")
                ? companySettings.getSync().getUrl()
                : (companySettings.getSync().getUrl() + '/');
        String key = companySettings.getSync().getId();
        String secret = companySettings.getSync().getKey();
        authorizationHeader = "Basic " + Base64.getEncoder().encodeToString((key + ":" + secret).getBytes());

        taskData = loadData(new TypeReference<>() {});
        if (taskData == null) taskData = new SyncWooData();
        if (taskData.errors == null) taskData.errors = new ArrayList<>();
        if (taskData.warnings == null) taskData.warnings = new ArrayList<>();

        if (step == Step.TAXES && page == 1) taskData.wooTaxes = new HashMap<>();

        try {
            switch (step) {
                case TAXES -> readTaxes();
                case PRODUCTS -> readProducts();
            }
        } catch (GamaException e) {
            taskData.errors.add(e.getMessage());
            finish(TaskResponse.errors(taskData.errors).withWarnings(taskData.warnings));
        }
    }

    private void readTaxes() {
        List<WooTax> wooTaxes = syncHttpService.getRequestData(
                SyncHttpService.HttpMethod.GET,
                api + API_3_SUFFIX + "taxes",
                Map.of("per_page", String.valueOf(TAX_PAGE_SIZE),
                        "page", String.valueOf(this.page)),
                new TypeReference<>() {},
                authorizationHeader
        );
        if (CollectionsHelper.hasValue(wooTaxes)) {
            wooTaxes.forEach(wooTax -> taskData.wooTaxes
                    .computeIfAbsent(wooTax.country(), k -> new HashMap<>())
                    .put(wooTax.tax_class(), wooTax.rate()));
            saveData(taskData);
            taskQueueService.queueTask(new SyncWooProductsTask(getToken(), getCompanyId(), getFileName(), date, page + 1, Step.TAXES));
        } else {
            log.info(className + ": Taxes=" + taskData.wooTaxes.values().stream().mapToInt(x -> x.values().size()).sum());

            // Prepare Vat rate codes
            var companySettings = auth.getSettings();

            CountryVatRateSql countryVatRate = Validators.checkNotNull(dbServiceSQL.getById(CountryVatRateSql.class, companySettings.getCountry()),
                    "No country Vat Rates data for companyId=" + getCompanyId() +
                            ", country=" + companySettings.getCountry());

            VATRatesDate vatRatesDate = Validators.checkNotNull(countryVatRate.getRatesMap(DateUtils.now().toLocalDate()),
                    "No Vat Rates data for companyId=" + getCompanyId() +
                            " for " + DateUtils.date(companySettings.getTimeZone()));

            taskData.rates = vatRatesDate.getRates();
            saveData(taskData);
            taskQueueService.queueTask(new SyncWooProductsTask(getToken(), getCompanyId(), getFileName(), date, 1, Step.PRODUCTS));
        }
    }

    public void readProducts() {
        List<WooProduct> wooProducts = syncHttpService.getRequestData(
                SyncHttpService.HttpMethod.GET,
                api + API_3_SUFFIX + "products",
                Map.of("modified_after", date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        "per_page", String.valueOf(PRODUCTS_PAGE_SIZE),
                        "page", String.valueOf(this.page)),
                new TypeReference<>() {},
                authorizationHeader
        );

        if (CollectionsHelper.hasValue(wooProducts)) {

            log.info(className + ": Syncing " + wooProducts.size() + " products");
            taskData.productsTotal += wooProducts.size();

            var productAsset = auth.getSettings().getGl().getProductAsset();
            var productIncome = new GLDC(auth.getSettings().getGl().getProductIncome());
            var productExpense = new GLDC(auth.getSettings().getGl().getProductExpense());

            var country = auth.getSettings().getCountry();
            var taxesByClass = taskData.wooTaxes.get(country);

            dbServiceSQL.executeInTransaction(entityManager -> {
                List<WooProduct> productsWithVariants = new ArrayList<>();
                wooProducts.forEach(wooProduct -> {
                    boolean hasVariations = CollectionsHelper.hasValue(wooProduct.variations());
                    if (hasVariations) {
                        productsWithVariants.add(wooProduct);
                    }
                    if (StringHelper.isEmpty(wooProduct.sku())) {
                        if (!hasVariations) {
                            String warning = "No SKU in product={" +
                                    "id=" + wooProduct.id() +
                                    ", name=" + wooProduct.name() +
                                    "}";
                            log.warn(className + ": " + warning);
                            taskData.warnings.add(warning);
                        }
                    } else {
                        var skuExists = checkSkuExists(entityManager, wooProduct.sku());
                        if (skuExists) {
                            var warning = MessageFormat.format(TranslationService.getInstance().translate(TranslationService.INVENTORY.ProductWithSkuAlreadyExists, auth.getLanguage()), wooProduct.sku());
                            log.warn(className + ": " + warning);
                            taskData.warnings.add(warning);
                        } else {
                            log.info(className + ": Syncing product with SKU=" + wooProduct.sku());
                            PartSql part = new PartSql();
                            part.setCompanyId(getCompanyId());
                            part.setType(PartType.PRODUCT);
                            part.setSku(wooProduct.sku());
                            part.setName(wooProduct.name());
                            part.setAccountAsset(productAsset);
                            part.setGlIncome(productIncome);
                            part.setGlExpense(productExpense);
                            if ("taxable".equals(wooProduct.tax_status())) {
                                part.setTaxable(true);
                                var vatRate = getVatRate(taskData.rates, taxesByClass.get(
                                        StringHelper.hasValue(wooProduct.tax_class()) ? wooProduct.tax_class() : STD_TAX_CLASS));
                                if (vatRate != null) {
                                    part.setVatRateCode(vatRate.getCode());
                                }
                            }
                            entityManager.persist(part);
                            taskData.productsCreated++;
                        }
                    }
                });

                var typeRefWooProductVariation = new TypeReference<List<WooProductVariation>>() {};
                productsWithVariants.forEach(wooProduct -> {
                    List<WooProductVariation> wooProductVariations = syncHttpService.getRequestData(
                            SyncHttpService.HttpMethod.GET,
                            api + API_3_SUFFIX + "products/" + wooProduct.id() + "/variations",
                            null,
                            typeRefWooProductVariation,
                            authorizationHeader
                    );
                    CollectionsHelper.streamOf(wooProductVariations).forEach(variation -> {
                        if (StringHelper.isEmpty(variation.sku())) {
                            String warning = "No SKU in product variation={" +
                                    "product.id=" + wooProduct.id() +
                                    "product.name=" + wooProduct.name() +
                                    "variation.id=" + variation.id() +
                                    "}";
                            log.warn(className + ": " + warning);
                            taskData.warnings.add(warning);
                        } else {
                            var skuExists = checkSkuExists(entityManager, variation.sku());
                            if (skuExists) {
                                var warning = MessageFormat.format(TranslationService.getInstance().translate(TranslationService.INVENTORY.ProductWithSkuAlreadyExists, auth.getLanguage()), wooProduct.sku());
                                log.warn(className + ": " + warning);
                                taskData.warnings.add(warning);
                            } else {
                                log.info(className + ": Syncing product with SKU=" + variation.sku());
                                PartSql part = new PartSql();
                                part.setCompanyId(getCompanyId());
                                part.setType(PartType.PRODUCT);
                                part.setSku(variation.sku());
                                if (CollectionsHelper.hasValue(variation.attributes())) {
                                    part.setName(wooProduct.name() + " - " + variation.attributes().get(0).option());
                                } else {
                                    part.setName(wooProduct.name() + " - " + variation.sku());
                                }
                                part.setAccountAsset(productAsset);
                                part.setGlIncome(productIncome);
                                part.setGlExpense(productExpense);
                                if ("taxable".equals(variation.tax_status())) {
                                    part.setTaxable(true);
                                    var vatRate = getVatRate(taskData.rates, taxesByClass.get(
                                            StringHelper.hasValue(variation.tax_class()) ? variation.tax_class() : STD_TAX_CLASS));
                                    if (vatRate != null) {
                                        part.setVatRateCode(vatRate.getCode());
                                    }
                                }
                                entityManager.persist(part);
                                taskData.productsCreated++;
                            }
                        }
                    });
                });
            });
        }

        if (wooProducts.size() == PRODUCTS_PAGE_SIZE) {
            saveData(taskData);
            taskQueueService.queueTask(new SyncWooProductsTask(getToken(), getCompanyId(), getFileName(), date, page + 1, Step.PRODUCTS));
            return;
        }

        var message = this.page == 1
                ? TranslationService.getInstance().translate(TranslationService.INVENTORY.NoProductsToSync, auth.getLanguage())
                : MessageFormat.format(TranslationService.getInstance().translate(TranslationService.INVENTORY.ProductsSynced, auth.getLanguage()),
                taskData.productsCreated, taskData.productsTotal);
        log.info(className + ": " + message);
        if (BooleanUtils.isTrue(auth.getSettings().getSync().getAbilities().order().toGama())) {
            taskData.warnings.add(0, message);
            saveData(taskData);
            taskQueueService.queueTask(new SyncWooOrdersTask(getToken(), getCompanyId(), getFileName(), date, 1, SyncWooOrdersTask.Step.ORDERS));
        } else {
            finish(message);
        }
    }

    private boolean checkSkuExists(EntityManager entityManager, String sku) {
        return entityManager.createQuery(
                        "SELECT COUNT(*) FROM " + PartSql.class.getName() + " a" +
                                " WHERE " + PartSql_.COMPANY_ID + " = :companyId" +
                                " AND " + PartSql_.SKU + " = :sku" +
                                " AND (archive IS null OR archive = false)" +
                                " AND (hidden IS null OR hidden = false)",
                        Long.class)
                .setParameter("companyId", getCompanyId())
                .setParameter("sku", sku)
                .getSingleResult() > 0;
    }

    private void finish(String message) {
        var response = Map.of(
                "message", message,
                "productsTotal", taskData.productsTotal,
                "productsCreated", taskData.productsCreated);
        if (taskData.errors.isEmpty()) {
            finish(TaskResponse.success(response).withWarnings(taskData.warnings));
        } else {
            finish(TaskResponse.errors(taskData.errors).withWarnings(taskData.warnings));
        }
    }

    private VATRate getVatRate(List<VATRate> rates, double rate) {
        return rates.stream()
                .filter(vat -> Math.abs(vat.getRate() - rate) < 0.01)
                .findFirst()
                .orElse(rates.stream()
                        .max(Comparator.comparing(VATRate::getRate))
                        .orElse(null));
    }

    @Override
    public String toString() {
        return "SyncWooProductsTask{" +
                "date=" + date +
                ", page=" + page +
                ", step=" + step +
                "} " + super.toString();
    }
}

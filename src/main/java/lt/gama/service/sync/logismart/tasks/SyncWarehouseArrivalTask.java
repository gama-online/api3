package lt.gama.service.sync.logismart.tasks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import lt.gama.api.response.TaskResponse;
import lt.gama.helpers.*;
import lt.gama.model.sql.base.BaseDocumentSql;
import lt.gama.model.sql.documents.PurchaseSql;
import lt.gama.model.sql.documents.items.PurchasePartSql;
import lt.gama.model.type.enums.PartType;
import lt.gama.model.type.sync.SyncSettings;
import lt.gama.service.ex.rt.GamaException;
import lt.gama.service.sync.i.ISyncWarehouseLogismart;
import lt.gama.service.sync.SyncHttpService;
import lt.gama.tasks.BaseDeferredTask;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serial;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Map;


public class SyncWarehouseArrivalTask extends BaseDeferredTask {

    @Serial
    private static final long serialVersionUID = -1L;


    @Autowired
    transient protected SyncHttpService syncHttpService;


    transient private String api;
    transient private String authorizationHeader;
    transient private List<SyncHttpService.Header> headers;


    final private long documentId;


    public SyncWarehouseArrivalTask(long companyId, long documentId) {
        super(companyId);
        this.documentId = documentId;
    }

    @Override
    public void execute() {
        try {
            SyncSettings syncSettings = Validators.checkNotNull(auth.getSettings().getSync(), "No Sync Settings");
            var warehouseAbilities = Validators.checkNotNull(syncSettings.getWarehouseAbilities(), "No Sync Warehouse Abilities");
            Validators.checkArgument(BooleanUtils.isTrue(warehouseAbilities.arrival().fromGama()), "Operation is not permitted");

            var syncWarehouseSettings = Validators.checkNotNull(syncSettings.getSyncWarehouse(), "No Sync Warehouse Settings");
            Validators.checkValid(syncWarehouseSettings.warehouse(), "No Warehouse in Sync Warehouse Settings");
            Validators.checkArgument(StringHelper.hasValue(syncWarehouseSettings.url()), "No URL in Sync Warehouse Settings");

            var username = Validators.checkNotNull(syncWarehouseSettings.username(), "No Username in Sync Warehouse Settings");
            var password = Validators.checkNotNull(syncWarehouseSettings.password(), "No Password in Sync Warehouse Settings");
            var key = Validators.checkNotNull(syncWarehouseSettings.key(), "No Key in Sync Warehouse Settings");

            authorizationHeader = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
            headers = List.of(new SyncHttpService.Header("api-key", key));
            api = syncWarehouseSettings.url().endsWith("/")
                    ? syncWarehouseSettings.url()
                    : (syncWarehouseSettings.url() + '/');

            upload(syncWarehouseSettings.warehouse().getId(), syncSettings.getTimeZone());

        } catch (NullPointerException | IllegalArgumentException e) {
            log.error(className + ": " + e.getMessage(), e);
            finish(TaskResponse.error(e.getMessage()));
        }
    }

    private void upload(long warehouseId, String timeZone) {
        var typeReference = new TypeReference<ISyncWarehouseLogismart.ArrivalResponse>() {};

        var document = Validators.checkNotNull(dbServiceSQL.getById(BaseDocumentSql.class, documentId), "Document not found");
        if (document instanceof PurchaseSql entity) {
            Hibernate.initialize(entity.getWarehouse());
            Validators.checkValid(entity.getWarehouse(), "No Warehouse in document");
            Validators.checkArgument(entity.getWarehouse().getId() == warehouseId, "Document Warehouse is not equal to Sync Warehouse");

            Hibernate.initialize(entity.getCounterparty());
            Hibernate.initialize(entity.getParts());

            var productsInWarehouse = getProductCodesInWarehouse();
            var productsNotInWarehouse = entity.getParts().stream()
                    .filter(product -> product.getType() != PartType.SERVICE)
                    .filter(product -> !productsInWarehouse.contains(product.getSku()))
                    .toList();
            uploadProducts(productsNotInWarehouse);

            var products = entity.getParts().stream()
                    .map(part -> new ArrivalProduct(part.getSku(), part.getQuantity()))
                    .toList();

            var arrival = new Arrival(DateUtils.date(timeZone), products);
            var response = Validators.checkNotNull(syncHttpService.getRequestData(SyncHttpService.HttpMethod.POST,
                            api + "api/arrivals/add",
                            null,
                            SyncHttpService.ContentType.JSON,
                            arrival,
                            typeReference, authorizationHeader, headers),
                    "No response from Sync Warehouse api/arrivals/add");
            if (response.isStatus()) {
                finish(TaskResponse.success());
            } else {
                log.error(className + ": response=" + response);
                finish(TaskResponse.errors(response.getAllErrors()));
            }
        } else {
            throw new GamaException("Documents other than Purchase are not supported");
        }
    }

    private void uploadProducts(List<PurchasePartSql> productsNotInWarehouse) {
        if (CollectionsHelper.isEmpty(productsNotInWarehouse)) return;
        var typeReference = new TypeReference<ResponseGetProductsCodeOnly>() {};
        productsNotInWarehouse.forEach(part -> {
            var response = Validators.checkNotNull(syncHttpService.getRequestData(SyncHttpService.HttpMethod.POST,
                            api + "api/products/add",
                            null,
                            SyncHttpService.ContentType.JSON,
                            Map.of(
                                    "product_code", part.getSku(),
                                    "title", part.getName(),
                                    "price", GamaMoneyUtils.isZero(part.getPrice()) ? BigDecimal.ZERO : part.getPrice().toMoney().getAmount()),
                            typeReference, authorizationHeader, headers),
                    "No response from Sync Warehouse api/products/add");
            if (!response.isStatus()) {
                log.error(className + ": response=" + response);
                throw new GamaException("Product not uploaded to Sync Warehouse: " + response.getError());
            }
        });
    }

    private List<String> getProductCodesInWarehouse() {
        var typeReference = new TypeReference<ResponseGetProductsCodeOnly>() {};
        var response = Validators.checkNotNull(syncHttpService.getRequestData(SyncHttpService.HttpMethod.GET,
                        api + "api/products",
                        Map.of("select", "product_code"),
                        SyncHttpService.ContentType.JSON,
                        null,
                        typeReference, authorizationHeader, headers),
                "No response from Sync Warehouse api/products");
        if (response.isStatus()) {
            return response.products.stream().map(ProductCodeOnly::productCode).toList();
        } else {
            log.error(className + ": response=" + response);
            finish(TaskResponse.errors(response.getAllErrors()));
            throw new GamaException("Get products error in Sync Warehouse: " + response.getError());
        }
    }



    record Arrival (
        @JsonProperty("delivery_date") LocalDate deliveryDate,
        List<ArrivalProduct> products
    ) {}

    record ArrivalProduct (
            @JsonProperty("product_code") String productCode,
            BigDecimal quantity
    ) {}


    static class ResponseGetProductsCodeOnly extends ISyncWarehouseLogismart.Response {
        private ISyncWarehouseLogismart.Paging paging;
        private List<ProductCodeOnly> products;

        public ISyncWarehouseLogismart.Paging getPaging() {
            return paging;
        }

        public void setPaging(ISyncWarehouseLogismart.Paging paging) {
            this.paging = paging;
        }

        public List<ProductCodeOnly> getProducts() {
            return products;
        }

        public void setProducts(List<ProductCodeOnly> products) {
            this.products = products;
        }
    }

    record ProductCodeOnly (
            @JsonProperty("product_code") String productCode
    ) {}

    @Override
    public String toString() {
        return "SyncWarehouseArrivalTask{" +
                "documentId=" + documentId +
                "} " + super.toString();
    }
}

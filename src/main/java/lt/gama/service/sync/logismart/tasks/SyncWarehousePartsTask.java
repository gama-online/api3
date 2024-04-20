package lt.gama.service.sync.logismart.tasks;

import com.fasterxml.jackson.core.type.TypeReference;
import lt.gama.api.response.TaskResponse;
import lt.gama.helpers.BooleanUtils;
import lt.gama.helpers.GamaMoneyUtils;
import lt.gama.helpers.StringHelper;
import lt.gama.helpers.Validators;
import lt.gama.model.sql.entities.PartSql;
import lt.gama.model.type.enums.PartType;
import lt.gama.model.type.sync.SyncSettings;
import lt.gama.service.sync.SyncHttpService;
import lt.gama.service.sync.i.ISyncWarehouseLogismart;
import lt.gama.tasks.BaseDeferredTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serial;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;
import java.util.Map;


public class SyncWarehousePartsTask extends BaseDeferredTask {

    @Serial
    private static final long serialVersionUID = -1L;


    @Autowired
    transient protected SyncHttpService syncHttpService;

    transient private String api;
    transient private String authorizationHeader;
    transient private List<SyncHttpService.Header> headers;


    private final int PAGE_SIZE = 100;
    private final int page;
    private int productsTotal;
    private int productsUploaded;


    public SyncWarehousePartsTask(long companyId) {
        this(null, companyId, 0, 0, 0);
    }

    public SyncWarehousePartsTask(String token, long companyId, int page, int productsTotal, int productsUploaded) {
        super(token, companyId);
        this.page = page;
        this.productsTotal = productsTotal;
        this.productsUploaded = productsUploaded;
    }

    @Override
    public void execute() {
        try {
            SyncSettings syncSettings = Validators.checkNotNull(auth.getSettings().getSync(), "No Sync Settings");
            var warehouseAbilities = Validators.checkNotNull(syncSettings.getWarehouseAbilities(), "No Sync Warehouse Abilities");
            Validators.checkArgument(BooleanUtils.isTrue(warehouseAbilities.product().fromGama()), "Operation is not permitted");

            var syncWarehouseSettings = Validators.checkNotNull(syncSettings.getSyncWarehouse(), "No Sync Warehouse Settings");
            Validators.checkArgument(StringHelper.hasValue(syncWarehouseSettings.url()), "No URL in Sync Warehouse Settings");
            var username = Validators.checkNotNull(syncWarehouseSettings.username(), "No Username in Sync Warehouse Settings");
            var password = Validators.checkNotNull(syncWarehouseSettings.password(), "No Password in Sync Warehouse Settings");
            var key = Validators.checkNotNull(syncWarehouseSettings.key(), "No Key in Sync Warehouse Settings");

            authorizationHeader = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
            headers = List.of(new SyncHttpService.Header("api-key", key));
            api = syncWarehouseSettings.url().endsWith("/")
                    ? syncWarehouseSettings.url()
                    : (syncWarehouseSettings.url() + '/');
            upload();

        } catch (NullPointerException | IllegalArgumentException e) {
            log.error(className + ": " + e.getMessage(), e);
            finish(TaskResponse.error(e.getMessage()));
        }
    }

    private void upload() {
        var typeReference = new TypeReference<ISyncWarehouseLogismart.Response>() {};

        var parts = entityManager.createQuery(
                "SELECT a FROM " + PartSql.class.getSimpleName() + " a" +
                        " WHERE a.companyId = :companyId" +
                        " AND a.type IN :types" +
                        " AND a.sku IS NOT NULL" +
                        " AND (a.archive IS null OR a.archive = false)" +
                        " AND (a.hidden IS null OR a.hidden = false)" +
                        " ORDER BY a.id",
                        PartSql.class)
                .setParameter("companyId", getCompanyId())
                .setParameter("types", List.of(PartType.PRODUCT, PartType.PRODUCT_SN))
                .setFirstResult(page * PAGE_SIZE)
                .setMaxResults(PAGE_SIZE)
                .getResultList();

        parts.stream()
                .map(part -> uploadPart(typeReference, part))
                .forEach(response -> {
                    productsTotal++;
                    if (response.isStatus()) {
                        productsUploaded++;
                    } else {
                        log.error(className + ": " + "Error uploading " +
                                "partId=" + response.partId +
                                ", sku=" + response.sku +
                                ", name=" + response.name +
                                ", error=" + response.getError() +
                                ", errors=" + response.getErrors());
                    }
                });

        if (parts.size() < PAGE_SIZE) {
            finish(TaskResponse.success().withData(Map.of(
                    "productsTotal", productsTotal,
                    "productsUploaded", productsUploaded,
                    "productsFailed", productsTotal - productsUploaded)));
        } else {
            taskQueueService.queueTask(new SyncWarehousePartsTask(getToken(), getCompanyId(), page + 1, productsTotal, productsUploaded));
        }
    }

    private ResponsePartAdd uploadPart(TypeReference<ISyncWarehouseLogismart.Response> typeReference, PartSql part) {
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
        return new ResponsePartAdd(response, part);
    }

    static class ResponsePartAdd extends ISyncWarehouseLogismart.Response {
        private long partId;
        private String sku;
        private String name;

        public ResponsePartAdd(ISyncWarehouseLogismart.Response response, PartSql part) {
            this.setStatus(response.isStatus());
            this.setError(response.getError());
            this.setErrors(response.getErrors());
            this.partId = part.getId();
            this.sku = part.getSku();
            this.name = part.getName();
        }

        public long getPartId() {
            return partId;
        }

        public void setPartId(long partId) {
            this.partId = partId;
        }

        public String getSku() {
            return sku;
        }

        public void setSku(String sku) {
            this.sku = sku;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}

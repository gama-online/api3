package lt.gama.service.sync.logismart.tasks;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.type.TypeReference;
import lt.gama.api.response.TaskResponse;
import lt.gama.helpers.*;
import lt.gama.model.sql.base.BaseDocumentSql;
import lt.gama.model.sql.documents.InvoiceSql;
import lt.gama.model.sql.documents.items.InvoicePartSql;
import lt.gama.model.type.Contact;
import lt.gama.model.type.enums.TaxpayerType;
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
import java.util.Base64;
import java.util.List;


public class SyncWarehouseOrderTask extends BaseDeferredTask {

    @Serial
    private static final long serialVersionUID = -1L;


    @Autowired
    transient protected SyncHttpService syncHttpService;


    transient private String api;
    transient private String authorizationHeader;
    transient private List<SyncHttpService.Header> headers;


    final private long documentId;

    public SyncWarehouseOrderTask(long companyId, long documentId) {
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
        var typeReference = new TypeReference<ISyncWarehouseLogismart.Response>() {};

        var document = Validators.checkNotNull(dbServiceSQL.getById(BaseDocumentSql.class, documentId), "Document not found");
        if (document instanceof InvoiceSql entity) {
            Hibernate.initialize(entity.getWarehouse());
            Validators.checkValid(entity.getWarehouse(), "No Warehouse in document");
            Validators.checkArgument(entity.getWarehouse().getId() == warehouseId, "Document Warehouse is not equal to Sync Warehouse");

            Hibernate.initialize(entity.getCounterparty());
            Hibernate.initialize(entity.getParts());

            Validators.checkNotNull(entity.getContact(), "No Contact info in document");
            Validators.checkArgument(StringHelper.hasValue(entity.getContact().getName()), "No Contact Name in document");
            Validators.checkArgument(CollectionsHelper.hasValue(entity.getContact().getContacts()), "No Contact Name in document");
            Validators.checkArgument(StringHelper.hasValue(entity.getContact().getName()), "No Contact Name in document");
            Validators.checkArgument(StringHelper.hasValue(entity.getContact().getName()), "No Contact Name in document");

            String email = entity.getContact().getContactByTypes(Contact.ContactType.email);
            String phone = entity.getContact().getContactByTypes(Contact.ContactType.mobile, Contact.ContactType.phone);

            Validators.checkArgument(StringHelper.hasValue(email), "No contact email in document");
            Validators.checkArgument(StringHelper.hasValue(phone), "No contact phone in document");

            Validators.checkNotNull(entity.getLocation(), "No Location info in document");
            Validators.checkNotNull(entity.getLocation().getZip(), "No Location postal code in document");
            Validators.checkNotNull(entity.getLocation().getCity(), "No Location city in document");

            var clientCode = entity.getCounterparty().getShortName();
            if (StringHelper.isEmpty(clientCode)) clientCode = entity.getCounterparty().getComCode();
            Validators.checkArgument(StringHelper.hasValue(clientCode), "No client Code or client Taxpayer code in document");

            var products = entity.getParts().stream()
                    .filter(p -> p instanceof InvoicePartSql)
                    .map(InvoicePartSql.class::cast)
                    .map(part -> new OrderProduct(part.getSku(), part.getQuantity(), part.getDiscountedPrice().getAmount()))
                    .toList();

            var order = new Order("G-" + entity.getNumber(),
                    new DeliveryMethodType(DeliverMethod.Logismart_Pick_UP),
                    products,
                    clientCode,
                    entity.getContact().getName().split("\\s")[0],
                    entity.getContact().getName().split("\\s")[1],
                    email,
                    phone,
                    entity.getCounterparty().getComCode(),
                    entity.getCounterparty().getName(),
                    LocationUtils.getStreetAddress(entity.getLocation()), // Street of Receiver’s shipping address
                    "XXX",
                    entity.getLocation().getZip(),
                    entity.getLocation().getCity(),
                    StringHelper.hasValue(entity.getLocation().getCountry()) ? entity.getLocation().getCountry() : "LT",
                    BigDecimal.ONE,
                    entity.getCounterparty().getTaxpayerType() == TaxpayerType.PHYSICAL ? DeliveryType.B2C : DeliveryType.B2B
            );

            var response = Validators.checkNotNull(syncHttpService.getRequestData(SyncHttpService.HttpMethod.POST,
                    api + "api/orders/add",
                    null,
                    SyncHttpService.ContentType.JSON,
                    order,
                    typeReference, authorizationHeader, headers),
            "No response from Sync Warehouse api/orders/add");
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

    record Order (
            @JsonProperty("order_code") String orderCode,
            DeliveryMethodType delivery,
            List<OrderProduct> products,
            @JsonProperty("customer_code") String customerCode,  // required
            @JsonProperty("first_name") String firstName,
            @JsonProperty("last_name") String lastName,
            String email,   // required
            String phone,   // required
            @JsonProperty("company_code") String companyCode,
            String company,
            String address, // required
            String house,   // required
            String postcode, // required
            String city, // required
            @JsonProperty("country_code") String country,
            BigDecimal total, // if order is not C.O.D., it could be a default value of 1 EUR
            @JsonProperty("delivery_type") DeliveryType deliveryType
    ) {}

    record OrderProduct (
            @JsonProperty("product_code") String productCode,
            BigDecimal quantity,
            BigDecimal price
    ) {
        public OrderProduct(String productCode, BigDecimal quantity) {
            this(productCode, quantity, null);
        }
    }

    record DeliveryMethodType (
            DeliverMethod method
    ) {}

    enum DeliverMethod {
        DPD_to_door(1),
        DPD_to_terminal(2),
        DPD_to_Parcelshop(3),
        Omniva_Parcel_lockers(4),
        Logismart_Pick_UP(10),
        LP_to_Door(11),
        LP_Express_terminals(12),
        LP_post(13),
        TNT_to_Door(14),
        LP_Express_to_Door(15),
        TNT_economy_express(16),
        Private_Carrier_to_Door(18),
        Venipak_to_door(36),
        Venipak_to_terminal(37);

        final int value;

        DeliverMethod(int value) {
            this.value = value;
        }

        @JsonCreator
        public static DeliverMethod from(Integer value) {
            if (value != null) {
                for (var t : values()) {
                    if (t.value == value) {
                        return t;
                    }
                }
            }
            return null;
        }

        @JsonValue
        public int getValue() {
            return value;
        }


        @Override
        public String toString() {
            return name().replace('_', ' ');
        }
    }

    enum DeliveryType {
        B2C(0),
        B2B(1);

        final int value;

        DeliveryType(int value) {
            this.value = value;
        }

        @JsonCreator
        public static DeliveryType from(Integer value) {
            if (value != null) {
                for (var t : values()) {
                    if (t.value == value) {
                        return t;
                    }
                }
            }
            return null;
        }

        @JsonValue
        public int getValue() {
            return value;
        }
    }
}

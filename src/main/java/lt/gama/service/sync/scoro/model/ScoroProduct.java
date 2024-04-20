package lt.gama.service.sync.scoro.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lt.gama.service.json.deser.NumericBooleanDeserializer;

import java.math.BigDecimal;

/**
 * gama-online
 * Created by valdas on 2017-10-12.
 */
public class ScoroProduct {

    @JsonProperty("product_id")
    private long productId;

    private String code;

    private String name;

    private BigDecimal price;

    private String description;

    @JsonProperty("is_service")
    @JsonSerialize(using = NumericBooleanSerializer.class)
    @JsonDeserialize(using = NumericBooleanDeserializer.class)
    private boolean service;

    @JsonProperty("default_vat_code_id")
    private long defaultVatCodeId;

    // generated

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isService() {
        return service;
    }

    public void setService(boolean service) {
        this.service = service;
    }

    public long getDefaultVatCodeId() {
        return defaultVatCodeId;
    }

    public void setDefaultVatCodeId(long defaultVatCodeId) {
        this.defaultVatCodeId = defaultVatCodeId;
    }

    @Override
    public String toString() {
        return "ScoroProduct{" +
                "productId=" + productId +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", description='" + description + '\'' +
                ", service=" + service +
                ", defaultVatCodeId=" + defaultVatCodeId +
                '}';
    }
}

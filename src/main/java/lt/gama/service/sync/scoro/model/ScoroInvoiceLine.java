package lt.gama.service.sync.scoro.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * gama-online
 * Created by valdas on 2017-10-13.
 */
public class ScoroInvoiceLine {

    private long id;

    @JsonProperty("product_id")
    private long productId;

    private BigDecimal price;

    private BigDecimal amount;

    private String unit;

    private BigDecimal sum;

    private BigDecimal vat;

    // generated

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getSum() {
        return sum;
    }

    public void setSum(BigDecimal sum) {
        this.sum = sum;
    }

    public BigDecimal getVat() {
        return vat;
    }

    public void setVat(BigDecimal vat) {
        this.vat = vat;
    }

    @Override
    public String toString() {
        return "ScoroInvoiceLine{" +
                "id=" + id +
                ", productId=" + productId +
                ", price=" + price +
                ", amount=" + amount +
                ", unit='" + unit + '\'' +
                ", sum=" + sum +
                ", vat=" + vat +
                '}';
    }
}

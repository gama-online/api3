package lt.gama.service.sync.openCart.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 11/11/2018.
 */
public class OCOrderLine {

    private long product_id;

    private String name;

    private String model;

    private String sku;

    private int quantity;

    private BigDecimal price;

    private BigDecimal total;

    private BigDecimal tax;

    // generated

    public long getProduct_id() {
        return product_id;
    }

    public void setProduct_id(long product_id) {
        this.product_id = product_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OCOrderLine that = (OCOrderLine) o;
        return product_id == that.product_id && quantity == that.quantity && Objects.equals(name, that.name) && Objects.equals(model, that.model) && Objects.equals(sku, that.sku) && Objects.equals(price, that.price) && Objects.equals(total, that.total) && Objects.equals(tax, that.tax);
    }

    @Override
    public int hashCode() {
        return Objects.hash(product_id, name, model, sku, quantity, price, total, tax);
    }

    @Override
    public String toString() {
        return "OCOrderLine{" +
                "product_id=" + product_id +
                ", name='" + name + '\'' +
                ", model='" + model + '\'' +
                ", sku='" + sku + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", total=" + total +
                ", tax=" + tax +
                '}';
    }
}

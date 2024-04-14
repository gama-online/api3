package lt.gama.model.type.part;

import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.doc.DocWarehouse;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Gama
 * Created by valdas on 15-08-05.
 */
public class PartRemainder implements Serializable {

    private DocWarehouse warehouse;

    private BigDecimal quantity;

    private GamaMoney cost;

    public PartRemainder() {
    }

    public PartRemainder(DocWarehouse warehouse, BigDecimal quantity, GamaMoney cost) {
        this.warehouse = warehouse;
        this.quantity = quantity;
        this.cost = cost;
    }

    // generated

    public DocWarehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(DocWarehouse warehouse) {
        this.warehouse = warehouse;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public GamaMoney getCost() {
        return cost;
    }

    public void setCost(GamaMoney cost) {
        this.cost = cost;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PartRemainder that = (PartRemainder) o;
        return Objects.equals(warehouse, that.warehouse) && Objects.equals(quantity, that.quantity) && Objects.equals(cost, that.cost);
    }

    @Override
    public int hashCode() {
        return Objects.hash(warehouse, quantity, cost);
    }

    @Override
    public String toString() {
        return "PartRemainder{" +
                "warehouse=" + warehouse +
                ", quantity=" + quantity +
                ", cost=" + cost +
                '}';
    }
}

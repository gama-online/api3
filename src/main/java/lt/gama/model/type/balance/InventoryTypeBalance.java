package lt.gama.model.type.balance;

import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.enums.InventoryType;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Gama
 * Created by valdas on 15-04-14.
 */
public class InventoryTypeBalance {

    private InventoryType type;

    private BigDecimal quantity;

    private GamaMoney cost;

    @SuppressWarnings("unused")
    protected InventoryTypeBalance() {
    }

    public InventoryTypeBalance(InventoryType type) {
        this.type = type;
    }

    public InventoryTypeBalance(InventoryType type, BigDecimal quantity, GamaMoney cost) {
        this.type = type;
        this.quantity = quantity;
        this.cost = cost;
    }

    // generated

    public InventoryType getType() {
        return type;
    }

    public void setType(InventoryType type) {
        this.type = type;
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
        InventoryTypeBalance that = (InventoryTypeBalance) o;
        return type == that.type && Objects.equals(quantity, that.quantity) && Objects.equals(cost, that.cost);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, quantity, cost);
    }

    @Override
    public String toString() {
        return "InventoryTypeBalance{" +
                "type=" + type +
                ", quantity=" + quantity +
                ", cost=" + cost +
                '}';
    }
}

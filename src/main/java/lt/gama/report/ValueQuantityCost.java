package lt.gama.report;

import lt.gama.model.type.GamaMoney;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

public class ValueQuantityCost implements Serializable {
    private BigDecimal quantity;
    private GamaMoney cost;
    private int count;

    public ValueQuantityCost() {
    }

    public ValueQuantityCost(BigDecimal quantity, GamaMoney cost) {
        this.count = 1;
        this.quantity = quantity;
        this.cost = cost;
    }

    public ValueQuantityCost(int count, BigDecimal quantity, GamaMoney cost) {
        this.count = count;
        this.quantity = quantity;
        this.cost = cost;
    }

    // generated

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

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValueQuantityCost that = (ValueQuantityCost) o;
        return Objects.equals(quantity, that.quantity) && Objects.equals(cost, that.cost);
    }

    @Override
    public int hashCode() {
        return Objects.hash(quantity, cost);
    }

    @Override
    public String toString() {
        return "ValueQuantityCost{" +
                "quantity=" + quantity +
                ", cost=" + cost +
                '}';
    }
}

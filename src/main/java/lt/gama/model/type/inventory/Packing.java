package lt.gama.model.type.inventory;

import java.math.BigDecimal;
import java.util.Objects;

public class Packing {

    private String name;

    private int quantity;

    private BigDecimal totalWeight;

    /**
     * units of Weight: kg, g
     */
    private String unitsWeight;

    // generated

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(BigDecimal totalWeight) {
        this.totalWeight = totalWeight;
    }

    public String getUnitsWeight() {
        return unitsWeight;
    }

    public void setUnitsWeight(String unitsWeight) {
        this.unitsWeight = unitsWeight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Packing packing = (Packing) o;
        return quantity == packing.quantity && Objects.equals(name, packing.name) && Objects.equals(totalWeight, packing.totalWeight) && Objects.equals(unitsWeight, packing.unitsWeight);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, quantity, totalWeight, unitsWeight);
    }

    @Override
    public String toString() {
        return "Packing{" +
                "name='" + name + '\'' +
                ", quantity=" + quantity +
                ", totalWeight=" + totalWeight +
                ", unitsWeight='" + unitsWeight + '\'' +
                '}';
    }
}

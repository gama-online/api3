package lt.gama.report;

import lt.gama.model.type.enums.InventoryType;

import java.util.Map;
import java.util.Objects;

class InventoryTypeWarehouseBalance {

    private InventoryType type;
    private Map<Long, ValueQuantityCost> warehouseBalanceMap;


    public InventoryTypeWarehouseBalance(InventoryType type, Map<Long, ValueQuantityCost> warehouseBalanceMap) {
        this.type = type;
        this.warehouseBalanceMap = warehouseBalanceMap;
    }

    // generated

    private InventoryTypeWarehouseBalance() {
    }

    public InventoryType getType() {
        return type;
    }

    public void setType(InventoryType type) {
        this.type = type;
    }

    public Map<Long, ValueQuantityCost> getWarehouseBalanceMap() {
        return warehouseBalanceMap;
    }

    public void setWarehouseBalanceMap(Map<Long, ValueQuantityCost> warehouseBalanceMap) {
        this.warehouseBalanceMap = warehouseBalanceMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InventoryTypeWarehouseBalance that = (InventoryTypeWarehouseBalance) o;
        return type == that.type && Objects.equals(warehouseBalanceMap, that.warehouseBalanceMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, warehouseBalanceMap);
    }

    @Override
    public String toString() {
        return "InventoryTypeWarehouseBalance{" +
                "type=" + type +
                ", warehouseBalanceMap=" + warehouseBalanceMap +
                '}';
    }
}

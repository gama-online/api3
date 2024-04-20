package lt.gama.report;

import lt.gama.helpers.BigDecimalUtils;
import lt.gama.helpers.GamaMoneyUtils;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.balance.InventoryTypeBalance;
import lt.gama.model.type.base.BaseDocPart;
import lt.gama.model.type.enums.InventoryType;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Gama
 * Created by valdas on 15-04-16.
 */
public class RepInventoryBalance {

    private BaseDocPart part;

    private BigDecimal quantity;

    private GamaMoney cost;

    private Map<InventoryType, InventoryTypeBalance> balanceMap;

    /**
     * Synthetic property - generated on get
     * @return map with string keys
     */
    public Map<String, InventoryTypeBalance> getBalance() {
        if (balanceMap == null) return null;
        Map<String, InventoryTypeBalance> balance = new HashMap<>();
        for (InventoryTypeBalance typeBalance : balanceMap.values()) {
            balance.put(typeBalance.getType().toString(), typeBalance);
        }
        return balance;
    }

    /**
     * Synthetic property - generated on get
     * @return quantity remainder
     */
    public BigDecimal getQuantityR() {
        BigDecimal quantityR = quantity;
        if (balanceMap != null) {
            for (InventoryTypeBalance typeBalance : balanceMap.values()) {
                quantityR = BigDecimalUtils.add(quantityR, typeBalance.getQuantity());
            }
        }
        return BigDecimalUtils.isZero(quantityR) ? null : quantityR;
    }

    /**
     * Synthetic property - generated on get
     * @return cost remainder
     */
    public GamaMoney getCostR() {
        GamaMoney costR = cost;
        if (balanceMap != null) {
            for (InventoryTypeBalance typeBalance : balanceMap.values()) {
                costR = GamaMoneyUtils.add(costR, typeBalance.getCost());
            }
        }
        return GamaMoneyUtils.isZero(costR) ? null : costR;
    }

    public BigDecimal getQuantity(InventoryType inventoryType) {
        if (balanceMap == null) return null;
        InventoryTypeBalance typeBalance = balanceMap.get(inventoryType);
        return typeBalance == null ? null : typeBalance.getQuantity();
    }

    private InventoryTypeBalance get(InventoryType inventoryType) {
        if (balanceMap == null) {
            balanceMap = new HashMap<>();
        }
        return balanceMap.computeIfAbsent(inventoryType, InventoryTypeBalance::new);
    }

    public void setQuantity(InventoryType inventoryType, BigDecimal quantity) {
        InventoryTypeBalance typeBalance = get(inventoryType);
        typeBalance.setQuantity(quantity);
    }

    public GamaMoney getCost(InventoryType inventoryType) {
        if (balanceMap == null) return null;
        InventoryTypeBalance typeBalance = balanceMap.get(inventoryType);
        return typeBalance == null ? null : typeBalance.getCost();
    }

    public void setCost(InventoryType inventoryType, GamaMoney cost) {
        InventoryTypeBalance typeBalance = get(inventoryType);
        typeBalance.setCost(cost);
    }

    // generated

    public BaseDocPart getPart() {
        return part;
    }

    public void setPart(BaseDocPart part) {
        this.part = part;
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

    public Map<InventoryType, InventoryTypeBalance> getBalanceMap() {
        return balanceMap;
    }

    public void setBalanceMap(Map<InventoryType, InventoryTypeBalance> balanceMap) {
        this.balanceMap = balanceMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RepInventoryBalance that = (RepInventoryBalance) o;
        return Objects.equals(part, that.part) && Objects.equals(quantity, that.quantity) && Objects.equals(cost, that.cost) && Objects.equals(balanceMap, that.balanceMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(part, quantity, cost, balanceMap);
    }

    @Override
    public String toString() {
        return "RepInventoryBalance{" +
                "part=" + part +
                ", quantity=" + quantity +
                ", cost=" + cost +
                ", balanceMap=" + balanceMap +
                '}';
    }
}

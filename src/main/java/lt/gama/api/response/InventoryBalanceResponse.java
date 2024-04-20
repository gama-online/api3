package lt.gama.api.response;

import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.part.PartSN;

import java.math.BigDecimal;

/**
 * Gama
 * Created by valdas on 15-07-20.
 */
public class InventoryBalanceResponse {

    private BigDecimal quantity;

    private GamaMoney cost;

    private long partId;

    private long warehouseId;

    private PartSN sn;

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

    public long getPartId() {
        return partId;
    }

    public void setPartId(long partId) {
        this.partId = partId;
    }

    public long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public PartSN getSn() {
        return sn;
    }

    public void setSn(PartSN sn) {
        this.sn = sn;
    }

    @Override
    public String toString() {
        return "InventoryBalanceResponse{" +
                "quantity=" + quantity +
                ", cost=" + cost +
                "} " + super.toString();
    }
}

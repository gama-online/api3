package lt.gama.model.dto.entities;

import lt.gama.model.type.doc.Doc;
import lt.gama.model.type.part.PartSN;
import lt.gama.model.dto.base.BaseCompanyDto;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.enums.InventoryType;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public class InventoryHistoryDto extends BaseCompanyDto {

    @Serial
    private static final long serialVersionUID = -1L;

    private PartDto part;

    private WarehouseDto warehouse;

    private PartSN sn;

    private UUID uuid;

    private InventoryType inventoryType;

    private Doc doc;

    private CounterpartyDto counterparty;

    private Doc originDoc;

    private CounterpartyDto originCounterparty;

    /**
     * Quantity at the end of period (the last date or until now)
     */
    private BigDecimal quantity;

    /**
     * Cost total at the end of period (the last date or until now)
     */
    private GamaMoney costTotal;

    /**
     * Selling totals
     */
    private GamaMoney sellTotal;

    /**
     * Selling totals in base currency
     */
    private GamaMoney sellBaseTotal;

    public InventoryHistoryDto() {
    }

    // generated

    public PartDto getPart() {
        return part;
    }

    public void setPart(PartDto part) {
        this.part = part;
    }

    public WarehouseDto getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(WarehouseDto warehouse) {
        this.warehouse = warehouse;
    }

    public PartSN getSn() {
        return sn;
    }

    public void setSn(PartSN sn) {
        this.sn = sn;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public InventoryType getInventoryType() {
        return inventoryType;
    }

    public void setInventoryType(InventoryType inventoryType) {
        this.inventoryType = inventoryType;
    }

    public Doc getDoc() {
        return doc;
    }

    public void setDoc(Doc doc) {
        this.doc = doc;
    }

    public CounterpartyDto getCounterparty() {
        return counterparty;
    }

    public void setCounterparty(CounterpartyDto counterparty) {
        this.counterparty = counterparty;
    }

    public Doc getOriginDoc() {
        return originDoc;
    }

    public void setOriginDoc(Doc originDoc) {
        this.originDoc = originDoc;
    }

    public CounterpartyDto getOriginCounterparty() {
        return originCounterparty;
    }

    public void setOriginCounterparty(CounterpartyDto originCounterparty) {
        this.originCounterparty = originCounterparty;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public GamaMoney getCostTotal() {
        return costTotal;
    }

    public void setCostTotal(GamaMoney costTotal) {
        this.costTotal = costTotal;
    }

    public GamaMoney getSellTotal() {
        return sellTotal;
    }

    public void setSellTotal(GamaMoney sellTotal) {
        this.sellTotal = sellTotal;
    }

    public GamaMoney getSellBaseTotal() {
        return sellBaseTotal;
    }

    public void setSellBaseTotal(GamaMoney sellBaseTotal) {
        this.sellBaseTotal = sellBaseTotal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        InventoryHistoryDto that = (InventoryHistoryDto) o;
        return Objects.equals(part, that.part) && Objects.equals(warehouse, that.warehouse) && Objects.equals(sn, that.sn) && Objects.equals(uuid, that.uuid) && inventoryType == that.inventoryType && Objects.equals(doc, that.doc) && Objects.equals(counterparty, that.counterparty) && Objects.equals(originDoc, that.originDoc) && Objects.equals(originCounterparty, that.originCounterparty) && Objects.equals(quantity, that.quantity) && Objects.equals(costTotal, that.costTotal) && Objects.equals(sellTotal, that.sellTotal) && Objects.equals(sellBaseTotal, that.sellBaseTotal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), part, warehouse, sn, uuid, inventoryType, doc, counterparty, originDoc, originCounterparty, quantity, costTotal, sellTotal, sellBaseTotal);
    }

    @Override
    public String toString() {
        return "InventoryHistoryDto{" +
                "part=" + part +
                ", warehouse=" + warehouse +
                ", sn=" + sn +
                ", uuid=" + uuid +
                ", inventoryType=" + inventoryType +
                ", doc=" + doc +
                ", counterparty=" + counterparty +
                ", originDoc=" + originDoc +
                ", originCounterparty=" + originCounterparty +
                ", quantity=" + quantity +
                ", costTotal=" + costTotal +
                ", sellTotal=" + sellTotal +
                ", sellBaseTotal=" + sellBaseTotal +
                "} " + super.toString();
    }
}

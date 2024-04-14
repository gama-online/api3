package lt.gama.model.dto.entities;

import lt.gama.model.dto.base.BaseCompanyDto;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.doc.Doc;
import lt.gama.model.type.enums.InventoryType;
import lt.gama.model.type.inventory.InventoryQ;
import lt.gama.model.type.part.PartSN;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;


public class InventoryNowDto extends BaseCompanyDto {

    private String timestamp;

    private PartDto part;

    private PartSN sn;

    private Doc doc;

    private CounterpartyDto counterparty;

    private WarehouseDto warehouse;

    private List<InventoryQ> remainder;

    private BigDecimal quantity;

    private GamaMoney costTotal;

    /**
     * Need for generate inventory balance record for forward-sell case
     */
    private InventoryType inventoryType;

    /**
     * Tag value if warehouse is tagged, i.e. 'withTag' is set
     */
    private String tag;

    public InventoryNowDto() {
    }

    public InventoryNowDto(PartDto part, PartSN sn, WarehouseDto warehouse,
                           Doc doc, CounterpartyDto counterparty, InventoryType inventoryType) {
        this.part = part;
        this.sn = sn;
        this.warehouse = warehouse;
        this.doc = doc;
        this.counterparty = counterparty;
        this.inventoryType = inventoryType;
    }

    public long getPartId() {
        return part.getId();
    }

    // generated

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public PartDto getPart() {
        return part;
    }

    public void setPart(PartDto part) {
        this.part = part;
    }

    public PartSN getSn() {
        return sn;
    }

    public void setSn(PartSN sn) {
        this.sn = sn;
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

    public WarehouseDto getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(WarehouseDto warehouse) {
        this.warehouse = warehouse;
    }

    public List<InventoryQ> getRemainder() {
        return remainder;
    }

    public void setRemainder(List<InventoryQ> remainder) {
        this.remainder = remainder;
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

    public InventoryType getInventoryType() {
        return inventoryType;
    }

    public void setInventoryType(InventoryType inventoryType) {
        this.inventoryType = inventoryType;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        InventoryNowDto that = (InventoryNowDto) o;
        return Objects.equals(timestamp, that.timestamp) && Objects.equals(part, that.part) && Objects.equals(sn, that.sn) && Objects.equals(doc, that.doc) && Objects.equals(counterparty, that.counterparty) && Objects.equals(warehouse, that.warehouse) && Objects.equals(remainder, that.remainder) && Objects.equals(quantity, that.quantity) && Objects.equals(costTotal, that.costTotal) && inventoryType == that.inventoryType && Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), timestamp, part, sn, doc, counterparty, warehouse, remainder, quantity, costTotal, inventoryType, tag);
    }

    @Override
    public String toString() {
        return "InventoryNowDto{" +
                "timestamp='" + timestamp + '\'' +
                ", part=" + part +
                ", sn=" + sn +
                ", doc=" + doc +
                ", counterparty=" + counterparty +
                ", warehouse=" + warehouse +
                ", remainder=" + remainder +
                ", quantity=" + quantity +
                ", costTotal=" + costTotal +
                ", inventoryType=" + inventoryType +
                ", tag='" + tag + '\'' +
                "} " + super.toString();
    }
}


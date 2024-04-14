package lt.gama.model.dto.documents.items;

import lt.gama.model.type.part.DocPartInvoice;
import lt.gama.model.type.part.PartCostSource;
import lt.gama.model.dto.base.BaseDocPartDto;
import lt.gama.model.dto.entities.WarehouseDto;
import lt.gama.model.i.IFinished;
import lt.gama.model.i.IQuantity;
import lt.gama.model.i.ISortOrder;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.ibase.IBaseDocPartCost;
import lt.gama.model.type.ibase.IBaseDocPartDto;
import lt.gama.model.type.ibase.IBaseDocPartOutRemainder;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class PartFromDto extends BaseDocPartDto implements IBaseDocPartOutRemainder, IBaseDocPartCost,
        IBaseDocPartDto, IFinished, ISortOrder, IQuantity {

    @Serial
    private static final long serialVersionUID = -1L;

    private Long recordId;

    private Double sortOrder;

    private BigDecimal quantity;

    private GamaMoney total;

    private GamaMoney baseTotal;

    private WarehouseDto warehouse;

    private String tag;

    private GamaMoney costTotal;

    /**
     * Is everything is done with part, i.e. calculated balance, cost, etc.
     */
    private Boolean finished;

    /**
     * Remainder to complete production process
     */
    private BigDecimal remainder;

    /**
     * Is part moved into warehouse for reservation
     */
    private Boolean reserved;

    /**
     * Reserved quantity, i.e. moved into reservation warehouse.
     * The rest, i.e. quantity - reservedQuantity should be ordered
     */
    private BigDecimal reservedQuantity;

    /**
     * Cost source info list, i.e. list of purchase/production docs info: id, document date and number, quantity and cost.
     * Information filled in the time of sale, i.e. at finishing invoice document.
     * The information here can be used for the reconstruction of inventory records (purchase, production or others)
     * at finishing returning invoice document (i.e. invoice with negative quantities)
     * which mus be linked through {@link DocPartInvoice#getDocReturn docReturn} to this document.
     */
    private List<PartCostSource> costInfo;

    /**
     *  Not enough quantity to finish operation - used in frontend only and is filled in invoice finishing procedure
     */
    private BigDecimal notEnough;

    // customized getters/setters

    public boolean isReserved() {
        return reserved != null && reserved;
    }

    // generated

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    @Override
    public Double getSortOrder() {
        return sortOrder;
    }

    @Override
    public void setSortOrder(Double sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Override
    public BigDecimal getQuantity() {
        return quantity;
    }

    @Override
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    @Override
    public GamaMoney getTotal() {
        return total;
    }

    @Override
    public void setTotal(GamaMoney total) {
        this.total = total;
    }

    @Override
    public GamaMoney getBaseTotal() {
        return baseTotal;
    }

    @Override
    public void setBaseTotal(GamaMoney baseTotal) {
        this.baseTotal = baseTotal;
    }

    @Override
    public WarehouseDto getWarehouse() {
        return warehouse;
    }

    @Override
    public void setWarehouse(WarehouseDto warehouse) {
        this.warehouse = warehouse;
    }

    @Override
    public String getTag() {
        return tag;
    }

    @Override
    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public GamaMoney getCostTotal() {
        return costTotal;
    }

    @Override
    public void setCostTotal(GamaMoney costTotal) {
        this.costTotal = costTotal;
    }

    @Override
    public Boolean getFinished() {
        return finished;
    }

    @Override
    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    @Override
    public BigDecimal getRemainder() {
        return remainder;
    }

    @Override
    public void setRemainder(BigDecimal remainder) {
        this.remainder = remainder;
    }

    public void setReserved(Boolean reserved) {
        this.reserved = reserved;
    }

    public BigDecimal getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(BigDecimal reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }

    @Override
    public List<PartCostSource> getCostInfo() {
        return costInfo;
    }

    @Override
    public void setCostInfo(List<PartCostSource> costInfo) {
        this.costInfo = costInfo;
    }

    @Override
    public BigDecimal getNotEnough() {
        return notEnough;
    }

    @Override
    public void setNotEnough(BigDecimal notEnough) {
        this.notEnough = notEnough;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PartFromDto that = (PartFromDto) o;
        return Objects.equals(recordId, that.recordId) && Objects.equals(sortOrder, that.sortOrder) && Objects.equals(quantity, that.quantity) && Objects.equals(total, that.total) && Objects.equals(baseTotal, that.baseTotal) && Objects.equals(warehouse, that.warehouse) && Objects.equals(tag, that.tag) && Objects.equals(costTotal, that.costTotal) && Objects.equals(finished, that.finished) && Objects.equals(remainder, that.remainder) && Objects.equals(reserved, that.reserved) && Objects.equals(reservedQuantity, that.reservedQuantity) && Objects.equals(costInfo, that.costInfo) && Objects.equals(notEnough, that.notEnough);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), recordId, sortOrder, quantity, total, baseTotal, warehouse, tag, costTotal, finished, remainder, reserved, reservedQuantity, costInfo, notEnough);
    }

    @Override
    public String toString() {
        return "PartFromDto{" +
                "recordId=" + recordId +
                ", sortOrder=" + sortOrder +
                ", quantity=" + quantity +
                ", total=" + total +
                ", baseTotal=" + baseTotal +
                ", warehouse=" + warehouse +
                ", tag='" + tag + '\'' +
                ", costTotal=" + costTotal +
                ", finished=" + finished +
                ", remainder=" + remainder +
                ", reserved=" + reserved +
                ", reservedQuantity=" + reservedQuantity +
                ", costInfo=" + costInfo +
                ", notEnough=" + notEnough +
                "} " + super.toString();
    }
}

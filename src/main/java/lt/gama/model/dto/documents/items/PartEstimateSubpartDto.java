package lt.gama.model.dto.documents.items;

import lt.gama.model.dto.base.BaseDocPartDto;
import lt.gama.model.dto.entities.WarehouseDto;
import lt.gama.model.i.*;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.l10n.LangPart;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public class PartEstimateSubpartDto extends BaseDocPartDto implements ISortOrder, IParentLinkUuid, IQuantity {

    @Serial
    private static final long serialVersionUID = -1L;

    private Long recordId;

    private Double sortOrder;

    private BigDecimal quantity;

    private GamaMoney total;

    private GamaMoney baseTotal;

    private WarehouseDto warehouse;

    private String tag;

    /**
     * Is everything is done with part, i.e. calculated balance, cost, etc.
     */
    private Boolean finished;

    /**
     * In Estimate - remainder to sell
     */
    private BigDecimal remainder;

    private UUID parentLinkUuid;

    public PartEstimateSubpartDto() {
    }

    public <P extends IPart & ITranslations<LangPart> & IDb> PartEstimateSubpartDto(P part) {
        super(part);
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

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public GamaMoney getTotal() {
        return total;
    }

    public void setTotal(GamaMoney total) {
        this.total = total;
    }

    public GamaMoney getBaseTotal() {
        return baseTotal;
    }

    public void setBaseTotal(GamaMoney baseTotal) {
        this.baseTotal = baseTotal;
    }

    public WarehouseDto getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(WarehouseDto warehouse) {
        this.warehouse = warehouse;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    public BigDecimal getRemainder() {
        return remainder;
    }

    public void setRemainder(BigDecimal remainder) {
        this.remainder = remainder;
    }

    @Override
    public UUID getParentLinkUuid() {
        return parentLinkUuid;
    }

    @Override
    public void setParentLinkUuid(UUID parentLinkUuid) {
        this.parentLinkUuid = parentLinkUuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PartEstimateSubpartDto that = (PartEstimateSubpartDto) o;
        return Objects.equals(recordId, that.recordId) && Objects.equals(sortOrder, that.sortOrder) && Objects.equals(quantity, that.quantity) && Objects.equals(total, that.total) && Objects.equals(baseTotal, that.baseTotal) && Objects.equals(warehouse, that.warehouse) && Objects.equals(tag, that.tag) && Objects.equals(finished, that.finished) && Objects.equals(remainder, that.remainder) && Objects.equals(parentLinkUuid, that.parentLinkUuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), recordId, sortOrder, quantity, total, baseTotal, warehouse, tag, finished, remainder, parentLinkUuid);
    }

    @Override
    public String toString() {
        return "PartEstimateSubpartDto{" +
                "recordId=" + recordId +
                ", sortOrder=" + sortOrder +
                ", quantity=" + quantity +
                ", total=" + total +
                ", baseTotal=" + baseTotal +
                ", warehouse=" + warehouse +
                ", tag='" + tag + '\'' +
                ", finished=" + finished +
                ", remainder=" + remainder +
                ", parentLinkUuid=" + parentLinkUuid +
                "} " + super.toString();
    }
}

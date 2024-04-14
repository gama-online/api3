package lt.gama.model.dto.entities;

import lt.gama.model.dto.base.BaseDocPartDto;
import lt.gama.model.i.ISortOrder;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Objects;

public class PartPartDto extends BaseDocPartDto implements ISortOrder {

    @Serial
    private static final long serialVersionUID = -1L;

    private Long recordId;

    private Double sortOrder;

    private BigDecimal quantity;

    public PartPartDto() {
    }

    public PartPartDto(PartDto part) {
        setId(part.getId());
        setName(part.getName());
        setSku(part.getSku());
        setBarcode(part.getBarcode());
        setUnit(part.getUnit());
        setType(part.getType());
        setCf(part.getCf());
        setTranslation(part.getTranslation());
        setManufacturer(part.getManufacturer());
        setBrutto(part.getBrutto());
        setNetto(part.getNetto());
        setAccountAsset(part.getAccountAsset());
        setGlIncome(part.getGlIncome());
        setGlExpense(part.getGlExpense());
        setDb(part.getDb());
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PartPartDto that = (PartPartDto) o;
        return Objects.equals(recordId, that.recordId) && Objects.equals(sortOrder, that.sortOrder) && Objects.equals(quantity, that.quantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), recordId, sortOrder, quantity);
    }

    @Override
    public String toString() {
        return "PartPartDto{" +
                "recordId=" + recordId +
                ", sortOrder=" + sortOrder +
                ", quantity=" + quantity +
                "} " + super.toString();
    }
}

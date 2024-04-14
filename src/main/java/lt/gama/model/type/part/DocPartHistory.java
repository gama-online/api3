package lt.gama.model.type.part;

import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.base.BaseDocPart;
import lt.gama.model.type.doc.DocWarehouse;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Objects;

public class DocPartHistory extends BaseDocPart {

    @Serial
    private static final long serialVersionUID = -1L;

    private DocWarehouse warehouse;

    private BigDecimal quantity;

    private GamaMoney costTotal;

    // generated

    public DocWarehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(DocWarehouse warehouse) {
        this.warehouse = warehouse;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DocPartHistory that = (DocPartHistory) o;
        return Objects.equals(warehouse, that.warehouse) && Objects.equals(quantity, that.quantity) && Objects.equals(costTotal, that.costTotal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), warehouse, quantity, costTotal);
    }

    @Override
    public String toString() {
        return "DocPartHistory{" +
                "warehouse=" + warehouse +
                ", quantity=" + quantity +
                ", costTotal=" + costTotal +
                "} " + super.toString();
    }
}

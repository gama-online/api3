package lt.gama.model.type.part;

import lt.gama.model.i.IDb;
import lt.gama.model.i.IPart;
import lt.gama.model.i.ITranslations;
import lt.gama.model.type.base.BaseDocPart;
import lt.gama.model.type.l10n.LangPart;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Objects;

public class DocPartPart extends BaseDocPart {

    @Serial
    private static final long serialVersionUID = -1L;

    private Double sortOrder;

    private BigDecimal quantity;


    public DocPartPart() {
    }

    public <P extends IPart & ITranslations<LangPart> & IDb> DocPartPart(P part) {
        super(part);
    }

    // generated

    public Double getSortOrder() {
        return sortOrder;
    }

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
        DocPartPart that = (DocPartPart) o;
        return Objects.equals(quantity, that.quantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), quantity);
    }

    @Override
    public String toString() {
        return "DocPartPart{" +
                "quantity=" + quantity +
                "} " + super.toString();
    }
}

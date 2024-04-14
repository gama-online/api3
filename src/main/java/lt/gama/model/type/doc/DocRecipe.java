package lt.gama.model.type.doc;

import lt.gama.model.i.IName;
import lt.gama.model.type.base.BaseDocEntity;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 2016-04-10.
 */
public class DocRecipe extends BaseDocEntity implements IName {

    @Serial
    private static final long serialVersionUID = -1L;

    private String name;

    private BigDecimal quantity;

    public DocRecipe() {
    }

    // generated

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
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
        DocRecipe docRecipe = (DocRecipe) o;
        return Objects.equals(name, docRecipe.name) && Objects.equals(quantity, docRecipe.quantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, quantity);
    }

    @Override
    public String toString() {
        return "DocRecipe{" +
                "name='" + name + '\'' +
                ", quantity=" + quantity +
                "} " + super.toString();
    }
}

package lt.gama.model.sql.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lt.gama.model.i.ISortOrder;
import lt.gama.model.sql.base.BaseCompanySql;

import java.math.BigDecimal;

@Entity
@Table(name = "part_parts")
@NamedEntityGraph(name = PartPartSql.GRAPH_ALL, attributeNodes = {
                @NamedAttributeNode("parent"),
                @NamedAttributeNode("part")
})
public class PartPartSql extends BaseCompanySql implements ISortOrder {

    public static final String GRAPH_ALL = "graph.PartPartSql.all";

    private Double sortOrder;

    private BigDecimal quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnoreProperties("parts")
    private PartSql parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id")
    private PartSql part;

    // generated

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

    public PartSql getParent() {
        return parent;
    }

    public void setParent(PartSql parent) {
        this.parent = parent;
    }

    public PartSql getPart() {
        return part;
    }

    public void setPart(PartSql part) {
        this.part = part;
    }

    @Override
    public String toString() {
        return "PartPartSql{" +
                "sortOrder=" + sortOrder +
                ", quantity=" + quantity +
                ", parent=" + parent.getId() +
                ", part=" + part.getId() +
                "} " + super.toString();
    }
}

package lt.gama.model.sql.documents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lt.gama.helpers.BooleanUtils;
import lt.gama.model.i.IDocPartsSql;
import lt.gama.model.sql.base.BaseDocumentSql;
import lt.gama.model.sql.documents.items.InventoryOpeningBalancePartSql;
import lt.gama.model.sql.entities.WarehouseSql;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inventory_ob")
@NamedEntityGraph(name = InventoryOpeningBalanceSql.GRAPH_ALL, attributeNodes = {
        @NamedAttributeNode(InventorySql_.PARTS),
        @NamedAttributeNode(InventorySql_.WAREHOUSE)
})
public class InventoryOpeningBalanceSql extends BaseDocumentSql implements IDocPartsSql<InventoryOpeningBalancePartSql> {

    public static final String GRAPH_ALL = "graph.InventoryOpeningBalanceSql.all";

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("parent")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @OrderBy("sortOrder, id")
    private List<InventoryOpeningBalancePartSql> parts = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "warehouse_id")
    private WarehouseSql warehouse;

    private String tag;

    private Boolean finishedParts;


    @Override
    public boolean isFullyFinished() {
        return super.isFullyFinished() &&  BooleanUtils.isTrue(finishedParts);
    }

    @Override
    public boolean setFullyFinished() {
        boolean changed = super.setFullyFinished() || BooleanUtils.isNotTrue(finishedParts);
        finishedParts = true;
        return changed;
    }

    // generated

    @Override
    public List<InventoryOpeningBalancePartSql> getParts() {
        return parts;
    }

    @Override
    public void setParts(List<InventoryOpeningBalancePartSql> parts) {
        this.parts = parts;
    }

    public WarehouseSql getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(WarehouseSql warehouse) {
        this.warehouse = warehouse;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public Boolean getFinishedParts() {
        return finishedParts;
    }

    @Override
    public void setFinishedParts(Boolean finishedParts) {
        this.finishedParts = finishedParts;
    }

    @Override
    public String toString() {
        return "InventoryOpeningBalanceSql{" +
                "warehouse=" + warehouse +
                ", tag='" + tag + '\'' +
                ", finishedParts=" + finishedParts +
                "} " + super.toString();
    }
}

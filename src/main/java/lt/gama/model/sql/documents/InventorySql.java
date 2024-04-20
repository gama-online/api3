package lt.gama.model.sql.documents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lt.gama.helpers.BooleanUtils;
import lt.gama.model.i.IDocPartsSql;
import lt.gama.model.i.ITranslations;
import lt.gama.model.sql.base.BaseDocumentSql;
import lt.gama.model.sql.documents.items.InventoryPartSql;
import lt.gama.model.sql.entities.WarehouseSql;
import lt.gama.model.type.l10n.LangInventory;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "inventory")
@NamedEntityGraph(name = InventorySql.GRAPH_ALL, attributeNodes = {
        @NamedAttributeNode(InventorySql_.PARTS),
        @NamedAttributeNode(InventorySql_.WAREHOUSE)
})
public class InventorySql extends BaseDocumentSql implements IDocPartsSql<InventoryPartSql>, ITranslations<LangInventory> {

    public static final String GRAPH_ALL = "graph.InventorySql.all";

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("parent")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @OrderBy("sortOrder, id")
    private List<InventoryPartSql> parts = new ArrayList<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "warehouse_id")
    private WarehouseSql warehouse;

    private String tag;

    private Boolean finishedParts;

    /**
     * Note visible to customer.
     * Can be translated
     */
    private String inventoryNote;

    /**
     * Translations
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, LangInventory> translation;

    @Override
    public boolean isFullyFinished() {
        return super.isFullyFinished() && BooleanUtils.isTrue(finishedParts);
    }

    @Override
    public boolean setFullyFinished() {
        boolean changed = super.setFullyFinished() || BooleanUtils.isNotTrue(finishedParts);
        finishedParts = true;
        return changed;
    }

    @Override
    public void reset() {
        super.reset();
        finishedParts = false;
        if (parts != null) {
            for (InventoryPartSql part : parts) {
                part.reset();
            }
        }
    }

    // generated

    @Override
    public List<InventoryPartSql> getParts() {
        return parts;
    }

    @Override
    public void setParts(List<InventoryPartSql> parts) {
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

    public String getInventoryNote() {
        return inventoryNote;
    }

    public void setInventoryNote(String inventoryNote) {
        this.inventoryNote = inventoryNote;
    }

    @Override
    public Map<String, LangInventory> getTranslation() {
        return translation;
    }

    @Override
    public void setTranslation(Map<String, LangInventory> translation) {
        this.translation = translation;
    }

    @Override
    public String toString() {
        return "InventorySql{" +
                "warehouse=" + warehouse +
                ", tag='" + tag + '\'' +
                ", finishedParts=" + finishedParts +
                ", inventoryNote='" + inventoryNote + '\'' +
                ", translation=" + translation +
                "} " + super.toString();
    }
}

package lt.gama.model.type.doc;

import lt.gama.model.i.IName;
import lt.gama.model.i.IWarehouse;
import lt.gama.model.type.base.BaseDocEntity;
import lt.gama.model.type.inventory.WarehouseTagged;

import java.io.Serial;
import java.util.Objects;

/**
 * Gama
 * Created by valdas on 15-04-07.
 */
public class DocWarehouse extends BaseDocEntity implements IName {

    @Serial
    private static final long serialVersionUID = -1L;

    private String name;

    /**
     * If warehouse operations are tagged,
     * i.e. inventory can be operated with tag only.
     * It acts similar to inventories with serial numbers.
     */
    private Boolean withTag;

    /**
     * Tag value is required if 'withTag' is set
     */
    private String tag;

    public DocWarehouse() {}

    public DocWarehouse(IWarehouse warehouse, String tag) {
       this(warehouse);
       this.tag = tag;
    }

    public DocWarehouse(IWarehouse warehouse) {
        super(warehouse.getId());
        this.name = warehouse.getName();
        this.withTag = warehouse.getWithTag();
        this.setDb(warehouse.getDb());
    }

    public DocWarehouse(WarehouseTagged warehouse) {
        super(warehouse.getId());
        this.name = warehouse.getName();
        this.withTag = warehouse.getWithTag();
        this.tag = warehouse.getTag();
        this.setDb(warehouse.getDb());
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

    public Boolean getWithTag() {
        return withTag;
    }

    public void setWithTag(Boolean withTag) {
        this.withTag = withTag;
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
        DocWarehouse that = (DocWarehouse) o;
        return Objects.equals(name, that.name) && Objects.equals(withTag, that.withTag) && Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, withTag, tag);
    }

    @Override
    public String toString() {
        return "DocWarehouse{" +
                "name='" + name + '\'' +
                ", withTag=" + withTag +
                ", tag='" + tag + '\'' +
                "} " + super.toString();
    }
}

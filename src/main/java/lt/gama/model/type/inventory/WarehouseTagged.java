package lt.gama.model.type.inventory;

import lt.gama.model.i.IId;
import lt.gama.model.type.enums.DBType;

public class WarehouseTagged implements IId<Long> {

    private Long id;

    private String name;

    private Boolean withTag = false;

    private String tag;

    private final DBType db = DBType.POSTGRESQL;

    public WarehouseTagged(long id, String name) {
        this.id = id;
        this.name = name;
    }
//TODO remove comments
//    public WarehouseTagged(WarehouseSql warehouse, String tag) {
//        this.id = warehouse.getId();
//        this.name = warehouse.getName();
//        this.withTag = warehouse.getWithTag();
//        this.tag = tag;
//    }

    // generated

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

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

    public DBType getDb() {
        return db;
    }
}

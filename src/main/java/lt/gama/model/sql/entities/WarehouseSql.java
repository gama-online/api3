package lt.gama.model.sql.entities;

import jakarta.persistence.*;
import lt.gama.helpers.StringHelper;
import lt.gama.model.i.IWarehouse;
import lt.gama.model.sql.base.BaseCompanySql;
import lt.gama.model.type.Location;
import org.apache.commons.lang3.StringUtils;


@Entity
@Table(name = "warehouse")
@NamedEntityGraph(name = WarehouseSql.GRAPH_ALL, attributeNodes = @NamedAttributeNode("storekeeper"))
public class WarehouseSql extends BaseCompanySql implements Comparable<WarehouseSql>, IWarehouse {

    public static final String GRAPH_ALL = "graph.WarehouseSql.all";

    private String name;

    @Embedded
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storekeeper_id")
    private EmployeeSql storekeeper;

    private boolean closed;

    /**
     * If warehouse operations are tagged,
     * i.e. inventory can be added and retrieved with the same tag only.
     * It acts similar to inventories with serial numbers.
     */
    private Boolean withTag;

    public WarehouseSql() {
    }

    @Override
    public int compareTo(WarehouseSql o) {
        return (StringHelper.isEmpty(name)
                ? "" : StringUtils.stripAccents(name)).compareToIgnoreCase(StringHelper.isEmpty(o.name)
                ? "" : StringUtils.stripAccents(o.name));
    }

    //generated

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public EmployeeSql getStorekeeper() {
        return storekeeper;
    }

    public void setStorekeeper(EmployeeSql storekeeper) {
        this.storekeeper = storekeeper;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    @Override
    public Boolean getWithTag() {
        return withTag;
    }

    public void setWithTag(Boolean withTag) {
        this.withTag = withTag;
    }

    @Override
    public String toString() {
        return "WarehouseSql{" +
                "name='" + name + '\'' +
                ", location=" + location +
                ", closed=" + closed +
                ", withTag=" + withTag +
                "} " + super.toString();
    }
}

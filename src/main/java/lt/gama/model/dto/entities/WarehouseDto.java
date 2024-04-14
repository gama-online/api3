package lt.gama.model.dto.entities;

import lt.gama.model.type.Location;
import lt.gama.model.dto.base.BaseCompanyDto;
import lt.gama.model.i.IWarehouse;
import lt.gama.model.type.enums.DBType;

import java.io.Serial;
import java.util.Objects;

public class WarehouseDto extends BaseCompanyDto implements IWarehouse {

    @Serial
    private static final long serialVersionUID = -1L;

    private String name;

    private Location location;

    private EmployeeDto storekeeper;

    private boolean closed;

    /**
     * If warehouse operations are tagged,
     * i.e. inventory can be added and retrieved with the same tag only.
     * It acts similar to inventories with serial numbers.
     */
    private Boolean withTag;

    public WarehouseDto() {
    }

    public WarehouseDto(long id, DBType db) {
        setId(id);
        setDb(db);
    }

    public WarehouseDto(IWarehouse warehouse) {
        if (warehouse == null) return;
        setId(warehouse.getId());
        setDb(warehouse.getDb());
        this.name = warehouse.getName();
        this.withTag = warehouse.getWithTag();
    }

    // generated

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

    public EmployeeDto getStorekeeper() {
        return storekeeper;
    }

    public void setStorekeeper(EmployeeDto storekeeper) {
        this.storekeeper = storekeeper;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public Boolean getWithTag() {
        return withTag;
    }

    public void setWithTag(Boolean withTag) {
        this.withTag = withTag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        WarehouseDto that = (WarehouseDto) o;
        return closed == that.closed && Objects.equals(name, that.name) && Objects.equals(location, that.location) && Objects.equals(storekeeper, that.storekeeper) && Objects.equals(withTag, that.withTag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, location, storekeeper, closed, withTag);
    }

    @Override
    public String toString() {
        return "WarehouseDto{" +
                "name='" + name + '\'' +
                ", location=" + location +
                ", storekeeper=" + storekeeper +
                ", closed=" + closed +
                ", withTag=" + withTag +
                "} " + super.toString();
    }
}

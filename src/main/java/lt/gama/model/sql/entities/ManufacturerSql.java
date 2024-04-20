package lt.gama.model.sql.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lt.gama.model.i.IManufacturer;
import lt.gama.model.sql.base.BaseCompanySql;


@Entity
@Table(name = "manufacturer")
public class ManufacturerSql extends BaseCompanySql implements IManufacturer {

    private String name;

    private String description;

    // generated

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "ManufacturerSql{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                "} " + super.toString();
    }
}

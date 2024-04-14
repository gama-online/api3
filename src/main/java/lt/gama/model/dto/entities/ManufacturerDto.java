package lt.gama.model.dto.entities;

import lt.gama.model.dto.base.BaseCompanyDto;
import lt.gama.model.i.IManufacturer;

public class ManufacturerDto extends BaseCompanyDto implements IManufacturer {

    private String name;

    private String description;

    public ManufacturerDto() {
    }

    public ManufacturerDto(IManufacturer m) {
        if (m == null) return;
        setId(m.getId());
        setName(m.getName());
    }

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
        return "ManufacturerDto{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                "} " + super.toString();
    }
}

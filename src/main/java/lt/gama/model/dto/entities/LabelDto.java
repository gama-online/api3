package lt.gama.model.dto.entities;

import lt.gama.model.dto.base.BaseCompanyDto;
import lt.gama.model.type.enums.LabelType;

public class LabelDto extends BaseCompanyDto {

    private String name;

    private LabelType type;


    public LabelDto() {
    }

    public LabelDto(String name) {
        this.name = name;
    }

    public LabelDto(String name, LabelType type) {
        this.name = name;
        this.type = type;
    }

    // generated

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LabelType getType() {
        return type;
    }

    public void setType(LabelType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "LabelDto{" +
                "name='" + name + '\'' +
                ", type=" + type +
                "} " + super.toString();
    }
}

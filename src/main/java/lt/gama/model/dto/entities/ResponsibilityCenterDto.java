package lt.gama.model.dto.entities;

import lt.gama.model.dto.base.BaseCompanyDto;

import java.io.Serial;

public class ResponsibilityCenterDto extends BaseCompanyDto {

    @Serial
    private static final long serialVersionUID = -1L;

    private String name;

    private String description;

    private ResponsibilityCenterDto parent;

    /**
     * length of the path to its root
     */
    private int depth;


    public ResponsibilityCenterDto() {
    }

    public ResponsibilityCenterDto(Long id) {
        setId(id);
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

    public ResponsibilityCenterDto getParent() {
        return parent;
    }

    public void setParent(ResponsibilityCenterDto parent) {
        this.parent = parent;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    @Override
    public String toString() {
        return "ResponsibilityCenterDto{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", parent=" + parent +
                ", depth=" + depth +
                "} " + super.toString();
    }
}

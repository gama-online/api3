package lt.gama.model.dto.entities;

import lt.gama.model.dto.base.BaseCompanyDto;
import lt.gama.model.dto.documents.items.PartFromDto;
import lt.gama.model.dto.documents.items.PartToDto;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RecipeDto extends BaseCompanyDto {

    @Serial
    private static final long serialVersionUID = -1L;

    private String name;

    private String description;

    private BigDecimal quantity;

    private List<PartFromDto> partsFrom = new ArrayList<>();

    private List<PartToDto> partsTo =  new ArrayList<>();

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

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public List<PartFromDto> getPartsFrom() {
        return partsFrom;
    }

    public void setPartsFrom(List<PartFromDto> partsFrom) {
        this.partsFrom = partsFrom;
    }

    public List<PartToDto> getPartsTo() {
        return partsTo;
    }

    public void setPartsTo(List<PartToDto> partsTo) {
        this.partsTo = partsTo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        RecipeDto recipeDto = (RecipeDto) o;
        return Objects.equals(name, recipeDto.name) && Objects.equals(description, recipeDto.description) && Objects.equals(quantity, recipeDto.quantity) && Objects.equals(partsFrom, recipeDto.partsFrom) && Objects.equals(partsTo, recipeDto.partsTo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, description, quantity, partsFrom, partsTo);
    }

    @Override
    public String toString() {
        return "RecipeDto{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", quantity=" + quantity +
                ", partsFrom=" + partsFrom +
                ", partsTo=" + partsTo +
                '}';
    }
}

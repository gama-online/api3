package lt.gama.model.type.l10n;

import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 2017-09-05.
 */
public class LangPart extends LangBase {

    private String name;

    private String description;

    private String unit;

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

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LangPart langPart = (LangPart) o;
        return Objects.equals(name, langPart.name) && Objects.equals(description, langPart.description) && Objects.equals(unit, langPart.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, description, unit);
    }

    @Override
    public String toString() {
        return "LangPart{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", unit='" + unit + '\'' +
                "} " + super.toString();
    }
}

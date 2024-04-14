package lt.gama.model.type.l10n;

import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 2018-03-16.
 */
public class LangEmployee extends LangBase {

    private String office;

    // generated

    public String getOffice() {
        return office;
    }

    public void setOffice(String office) {
        this.office = office;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        LangEmployee that = (LangEmployee) o;
        return Objects.equals(office, that.office);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), office);
    }

    @Override
    public String toString() {
        return "LangEmployee{" +
                "office='" + office + '\'' +
                "} " + super.toString();
    }
}

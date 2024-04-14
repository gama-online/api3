package lt.gama.model.type.auth;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * Gama
 * Created by valdas on 15-02-25.
 */
public class CurrencySettings implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    private String code;

    /**
     * Currency localized names
     */
    private Map<String, String> name;

    /**
     * small part names
     */
    private Map<String, String> small;

    // generated

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Map<String, String> getName() {
        return name;
    }

    public void setName(Map<String, String> name) {
        this.name = name;
    }

    public Map<String, String> getSmall() {
        return small;
    }

    public void setSmall(Map<String, String> small) {
        this.small = small;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CurrencySettings that = (CurrencySettings) o;
        return Objects.equals(code, that.code) && Objects.equals(name, that.name) && Objects.equals(small, that.small);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, name, small);
    }

    @Override
    public String toString() {
        return "CurrencySettings{" +
                "code='" + code + '\'' +
                ", name=" + name +
                ", small=" + small +
                '}';
    }
}

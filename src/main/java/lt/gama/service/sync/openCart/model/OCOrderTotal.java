package lt.gama.service.sync.openCart.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 11/11/2018.
 */
public class OCOrderTotal {

    /**
     * "sub_total"
     * "tax"
     * "shipping"
     * "total"
     */
    private String code;

    private String title;

    private BigDecimal value;

    // generated

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OCOrderTotal that = (OCOrderTotal) o;
        return Objects.equals(code, that.code) && Objects.equals(title, that.title) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, title, value);
    }

    @Override
    public String toString() {
        return "OCOrderTotal{" +
                "code='" + code + '\'' +
                ", title='" + title + '\'' +
                ", value=" + value +
                '}';
    }
}

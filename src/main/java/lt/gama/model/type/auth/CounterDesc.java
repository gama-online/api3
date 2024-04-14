package lt.gama.model.type.auth;

import lt.gama.helpers.EntityUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Gama
 * Created by valdas on 15-08-10.
 */
public class CounterDesc implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    private String label;

    private String prefix;

    private Integer start;

    private String format;


    protected CounterDesc() {
    }

    // fix for endpoint serialization bug/feature do not serialize map key with object with null values
    public CounterDesc(String label) {
        this.label = label;
    }

    public CounterDesc(Class<?> type) {
        this(EntityUtils.normalizeEntityClassName(type));
    }

    // generated

    public String getLabel() {
        return label;
    }


    public void setLabel(String label) {
        this.label = label;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CounterDesc that = (CounterDesc) o;
        return Objects.equals(label, that.label) && Objects.equals(prefix, that.prefix) && Objects.equals(start, that.start) && Objects.equals(format, that.format);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, prefix, start, format);
    }

    @Override
    public String toString() {
        return "CounterDesc{" +
                "label='" + label + '\'' +
                ", prefix='" + prefix + '\'' +
                ", start=" + start +
                ", format='" + format + '\'' +
                '}';
    }
}

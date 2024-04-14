package lt.gama.model.type.cf;

import lt.gama.model.type.enums.CFValueType;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Gama
 * Created by valdas on 15-06-29.
 */
public class CFDescription implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    private String key;

    private String label;

    private CFValueType type;

    private int order;

    // generated

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public CFValueType getType() {
        return type;
    }

    public void setType(CFValueType type) {
        this.type = type;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CFDescription that = (CFDescription) o;
        return order == that.order && Objects.equals(key, that.key) && Objects.equals(label, that.label) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, label, type, order);
    }

    @Override
    public String toString() {
        return "CFDescription{" +
                "key='" + key + '\'' +
                ", label='" + label + '\'' +
                ", type=" + type +
                ", order=" + order +
                '}';
    }
}

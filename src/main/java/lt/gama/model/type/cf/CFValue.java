package lt.gama.model.type.cf;

import lt.gama.helpers.StringHelper;
import lt.gama.model.type.enums.CFValueType;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Gama
 * Created by valdas on 15-06-29.
 */
public class CFValue implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    private String key;

    private CFValueType type;

    private String value;


    public CFValue() {
    }

    public CFValue(String key, CFValueType type, String value) {
        this.key = key;
        this.type = type;
        this.value = StringHelper.trimNormalize2null(value);
    }

    public void setValue(String value) {
        this.value = StringHelper.trimNormalize2null(value);
    }

    // generated

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public CFValueType getType() {
        return type;
    }

    public void setType(CFValueType type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CFValue cfValue = (CFValue) o;
        return Objects.equals(key, cfValue.key) && type == cfValue.type && Objects.equals(value, cfValue.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, type, value);
    }

    @Override
    public String toString() {
        return "CFValue{" +
                "key='" + key + '\'' +
                ", type=" + type +
                ", value='" + value + '\'' +
                '}';
    }
}

package lt.gama.model.type.cf;

import java.io.Serializable;
import java.util.Objects;

/**
 * Gama
 * Created by valdas on 15-06-29.
 */
public class CF implements Serializable {

    private String key;

    private CFValue value;

    // generated

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public CFValue getValue() {
        return value;
    }

    public void setValue(CFValue value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CF cf = (CF) o;
        return Objects.equals(key, cf.key) && Objects.equals(value, cf.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return "CF{" +
                "key='" + key + '\'' +
                ", value=" + value +
                '}';
    }
}

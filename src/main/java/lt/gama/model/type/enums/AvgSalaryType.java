package lt.gama.model.type.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;

/**
 * gama-online
 * Created by valdas on 2016-02-05.
 */
public enum AvgSalaryType implements Serializable {

    NONE("N"),
    ALL("A"),           // all amount goes to current month
    PROPORTIONAL("P");  // proportional amount depending on period length

    private final String value;

    AvgSalaryType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static AvgSalaryType from(String value) {
        if (value != null) {
            for (var t : values()) {
                if (t.value.equals(value)) {
                    return t;
                }
            }
            for (var t : values()) {
                if (t.name().equals(value)) {
                    return t;
                }
            }
        }
        return null;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }
}

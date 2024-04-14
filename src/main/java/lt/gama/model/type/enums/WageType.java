package lt.gama.model.type.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;

/**
 * gama-online
 * Created by valdas on 2016-04-05.
 */
public enum WageType implements Serializable {

    MONTHLY("M"), HOURLY("H"), FIXED("F"), OTHER("O");

    private final String value;

    WageType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static WageType from(String value) {
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

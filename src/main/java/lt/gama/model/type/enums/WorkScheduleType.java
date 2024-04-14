package lt.gama.model.type.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;

/**
 * gama-online
 * Created by valdas on 2016-04-04.
 */
public enum WorkScheduleType implements Serializable {

    WEEKLY("W"), PERIODIC("P");

    private final String value;

    WorkScheduleType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static WorkScheduleType from(String value) {
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

package lt.gama.model.type.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;

/**
 * gama-online
 * Created by valdas on 2016-12-18.
 */
public enum WorkTimeCodeType implements Serializable {

    WORK("W"),
    WEEKEND("S"),
    HOLIDAY("H"),   // state holidays
    VACATION("V"),  // employee vacation
    ILLNESS("L"),
    CHILDDAY("M");  // child day (like regular vacation)

    private final String value;

    WorkTimeCodeType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static WorkTimeCodeType from(String value) {
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

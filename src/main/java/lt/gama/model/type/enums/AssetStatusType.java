package lt.gama.model.type.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;

/**
 * gama-online
 * Created by valdas on 2015-10-28.
 */
public enum AssetStatusType implements Serializable {

    OPERATING("O"), CONSERVED("C"), WRITTEN_OFF("X");

    private final String value;

    AssetStatusType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static AssetStatusType from(String value) {
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

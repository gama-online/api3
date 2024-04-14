package lt.gama.model.type.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.*;

/**
 * Gama
 * Created by valdas on 15-03-27.
 */
@Schema(description =
        "* V - Vendor, \n" +
        "* C - Customer")
public enum DebtType {

    VENDOR("V"), CUSTOMER("C");

    private final String value;

    DebtType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static DebtType from(String value) {
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

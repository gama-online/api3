package lt.gama.model.type.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;

/**
 * Gama
 * Created by valdas on 15-04-14.
 */
public enum InventoryType implements Serializable {

    OPENING_BALANCE ("O"),
    INVOICE("S"),
    PURCHASE("B"),
    INVENTORY("I"),
    TRANSPORT("T"),
    PRODUCTION("P");

    private final String value;

    InventoryType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static InventoryType from(String value) {
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

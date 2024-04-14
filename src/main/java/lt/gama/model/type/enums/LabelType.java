package lt.gama.model.type.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum LabelType {

    INVOICE("Invoice"),
    PURCHASE("Purchase"),
    ASSET("Asset"),
    PART("Part"),
    EMPLOYEE("Employee"),
    COUNTERPARTY("Counterparty"),
    MANUFACTURER("Manufacturer");

    private final String value;

    LabelType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static LabelType from(String value) {
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

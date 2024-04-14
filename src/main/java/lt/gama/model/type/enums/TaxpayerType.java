package lt.gama.model.type.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

@Schema(description =
        """
        * L - Legal party,
        * P - Physical person,
        * F - Farmer""")
public enum TaxpayerType implements Serializable {

    LEGAL("L"),     // legal party
    PHYSICAL("P"),  // farmer
    FARMER("F");    // physical person

    private final String value;

    TaxpayerType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static TaxpayerType from(String value) {
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

package lt.gama.model.type.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.*;

import java.io.Serializable;

/**
 * Gama
 * Created by valdas on 15-04-08.
 */
@Schema(description =
        """
        * N - Service,
        * P - Product,
        * S - Product with serial number""")
public enum PartType implements Serializable {

    SERVICE("N"),
    PRODUCT("P"),
    PRODUCT_SN("S");

    private final String value;

    PartType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static PartType from(String value) {
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

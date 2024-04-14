package lt.gama.model.type.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;

/**
 * Gama
 * Created by valdas on 15-08-25.
 */
public enum EmployeeType implements Serializable {

    INTERNAL("I"),      // internal ('normal') employee
    ACCOUNTANT("A"),    // accountant from outside (from other company)
    API("P");           // Another program

    private final String value;

    EmployeeType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static EmployeeType from(String value) {
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

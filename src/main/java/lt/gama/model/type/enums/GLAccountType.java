package lt.gama.model.type.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;

/**
 * Gama
 * Created by valdas on 15-05-26.
 */
public enum GLAccountType implements Serializable {

    ASSETS("A"),        // 1 Fixed/Non-current assets
    CURRENT_A("C"),     // 2 Current/liquid assets
    EQUITY("Q"),        // 3
    LIABILITIES("L"),   // 4
    INCOME("I"),        // 5
    EXPENSES("X");      // 6 Expenses/Costs

    private final String value;

    GLAccountType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static GLAccountType from(String value) {
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

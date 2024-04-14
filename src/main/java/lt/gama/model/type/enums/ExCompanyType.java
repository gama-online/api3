package lt.gama.model.type.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;

/**
 * How company subscriptions in other company will be calculated:
 * <ul>
 *  <li>COMPANY - all connections in other (!) company where this company is a payer will be counted as single</li>
 *  <li>CONNECTION - each connection will be counted (default)</li>
 * </ul>
 * Created by valdas on 2018-06-17.
 */
public enum ExCompanyType implements Serializable {

    COMPANY("C"), CONNECTION("N");

    private final String value;

    ExCompanyType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static ExCompanyType from(String value) {
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

package lt.gama.model.type.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;

/**
 * Gama
 * Created by valdas on 15-05-11.
 * <br>
 * Account types:
 * <ul>
 *     <li>BANK - bank operation</li>
 *     <li>CASH - cash orders</li>
 *     <li>EMPLOYEE - advances</li>
 *     <li>BANK2 - bank transaction with my other bank account</li>
 * </ul>
 * Are used in {@link lt.gama.model.dto.base.BaseMoneyDocumentDto} and {@link lt.gama.model.sql.base.BaseMoneyDocumentSql}
 */
public enum AccountType implements Serializable {

    BANK("B"), CASH("C"), EMPLOYEE("E"), BANK2("2");

    private final String value;

    AccountType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static AccountType from(String value) {
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

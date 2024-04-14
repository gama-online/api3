package lt.gama.model.type.enums;

import java.io.Serializable;

/**
 * Gama
 * Created by valdas on 15-08-18.
 */
public enum EstimateType implements Serializable {

    SIMPLE("S"), PERIODIC("P");

    private final String value;

    EstimateType(String value) {
        this.value = value;
    }

    public static EstimateType from(String value) {
        if (value != null) {
            for (EstimateType t : values()) {
                if (t.value.equals(value)) {
                    return t;
                }
            }
        }
        return null;
     }

    @Override
    public String toString() {
        return value;
    }

}

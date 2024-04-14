package lt.gama.model.type.enums;

import java.io.Serializable;

/**
 * gama-online
 * Created by valdas on 2017-01-20.
 */
public enum ProcessingStatusType implements Serializable {

    RUNNING("R"), COMPLETED("C"), STOPPED("S"), ERROR("E");

    private final String value;

    ProcessingStatusType(String value) {
        this.value = value;
    }

    public static ProcessingStatusType from(String value) {
        if (value != null) {
            for (ProcessingStatusType t : values()) {
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

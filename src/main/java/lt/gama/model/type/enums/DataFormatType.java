package lt.gama.model.type.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DataFormatType {

    JSON,
    CSV,
    CSV_TAB;

    @SuppressWarnings("unused")
    @JsonCreator
    public static DataFormatType from(String value) {
        for (DataFormatType dataFormat : DataFormatType.values()) {
            if (dataFormat.toString().equalsIgnoreCase(value)) {
                return dataFormat;
            }
        }
        return null;
    }

    @JsonValue
    @Override
    public String toString() {
        return name().toLowerCase();
    }
}

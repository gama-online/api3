package lt.gama.model.type.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;

/**
 * gama-online
 * Created by valdas on 2015-10-28.
 */
public enum DepreciationType implements Serializable {

    LINE("L"),          // Straight-line: https://en.wikipedia.org/wiki/Depreciation#Straight-line_depreciation
    DOUBLE("D"),        // Double declining:
    //YEARS("Y"),         // Sum-of-years-digits: https://en.wikipedia.org/wiki/Depreciation#Sum-of-years-digits_method
    //PRODUCTION("P"),    // Units-of-production: https://en.wikipedia.org/wiki/Depreciation#Units-of-production_depreciation_method
    OTHER("X");         // Other method based on deprecation rate or amount

    private final String value;

    DepreciationType(String value) {
        this.value = value;
    }

    @JsonCreator
    public static DepreciationType from(String value) {
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

package lt.gama.jpa.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lt.gama.helpers.BooleanUtils;

@Converter(autoApply = true)
public class BooleanTypeConverter implements AttributeConverter<Boolean, Boolean> {
    @Override
    public Boolean convertToDatabaseColumn(Boolean attribute) {
        return BooleanUtils.isTrue(attribute);
    }

    @Override
    public Boolean convertToEntityAttribute(Boolean dbData) {
        return BooleanUtils.isTrue(dbData);
    }
}

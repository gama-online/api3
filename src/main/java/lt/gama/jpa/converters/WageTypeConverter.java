package lt.gama.jpa.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lt.gama.model.type.enums.WageType;

@Converter(autoApply = true)
public class WageTypeConverter implements AttributeConverter<WageType, String> {
    @Override
    public String convertToDatabaseColumn(WageType attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public WageType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : WageType.from(dbData);
    }
}

package lt.gama.jpa.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lt.gama.model.type.enums.PlType;

@Converter(autoApply = true)
public class PlTypeConverter implements AttributeConverter<PlType, String> {
    @Override
    public String convertToDatabaseColumn(PlType attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public PlType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : PlType.from(dbData);
    }
}

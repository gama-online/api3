package lt.gama.jpa.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lt.gama.model.type.enums.PartType;

@Converter(autoApply = true)
public class PartTypeConverter implements AttributeConverter<PartType, String> {
    @Override
    public String convertToDatabaseColumn(PartType attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public PartType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : PartType.from(dbData);
    }
}

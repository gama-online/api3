package lt.gama.jpa.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lt.gama.model.type.enums.LabelType;

@Converter(autoApply = true)
public class LabelTypeConverter implements AttributeConverter<LabelType, String> {
    @Override
    public String convertToDatabaseColumn(LabelType attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public LabelType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : LabelType.from(dbData);
    }
}

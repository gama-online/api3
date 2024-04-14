package lt.gama.jpa.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lt.gama.model.type.enums.SexType;

@Converter(autoApply = true)
public class SexTypeConverter implements AttributeConverter<SexType, String> {
    @Override
    public String convertToDatabaseColumn(SexType attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public SexType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : SexType.from(dbData);
    }
}

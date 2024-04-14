package lt.gama.jpa.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lt.gama.model.type.enums.DepreciationType;

@Converter(autoApply = true)
public class DepreciationTypeConverter implements AttributeConverter<DepreciationType, String> {
    @Override
    public String convertToDatabaseColumn(DepreciationType attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public DepreciationType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : DepreciationType.from(dbData);
    }
}

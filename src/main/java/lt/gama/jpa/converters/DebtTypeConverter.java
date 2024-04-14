package lt.gama.jpa.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lt.gama.model.type.enums.DebtType;

@Converter(autoApply = true)
public class DebtTypeConverter implements AttributeConverter<DebtType, String> {
    @Override
    public String convertToDatabaseColumn(DebtType attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public DebtType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : DebtType.from(dbData);
    }
}

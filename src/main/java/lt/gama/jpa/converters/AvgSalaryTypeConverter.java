package lt.gama.jpa.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lt.gama.model.type.enums.AvgSalaryType;

@Converter(autoApply = true)
public class AvgSalaryTypeConverter implements AttributeConverter<AvgSalaryType, String> {
    @Override
    public String convertToDatabaseColumn(AvgSalaryType attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public AvgSalaryType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : AvgSalaryType.from(dbData);
    }
}

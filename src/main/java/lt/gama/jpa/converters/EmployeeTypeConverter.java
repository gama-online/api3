package lt.gama.jpa.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lt.gama.model.type.enums.EmployeeType;

@Converter(autoApply = true)
public class EmployeeTypeConverter implements AttributeConverter<EmployeeType, String> {
    @Override
    public String convertToDatabaseColumn(EmployeeType attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public EmployeeType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : EmployeeType.from(dbData);
    }
}

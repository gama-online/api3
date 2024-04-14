package lt.gama.jpa.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lt.gama.model.type.enums.CompanyStatusType;

@Converter(autoApply = true)
public class CompanyStatusTypeConverter implements AttributeConverter<CompanyStatusType, String> {
    @Override
    public String convertToDatabaseColumn(CompanyStatusType attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public CompanyStatusType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : CompanyStatusType.from(dbData);
    }
}

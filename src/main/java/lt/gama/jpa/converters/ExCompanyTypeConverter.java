package lt.gama.jpa.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lt.gama.model.type.enums.ExCompanyType;

@Converter(autoApply = true)
public class ExCompanyTypeConverter implements AttributeConverter<ExCompanyType, String> {
    @Override
    public String convertToDatabaseColumn(ExCompanyType attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public ExCompanyType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : ExCompanyType.from(dbData);
    }
}

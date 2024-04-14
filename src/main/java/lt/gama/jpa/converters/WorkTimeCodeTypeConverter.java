package lt.gama.jpa.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lt.gama.model.type.enums.WorkTimeCodeType;

@Converter(autoApply = true)
public class WorkTimeCodeTypeConverter implements AttributeConverter<WorkTimeCodeType, String> {
    @Override
    public String convertToDatabaseColumn(WorkTimeCodeType attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public WorkTimeCodeType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : WorkTimeCodeType.from(dbData);
    }
}

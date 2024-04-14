package lt.gama.jpa.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lt.gama.model.type.enums.WorkScheduleType;

@Converter(autoApply = true)
public class WorkScheduleTypeConverter implements AttributeConverter<WorkScheduleType, String> {
    @Override
    public String convertToDatabaseColumn(WorkScheduleType attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public WorkScheduleType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : WorkScheduleType.from(dbData);
    }
}

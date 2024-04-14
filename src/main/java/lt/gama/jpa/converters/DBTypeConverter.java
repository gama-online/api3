package lt.gama.jpa.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lt.gama.model.type.enums.DBType;

@Converter(autoApply = true)
public class DBTypeConverter implements AttributeConverter<DBType, String> {
    @Override
    public String convertToDatabaseColumn(DBType attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public DBType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : DBType.from(dbData);
    }
}

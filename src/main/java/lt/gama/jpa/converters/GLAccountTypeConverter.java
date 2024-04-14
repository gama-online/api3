package lt.gama.jpa.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lt.gama.model.type.enums.GLAccountType;

@Converter(autoApply = true)
public class GLAccountTypeConverter implements AttributeConverter<GLAccountType, String> {
    @Override
    public String convertToDatabaseColumn(GLAccountType attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public GLAccountType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : GLAccountType.from(dbData);
    }
}

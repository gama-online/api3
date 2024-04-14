package lt.gama.jpa.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lt.gama.model.type.enums.AssetStatusType;

@Converter(autoApply = true)
public class AssetStatusTypeConverter implements AttributeConverter<AssetStatusType, String> {
    @Override
    public String convertToDatabaseColumn(AssetStatusType attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public AssetStatusType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : AssetStatusType.from(dbData);
    }
}

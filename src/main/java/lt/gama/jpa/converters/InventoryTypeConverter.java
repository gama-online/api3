package lt.gama.jpa.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lt.gama.model.type.enums.InventoryType;


@Converter(autoApply = true)
public class InventoryTypeConverter implements AttributeConverter<InventoryType, String> {
    @Override
    public String convertToDatabaseColumn(InventoryType attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public InventoryType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : InventoryType.from(dbData);
    }
}

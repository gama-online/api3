package lt.gama.jpa.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lt.gama.model.type.enums.TaxpayerType;

@Converter(autoApply = true)
public class TaxpayerTypeConverter implements AttributeConverter<TaxpayerType, String> {
    @Override
    public String convertToDatabaseColumn(TaxpayerType attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public TaxpayerType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : TaxpayerType.from(dbData);
    }
}

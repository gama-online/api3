package lt.gama.jpa.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lt.gama.model.type.enums.AccountType;

@Converter(autoApply = true)
public class AccountTypeConverter implements AttributeConverter<AccountType, String> {
    @Override
    public String convertToDatabaseColumn(AccountType attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public AccountType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : AccountType.from(dbData);
    }
}

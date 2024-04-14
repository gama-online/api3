package lt.gama.jpa;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

/**
 * Transform entity fields names into SQL table field names.
 * for example:
 * <br> camelCaseToSnakeCase -> camel_case_to_snake_case</li>
 * <br> nameWithTWoORMoreCapitalLetters -> name_with_two_or_more_capital_letters
 */
public class CamelCaseToSnakeCaseNamingStrategy extends PhysicalNamingStrategyStandardImpl {

    public static final String CAMEL_CASE_REGEX = "(?<a1>)(?<a2>[A-Z][a-z])|((?<b1>[a-z])(?<b2>[A-Z]))";

    public static final String SNAKE_CASE_PATTERN = "${a1}${b1}\\_${a2}${b2}";

    @Override
    public Identifier toPhysicalCatalogName(Identifier name, JdbcEnvironment context) {
        return formatIdentifier(super.toPhysicalCatalogName(name, context));
    }

    @Override
    public Identifier toPhysicalSchemaName(Identifier name, JdbcEnvironment context) {
        return formatIdentifier(super.toPhysicalSchemaName(name, context));
    }

    @Override
    public Identifier toPhysicalTableName(Identifier name, JdbcEnvironment context) {
        return formatIdentifier(super.toPhysicalTableName(name, context));
    }

    @Override
    public Identifier toPhysicalSequenceName(Identifier name, JdbcEnvironment context) {
        return formatIdentifier(super.toPhysicalSequenceName(name, context));
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier name, JdbcEnvironment context) {
        return formatIdentifier(super.toPhysicalColumnName(name, context));
    }

    private Identifier formatIdentifier(Identifier identifier) {
        if (identifier != null) {
            String name = identifier.getText();

            String formattedName = name.replaceAll(CAMEL_CASE_REGEX, SNAKE_CASE_PATTERN).toLowerCase();

            return !formattedName.equals(name)
                    ? Identifier.toIdentifier(formattedName, identifier.isQuoted())
                    : identifier;
        } else {
            return null;
        }
    }
}

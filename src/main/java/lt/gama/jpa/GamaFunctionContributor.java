package lt.gama.jpa;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StandardBasicTypes;

import static lt.gama.jpa.GamaPostgreSQLDialect.*;

public class GamaFunctionContributor implements org.hibernate.boot.model.FunctionContributor {

    @Override
    public void contributeFunctions(FunctionContributions functionContributions) {
        var basicTypeRegistry = functionContributions.getTypeConfiguration().getBasicTypeRegistry();
        var booleanType = basicTypeRegistry.resolve(StandardBasicTypes.BOOLEAN);

        functionContributions.getFunctionRegistry().registerPattern(JSONB_CONTAINS, "?1 @> ?2::jsonb", booleanType);
        functionContributions.getFunctionRegistry().registerPattern(JSONB_EXISTS, "?1 @> to_jsonb(?2::text)", booleanType);
        functionContributions.getFunctionRegistry().registerPattern(JSONB_ADD_TEXT, "?1 || to_jsonb(?2::text)");
        functionContributions.getFunctionRegistry().registerPattern(JSONB_REMOVE_KEY, "?1 - ?2");

        functionContributions.getFunctionRegistry().register("jsonb_path_exists", new StandardSQLFunction("jsonb_path_exists", StandardBasicTypes.BOOLEAN));
        functionContributions.getFunctionRegistry().register("regexp_replace", new StandardSQLFunction("regexp_replace", StandardBasicTypes.STRING));
        functionContributions.getFunctionRegistry().register("unaccent", new StandardSQLFunction("unaccent", StandardBasicTypes.STRING));
    }
}

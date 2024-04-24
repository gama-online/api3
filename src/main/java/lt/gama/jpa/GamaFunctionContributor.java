package lt.gama.jpa;

import lt.gama.jpa.functions.JsonBinaryContainsFunction;
import lt.gama.jpa.functions.JsonBinaryExistsFunction;
import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StandardBasicTypes;

import static lt.gama.jpa.GamaPostgreSQLDialect.*;

public class GamaFunctionContributor implements org.hibernate.boot.model.FunctionContributor {

    @Override
    public void contributeFunctions(FunctionContributions functionContributions) {
        var functionRegistry = functionContributions.getFunctionRegistry();

        functionRegistry.registerPattern(JSONB_ADD_TEXT, "?1 || to_jsonb(?2::text)");
        functionRegistry.registerPattern(JSONB_REMOVE_KEY, "?1 - ?2");

        functionRegistry.register(JSONB_CONTAINS, new JsonBinaryContainsFunction(JSONB_CONTAINS));
        functionRegistry.register(JSONB_EXISTS, new JsonBinaryExistsFunction(JSONB_EXISTS));

        functionRegistry.register("jsonb_path_exists", new StandardSQLFunction("jsonb_path_exists", StandardBasicTypes.BOOLEAN));
        functionRegistry.register("regexp_replace", new StandardSQLFunction("regexp_replace", StandardBasicTypes.STRING));
        functionRegistry.register("unaccent", new StandardSQLFunction("unaccent", StandardBasicTypes.STRING));
    }
}

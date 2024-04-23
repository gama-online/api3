package lt.gama.jpa;

import lt.gama.jpa.functions.JsonBinaryContainsFunction;
import lt.gama.jpa.functions.JsonBinaryExistsFunction;
import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;

import static lt.gama.jpa.GamaPostgreSQLDialect.JSONB_CONTAINS;
import static lt.gama.jpa.GamaPostgreSQLDialect.JSONB_EXISTS;

public class CustomFunctionsContributor implements FunctionContributor {
    @Override
    public void contributeFunctions(FunctionContributions functionContributions) {
        var reg = functionContributions.getFunctionRegistry();
        reg.register(JSONB_CONTAINS, new JsonBinaryContainsFunction(JSONB_CONTAINS));
        reg.register(JSONB_EXISTS, new JsonBinaryExistsFunction(JSONB_EXISTS));
    }
}

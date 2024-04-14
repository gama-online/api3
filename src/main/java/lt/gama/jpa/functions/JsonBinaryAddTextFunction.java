package lt.gama.jpa.functions;

//public class JsonBinaryAddTextFunction implements SqlFunction {
//    @Override
//    public boolean hasArguments() {
//        return true;
//    }
//
//    @Override
//    public boolean hasParenthesesIfNoArguments() {
//        return false;
//    }
//
//    @Override
//    public Type getReturnType(Type type, Mapping mapping) throws QueryException {
//        return new JsonBinaryType();
//    }
//
//    @Override
//    public String render(Type type, List args, SessionFactoryImplementor sessionFactoryImplementor) throws QueryException {
//        String jsonb = (String) args.get(0);
//        String value = (String) args.get(1);
//        return String.format("%s || to_jsonb(%s::text)", jsonb, value);
//    }
//}


import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StandardBasicTypes;

import static lt.gama.jpa.GamaPostgreSQLDialect.JSONB_ADD_TEXT;

public class JsonBinaryAddTextFunction extends StandardSQLFunction {
    public JsonBinaryAddTextFunction() {
        super(JSONB_ADD_TEXT, StandardBasicTypes.STRING); // Set the function name and return type
    }

}
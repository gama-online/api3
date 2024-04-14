package lt.gama.jpa.functions;//package lt.gama.jpa.functions;
//
//import org.hibernate.QueryException;
//import org.hibernate.dialect.function.SQLFunction;
//import org.hibernate.engine.spi.Mapping;
//import org.hibernate.engine.spi.SessionFactoryImplementor;
//import org.hibernate.type.BooleanType;
//import org.hibernate.type.Type;
//
//import java.util.List;
//
//public class JsonBinaryExistsFunction implements SQLFunction {
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
//        return new BooleanType();
//    }
//
//    @Override
//    public String render(Type type, List args, SessionFactoryImplementor sessionFactoryImplementor) throws QueryException {
//        String jsonb = (String) args.get(0);
//        String value = (String) args.get(1);
//        return String.format("%s ?? %s", jsonb, value);
//    }
//}

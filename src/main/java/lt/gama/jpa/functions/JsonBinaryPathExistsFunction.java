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
//public class JsonBinaryPathExistsFunction implements SQLFunction {
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
//    public Type getReturnType(Type firstArgumentType, Mapping mapping) throws QueryException {
//        return new BooleanType();
//    }
//
//    @Override
//    public String render(Type firstArgumentType, List arguments, SessionFactoryImplementor factory) throws QueryException {
//        String field = (String) arguments.get(0);
//        String path = (String) arguments.get(1);
//
//        if(arguments.size() > 3) {
//            String vars = (String) arguments.get(2);
//            String silent = (String) arguments.get(3);
//            return String.format("jsonb_path_exists(%s::jsonb, %s::jsonpath, %s::jsonb, %s)", field, path, vars, silent);
//        } else if(arguments.size() > 2) {
//            String vars = (String) arguments.get(2);
//            return String.format("jsonb_path_exists(%s::jsonb, %s::jsonpath, %s::jsonb)", field, path, vars);
//        }
//
//        return String.format("jsonb_path_exists(%s::jsonb, %s::jsonpath)", field, path);
//    }
//}

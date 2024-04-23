package lt.gama.jpa.functions;
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
//public class JsonBinaryContainsFunction implements SQLFunction {
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
//        String field = (String) args.get(0);
//        String value = (String) args.get(1);
//        return String.format("%s @> %s::jsonb", field, value);
//    }
//}

import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.query.ReturnableType;
import org.hibernate.sql.ast.SqlAstTranslator;
import org.hibernate.sql.ast.spi.SqlAppender;
import org.hibernate.sql.ast.tree.SqlAstNode;
import org.hibernate.type.BasicTypeReference;
import org.hibernate.type.SqlTypes;

import java.util.List;

import static lt.gama.jpa.GamaPostgreSQLDialect.JSONB_CONTAINS;

public class JsonBinaryContainsFunction extends StandardSQLFunction {

    private static final BasicTypeReference<Boolean> RETURN_TYPE = new BasicTypeReference<>("boolean", Boolean.class, SqlTypes.BOOLEAN);

    public JsonBinaryContainsFunction(String name) {
        super(name, RETURN_TYPE);
    }

    @Override
    public void render(SqlAppender sqlAppender, List<? extends SqlAstNode> sqlAstArguments, ReturnableType<?> returnType, SqlAstTranslator<?> translator) {
        if (sqlAstArguments.size() != 2) {
            throw new IllegalArgumentException("Function " + JSONB_CONTAINS + " requires exactly 2 arguments");
        }
        sqlAstArguments.get(0).accept(translator);
        sqlAppender.append(" @> ");
        sqlAstArguments.get(1).accept(translator);
        sqlAppender.append("::jsonb");
    }
}

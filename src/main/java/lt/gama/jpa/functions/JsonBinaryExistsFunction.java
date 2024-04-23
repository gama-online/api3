package lt.gama.jpa.functions;

import org.hibernate.QueryException;
import org.hibernate.dialect.function.SqlFunction;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.query.ReturnableType;
import org.hibernate.sql.ast.SqlAstTranslator;
import org.hibernate.sql.ast.spi.SqlAppender;
import org.hibernate.sql.ast.tree.SqlAstNode;
import org.hibernate.type.BasicTypeReference;
import org.hibernate.type.SqlTypes;

import java.util.List;

import static lt.gama.jpa.GamaPostgreSQLDialect.JSONB_EXISTS;

public class JsonBinaryExistsFunction extends StandardSQLFunction {

    private static final BasicTypeReference<Boolean> RETURN_TYPE = new BasicTypeReference<>("boolean", Boolean.class, SqlTypes.BOOLEAN);

    public JsonBinaryExistsFunction(String name) {
        super(name, RETURN_TYPE);
    }

    @Override
    public void render(SqlAppender sqlAppender, List<? extends SqlAstNode> sqlAstArguments, ReturnableType<?> returnType, SqlAstTranslator<?> translator) {
        if (sqlAstArguments.size() != 2) {
            throw new IllegalArgumentException("Function " + JSONB_EXISTS + " requires exactly 2 arguments");
        }
        sqlAstArguments.get(0).accept(translator);
        sqlAppender.append(" ?? ");
        sqlAstArguments.get(1).accept(translator);
    }
}

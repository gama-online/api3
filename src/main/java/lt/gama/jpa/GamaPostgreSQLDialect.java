package lt.gama.jpa;

import org.hibernate.dialect.DatabaseVersion;
import org.hibernate.dialect.PostgreSQLDialect;

public class GamaPostgreSQLDialect extends PostgreSQLDialect {

    // operators as functions
    public static final String JSONB_CONTAINS = "gama_jsonb_contains";
    public static final String JSONB_EXISTS = "gama_jsonb_exists";
    public static final String JSONB_REMOVE_KEY = "gama_jsonb_remove_key";
    public static final String JSONB_ADD_TEXT = "gama_jsonb_add_text";

    public GamaPostgreSQLDialect() {
        super(DatabaseVersion.make(13));
    }
}

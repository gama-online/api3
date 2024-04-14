package lt.gama;

public enum AppProp {

    GAMA_VERSION("gama.version"),
    GAMA_INIT_LOGIN("gama.init.login"),
    GAMA_INIT_PASSWORD("gama.init.password"),
    GAMA_JDBC_DRIVER("gama.jdbc.driver"),
    GAMA_JDBC_URL("gama.jdbc.url"),
    GAMA_JDBC_USERNAME("gama.jdbc.username"),
    GAMA_JDBC_PASSWORD("gama.jdbc.password"),
    GAMA_DB_VERSION("gama.db.version"),
    GAMA_TOKEN_SECRET_KEY("gama.token.secret-key"),
    GAMA_TOKEN_REFRESH_EXPIRE("gama.token.refresh-expire"),
    GAMA_TOKEN_ACCESS_EXPIRE("gama.token.access-expire"),
    GAMA_TOKEN_RESET_EXPIRE("gama.token.reset-expire");

    private final String value;

    AppProp(String value) {
        this.value = value;
    }
}

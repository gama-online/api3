package lt.gama.test.base;

import java.sql.Connection;
import java.sql.DriverManager;

public class GamaConnectionFactory {

    private final String driver;
    private final String url;
    private final String username;
    private final String password;

    public GamaConnectionFactory(String driver, String url, String username, String password) {
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public Connection get(User user) {
        try {
            Class.forName(driver != null ? driver : org.postgresql.Driver.class.getName());
            return DriverManager.getConnection(
                    url,
                    user != null ? user.username() : username,
                    user != null ? user.username() : password);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Connection get() {
        return get(null);
    }
}

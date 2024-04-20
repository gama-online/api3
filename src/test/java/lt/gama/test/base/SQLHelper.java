package lt.gama.test.base;

import lt.gama.helpers.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class SQLHelper {

    private static final Logger log = LoggerFactory.getLogger(SQLHelper.class);

    private SQLHelper() {}

    private static final Map<String, String> cache = new HashMap<>();

    public static void executeSqlScriptFromAsAdmin(java.sql.Connection connection, String filename, String... params) {
        executeSqlScriptFromAsUser(connection, null, filename, params);
    }

    public static void executeSqlScriptFromAsUser(java.sql.Connection connection, String user, String filename, String... params) {
        long start = System.currentTimeMillis();

        try (Statement statement = connection.createStatement()) {
            String scripts = cache.get(filename);
            if (scripts == null) {
                scripts = loadScriptFrom(filename);
                cache.put(filename, scripts);
            }
            scripts = params.length == 0 ? scripts : MessageFormat.format(scripts, (Object[]) params);
            statement.execute(scripts);

        } catch (Exception e) {
            log.error("SQLHelper: " + e.getMessage(), e);

        } finally {
            log.info(MessageFormat.format("Executed script as user {0} from file {1} with parameters {3} in {2}ms",
                    user != null ? user : "-std-",
                    filename,
                    System.currentTimeMillis() - start,
                    params.length == 0 ? "-none-" : Arrays.stream(params).toList()));
        }
    }

    public static String loadScriptFrom(String filename) throws IOException {
        try (InputStream is = SQLHelper.class.getClassLoader().getResourceAsStream(filename);
             BufferedReader in = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            StringBuilder sb = new StringBuilder();
            String line;
            int commentIndex;
            while ((line = in.readLine()) != null) {
                if ((commentIndex = line.indexOf("--")) >= 0) {
                    line = line.substring(0, commentIndex);
                }
                if (StringHelper.hasValue(line)) {
                    sb.append(line).append('\n');
                }
            }
            return sb.toString();
        }
    }
}

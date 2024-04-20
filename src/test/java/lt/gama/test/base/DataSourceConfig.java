package lt.gama.test.base;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.apache.commons.lang3.RandomStringUtils;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    @Value("${gama.jdbc.driver}") private String driver;
    @Value("${gama.jdbc.url}") private String url;
    @Value("${gama.jdbc.username}") private String username;
    @Value("${gama.jdbc.password}") private String password;
    @Value("${gama.db.version}") private String dbVersion;

    @Bean
    public User user() {
        return new User("user_" + RandomStringUtils.randomNumeric(3) + "_" + RandomStringUtils.randomNumeric(3));
    }

    @Bean
    public GamaConnectionFactory gamaConnectionFactory() {
        return new GamaConnectionFactory(driver, url, username, password);
    }

    @Bean
    public DataSource dataSource() {
        var dataSource = new PGSimpleDataSource();
        dataSource.setServerNames(new String[] {"localhost"});
        dataSource.setPortNumbers(new int[] {5432});
        dataSource.setDatabaseName("gama-test");
        dataSource.setUser(user().username());
        dataSource.setPassword(user().username());
        dataSource.setCurrentSchema(user().username());
        return dataSource;
    }

    @Bean
    public EntityManagerFactory entityManagerFactory() {
        // create DB user and data
        SQLHelper.executeSqlScriptFromAsAdmin(gamaConnectionFactory().get(), "sql/create-user.sql", user().username());
        SQLHelper.executeSqlScriptFromAsUser(gamaConnectionFactory().get(user()), user().username(), "sql/" + dbVersion + ".sql");

        return Persistence.createEntityManagerFactory("test-lt.gama.persistence-unit", Map.of(
                "hibernate.dialect", lt.gama.jpa.GamaPostgreSQLDialect.class.getName(),
                "hibernate.default_schema", user().username(),
                "hibernate.hbm2ddl.auto", "none", // none, create, create-drop, validate, update
                "hibernate.globally_quoted_identifiers", "true",
                "hibernate.globally_quoted_identifiers_skip_column_definitions", "true",
                "hibernate.hbm2ddl.schema_filter_provider", lt.gama.jpa.GamaSchemaFilterProvider.class.getName(),
                "hibernate.naming.physical-strategy", lt.gama.jpa.CamelCaseToSnakeCaseNamingStrategy.class.getName(),
                "hibernate.naming.implicit-strategy", lt.gama.jpa.ImplicitNamingStrategyComponentPath.class.getName()
                ));
    }
}

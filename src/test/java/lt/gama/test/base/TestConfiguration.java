package lt.gama.test.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import lt.gama.service.repo.base.InCompanyRepository;
import lt.gama.service.repo.base.InCompanyRepositoryFactoryBean;
import org.apache.commons.lang3.RandomStringUtils;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.type.format.jackson.JacksonJsonFormatMapper;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
@EnableJpaRepositories(repositoryBaseClass = InCompanyRepository.class, repositoryFactoryBeanClass= InCompanyRepositoryFactoryBean.class)
@EnableTransactionManagement
public class TestConfiguration {

    @Value("${gama.jdbc.driver}") private String driver;
    @Value("${gama.jdbc.url}") private String url;
    @Value("${gama.jdbc.username}") private String username;
    @Value("${gama.jdbc.password}") private String password;
    @Value("${gama.db.version}") private String dbVersion;

    @Autowired
    private ObjectMapper objectMapper;

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
                AvailableSettings.DIALECT, lt.gama.jpa.GamaPostgreSQLDialect.class.getName(),
                AvailableSettings.DEFAULT_SCHEMA, user().username(),
                AvailableSettings.HBM2DDL_AUTO, "none", // none, create, create-drop, validate, update
                AvailableSettings.GLOBALLY_QUOTED_IDENTIFIERS, "true",
                AvailableSettings.GLOBALLY_QUOTED_IDENTIFIERS_SKIP_COLUMN_DEFINITIONS, "true",
                AvailableSettings.HBM2DDL_FILTER_PROVIDER, lt.gama.jpa.GamaSchemaFilterProvider.class.getName(),
                AvailableSettings.PHYSICAL_NAMING_STRATEGY, lt.gama.jpa.CamelCaseToSnakeCaseNamingStrategy.class.getName(),
                AvailableSettings.IMPLICIT_NAMING_STRATEGY, lt.gama.jpa.ImplicitNamingStrategyComponentPath.class.getName(),
                AvailableSettings.JSON_FORMAT_MAPPER, new JacksonJsonFormatMapper(objectMapper)));
    }
}

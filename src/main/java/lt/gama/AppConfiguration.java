package lt.gama;

import com.fasterxml.jackson.databind.ObjectMapper;
import lt.gama.auth.impl.Auth;
import lt.gama.service.repo.base.InCompanyRepository;
import lt.gama.service.repo.base.InCompanyRepositoryFactoryBean;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.type.format.jackson.JacksonJsonFormatMapper;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.context.annotation.RequestScope;


@Configuration
@ImportRuntimeHints({AppRuntimeHints.class})
@EnableJpaRepositories(repositoryBaseClass = InCompanyRepository.class, repositoryFactoryBeanClass= InCompanyRepositoryFactoryBean.class)
@EnableTransactionManagement
public class AppConfiguration {

    @Bean
    public HibernatePropertiesCustomizer jsonFormatMapperCustomizer(ObjectMapper objectMapper) {
        return properties -> properties.put(AvailableSettings.JSON_FORMAT_MAPPER,
                new JacksonJsonFormatMapper(objectMapper));
    }

    @Bean
    @RequestScope
    public Auth auth() {
        return new Auth();
    }
}

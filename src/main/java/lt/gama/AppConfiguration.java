package lt.gama;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lt.gama.service.json.module.GamaMoneyModule;
import lt.gama.service.json.module.LocalDateTimeModule;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.type.format.jackson.JacksonJsonFormatMapper;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.context.annotation.Primary;

import static com.fasterxml.jackson.core.JsonGenerator.Feature.*;

@Configuration
@ImportRuntimeHints({AppRuntimeHints.class})
public class AppConfiguration {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)

                .enable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)

                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)

                .configure(ESCAPE_NON_ASCII, true)
                .configure(AUTO_CLOSE_TARGET, false)
                .configure(FLUSH_PASSED_TO_STREAM, false)

                .registerModule(new LocalDateTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS , false)

                .registerModule(new GamaMoneyModule());
    }

    @Bean
    HibernatePropertiesCustomizer jsonFormatMapperCustomizer(ObjectMapper objectMapper) {
        return (properties) -> properties.put(AvailableSettings.JSON_FORMAT_MAPPER,
                new JacksonJsonFormatMapper(objectMapper));
    }
}

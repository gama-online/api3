package lt.gama.service.json.module;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lt.gama.service.json.deser.DateDeserializer;
import lt.gama.service.json.deser.InstantDeserializer;
import lt.gama.service.json.deser.LocalDateDeserializer;
import lt.gama.service.json.deser.LocalDateTimeDeserializer;
import lt.gama.service.json.ser.DateSerializer;
import lt.gama.service.json.ser.InstantSerializer;
import lt.gama.service.json.ser.LocalDateSerializer;
import lt.gama.service.json.ser.LocalDateTimeSerializer;

import java.io.Serial;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * gama-online
 * Created by valdas on 2018-07-31.
 */
public class LocalDateTimeModule extends SimpleModule {

    @Serial
    private static final long serialVersionUID = -4213615132678061845L;

    public LocalDateTimeModule() {
        super("Date", Version.unknownVersion());

        this.addDeserializer(LocalDate.class, new LocalDateDeserializer());
        this.addSerializer(LocalDate.class, new LocalDateSerializer());

        this.addDeserializer(Date.class, new DateDeserializer());
        this.addSerializer(Date.class, new DateSerializer());

        this.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
        this.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());

        this.addDeserializer(Instant.class, new InstantDeserializer());
        this.addSerializer(Instant.class, new InstantSerializer());

    }
}
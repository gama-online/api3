package lt.gama.service.json.ser;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class InstantSerializer extends JsonSerializer<Instant> {

    @Override
    public void serialize(Instant value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeString(LocalDateTime.ofInstant(value, ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z");
    }
}

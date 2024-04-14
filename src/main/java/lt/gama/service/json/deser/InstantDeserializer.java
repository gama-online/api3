package lt.gama.service.json.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lt.gama.helpers.DateUtils;
import lt.gama.helpers.StringHelper;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;

public class InstantDeserializer extends JsonDeserializer<Instant> {

    @Override
    public Instant deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        String value = jp.getText();
        if (StringHelper.isEmpty(value)) return null;
        return DateUtils.parseLocalDateTime(value).atZone(ZoneOffset.UTC).toInstant();
    }
}

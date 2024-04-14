package lt.gama.service.json.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lt.gama.helpers.DateUtils;
import lt.gama.helpers.StringHelper;

import java.io.IOException;
import java.time.LocalDateTime;

public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    @Override
    public LocalDateTime deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        String value = jp.getText();
        if (StringHelper.isEmpty(value)) return null;
        return DateUtils.parseLocalDateTime(value);
    }
}

package lt.gama.service.json.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * gama-online
 * Created by valdas on 2017-10-12.
 */
public class NumericBooleanDeserializer extends JsonDeserializer<Boolean> {

    @Override
    public Boolean deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        return !"0".equals(parser.getText());
    }
}
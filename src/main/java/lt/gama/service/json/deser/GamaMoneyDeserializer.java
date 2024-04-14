package lt.gama.service.json.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import lt.gama.model.type.GamaMoney;

import java.io.IOException;
import java.io.Serial;
import java.math.BigDecimal;
import java.util.Collections;

public class GamaMoneyDeserializer extends StdDeserializer<GamaMoney> {

    @Serial
    private static final long serialVersionUID = 4104533405465543535L;

    public GamaMoneyDeserializer() {
        super(GamaMoney.class);
    }

    @Override
    public GamaMoney deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        if (jp.currentToken() == JsonToken.VALUE_STRING) {
            String moneyStr = jp.getValueAsString();
            return GamaMoney.parse(moneyStr);
        }

        BigDecimal amount = null;
        String currency = null;

        if (jp.isExpectedStartObjectToken()) {
            jp.nextToken();
        }

        for (; jp.currentToken() == JsonToken.FIELD_NAME; jp.nextToken()) {
            final String field = jp.currentName();

            jp.nextToken();

            if ("amount".equals(field)) {
                String amountStr = jp.getValueAsString();
                if (amountStr != null)  amount = new BigDecimal(amountStr);
            } else if ("currency".equals(field)) {
                currency = jp.getValueAsString();
            } else if (ctxt.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)) {
                throw UnrecognizedPropertyException.from(jp, GamaMoney.class, field, Collections.singletonList("amount, currency")
                );
            } else {
                jp.skipChildren();
            }
        }

        return GamaMoney.ofNullable(currency, amount);

    }

    @Override
    public Object deserializeWithType(JsonParser jp, DeserializationContext ctxt,
                                      TypeDeserializer typeDeserializer) throws IOException {
        // In future could check current token... for now this should be enough:
        return typeDeserializer.deserializeTypedFromObject(jp, ctxt);
    }
}

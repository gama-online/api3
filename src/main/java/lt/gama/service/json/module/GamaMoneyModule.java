package lt.gama.service.json.module;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lt.gama.model.type.GamaBigMoney;
import lt.gama.model.type.GamaMoney;
import lt.gama.service.json.deser.GamaBigMoneyDeserializer;
import lt.gama.service.json.deser.GamaMoneyDeserializer;

import java.io.Serial;

public class GamaMoneyModule extends SimpleModule {

    @Serial
    private static final long serialVersionUID = -3052986648153993983L;

    public GamaMoneyModule() {
        super("GamaMoney", Version.unknownVersion());

        this.addDeserializer(GamaMoney.class, new GamaMoneyDeserializer());
        this.addDeserializer(GamaBigMoney.class, new GamaBigMoneyDeserializer());
    }
}

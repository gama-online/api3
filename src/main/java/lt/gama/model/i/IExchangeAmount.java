package lt.gama.model.i;

import lt.gama.model.type.Exchange;
import lt.gama.model.type.GamaMoney;

public interface IExchangeAmount {

    void setExchange(Exchange exchange);

    Exchange getExchange();

    void setAmount(GamaMoney amount);

    GamaMoney getAmount();

    void setBaseAmount(GamaMoney amount);

    GamaMoney getBaseAmount();
}

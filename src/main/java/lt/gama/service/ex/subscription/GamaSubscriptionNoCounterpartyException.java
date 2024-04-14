package lt.gama.service.ex.subscription;

import lt.gama.service.ex.GamaBaseException;

public class GamaSubscriptionNoCounterpartyException extends GamaBaseException {

    public GamaSubscriptionNoCounterpartyException(String message) {
        super(message);
    }
}

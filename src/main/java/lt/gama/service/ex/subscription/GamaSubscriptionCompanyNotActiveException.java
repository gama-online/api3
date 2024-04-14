package lt.gama.service.ex.subscription;

import lt.gama.service.ex.GamaBaseException;

public class GamaSubscriptionCompanyNotActiveException extends GamaBaseException {

    public GamaSubscriptionCompanyNotActiveException(String message) {
        super(message);
    }
}

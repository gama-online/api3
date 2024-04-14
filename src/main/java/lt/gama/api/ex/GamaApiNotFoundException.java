package lt.gama.api.ex;

import org.springframework.http.HttpStatus;

public class GamaApiNotFoundException extends GamaApiException {

    private final static int STATUS_CODE = 404;

    public GamaApiNotFoundException(String message) {
        super(STATUS_CODE, message);
    }

    public GamaApiNotFoundException(String message, Throwable cause) {
        super(STATUS_CODE, message, cause);
    }
}

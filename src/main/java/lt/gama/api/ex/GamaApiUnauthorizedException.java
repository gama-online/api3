package lt.gama.api.ex;

public class GamaApiUnauthorizedException extends GamaApiException {

    private final static int STATUS_CODE = 401;

    public GamaApiUnauthorizedException(String message) {
        super(STATUS_CODE, message);
    }

    public GamaApiUnauthorizedException(String message, Throwable cause) {
        super(STATUS_CODE, message, cause);
    }
}

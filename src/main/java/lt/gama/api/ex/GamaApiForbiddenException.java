package lt.gama.api.ex;


public class GamaApiForbiddenException extends GamaApiException {

    private final static int STATUS_CODE = 403;

    public GamaApiForbiddenException(String message, Throwable cause) {
        super(STATUS_CODE, message, cause);
    }
}

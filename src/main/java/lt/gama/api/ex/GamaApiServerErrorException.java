package lt.gama.api.ex;

public class GamaApiServerErrorException extends GamaApiException {

    private final static int STATUS_CODE = 500;

    public GamaApiServerErrorException(String message, Throwable cause) {
        super(STATUS_CODE, message, cause);
    }
}

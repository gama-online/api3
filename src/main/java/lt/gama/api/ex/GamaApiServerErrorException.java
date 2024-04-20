package lt.gama.api.ex;

public class GamaApiServerErrorException extends GamaApiException {

    private final static int STATUS_CODE = 500;

    public GamaApiServerErrorException(Throwable cause) {
        super(STATUS_CODE, cause);
    }

    public GamaApiServerErrorException(String message, Throwable cause) {
        super(STATUS_CODE, message, cause);
    }
}

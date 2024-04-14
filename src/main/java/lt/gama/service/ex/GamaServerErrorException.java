package lt.gama.service.ex;

public class GamaServerErrorException extends GamaBaseException {
    public GamaServerErrorException(Throwable cause) {
        super(cause);
    }

    public GamaServerErrorException(String message) {
        super(message);
    }

    public GamaServerErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}

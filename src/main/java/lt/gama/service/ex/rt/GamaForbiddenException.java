package lt.gama.service.ex.rt;

public class GamaForbiddenException extends GamaException {

    public GamaForbiddenException(Throwable cause) {
        super(cause);
    }

    public GamaForbiddenException(String message) {
        super(message);
    }
}

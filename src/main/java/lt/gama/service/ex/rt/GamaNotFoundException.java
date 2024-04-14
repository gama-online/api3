package lt.gama.service.ex.rt;

public class GamaNotFoundException extends GamaException {

    public GamaNotFoundException(Throwable cause) {
        super(cause);
    }

    public GamaNotFoundException(String message) {
        super(message);
    }

    public GamaNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

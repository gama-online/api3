package lt.gama.service.ex;

/**
 * gama-online
 * Created by valdas on 2017-04-29.
 */
public abstract class GamaBaseException extends Exception {

    protected GamaBaseException() {}

    public GamaBaseException(Throwable cause) {
        super(cause.getMessage(), cause);
    }

    public GamaBaseException(String message) {
        super(message);
    }

    public GamaBaseException(String message, Throwable cause) {
        super(message, cause);
    }
}

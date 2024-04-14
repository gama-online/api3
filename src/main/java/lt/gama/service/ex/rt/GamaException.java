package lt.gama.service.ex.rt;

import java.io.Serial;

/**
 * gama-online
 * Created by valdas on 2016-02-19.
 */
public class GamaException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected GamaException() {}

    public GamaException(String message) {
        super(message);
    }

    public GamaException(String message, Throwable cause) {
        super(message, cause);
    }

    public GamaException(Throwable cause) {
        super(cause);
    }
}

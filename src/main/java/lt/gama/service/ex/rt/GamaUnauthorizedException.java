package lt.gama.service.ex.rt;

import java.io.Serial;

public class GamaUnauthorizedException extends GamaException {

    @Serial
    private static final long serialVersionUID = 1L;

    public GamaUnauthorizedException() {}

    public GamaUnauthorizedException(String message) {
        super(message);
    }

    public GamaUnauthorizedException(Throwable cause) {
        super(cause);
    }

    public GamaUnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}

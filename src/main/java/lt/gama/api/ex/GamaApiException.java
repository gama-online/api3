package lt.gama.api.ex;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

public abstract class GamaApiException extends ResponseStatusException {

    public GamaApiException(int statusCode, String message) {
        super(HttpStatusCode.valueOf(statusCode), message);
    }

    public GamaApiException(int statusCode, Throwable cause) {
        this(statusCode, cause.getMessage(), cause);
    }

    public GamaApiException(int statusCode, String message, Throwable cause) {
        super(HttpStatusCode.valueOf(statusCode), message, cause);
    }
}

package lt.gama.api.ex;

public abstract class GamaApiException extends RuntimeException {

    private final int statusCode;

    public GamaApiException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public GamaApiException(int statusCode, Throwable cause) {
        super(cause);
        this.statusCode = statusCode;
    }

    public GamaApiException(int statusCode, String message, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}

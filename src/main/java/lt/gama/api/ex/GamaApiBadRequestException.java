package lt.gama.api.ex;

import lt.gama.helpers.CollectionsHelper;
import lt.gama.helpers.StringHelper;

import java.util.List;

public class GamaApiBadRequestException extends GamaApiException {

    private final static int STATUS_CODE = 400;

    public GamaApiBadRequestException(String message) {
        super(STATUS_CODE, message);
    }

    public GamaApiBadRequestException(Throwable cause) {
        super(STATUS_CODE, cause);
    }

    public GamaApiBadRequestException(String message, Throwable cause) {
        super(STATUS_CODE, message, cause);
    }

    public GamaApiBadRequestException(String message, List<String> messages, Throwable cause) {
        super(STATUS_CODE,
                CollectionsHelper.isEmpty(messages) ? message
                        : (StringHelper.hasValue(message) ? message + "|" : "") + String.join("|", messages),
                cause);
    }
}

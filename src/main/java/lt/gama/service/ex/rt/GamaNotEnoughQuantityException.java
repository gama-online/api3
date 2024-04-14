package lt.gama.service.ex.rt;

import java.util.List;

/**
 * Thrown to indicate that where is not enough quantity to complete inventory operation.
 *
 * @author  Valdas
 */
public class GamaNotEnoughQuantityException extends GamaException {

    private List<String> messages;

    public GamaNotEnoughQuantityException(String message) {
        super(message);
    }

    public GamaNotEnoughQuantityException(List<String> messages) {
        this.messages = messages;
    }

    public List<String> getMessages() {
        return messages;
    }
}

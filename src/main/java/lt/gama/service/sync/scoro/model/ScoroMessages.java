package lt.gama.service.sync.scoro.model;

import java.util.List;

/**
 * gama-online
 * Created by valdas on 2017-10-06.
 */
public class ScoroMessages {

    private List<String> error;

    // generated

    public List<String> getError() {
        return error;
    }

    public void setError(List<String> error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "ScoroMessages{" +
                "error=" + error +
                '}';
    }
}

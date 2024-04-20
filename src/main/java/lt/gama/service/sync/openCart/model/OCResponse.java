package lt.gama.service.sync.openCart.model;

import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 11/11/2018.
 */
public class OCResponse {

    private String error;

    // generated

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OCResponse that = (OCResponse) o;
        return Objects.equals(error, that.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(error);
    }


    @Override
    public String toString() {
        return "OCResponse{" +
                "error='" + error + '\'' +
                '}';
    }
}

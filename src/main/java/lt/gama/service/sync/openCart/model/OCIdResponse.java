package lt.gama.service.sync.openCart.model;

import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 2019-03-19.
 */
public class OCIdResponse extends OCResponse {

    private String id;

    public OCIdResponse() {
    }

    public OCIdResponse(String id) {
        this.id = id;
    }

    // generated

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OCIdResponse that = (OCIdResponse) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "OCIdResponse{" +
                "id='" + id + '\'' +
                "} " + super.toString();
    }
}

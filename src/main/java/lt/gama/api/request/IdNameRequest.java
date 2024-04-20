package lt.gama.api.request;

/**
 * gama-online
 * Created by valdas on 2016-03-11.
 */
public class IdNameRequest {

    private String name;

    protected IdNameRequest() {
    }

    public IdNameRequest(String name) {
        this.name = name;
    }

    // generated

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "IdNameRequest{" +
                "name='" + name + '\'' +
                '}';
    }
}

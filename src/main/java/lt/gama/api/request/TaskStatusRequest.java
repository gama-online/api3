package lt.gama.api.request;

/**
 * gama-online
 * Created by valdas on 2017-04-27.
 */
public class TaskStatusRequest {

    private String id;

    // generated

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "TaskStatusRequest{" +
                "id='" + id + '\'' +
                '}';
    }
}

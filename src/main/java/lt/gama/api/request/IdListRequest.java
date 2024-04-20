package lt.gama.api.request;

import java.util.List;

/**
 * gama-online
 * Created by valdas on 2016-06-03.
 */
public class IdListRequest {

    private List<Long> ids;

    protected IdListRequest() {
    }

    public IdListRequest(List<Long> ids) {
        this.ids = ids;
    }

    // generated

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    @Override
    public String toString() {
        return "IdListRequest{" +
                "ids=" + ids +
                '}';
    }
}

package lt.gama.api.request;

/**
 * gama-online
 * Created by valdas on 2017-04-24.
 */
public class UploadDataRequest {

    private String data;

    private String contentType;

    // generated

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public String toString() {
        return "UploadDataRequest{" +
                "data='" + data + '\'' +
                ", contentType='" + contentType + '\'' +
                '}';
    }
}

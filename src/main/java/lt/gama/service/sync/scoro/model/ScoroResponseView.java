package lt.gama.service.sync.scoro.model;

/**
 * gama-online
 * Created by valdas on 2017-10-11.
 */
public class ScoroResponseView<T> {

    private String status;

    private String statusCode;

    private T data;

    private ScoroMessages messages;

    // generated

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public ScoroMessages getMessages() {
        return messages;
    }

    public void setMessages(ScoroMessages messages) {
        this.messages = messages;
    }

    @Override
    public String toString() {
        return "ScoroResponseView{" +
                "status='" + status + '\'' +
                ", statusCode='" + statusCode + '\'' +
                ", data=" + data +
                ", messages=" + messages +
                '}';
    }
}


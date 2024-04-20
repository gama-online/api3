package lt.gama.api.request;

import lt.gama.model.type.auth.CounterDesc;

/**
 * gama-online
 * Created by valdas on 2017-03-31.
 */
public class CounterRequest {

    private String key;

    private CounterDesc desc;


    @SuppressWarnings("unused")
    protected CounterRequest() {}

    public CounterRequest(CounterDesc desc) {
        this.desc = desc;
    }

    // generated

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public CounterDesc getDesc() {
        return desc;
    }

    public void setDesc(CounterDesc desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "CounterRequest{" +
                "key='" + key + '\'' +
                ", desc=" + desc +
                '}';
    }
}

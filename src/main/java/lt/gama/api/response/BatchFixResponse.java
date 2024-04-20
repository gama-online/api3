package lt.gama.api.response;

/**
 * gama-online
 * Created by valdas on 2016-02-17.
 */
public class BatchFixResponse {

    private int processed;

    private int fixed;


    public BatchFixResponse() {
        processed = 0;
        fixed = 0;
    }

    public BatchFixResponse(int processed, int fixed) {
        this.processed = processed;
        this.fixed = fixed;
    }

    public void add(BatchFixResponse batchFixResponse) {
        setProcessed(getProcessed() + batchFixResponse.getProcessed());
        setFixed(getFixed() + batchFixResponse.getFixed());
    }

    public void incFixed() {
        fixed++;
    }

    public void incProcessed() {
        processed++;
    }

    // generated

    public int getProcessed() {
        return processed;
    }

    public void setProcessed(int processed) {
        this.processed = processed;
    }

    public int getFixed() {
        return fixed;
    }

    public void setFixed(int fixed) {
        this.fixed = fixed;
    }

    @Override
    public String toString() {
        return "BatchFixResponse{" +
                "processed=" + processed +
                ", fixed=" + fixed +
                '}';
    }
}

package lt.gama.api.request;

/**
 * gama-online
 * Created by valdas on 2016-01-07.
 */
public class CheckQueryRequest {

    private String jobId;

    // generated

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    @Override
    public String toString() {
        return "CheckQueryRequest{" +
                "jobId='" + jobId + '\'' +
                '}';
    }
}

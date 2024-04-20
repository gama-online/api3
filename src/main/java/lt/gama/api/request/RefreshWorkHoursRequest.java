package lt.gama.api.request;

/**
 * gama-online
 * Created by valdas on 2017-01-25.
 */
public class RefreshWorkHoursRequest {

    private int year;

    private int month;

    private Boolean fresh;


    @SuppressWarnings("unused")
    protected RefreshWorkHoursRequest() {}

    public RefreshWorkHoursRequest(int year, int month, Boolean fresh) {
        this.year = year;
        this.month = month;
        this.fresh = fresh;
    }

    // generated

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public Boolean getFresh() {
        return fresh;
    }

    public void setFresh(Boolean fresh) {
        this.fresh = fresh;
    }

    @Override
    public String toString() {
        return "RefreshWorkHoursRequest{" +
                "year=" + year +
                ", month=" + month +
                ", fresh=" + fresh +
                '}';
    }
}

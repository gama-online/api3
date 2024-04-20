package lt.gama.api.request;

/**
 * gama-online
 * Created by valdas on 2016-12-16.
 */
public class GenerateWorkHoursRequest {

    private int year;

    private int month;

    private long employeeId;


    @SuppressWarnings("unused")
    protected GenerateWorkHoursRequest() {}

    public GenerateWorkHoursRequest(int year, int month, long employeeId) {
        this.year = year;
        this.month = month;
        this.employeeId = employeeId;
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

    public long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(long employeeId) {
        this.employeeId = employeeId;
    }

    @Override
    public String toString() {
        return "GenerateWorkHoursRequest{" +
                "year=" + year +
                ", month=" + month +
                ", employeeId=" + employeeId +
                '}';
    }
}

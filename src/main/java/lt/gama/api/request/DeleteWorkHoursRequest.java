package lt.gama.api.request;

public class DeleteWorkHoursRequest {

    private int year;

    private int month;

    private long id;

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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "DeleteWorkHoursRequest{" +
                "year=" + year +
                ", month=" + month +
                ", id=" + id +
                '}';
    }
}

package lt.gama.api.request;

import lt.gama.model.type.doc.DocPosition;

import java.time.LocalDate;

/**
 * gama-online
 * Created by valdas on 2017-01-02.
 */
public class GenerateWorkHoursPositionRequest {

    private int year;

    private int month;

    private LocalDate hired;

    private LocalDate fired;

    private DocPosition position;


    @SuppressWarnings("unused")
    protected GenerateWorkHoursPositionRequest() {}

    public GenerateWorkHoursPositionRequest(int year, int month, LocalDate hired, LocalDate fired, DocPosition position) {
        this.year = year;
        this.month = month;
        this.hired = hired;
        this.fired = fired;
        this.position = position;
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

    public LocalDate getHired() {
        return hired;
    }

    public void setHired(LocalDate hired) {
        this.hired = hired;
    }

    public LocalDate getFired() {
        return fired;
    }

    public void setFired(LocalDate fired) {
        this.fired = fired;
    }

    public DocPosition getPosition() {
        return position;
    }

    public void setPosition(DocPosition position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "GenerateWorkHoursPositionRequest{" +
                "year=" + year +
                ", month=" + month +
                ", hired=" + hired +
                ", fired=" + fired +
                ", position=" + position +
                '}';
    }
}

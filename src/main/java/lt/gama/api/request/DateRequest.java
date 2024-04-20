package lt.gama.api.request;

import java.time.LocalDate;

/**
 * gama-online
 * Created by valdas on 2017-05-15.
 */
public class DateRequest {

    private LocalDate date;


    @SuppressWarnings("unused")
    protected DateRequest() {}

    public DateRequest(LocalDate date) {
        this.date = date;
    }

    // generated

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "DateRequest{" +
                "date=" + date +
                '}';
    }
}

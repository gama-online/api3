package lt.gama.api.request;

import java.time.LocalDate;

/**
 * gama-online
 * Created by valdas on 2015-11-10.
 */
public class GenerateDERequest {

    private LocalDate date;


    @SuppressWarnings("unused")
    protected GenerateDERequest() {}

    public GenerateDERequest(LocalDate date) {
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
        return "GenerateDERequest{" +
                "date=" + date +
                '}';
    }
}


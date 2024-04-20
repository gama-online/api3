package lt.gama.api.request;

import java.time.LocalDate;

/**
 * Gama
 * Created by valdas on 15-09-22.
 */
public class ReportBalanceRequest {

    private LocalDate dateFrom;

    private LocalDate dateTo;


    @SuppressWarnings("unused")
    protected ReportBalanceRequest() {}

    public ReportBalanceRequest(LocalDate dateFrom, LocalDate dateTo) {
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
    }

    // generated

    public LocalDate getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(LocalDate dateFrom) {
        this.dateFrom = dateFrom;
    }

    public LocalDate getDateTo() {
        return dateTo;
    }

    public void setDateTo(LocalDate dateTo) {
        this.dateTo = dateTo;
    }

    @Override
    public String toString() {
        return "ReportBalanceRequest{" +
                "dateFrom=" + dateFrom +
                ", dateTo=" + dateTo +
                '}';
    }
}

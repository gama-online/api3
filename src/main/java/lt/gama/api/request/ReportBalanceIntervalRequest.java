package lt.gama.api.request;

import java.time.LocalDate;

public class ReportBalanceIntervalRequest {

    private LocalDate dateFrom;

    private LocalDate dateTo;

    private ReportInterval interval;

    private Long accountId;


    public ReportBalanceIntervalRequest() {
    }

    public ReportBalanceIntervalRequest(LocalDate dateFrom, LocalDate dateTo, ReportInterval interval) {
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.interval = interval;
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

    public ReportInterval getInterval() {
        return interval;
    }

    public void setInterval(ReportInterval interval) {
        this.interval = interval;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    @Override
    public String toString() {
        return "ReportBalanceIntervalRequest{" +
                "dateFrom=" + dateFrom +
                ", dateTo=" + dateTo +
                ", interval=" + interval +
                ", accountId=" + accountId +
                '}';
    }
}

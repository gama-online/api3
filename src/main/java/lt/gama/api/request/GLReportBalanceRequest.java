package lt.gama.api.request;

import java.time.LocalDate;

/**
 * gama-online
 * Created by valdas on 2019-02-22.
 */
public class GLReportBalanceRequest extends ReportBalanceRequest {

    private Boolean withRC;

    private Boolean withHiddenRC;


    @SuppressWarnings("unused")
    protected GLReportBalanceRequest() {}

    public GLReportBalanceRequest(LocalDate dateFrom, LocalDate dateTo, Boolean withRC) {
        this(dateFrom, dateTo, withRC, null);
    }

    public GLReportBalanceRequest(LocalDate dateFrom, LocalDate dateTo, Boolean withRC, Boolean withHiddenRC) {
        super(dateFrom, dateTo);
        this.withRC = withRC;
        this.withHiddenRC = withHiddenRC;
    }

    // generated

    public Boolean getWithRC() {
        return withRC;
    }

    public void setWithRC(Boolean withRC) {
        this.withRC = withRC;
    }

    public Boolean getWithHiddenRC() {
        return withHiddenRC;
    }

    public void setWithHiddenRC(Boolean withHiddenRC) {
        this.withHiddenRC = withHiddenRC;
    }

    @Override
    public String toString() {
        return "GLReportBalanceRequest{" +
                "withRC=" + withRC +
                ", withHiddenRC=" + withHiddenRC +
                "} " + super.toString();
    }
}

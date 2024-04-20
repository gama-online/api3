package lt.gama.api.request;

import lt.gama.model.type.enums.DebtType;

import java.time.LocalDate;

public class ReportDebtBalanceIntervalRequest extends ReportBalanceIntervalRequest {

    private DebtType type;


    @SuppressWarnings("unused")
    protected ReportDebtBalanceIntervalRequest() {}

    public ReportDebtBalanceIntervalRequest(LocalDate dateFrom, LocalDate dateTo, ReportInterval interval, DebtType type) {
        super(dateFrom, dateTo, interval);
        this.type = type;
    }

    // generated

    public DebtType getType() {
        return type;
    }

    public void setType(DebtType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "ReportDebtBalanceIntervalRequest{" +
                "type=" + type +
                "} " + super.toString();
    }
}

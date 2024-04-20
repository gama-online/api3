package lt.gama.api.request;

import lt.gama.model.type.enums.DebtType;

/**
 * gama-online
 * Created by valdas on 2018-05-11.
 */
public class GetDebtCoverageRequestParentObj {

    private long counterpartyId;

    private DebtType debtType;

    private String currency;

    // generated

    public long getCounterpartyId() {
        return counterpartyId;
    }

    public void setCounterpartyId(long counterpartyId) {
        this.counterpartyId = counterpartyId;
    }

    public DebtType getDebtType() {
        return debtType;
    }

    public void setDebtType(DebtType debtType) {
        this.debtType = debtType;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Override
    public String toString() {
        return "GetDebtCoverageRequestParentObj{" +
                "counterpartyId=" + counterpartyId +
                ", debtType=" + debtType +
                ", currency='" + currency + '\'' +
                '}';
    }
}

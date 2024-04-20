package lt.gama.api.request;

import lt.gama.model.type.enums.DebtType;

/**
 * gama-online
 * Created by valdas on 2019-01-27.
 */
public class DebtNowRequest {

    private long id;

    private DebtType type;

    private String currency;

    // generated

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public DebtType getType() {
        return type;
    }

    public void setType(DebtType type) {
        this.type = type;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Override
    public String toString() {
        return "DebtNowRequest{" +
                "id=" + id +
                ", type=" + type +
                ", currency='" + currency + '\'' +
                '}';
    }
}

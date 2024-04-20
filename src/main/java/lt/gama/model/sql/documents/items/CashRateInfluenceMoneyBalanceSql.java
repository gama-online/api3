package lt.gama.model.sql.documents.items;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lt.gama.model.sql.base.BaseMoneyBalanceSql;
import lt.gama.model.sql.documents.CashRateInfluenceSql;
import lt.gama.model.sql.entities.CashSql;


@Entity
@Table(name = "cash_rate_influences_cash")
public class CashRateInfluenceMoneyBalanceSql extends BaseMoneyBalanceSql {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnoreProperties("accounts")
    private CashRateInfluenceSql parent;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cash_id")
    private CashSql cash;

    private Double sortOrder;

    @Override
    public Long getAccountId() {
        return cash != null ? cash.getId() : null;
    }

    @Override
    public String getAccountName() {
        return cash != null ? cash.getName() : null;
    }


    /**
     * toString except parent
     */
    @Override
    public String toString() {
        return "CashRateInfluenceMoneyBalanceSql{" +
                ", cash=" + cash +
                ", sortOrder=" + sortOrder +
                "} " + super.toString();
    }

    // generated

    public CashRateInfluenceSql getParent() {
        return parent;
    }

    public void setParent(CashRateInfluenceSql parent) {
        this.parent = parent;
    }

    @Override
    public CashSql getCash() {
        return cash;
    }

    public void setCash(CashSql cash) {
        this.cash = cash;
    }

    public Double getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Double sortOrder) {
        this.sortOrder = sortOrder;
    }
}

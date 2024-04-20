package lt.gama.model.sql.documents.items;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lt.gama.model.i.ISortOrder;
import lt.gama.model.sql.base.BaseMoneyBalanceSql;
import lt.gama.model.sql.documents.CashOpeningBalanceSql;
import lt.gama.model.sql.entities.CashSql;


@Entity
@Table(name = "cash_ob_balances")
public class CashOpeningBalanceCashSql extends BaseMoneyBalanceSql implements ISortOrder {

    private Double sortOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnoreProperties("cashes")
    private CashOpeningBalanceSql parent;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cash_id")
    private CashSql cash;

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
        return "CashOpeningBalanceCashSql{" +
                "sortOrder=" + sortOrder +
                ", parent=" + parent +
                ", cash=" + cash +
                "} " + super.toString();
    }

    // generated

    @Override
    public Double getSortOrder() {
        return sortOrder;
    }

    @Override
    public void setSortOrder(Double sortOrder) {
        this.sortOrder = sortOrder;
    }

    public CashOpeningBalanceSql getParent() {
        return parent;
    }

    public void setParent(CashOpeningBalanceSql parent) {
        this.parent = parent;
    }

    @Override
    public CashSql getCash() {
        return cash;
    }

    public void setCash(CashSql cash) {
        this.cash = cash;
    }
}

package lt.gama.model.sql.documents.items;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lt.gama.model.sql.base.BaseMoneyBalanceSql;
import lt.gama.model.sql.documents.DebtRateInfluenceSql;
import lt.gama.model.sql.entities.CounterpartySql;
import lt.gama.model.type.enums.DebtType;


@Entity
@Table(name = "debt_rate_influence_counterparties")
public class DebtRateInfluenceMoneyBalanceSql extends BaseMoneyBalanceSql {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnoreProperties("accounts")
    private DebtRateInfluenceSql parent;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "counterparty_id")
    private CounterpartySql counterparty;

    private DebtType type;

    private Double sortOrder;

    @Override
    public Long getAccountId() {
        return counterparty != null ? counterparty.getId() : null;
    }

    @Override
    public String getAccountName() {
        return counterparty != null ? counterparty.getName() : null;
    }

    /**
     * toString except parent
     */
    @Override
    public String toString() {
        return "DebtRateInfluenceMoneyBalanceSql{" +
                "parent=" + parent +
                ", counterparty=" + counterparty +
                ", sortOrder=" + sortOrder +
                "} " + super.toString();
    }

    // generated

    public DebtRateInfluenceSql getParent() {
        return parent;
    }

    public void setParent(DebtRateInfluenceSql parent) {
        this.parent = parent;
    }

    @Override
    public CounterpartySql getCounterparty() {
        return counterparty;
    }

    public void setCounterparty(CounterpartySql counterparty) {
        this.counterparty = counterparty;
    }

    public DebtType getType() {
        return type;
    }

    public void setType(DebtType type) {
        this.type = type;
    }

    public Double getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Double sortOrder) {
        this.sortOrder = sortOrder;
    }
}

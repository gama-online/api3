package lt.gama.model.sql.documents.items;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lt.gama.model.i.ISortOrder;
import lt.gama.model.sql.base.BaseMoneyBalanceSql;
import lt.gama.model.sql.documents.DebtOpeningBalanceSql;
import lt.gama.model.sql.entities.CounterpartySql;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.enums.DebtType;


@Entity
@Table(name = "debt_ob_counterparties")
public class DebtOpeningBalanceCounterpartySql extends BaseMoneyBalanceSql implements ISortOrder {

    private Double sortOrder;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "counterparty_id")
    private CounterpartySql counterparty;

    private DebtType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnoreProperties("counterparties")
    private DebtOpeningBalanceSql parent;

    public DebtOpeningBalanceCounterpartySql() {
    }

    public DebtOpeningBalanceCounterpartySql(CounterpartySql counterparty, DebtType type, GamaMoney sum) {
        this.counterparty = counterparty;
        this.type = type;
        this.setAmount(sum);
    }

    public DebtOpeningBalanceCounterpartySql(CounterpartySql counterparty, DebtType type, GamaMoney sum, GamaMoney baseSum, GamaMoney baseFixAmount) {
        this.counterparty = counterparty;
        this.type = type;
        this.setAmount(sum);
        this.setBaseAmount(baseSum);
        this.setBaseFixAmount(baseFixAmount);
    }

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
        return "DebtOpeningBalanceCounterpartySql{" +
                "sortOrder=" + sortOrder +
                ", counterparty=" + counterparty +
                ", type=" + type +
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

    public DebtOpeningBalanceSql getParent() {
        return parent;
    }

    public void setParent(DebtOpeningBalanceSql parent) {
        this.parent = parent;
    }
}

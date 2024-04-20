package lt.gama.model.sql.documents;

import jakarta.persistence.*;
import lt.gama.helpers.BooleanUtils;
import lt.gama.model.sql.base.BaseMoneyDocumentSql;
import lt.gama.model.sql.entities.CashSql;
import lt.gama.model.type.enums.AccountType;


@Entity
@Table(name = "cash_operation")
@NamedEntityGraph(name = CashOperationSql.GRAPH_ALL, attributeNodes = {
        @NamedAttributeNode(CashOperationSql_.CASH),
        @NamedAttributeNode(CashOperationSql_.COUNTERPARTY),
        @NamedAttributeNode(CashOperationSql_.EMPLOYEE),
})
public class CashOperationSql extends BaseMoneyDocumentSql {

    public static final String GRAPH_ALL = "graph.CashOperationSql.all";

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cash_id")
    private CashSql cash;


    @Override
    public boolean isFullyFinished() {
        return super.isFullyFinished() && isFinishedMoneyAll();
    }

    public boolean isFinishedMoneyAll() {
        return isFinishedMoneyType(AccountType.CASH) &&
                (BooleanUtils.isTrue(getNoDebt()) || getEmployee() == null || isFinishedMoneyType(AccountType.EMPLOYEE));
    }

    // generated

    @Override
    public CashSql getCash() {
        return cash;
    }

    public void setCash(CashSql cash) {
        this.cash = cash;
    }

    @Override
    public String toString() {
        return "CashOperationSql{" +
                "cash=" + cash +
                "} " + super.toString();
    }
}

package lt.gama.model.sql.documents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lt.gama.helpers.BooleanUtils;
import lt.gama.model.i.IDebtFinished;
import lt.gama.model.sql.base.BaseDocumentSql;
import lt.gama.model.sql.documents.items.DebtOpeningBalanceCounterpartySql;
import lt.gama.model.sql.documents.items.DebtOpeningBalanceCounterpartySql_;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "debt_opening_balances")
@NamedEntityGraph(
        name = DebtOpeningBalanceSql.GRAPH_ALL,
        attributeNodes = @NamedAttributeNode(value = DebtOpeningBalanceSql_.COUNTERPARTIES, subgraph = "counterparty"),
        subgraphs = @NamedSubgraph(name = "counterparty", attributeNodes = @NamedAttributeNode(DebtOpeningBalanceCounterpartySql_.COUNTERPARTY)))
public class DebtOpeningBalanceSql extends BaseDocumentSql implements IDebtFinished {

    public static final String GRAPH_ALL = "graph.DebtOpeningBalanceSql.all";

    private Boolean finishedDebt;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("parent")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @OrderBy("sortOrder, id")
    private Set<DebtOpeningBalanceCounterpartySql> counterparties = new LinkedHashSet<>();

    @Override
    public boolean isUnfinishedDebt() {
        return BooleanUtils.isNotTrue(finishedDebt);
    }

    /**
     * toString without counterparties
     */
    @Override
    public String toString() {
        return "DebtOpeningBalanceSql{" +
                "finishedDebt=" + finishedDebt +
                "} " + super.toString();
    }

    // generated

    @Override
    public Boolean getFinishedDebt() {
        return finishedDebt;
    }

    @Override
    public void setFinishedDebt(Boolean finishedDebt) {
        this.finishedDebt = finishedDebt;
    }

    public Set<DebtOpeningBalanceCounterpartySql> getCounterparties() {
        return counterparties;
    }

    public void setCounterparties(Set<DebtOpeningBalanceCounterpartySql> counterparties) {
        this.counterparties = counterparties;
    }
}

package lt.gama.model.sql.documents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lt.gama.model.i.IDebtFinished;
import lt.gama.model.sql.base.BaseMoneyRateInfluenceSql;
import lt.gama.model.sql.documents.items.DebtRateInfluenceMoneyBalanceSql;
import lt.gama.model.sql.documents.items.DebtRateInfluenceMoneyBalanceSql_;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "debt_rate_influences")
@NamedEntityGraph(
        name = DebtRateInfluenceSql.GRAPH_ALL,
        attributeNodes = @NamedAttributeNode(value = DebtRateInfluenceSql_.ACCOUNTS, subgraph = "account"),
        subgraphs = @NamedSubgraph(name = "account", attributeNodes = @NamedAttributeNode(DebtRateInfluenceMoneyBalanceSql_.COUNTERPARTY)))
public class DebtRateInfluenceSql extends BaseMoneyRateInfluenceSql implements IDebtFinished {

    public static final String GRAPH_ALL = "graph.DebtRateInfluenceSql.all";

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("parent")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @OrderBy("sortOrder, id")
    private Set<DebtRateInfluenceMoneyBalanceSql> accounts = new LinkedHashSet<>();

    @Override
    public Boolean getFinishedDebt() {
        return !hasItemUnfinished();
    }

    @Override
    public void setFinishedDebt(Boolean finished) {
        setItemsFinished(finished);
    }

    @Override
    public boolean isUnfinishedDebt() {
        return !hasItemFinished();
    }

    /**
     * toString without accounts
     */
    @Override
    public String toString() {
        return "DebtRateInfluenceSql{} " + super.toString();
    }

    // generated

    @Override
    public Set<DebtRateInfluenceMoneyBalanceSql> getAccounts() {
        return accounts;
    }

    public void setAccounts(Set<DebtRateInfluenceMoneyBalanceSql> accounts) {
        this.accounts = accounts;
    }
}

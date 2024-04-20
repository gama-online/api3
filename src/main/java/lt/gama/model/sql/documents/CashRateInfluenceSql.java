package lt.gama.model.sql.documents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lt.gama.model.sql.base.BaseMoneyRateInfluenceSql;
import lt.gama.model.sql.documents.items.CashRateInfluenceMoneyBalanceSql;
import lt.gama.model.sql.documents.items.CashRateInfluenceMoneyBalanceSql_;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "cash_rate_influences")
@NamedEntityGraph(
        name = CashRateInfluenceSql.GRAPH_ALL,
        attributeNodes = @NamedAttributeNode(value = CashRateInfluenceSql_.ACCOUNTS, subgraph = "account"),
        subgraphs = @NamedSubgraph(name = "account", attributeNodes = @NamedAttributeNode(CashRateInfluenceMoneyBalanceSql_.CASH)))
public class CashRateInfluenceSql extends BaseMoneyRateInfluenceSql {

    public static final String GRAPH_ALL = "graph.CashRateInfluenceSql.all";

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("parent")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @OrderBy("sortOrder, id")
    private Set<CashRateInfluenceMoneyBalanceSql> accounts = new LinkedHashSet<>();


    @Override
    public String toString() {
        return "CashRateInfluenceSql{} " + super.toString();
    }

    // generated

    @Override
    public Set<CashRateInfluenceMoneyBalanceSql> getAccounts() {
        return accounts;
    }

    public void setAccounts(Set<CashRateInfluenceMoneyBalanceSql> accounts) {
        this.accounts = accounts;
    }
}

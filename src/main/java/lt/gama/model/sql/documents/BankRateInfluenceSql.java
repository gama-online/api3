package lt.gama.model.sql.documents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lt.gama.model.sql.base.BaseMoneyRateInfluenceSql;
import lt.gama.model.sql.documents.items.BankRateInfluenceMoneyBalanceSql;
import lt.gama.model.sql.documents.items.BankRateInfluenceMoneyBalanceSql_;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "bank_rate_influences")
@NamedEntityGraph(
        name = BankRateInfluenceSql.GRAPH_ALL,
        attributeNodes = @NamedAttributeNode(value = BankRateInfluenceSql_.ACCOUNTS, subgraph = "account"),
        subgraphs = @NamedSubgraph(name = "account", attributeNodes = @NamedAttributeNode(BankRateInfluenceMoneyBalanceSql_.BANK_ACCOUNT))
)
public class BankRateInfluenceSql extends BaseMoneyRateInfluenceSql {

    public static final String GRAPH_ALL = "graph.BankRateInfluenceSql.all";

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("parent")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @OrderBy("sortOrder, id")
    private Set<BankRateInfluenceMoneyBalanceSql> accounts = new LinkedHashSet<>();


    @Override
    public String toString() {
        return "BankRateInfluenceSql{} " + super.toString();
    }

    // generated

    @Override
    public Set<BankRateInfluenceMoneyBalanceSql> getAccounts() {
        return accounts;
    }

    public void setAccounts(Set<BankRateInfluenceMoneyBalanceSql> accounts) {
        this.accounts = accounts;
    }
}

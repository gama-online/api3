package lt.gama.model.sql.documents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lt.gama.model.sql.base.BaseMoneyRateInfluenceSql;
import lt.gama.model.sql.documents.items.EmployeeRateInfluenceMoneyBalanceSql;
import lt.gama.model.sql.documents.items.EmployeeRateInfluenceMoneyBalanceSql_;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "employee_rate_influences")
@NamedEntityGraph(
        name = EmployeeRateInfluenceSql.GRAPH_ALL,
        attributeNodes = @NamedAttributeNode(value = EmployeeRateInfluenceSql_.ACCOUNTS, subgraph = "account"),
        subgraphs = @NamedSubgraph(name = "account", attributeNodes = @NamedAttributeNode(EmployeeRateInfluenceMoneyBalanceSql_.EMPLOYEE)))
public class EmployeeRateInfluenceSql extends BaseMoneyRateInfluenceSql {

    public static final String GRAPH_ALL = "graph.EmployeeRateInfluenceSql.all";

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("parent")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @OrderBy("sortOrder, id")
    private Set<EmployeeRateInfluenceMoneyBalanceSql> accounts = new LinkedHashSet<>();

    /**
     * toString except accounts
     */
    @Override
    public String toString() {
        return "EmployeeRateInfluenceSql{} " + super.toString();
    }

    // generated

    @Override
    public Set<EmployeeRateInfluenceMoneyBalanceSql> getAccounts() {
        return accounts;
    }

    public void setAccounts(Set<EmployeeRateInfluenceMoneyBalanceSql> accounts) {
        this.accounts = accounts;
    }
}

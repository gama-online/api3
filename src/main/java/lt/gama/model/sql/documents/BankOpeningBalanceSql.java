package lt.gama.model.sql.documents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lt.gama.model.sql.base.BaseDocumentSql;
import lt.gama.model.sql.documents.items.BankOpeningBalanceBankSql;
import lt.gama.model.sql.documents.items.BankOpeningBalanceBankSql_;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "bank_ob")
@NamedEntityGraph(
        name = BankOpeningBalanceSql.GRAPH_ALL,
        attributeNodes = @NamedAttributeNode(value = BankOpeningBalanceSql_.BANK_ACCOUNTS, subgraph = "account"),
        subgraphs = @NamedSubgraph(name = "account", attributeNodes = @NamedAttributeNode(BankOpeningBalanceBankSql_.BANK_ACCOUNT)))
public class BankOpeningBalanceSql extends BaseDocumentSql {

    public static final String GRAPH_ALL = "graph.BankOpeningBalanceSql.all";

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("parent")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @OrderBy("sortOrder, id")
    private Set<BankOpeningBalanceBankSql> bankAccounts = new LinkedHashSet<>();


    @Override
    public String toString() {
        return "BankOpeningBalanceSql{} " + super.toString();
    }

    // generated

    public Set<BankOpeningBalanceBankSql> getBankAccounts() {
        return bankAccounts;
    }

    public void setBankAccounts(Set<BankOpeningBalanceBankSql> bankAccounts) {
        this.bankAccounts = bankAccounts;
    }
}

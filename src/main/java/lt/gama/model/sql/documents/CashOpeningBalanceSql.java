package lt.gama.model.sql.documents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lt.gama.model.sql.base.BaseDocumentSql;
import lt.gama.model.sql.documents.items.CashOpeningBalanceCashSql;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "cash_ob")
@NamedEntityGraph(name = CashOpeningBalanceSql.GRAPH_ALL, attributeNodes = @NamedAttributeNode(CashOpeningBalanceSql_.CASHES))
public class CashOpeningBalanceSql extends BaseDocumentSql {

    public static final String GRAPH_ALL = "graph.CashOpeningBalanceSql.all";

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("parent")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @OrderBy("sortOrder, id")
    private Set<CashOpeningBalanceCashSql> cashes = new LinkedHashSet<>();


    @Override
    public String toString() {
        return "CashOpeningBalanceSql{} " + super.toString();
    }

    // generated

    public Set<CashOpeningBalanceCashSql> getCashes() {
        return cashes;
    }

    public void setCashes(Set<CashOpeningBalanceCashSql> cashes) {
        this.cashes = cashes;
    }
}

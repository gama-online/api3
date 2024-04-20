package lt.gama.model.sql.documents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lt.gama.helpers.BooleanUtils;
import lt.gama.model.sql.base.BaseDocumentSql;
import lt.gama.model.sql.documents.items.GLOpeningBalanceOperationSql;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "gl_opening_balances")
@NamedEntityGraph(name = GLOpeningBalanceSql.GRAPH_ALL, attributeNodes = @NamedAttributeNode(GLOpeningBalanceSql_.BALANCES))
public class GLOpeningBalanceSql extends BaseDocumentSql {

    public static final String GRAPH_ALL = "graph.GLOpeningBalanceSql.all";

    @OneToMany(mappedBy = "glOpeningBalance", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("glOpeningBalance")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<GLOpeningBalanceOperationSql> balances = new ArrayList<>();

    /**
     * @return the same as isFinishedGL() - need for compatibility in frontend
     */
    public boolean isFinished() {
        return BooleanUtils.isTrue(getFinishedGL());
    }

    /**
     * toString except balances
     */
    @Override
    public String toString() {
        return "GLOpeningBalanceSql{} " + super.toString();
    }

    // generated

    public List<GLOpeningBalanceOperationSql> getBalances() {
        return balances;
    }

    public void setBalances(List<GLOpeningBalanceOperationSql> balances) {
        this.balances = balances;
    }
}

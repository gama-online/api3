package lt.gama.model.sql.documents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lt.gama.model.sql.base.BaseDocumentSql;
import lt.gama.model.sql.documents.items.EmployeeOpeningBalanceEmployeeSql;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "employee_ob")
@NamedEntityGraph(name = EmployeeOpeningBalanceSql.GRAPH_ALL, attributeNodes = @NamedAttributeNode(EmployeeOpeningBalanceSql_.EMPLOYEES))
public class EmployeeOpeningBalanceSql extends BaseDocumentSql {

    public static final String GRAPH_ALL = "graph.EmployeeOpeningBalanceSql.all";

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("parent")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @OrderBy("sortOrder, id")
    private Set<EmployeeOpeningBalanceEmployeeSql> employees = new LinkedHashSet<>();

    /**
     * toString except employees
     */
    @Override
    public String toString() {
        return "EmployeeOpeningBalanceSql{" +
                "} " + super.toString();
    }

    // generated

    public Set<EmployeeOpeningBalanceEmployeeSql> getEmployees() {
        return employees;
    }

    public void setEmployees(Set<EmployeeOpeningBalanceEmployeeSql> employees) {
        this.employees = employees;
    }
}

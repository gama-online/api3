package lt.gama.model.sql.documents;

import jakarta.persistence.Entity;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.Table;
import lt.gama.model.sql.base.BaseMoneyDocumentSql;
import lt.gama.model.type.enums.AccountType;


@Entity
@Table(name = "employee_operation")
@NamedEntityGraph(name = EmployeeOperationSql.GRAPH_ALL, attributeNodes = {
        @NamedAttributeNode(EmployeeOperationSql_.COUNTERPARTY),
        @NamedAttributeNode(EmployeeOperationSql_.EMPLOYEE),
})
public class EmployeeOperationSql extends BaseMoneyDocumentSql {

    public static final String GRAPH_ALL = "graph.EmployeeOperationSql.all";

    @Override
    public boolean isFullyFinished() {
        return super.isFullyFinished() && isFinishedMoneyAll();
    }

    public boolean isFinishedMoneyAll() {
        return isFinishedMoneyType(AccountType.EMPLOYEE);
    }

    // generated

    @Override
    public String toString() {
        return "EmployeeOperationSql{} " + super.toString();
    }
}

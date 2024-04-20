package lt.gama.model.sql.documents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lt.gama.helpers.BooleanUtils;
import lt.gama.model.sql.base.BaseNumberDocumentSql;
import lt.gama.model.sql.documents.items.GLOperationSql;
import lt.gama.model.sql.entities.CounterpartySql;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.enums.DBType;
import lt.gama.service.ex.rt.GamaUnauthorizedException;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "double_entries")
@NamedEntityGraph(name = DoubleEntrySql.GRAPH_ALL, attributeNodes = {
        @NamedAttributeNode(DoubleEntrySql_.OPERATIONS),
        @NamedAttributeNode(DoubleEntrySql_.PARENT_COUNTERPARTY)
})
@SuppressWarnings("unused")
public class DoubleEntrySql extends BaseNumberDocumentSql {

    public static final String GRAPH_ALL = "graph.DoubleEntrySql.all";

    @OneToMany(mappedBy = "doubleEntry", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = {"doubleEntry"}, allowSetters = true)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @OrderBy("sortOrder, id")
    private List<GLOperationSql> operations = new ArrayList<>();

    private String content;

    /**
     * finished or unfinished G.L. operations - can be finished separately from all others
     */
    private Boolean finishedGL = false;

    /**
     * if true - operations are frozen and will not regenerate on finishing document
     */
    private Boolean frozen = false;

    @Embedded
    private GamaMoney total;

    private DBType parentDb;

    /**
     * Parent document type - i.e. Simple class name - need for client to open cashed document
     */
    private String parentType;

    private Long parentId;

    /**
     * Parent document number
     */
    private String parentNumber;

    /**
     * Parent document Counterparty
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_counterparty_id")
    private CounterpartySql parentCounterparty;


    public void addOperation(GLOperationSql operation) {
        if (getCompanyId() == 0) {
            throw new GamaUnauthorizedException("No companyId");
        }
        operation.setCompanyId(this.getCompanyId());
        operations.add(operation);
        operation.setDoubleEntry(this);
    }

    public void removeOperation(GLOperationSql operation) {
        operations.remove(operation);
        operation.setDoubleEntry(null);
    }

    /**
     * @return the same as isFinishedGL() - need for compatibility in frontend
     */
    public boolean isFinished() {
        return BooleanUtils.isTrue(getFinishedGL());
    }

    /**
     * @return the same as isFinishedGL() - need for compatibility in frontend
     */
    public boolean isFullyFinished() {
        return BooleanUtils.isTrue(getFinishedGL());
    }

    /**
     * toString except operations, parentCounterparty
     */
    @Override
    public String toString() {
        return "DoubleEntrySql{" +
                "content='" + content + '\'' +
                ", finishedGL=" + finishedGL +
                ", frozen=" + frozen +
                ", total=" + total +
                ", parentDb=" + parentDb +
                ", parentType='" + parentType + '\'' +
                ", parentId=" + parentId +
                ", parentNumber='" + parentNumber + '\'' +
                "} " + super.toString();
    }

    // generated

    public List<GLOperationSql> getOperations() {
        return operations;
    }

    public void setOperations(List<GLOperationSql> operations) {
        this.operations = operations;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getFinishedGL() {
        return finishedGL;
    }

    public void setFinishedGL(Boolean finishedGL) {
        this.finishedGL = finishedGL;
    }

    public Boolean getFrozen() {
        return frozen;
    }

    public void setFrozen(Boolean frozen) {
        this.frozen = frozen;
    }

    public GamaMoney getTotal() {
        return total;
    }

    public void setTotal(GamaMoney total) {
        this.total = total;
    }

    public DBType getParentDb() {
        return parentDb;
    }

    public void setParentDb(DBType parentDb) {
        this.parentDb = parentDb;
    }

    public String getParentType() {
        return parentType;
    }

    public void setParentType(String parentType) {
        this.parentType = parentType;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getParentNumber() {
        return parentNumber;
    }

    public void setParentNumber(String parentNumber) {
        this.parentNumber = parentNumber;
    }

    public CounterpartySql getParentCounterparty() {
        return parentCounterparty;
    }

    public void setParentCounterparty(CounterpartySql parentCounterparty) {
        this.parentCounterparty = parentCounterparty;
    }
}

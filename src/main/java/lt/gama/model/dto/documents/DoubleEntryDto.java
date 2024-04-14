package lt.gama.model.dto.documents;

import lt.gama.helpers.GamaMoneyUtils;
import lt.gama.model.type.doc.DocCounterparty;
import lt.gama.model.dto.base.BaseNumberDocumentDto;
import lt.gama.model.dto.entities.GLOperationDto;
import lt.gama.model.dto.i.IHasParentDto;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.enums.DBType;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

public class DoubleEntryDto extends BaseNumberDocumentDto implements IHasParentDto {

    @Serial
    private static final long serialVersionUID = -2;

    /**
     * Content of G.L. double entry records
     */
    private String content;

    private List<GLOperationDto> operations = new ArrayList<>();

    /**
     * finished or unfinished G.L. operations - can be finished separately from all others
     */
    private Boolean finishedGL;

    /**
     * if true - operations are frozen and will not regenerate on finishing document
     */
    private Boolean frozen;

    private GamaMoney total;


    private String parent;

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
    private DocCounterparty parentCounterparty;


    public DoubleEntryDto() {
    }

    public DoubleEntryDto(long parentId, String parentType, long companyId) {
        this.parentId = parentId;
        this.parentType = parentType;
        setCompanyId(companyId);
    }

    /**
     * @return the same as isFinishedGL() - need for compatibility in frontend
     */
    public Boolean getFinished() {
        return getFinishedGL();
    }

    /**
     * @return the same as isFinishedGL() - need for compatibility in frontend
     */
    public Boolean getFullyFinished() {
        return getFinishedGL();
    }


    @Override
    public void reset() {
        super.reset();
        finishedGL = false;
    }

    public void calculateTotals() {
        setTotal(null);
        if (getOperations() != null) {
            for (GLOperationDto op : getOperations()) {
                setTotal(GamaMoneyUtils.add(getTotal(), op.getAmount()));
            }
        }
    }

    // generated

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<GLOperationDto> getOperations() {
        return operations;
    }

    public void setOperations(List<GLOperationDto> operations) {
        this.operations = operations;
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

    @Override
    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
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

    public DocCounterparty getParentCounterparty() {
        return parentCounterparty;
    }

    public void setParentCounterparty(DocCounterparty parentCounterparty) {
        this.parentCounterparty = parentCounterparty;
    }

    @Override
    public String toString() {
        return "DoubleEntryDto{" +
                "content='" + content + '\'' +
                ", operations=" + operations +
                ", finishedGL=" + finishedGL +
                ", frozen=" + frozen +
                ", total=" + total +
                ", parent='" + parent + '\'' +
                ", parentDb=" + parentDb +
                ", parentType='" + parentType + '\'' +
                ", parentId=" + parentId +
                ", parentNumber='" + parentNumber + '\'' +
                ", parentCounterparty=" + parentCounterparty +
                "} " + super.toString();
    }
}

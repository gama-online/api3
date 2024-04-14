package lt.gama.model.type.part;

import jakarta.persistence.Transient;
import lt.gama.model.i.IDb;
import lt.gama.model.i.IFinished;
import lt.gama.model.i.IPart;
import lt.gama.model.i.ITranslations;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.base.BaseDocPart;
import lt.gama.model.type.doc.DocWarehouse;
import lt.gama.model.type.ibase.IBaseDocPart;
import lt.gama.model.type.l10n.LangPart;

import java.io.Serial;
import java.math.BigDecimal;

public class DocPartBalance extends BaseDocPart implements IBaseDocPart, IFinished {

    @Serial
    private static final long serialVersionUID = -1L;

    private BigDecimal quantity;

    private GamaMoney total;

    private GamaMoney baseTotal;

    private DocWarehouse warehouse;

    /**
     * Is everything is done with part, i.e. calculated balance, cost, etc.
     */
    private Boolean finished;

    /**
     *  Not enough quantity to finish operation - used in frontend only and is filled in invoice finishing procedure
     */
    @Transient
    private BigDecimal notEnough;


    public DocPartBalance() {
    }

    public <P extends IPart & ITranslations<LangPart> & IDb> DocPartBalance(P part) {
        super(part);
    }

    // generated

    @Override
    public BigDecimal getQuantity() {
        return quantity;
    }

    @Override
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    @Override
    public GamaMoney getTotal() {
        return total;
    }

    @Override
    public void setTotal(GamaMoney total) {
        this.total = total;
    }

    @Override
    public GamaMoney getBaseTotal() {
        return baseTotal;
    }

    @Override
    public void setBaseTotal(GamaMoney baseTotal) {
        this.baseTotal = baseTotal;
    }

    @Override
    public DocWarehouse getWarehouse() {
        return warehouse;
    }

    @Override
    public void setWarehouse(DocWarehouse warehouse) {
        this.warehouse = warehouse;
    }

    @Override
    public Boolean getFinished() {
        return finished;
    }

    @Override
    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    @Override
    public BigDecimal getNotEnough() {
        return notEnough;
    }

    @Override
    public void setNotEnough(BigDecimal notEnough) {
        this.notEnough = notEnough;
    }

    @Override
    public String toString() {
        return "DocPartBalance{" +
                "quantity=" + quantity +
                ", total=" + total +
                ", baseTotal=" + baseTotal +
                ", warehouse=" + warehouse +
                ", finished=" + finished +
                ", notEnough=" + notEnough +
                "} " + super.toString();
    }
}

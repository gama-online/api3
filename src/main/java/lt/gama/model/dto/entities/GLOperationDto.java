package lt.gama.model.dto.entities;

import com.google.common.collect.ComparisonChain;
import lt.gama.helpers.GamaMoneyUtils;
import lt.gama.model.type.doc.DocRC;
import lt.gama.model.type.gl.GLDC;
import lt.gama.model.type.gl.GLOperationAccount;
import lt.gama.model.dto.base.BaseCompanyDto;
import lt.gama.model.i.IGLOperation;
import lt.gama.model.i.ISortOrder;
import lt.gama.model.type.GamaMoney;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public class GLOperationDto extends BaseCompanyDto implements Comparable<GLOperationDto>, ISortOrder, Serializable, IGLOperation {

    @Serial
    private static final long serialVersionUID = -1L;

    private Double sortOrder;

    private GLOperationAccount debit;

    private GLOperationAccount credit;

    private GamaMoney amount;

    private List<DocRC> debitRC;

    private List<DocRC> creditRC;


    public GLOperationDto() {
    }

    public GLOperationDto(GLOperationAccount debit, GLOperationAccount credit, GamaMoney amount) {
        if (GamaMoneyUtils.isNegative(amount)) {
            this.debit = credit;
            this.credit = debit;
            this.amount = GamaMoneyUtils.negated(amount);
        } else {
            this.debit = debit;
            this.credit = credit;
            this.amount = amount;
        }
    }

    public GLOperationDto(GLOperationAccount debit, List<DocRC> debitRC, GLOperationAccount credit, List<DocRC> creditRC, GamaMoney amount) {
        this(debit, credit, amount);
        if (GamaMoneyUtils.isNegative(amount)) {
            this.debitRC = creditRC;
            this.creditRC = debitRC;
        } else {
            this.debitRC = debitRC;
            this.creditRC = creditRC;
        }
    }

    public GLOperationDto(GLOperationAccount debit, GLDC credit, GamaMoney amount) {
        this(debit, credit.getCreditEx(), amount);
    }

    public GLOperationDto(GLDC debit, GLOperationAccount credit, GamaMoney amount) {
        this(debit.getDebitEx(), credit, amount);
    }

    public GLOperationDto(GLDC debit, GLDC credit, GamaMoney amount) {
        this(debit.getDebitEx(), credit.getCreditEx(), amount);
    }

    @Override
    public int compareTo(GLOperationDto o) {
        return ComparisonChain.start()
                .compare(debit, o.debit)
                .compare(credit, o.credit)
                .result();
    }

    // required for import from old format, i.e. 'sum' instead of 'amount'
    public void setSum(GamaMoney amount) {
        setAmount(amount);
    }

    // generated

    @Override
    public Double getSortOrder() {
        return sortOrder;
    }

    @Override
    public void setSortOrder(Double sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Override
    public GLOperationAccount getDebit() {
        return debit;
    }

    public void setDebit(GLOperationAccount debit) {
        this.debit = debit;
    }

    @Override
    public GLOperationAccount getCredit() {
        return credit;
    }

    public void setCredit(GLOperationAccount credit) {
        this.credit = credit;
    }

    @Override
    public GamaMoney getAmount() {
        return amount;
    }

    public void setAmount(GamaMoney amount) {
        this.amount = amount;
    }

    @Override
    public List<DocRC> getDebitRC() {
        return debitRC;
    }

    public void setDebitRC(List<DocRC> debitRC) {
        this.debitRC = debitRC;
    }

    @Override
    public List<DocRC> getCreditRC() {
        return creditRC;
    }

    public void setCreditRC(List<DocRC> creditRC) {
        this.creditRC = creditRC;
    }

    @Override
    public String toString() {
        return "GLOperationDto{" +
                "sortOrder=" + sortOrder +
                ", debit=" + debit +
                ", credit=" + credit +
                ", amount=" + amount +
                ", debitRC=" + debitRC +
                ", creditRC=" + creditRC +
                "} " + super.toString();
    }
}

package lt.gama.model.sql.documents.items;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.ComparisonChain;
import jakarta.persistence.*;
import lt.gama.helpers.GamaMoneyUtils;
import lt.gama.model.i.IGLOperation;
import lt.gama.model.i.ISortOrder;
import lt.gama.model.sql.base.BaseCompanySql;
import lt.gama.model.sql.documents.DoubleEntrySql;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.doc.DocRC;
import lt.gama.model.type.gl.GLDC;
import lt.gama.model.type.gl.GLOperationAccount;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "gl_operations")
public class GLOperationSql extends BaseCompanySql implements Comparable<GLOperationSql>, ISortOrder, IGLOperation {

    private Double sortOrder;

    @Embedded
    private GLOperationAccount debit;

    @Embedded
    private GLOperationAccount credit;

    @Embedded
    private GamaMoney amount;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<DocRC> debitRC = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    private List<DocRC> creditRC = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnoreProperties("operations")
    private DoubleEntrySql doubleEntry;


    public GLOperationSql() {
    }

    public GLOperationSql(GLOperationAccount debit, GLOperationAccount credit, GamaMoney amount) {
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

    public GLOperationSql(GLOperationAccount debit, List<DocRC> debitRC, GLOperationAccount credit, List<DocRC> creditRC, GamaMoney sum) {
        this(debit, credit, sum);
        if (GamaMoneyUtils.isNegative(sum)) {
            this.debitRC = creditRC;
            this.creditRC = debitRC;
        } else {
            this.debitRC = debitRC;
            this.creditRC = creditRC;
        }
    }

    public GLOperationSql(GLOperationAccount debit, GLDC credit, GamaMoney sum) {
        this(debit, credit.getCreditEx(), sum);
    }

    public GLOperationSql(GLDC debit, GLOperationAccount credit, GamaMoney sum) {
        this(debit.getDebitEx(), credit, sum);
    }

    public GLOperationSql(GLDC debit, GLDC credit, GamaMoney sum) {
        this(debit.getDebitEx(), credit.getCreditEx(), sum);
    }


    @Override
    public int compareTo(GLOperationSql o) {
        return ComparisonChain.start()
                .compare(debit, o.debit)
                .compare(credit, o.credit)
                .result();
    }

    /**
     * toString except doubleEntry
     */
    @Override
    public String toString() {
        return "GLOperationSql{" +
                "sortOrder=" + sortOrder +
                ", debit=" + debit +
                ", credit=" + credit +
                ", amount=" + amount +
                ", debitRC=" + debitRC +
                ", creditRC=" + creditRC +
                "} " + super.toString();
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

    public DoubleEntrySql getDoubleEntry() {
        return doubleEntry;
    }

    public void setDoubleEntry(DoubleEntrySql doubleEntry) {
        this.doubleEntry = doubleEntry;
    }
}

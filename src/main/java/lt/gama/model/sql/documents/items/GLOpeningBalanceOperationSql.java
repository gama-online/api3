package lt.gama.model.sql.documents.items;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lt.gama.model.sql.base.BaseCompanySql;
import lt.gama.model.sql.documents.GLOpeningBalanceSql;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.doc.DocRC;
import lt.gama.model.type.gl.GLOperationAccount;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "gl_ob_operations")
public class GLOpeningBalanceOperationSql extends BaseCompanySql {

    private Double sortNr;

    @Embedded
    private GLOperationAccount account;

    @Embedded
    private GamaMoney debit;

    @Embedded
    private GamaMoney credit;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<DocRC> rc = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnoreProperties("balances")
    private GLOpeningBalanceSql glOpeningBalance;

    /**
     * toString except glOpeningBalance
     */
    @Override
    public String toString() {
        return "GLOpeningBalanceOperationSql{" +
                "sortNr=" + sortNr +
                ", account=" + account +
                ", debit=" + debit +
                ", credit=" + credit +
                ", rc=" + rc +
                "} " + super.toString();
    }

    // generated

    public Double getSortNr() {
        return sortNr;
    }

    public void setSortNr(Double sortNr) {
        this.sortNr = sortNr;
    }

    public GLOperationAccount getAccount() {
        return account;
    }

    public void setAccount(GLOperationAccount account) {
        this.account = account;
    }

    public GamaMoney getDebit() {
        return debit;
    }

    public void setDebit(GamaMoney debit) {
        this.debit = debit;
    }

    public GamaMoney getCredit() {
        return credit;
    }

    public void setCredit(GamaMoney credit) {
        this.credit = credit;
    }

    public List<DocRC> getRc() {
        return rc;
    }

    public void setRc(List<DocRC> rc) {
        this.rc = rc;
    }

    public GLOpeningBalanceSql getGlOpeningBalance() {
        return glOpeningBalance;
    }

    public void setGlOpeningBalance(GLOpeningBalanceSql glOpeningBalance) {
        this.glOpeningBalance = glOpeningBalance;
    }
}

package lt.gama.model.dto.documents.items;

import com.google.common.collect.ComparisonChain;
import lt.gama.model.type.doc.DocRC;
import lt.gama.model.type.gl.GLOperationAccount;
import lt.gama.model.dto.base.BaseCompanyDto;
import lt.gama.model.type.GamaMoney;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GLOpeningBalanceOperationDto extends BaseCompanyDto implements Comparable<GLOpeningBalanceOperationDto>, Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    private GLOperationAccount account;

    private GamaMoney debit;

    private GamaMoney credit;

    private List<DocRC> rc;


    public GLOpeningBalanceOperationDto() {
    }

    public GLOpeningBalanceOperationDto(GLOperationAccount account, GamaMoney debit, GamaMoney credit) {
        this.account = account;
        this.debit = debit;
        this.credit = credit;
    }

    public GLOpeningBalanceOperationDto(GLOperationAccount account, DocRC rc, GamaMoney debit, GamaMoney credit) {
        this(account, debit, credit);
        if (rc != null) {
            this.rc = new ArrayList<>();
            this.rc.add(rc);
        }
    }

    @Override
    public int compareTo(GLOpeningBalanceOperationDto o) {
        return ComparisonChain.start()
                .compare(debit, o.debit)
                .compare(credit, o.credit)
                .result();
    }

    // generated

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

    @Override
    public String toString() {
        return "GLOpeningBalanceOperationDto{" +
                "account=" + account +
                ", debit=" + debit +
                ", credit=" + credit +
                ", rc=" + rc +
                "} " + super.toString();
    }
}

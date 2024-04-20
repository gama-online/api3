package lt.gama.impexp.entity;

import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.doc.DocRC;
import lt.gama.model.type.gl.GLOperationAccount;

/**
 * Gama
 * Created by valdas on 2015-10-20.
 */
public class GLOperationExport {

    private GLOperationAccount debit;

    private GLOperationAccount credit;

    private GamaMoney sum;

    private DocRC debitRC;

    private DocRC creditRC;

    // generated

    public GLOperationAccount getDebit() {
        return debit;
    }

    public void setDebit(GLOperationAccount debit) {
        this.debit = debit;
    }

    public GLOperationAccount getCredit() {
        return credit;
    }

    public void setCredit(GLOperationAccount credit) {
        this.credit = credit;
    }

    public GamaMoney getSum() {
        return sum;
    }

    public void setSum(GamaMoney sum) {
        this.sum = sum;
    }

    public DocRC getDebitRC() {
        return debitRC;
    }

    public void setDebitRC(DocRC debitRC) {
        this.debitRC = debitRC;
    }

    public DocRC getCreditRC() {
        return creditRC;
    }

    public void setCreditRC(DocRC creditRC) {
        this.creditRC = creditRC;
    }

    @Override
    public String toString() {
        return "GLOperationExport{" +
                "debit=" + debit +
                ", credit=" + credit +
                ", sum=" + sum +
                ", debitRC=" + debitRC +
                ", creditRC=" + creditRC +
                '}';
    }
}

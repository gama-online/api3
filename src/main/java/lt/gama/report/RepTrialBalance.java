package lt.gama.report;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import lt.gama.helpers.GamaMoneyUtils;
import lt.gama.helpers.StringHelper;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.doc.DocRC;
import lt.gama.model.type.gl.GLOperationAccount;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * gama-online
 * Created by valdas on 15-03-05.
 */
public class RepTrialBalance implements Serializable, Comparable<RepTrialBalance> {

    @Serial
    private static final long serialVersionUID = -1L;

    private DocRC rc;

    private GLOperationAccount account;

    private GamaMoney opening;

    private GamaMoney debit;

    private GamaMoney credit;


    @SuppressWarnings("unused")
    protected RepTrialBalance() {}

    public RepTrialBalance(String number, String name, String currency, BigDecimal ob,
                           BigDecimal debit, BigDecimal credit) {
        this.account = new GLOperationAccount(number, name);
        this.opening = GamaMoney.ofNullable(currency, ob);
        this.debit = GamaMoney.ofNullable(currency, debit);
        this.credit = GamaMoney.ofNullable(currency, credit);
    }

    public RepTrialBalance(String rcName, BigInteger rcId, String number, String name, String currency, BigDecimal ob,
                           BigDecimal debit, BigDecimal credit) {
        this(number, name, currency, ob, debit, credit);
        if (StringHelper.hasValue(rcName)) {
            this.rc = new DocRC(rcId.longValue(), rcName);
        }
    }

    public RepTrialBalance(DocRC rc, GLOperationAccount account) {
        this.rc = rc;
        this.account = account;
    }

    public GamaMoney getOpeningDebit() {
        return GamaMoneyUtils.isPositive(opening) ? opening : null;
    }

    public GamaMoney getOpeningCredit() {
        return GamaMoneyUtils.isNegative(opening) ? opening.negated() : null;
    }

    public GamaMoney getFinalDebit() {
        GamaMoney total = GamaMoneyUtils.total(opening, debit, GamaMoneyUtils.negated(credit));
        return GamaMoneyUtils.isPositive(total) ? total : null;
    }

    public GamaMoney getFinalCredit() {
        GamaMoney total = GamaMoneyUtils.total(opening, debit, GamaMoneyUtils.negated(credit));
        return GamaMoneyUtils.isNegative(total) ? total.negated() : null;
    }

	@Override
	public int compareTo(RepTrialBalance o) {
        return ComparisonChain.start()
                .compare(rc, o.rc, Ordering.natural().nullsFirst())
                .compare(account, o.account, Ordering. natural().nullsFirst())
                .result();
	}

    // generated

    public DocRC getRc() {
        return rc;
    }

    public void setRc(DocRC rc) {
        this.rc = rc;
    }

    public GLOperationAccount getAccount() {
        return account;
    }

    public void setAccount(GLOperationAccount account) {
        this.account = account;
    }

    public GamaMoney getOpening() {
        return opening;
    }

    public void setOpening(GamaMoney opening) {
        this.opening = opening;
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

    @Override
    public String toString() {
        return "RepTrialBalance{" +
                "rc=" + rc +
                ", account=" + account +
                ", opening=" + opening +
                ", debit=" + debit +
                ", credit=" + credit +
                '}';
    }
}

package lt.gama.model.type.balance;

import lt.gama.model.type.GamaMoney;

/**
 * Gama
 * Created by valdas on 15-03-21.
 */
public class GLAccountBalance {

    private String account;

    private GamaMoney debit;

    private GamaMoney credit;

    // generated

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
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

    @Override
    public String toString() {
        return "GLAccountBalance{" +
                "account='" + account + '\'' +
                ", debit=" + debit +
                ", credit=" + credit +
                '}';
    }
}

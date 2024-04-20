package lt.gama.model.type.balance;

import lt.gama.model.type.GamaMoney;

/**
 * Gama
 * Created by valdas on 15-03-28.
 */
public class Balance {

    private GamaMoney debit;

    private GamaMoney credit;

    // generated

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
        return "Balance{" +
                "debit=" + debit +
                ", credit=" + credit +
                '}';
    }
}

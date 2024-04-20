package lt.gama.report;

import lt.gama.helpers.GamaMoneyUtils;
import lt.gama.model.type.GamaMoney;

/**
 * gama-online
 * Created by valdas on 2016-04-30.
 */
public class RepMoneyDetailCurrency {

    private GamaMoney opening;

    private GamaMoney debit;

    private GamaMoney credit;

    private GamaMoney baseOpening;

    private GamaMoney baseDebit;

    private GamaMoney baseCredit;


    public GamaMoney getOpeningDebit() {
        return GamaMoneyUtils.isPositive(opening) ? opening : null;
    }

    public GamaMoney getOpeningCredit() {
        return GamaMoneyUtils.isNegative(opening) ? opening.negated() : null;
    }

    public GamaMoney getRemainder() {
        return GamaMoneyUtils.subtract(GamaMoneyUtils.add(opening, debit), credit);
    }

    public GamaMoney getRemainderDebit() {
        GamaMoney remainder = getRemainder();
        return GamaMoneyUtils.isPositive(remainder) ? remainder : null;
    }

    public GamaMoney getRemainderCredit() {
        GamaMoney remainder = getRemainder();
        return GamaMoneyUtils.isNegative(remainder) ? remainder.negated() : null;
    }

    public GamaMoney getBaseRemainder() {
        return GamaMoneyUtils.subtract(GamaMoneyUtils.add(baseOpening, baseDebit), baseCredit);
    }

    // generated

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

    public GamaMoney getBaseOpening() {
        return baseOpening;
    }

    public void setBaseOpening(GamaMoney baseOpening) {
        this.baseOpening = baseOpening;
    }

    public GamaMoney getBaseDebit() {
        return baseDebit;
    }

    public void setBaseDebit(GamaMoney baseDebit) {
        this.baseDebit = baseDebit;
    }

    public GamaMoney getBaseCredit() {
        return baseCredit;
    }

    public void setBaseCredit(GamaMoney baseCredit) {
        this.baseCredit = baseCredit;
    }

    @Override
    public String toString() {
        return "RepMoneyDetailCurrency{" +
                "opening=" + opening +
                ", debit=" + debit +
                ", credit=" + credit +
                ", baseOpening=" + baseOpening +
                ", baseDebit=" + baseDebit +
                ", baseCredit=" + baseCredit +
                '}';
    }
}

package lt.gama.report;

import lt.gama.helpers.GamaMoneyUtils;
import lt.gama.model.type.GamaMoney;

/**
 * gama-online
 * Created by valdas on 2016-04-28.
 */
public class RepDebtDetailCurrency {

    private GamaMoney debtFrom;

    private GamaMoney debit;

    private GamaMoney credit;

    private GamaMoney debtTo;

    private GamaMoney baseDebtFrom;

    private GamaMoney baseDebit;

    private GamaMoney baseCredit;

    private GamaMoney baseDebtTo;

    // all these getters are need for reconciliation statement form:

    public GamaMoney getDebitFrom() {
        return GamaMoneyUtils.isPositive(debtFrom) ? debtFrom : null;
    }

    public GamaMoney getBaseDebitFrom() {
        return GamaMoneyUtils.isPositive(baseDebtFrom) ? baseDebtFrom : null;
    }

    public GamaMoney getCreditFrom() {
        return GamaMoneyUtils.isNegative(debtFrom) ? debtFrom.negated() : null;
    }

    public GamaMoney getBaseCreditFrom() {
        return GamaMoneyUtils.isNegative(baseDebtFrom) ? baseDebtFrom.negated() : null;
    }

    public GamaMoney getDebitTo() {
        return GamaMoneyUtils.isPositive(debtTo) ? debtTo : null;
    }

    public GamaMoney getBaseDebitTo() {
        return GamaMoneyUtils.isPositive(baseDebtTo) ? baseDebtTo : null;
    }

    public GamaMoney getCreditTo() {
        return GamaMoneyUtils.isNegative(debtTo) ? debtTo.negated() : null;
    }

    public GamaMoney getBaseCreditTo() {
        return GamaMoneyUtils.isNegative(baseDebtTo) ? baseDebtTo.negated() : null;
    }

    // generated

    public GamaMoney getDebtFrom() {
        return debtFrom;
    }

    public void setDebtFrom(GamaMoney debtFrom) {
        this.debtFrom = debtFrom;
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

    public GamaMoney getDebtTo() {
        return debtTo;
    }

    public void setDebtTo(GamaMoney debtTo) {
        this.debtTo = debtTo;
    }

    public GamaMoney getBaseDebtFrom() {
        return baseDebtFrom;
    }

    public void setBaseDebtFrom(GamaMoney baseDebtFrom) {
        this.baseDebtFrom = baseDebtFrom;
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

    public GamaMoney getBaseDebtTo() {
        return baseDebtTo;
    }

    public void setBaseDebtTo(GamaMoney baseDebtTo) {
        this.baseDebtTo = baseDebtTo;
    }

    @Override
    public String toString() {
        return "RepDebtDetailCurrency{" +
                "debtFrom=" + debtFrom +
                ", debit=" + debit +
                ", credit=" + credit +
                ", debtTo=" + debtTo +
                ", baseDebtFrom=" + baseDebtFrom +
                ", baseDebit=" + baseDebit +
                ", baseCredit=" + baseCredit +
                ", baseDebtTo=" + baseDebtTo +
                '}';
    }
}

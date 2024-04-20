package lt.gama.report;

import lt.gama.helpers.GamaMoneyUtils;
import lt.gama.model.type.Exchange;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.doc.DocCounterparty;
import lt.gama.model.type.enums.DebtType;

/**
 * Gama
 * Created by valdas on 15-04-27.
 */
public class RepDebtBalance {

    private DocCounterparty counterparty;

    private DebtType type;

    private GamaMoney debt;

    private GamaMoney debit;

    private GamaMoney credit;

    private GamaMoney baseDebt;

    private GamaMoney baseDebit;

    private GamaMoney baseCredit;

    private GamaMoney baseNowDebt;

    private GamaMoney baseNowDebit;

    private GamaMoney baseNowCredit;


    private Exchange exchange;

    private GamaMoney fix;

    /*
     * Synthetics properties - generated on get
     * @return debt remainder
     */

    public GamaMoney getDebtR() {
        GamaMoney debtR = GamaMoneyUtils.subtract(GamaMoneyUtils.add(debt, debit), credit);
        return GamaMoneyUtils.isZero(debtR) ? null : debtR;
    }

    public GamaMoney getBaseDebtR() {
        GamaMoney baseDebtR = GamaMoneyUtils.subtract(GamaMoneyUtils.add(baseDebt, baseDebit), baseCredit);
        return GamaMoneyUtils.isZero(baseDebtR) ? null : baseDebtR;
    }

    public GamaMoney getBaseNowDebtR() {
        GamaMoney baseDebtNowR = GamaMoneyUtils.subtract(GamaMoneyUtils.add(baseNowDebt, baseNowDebit), baseNowCredit);
        return GamaMoneyUtils.isZero(baseDebtNowR) ? null : baseDebtNowR;
    }

    public String getCurrency() {
        return debt != null ? debt.getCurrency()
                : debit != null ? debit.getCurrency()
                : credit != null ? credit.getCurrency()
                : exchange != null ? exchange.getCurrency()
                : null;
    }

    public String getBaseCurrency() {
        return baseDebt != null ? baseDebt.getCurrency()
                : baseDebit != null ? baseDebit.getCurrency()
                : baseCredit != null ? baseCredit.getCurrency()
                : exchange != null ? exchange.getBase()
                : null;
    }

    // generated

    public DocCounterparty getCounterparty() {
        return counterparty;
    }

    public void setCounterparty(DocCounterparty counterparty) {
        this.counterparty = counterparty;
    }

    public DebtType getType() {
        return type;
    }

    public void setType(DebtType type) {
        this.type = type;
    }

    public GamaMoney getDebt() {
        return debt;
    }

    public void setDebt(GamaMoney debt) {
        this.debt = debt;
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

    public GamaMoney getBaseDebt() {
        return baseDebt;
    }

    public void setBaseDebt(GamaMoney baseDebt) {
        this.baseDebt = baseDebt;
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

    public GamaMoney getBaseNowDebt() {
        return baseNowDebt;
    }

    public void setBaseNowDebt(GamaMoney baseNowDebt) {
        this.baseNowDebt = baseNowDebt;
    }

    public GamaMoney getBaseNowDebit() {
        return baseNowDebit;
    }

    public void setBaseNowDebit(GamaMoney baseNowDebit) {
        this.baseNowDebit = baseNowDebit;
    }

    public GamaMoney getBaseNowCredit() {
        return baseNowCredit;
    }

    public void setBaseNowCredit(GamaMoney baseNowCredit) {
        this.baseNowCredit = baseNowCredit;
    }

    public Exchange getExchange() {
        return exchange;
    }

    public void setExchange(Exchange exchange) {
        this.exchange = exchange;
    }

    public GamaMoney getFix() {
        return fix;
    }

    public void setFix(GamaMoney fix) {
        this.fix = fix;
    }

    @Override
    public String toString() {
        return "RepDebtBalance{" +
                "counterparty=" + counterparty +
                ", type=" + type +
                ", debt=" + debt +
                ", debit=" + debit +
                ", credit=" + credit +
                ", baseDebt=" + baseDebt +
                ", baseDebit=" + baseDebit +
                ", baseCredit=" + baseCredit +
                ", baseNowDebt=" + baseNowDebt +
                ", baseNowDebit=" + baseNowDebit +
                ", baseNowCredit=" + baseNowCredit +
                ", exchange=" + exchange +
                ", fix=" + fix +
                '}';
    }
}

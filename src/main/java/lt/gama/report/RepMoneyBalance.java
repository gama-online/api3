package lt.gama.report;

import lt.gama.helpers.GamaMoneyUtils;
import lt.gama.model.type.Exchange;
import lt.gama.model.type.GamaMoney;

/**
 * Gama
 * Created by valdas on 15-05-11.
 */
public class RepMoneyBalance<E> {

    private String currency;


    private GamaMoney opening;

    private GamaMoney debit;

    private GamaMoney credit;


    private GamaMoney baseOpening;

    private GamaMoney baseDebit;

    private GamaMoney baseCredit;


    private GamaMoney baseNowOpening;

    private GamaMoney baseNowDebit;

    private GamaMoney baseNowCredit;


    private Exchange exchange;

    private GamaMoney fix;


    private E object;


    @SuppressWarnings("unused")
    protected RepMoneyBalance() {}

    public RepMoneyBalance(E object) {
        this.object = object;
    }

    public GamaMoney getRemainder() {
        return GamaMoneyUtils.subtract(GamaMoneyUtils.add(opening, debit), credit);
    }

    public GamaMoney getBaseRemainder() {
        return GamaMoneyUtils.subtract(GamaMoneyUtils.add(baseOpening, baseDebit), baseCredit);
    }

    public GamaMoney getBaseNowRemainder() {
        return GamaMoneyUtils.subtract(GamaMoneyUtils.add(baseNowOpening, baseNowDebit), baseNowCredit);
    }

    // generated

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
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

    public GamaMoney getBaseNowOpening() {
        return baseNowOpening;
    }

    public void setBaseNowOpening(GamaMoney baseNowOpening) {
        this.baseNowOpening = baseNowOpening;
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

    public E getObject() {
        return object;
    }

    public void setObject(E object) {
        this.object = object;
    }

    @Override
    public String toString() {
        return "RepMoneyBalance{" +
                "currency='" + currency + '\'' +
                ", opening=" + opening +
                ", debit=" + debit +
                ", credit=" + credit +
                ", baseOpening=" + baseOpening +
                ", baseDebit=" + baseDebit +
                ", baseCredit=" + baseCredit +
                ", baseNowOpening=" + baseNowOpening +
                ", baseNowDebit=" + baseNowDebit +
                ", baseNowCredit=" + baseNowCredit +
                ", exchange=" + exchange +
                ", fix=" + fix +
                ", object=" + object +
                '}';
    }
}

package lt.gama.model.type.balance;

import lt.gama.helpers.GamaMoneyUtils;
import lt.gama.model.type.Exchange;
import lt.gama.model.type.GamaMoney;

import java.time.LocalDate;

/**
 * Gama
 * Created by valdas on 15-03-16.
 */
public class BalanceDay {

    private LocalDate date;

    private GamaMoney debit;

    private GamaMoney credit;

    private Exchange exchange;

    private GamaMoney baseDebit;

    private GamaMoney baseCredit;

    /**
     * if inventory record
     */
    private boolean inventory;


    @SuppressWarnings("unused")
    protected BalanceDay() {
    }

    public BalanceDay(LocalDate date, Exchange exchange) {
        this.date = date;
        this.exchange = exchange;
    }

    public void setSums(GamaMoney sum, GamaMoney baseSum, boolean recall) {

        if (recall) {

            if (GamaMoneyUtils.isPositive(sum)) {
                GamaMoney credit = GamaMoneyUtils.subtract(getCredit(), sum);
                if (GamaMoneyUtils.isZero(credit)) {
                    setCredit(null);
                } else if (GamaMoneyUtils.isNegative(credit)) {
                    setCredit(null);
                    setDebit(GamaMoneyUtils.subtract(getDebit(), credit));
                } else {
                    setCredit(credit);
                }
            } else if (GamaMoneyUtils.isNegative(sum)) {
                GamaMoney debit = GamaMoneyUtils.add(getDebit(), sum);
                if (GamaMoneyUtils.isZero(debit)) {
                    setDebit(null);
                } else if (GamaMoneyUtils.isNegative(debit)) {
                    setDebit(null);
                    setCredit(GamaMoneyUtils.subtract(getCredit(), debit));
                } else {
                    setDebit(debit);
                }
            }

            if (GamaMoneyUtils.isPositive(baseSum)) {
                GamaMoney baseCredit = GamaMoneyUtils.subtract(getBaseCredit(), baseSum);
                if (GamaMoneyUtils.isZero(baseCredit)) {
                    setBaseCredit(null);
                } else if (GamaMoneyUtils.isNegative(baseCredit)) {
                    setBaseCredit(null);
                    setBaseDebit(GamaMoneyUtils.subtract(getBaseDebit(), baseCredit));
                } else {
                    setBaseCredit(baseCredit);
                }
            } else if (GamaMoneyUtils.isNegative(baseSum)) {
                GamaMoney baseDebit = GamaMoneyUtils.add(getBaseDebit(), baseSum);

                if (GamaMoneyUtils.isZero(baseDebit)) {
                    setBaseDebit(null);
                } else if (GamaMoneyUtils.isNegative(baseDebit)) {
                    setBaseDebit(null);
                    setBaseCredit(GamaMoneyUtils.subtract(getBaseCredit(), baseDebit));
                } else {
                    setBaseDebit(baseDebit);
                }
            }

        } else {

            if (GamaMoneyUtils.isPositive(sum)) {
                setDebit(GamaMoneyUtils.add(getDebit(), sum));
            } else if (GamaMoneyUtils.isNegative(sum)) {
                setCredit(GamaMoneyUtils.subtract(getCredit(), sum));
            }

            if (GamaMoneyUtils.isPositive(baseSum)) {
                setBaseDebit(GamaMoneyUtils.add(getBaseDebit(), baseSum));
            } else if (GamaMoneyUtils.isNegative(baseSum)) {
                setBaseCredit(GamaMoneyUtils.subtract(getBaseCredit(), baseSum));
            }
        }
    }

    // generated

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
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

    public Exchange getExchange() {
        return exchange;
    }

    public void setExchange(Exchange exchange) {
        this.exchange = exchange;
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

    public boolean isInventory() {
        return inventory;
    }

    public void setInventory(boolean inventory) {
        this.inventory = inventory;
    }

    @Override
    public String toString() {
        return "BalanceDay{" +
                "date=" + date +
                ", debit=" + debit +
                ", credit=" + credit +
                ", exchange=" + exchange +
                ", baseDebit=" + baseDebit +
                ", baseCredit=" + baseCredit +
                ", inventory=" + inventory +
                '}';
    }
}

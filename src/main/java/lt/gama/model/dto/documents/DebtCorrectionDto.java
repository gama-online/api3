package lt.gama.model.dto.documents;

import lt.gama.model.dto.base.BaseDebtDocumentDto;
import lt.gama.model.i.IExchangeAmount;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.enums.DebtType;

public class DebtCorrectionDto extends BaseDebtDocumentDto implements IExchangeAmount {

    private GamaMoney amount;

    private GamaMoney baseAmount;

    private DebtType debit;

    private DebtType credit;

    /**
     * Is currency correction record,
     * i.e. amount is zero but baseAmount has some value to correct currency exchange rate fluctuations
     */
    private Boolean correction;


    @Override
    public GamaMoney getDebt() {
        return null;
    }

    @Override
    public GamaMoney getBaseDebt() {
        return null;
    }

    // for import compatibility

    public void setSum(GamaMoney amount) {
        this.amount = amount;
    }

    public void setBaseSum(GamaMoney baseAmount) {
        this.baseAmount = baseAmount;
    }

    // generated

    @Override
    public GamaMoney getAmount() {
        return amount;
    }

    @Override
    public void setAmount(GamaMoney amount) {
        this.amount = amount;
    }

    @Override
    public GamaMoney getBaseAmount() {
        return baseAmount;
    }

    @Override
    public void setBaseAmount(GamaMoney baseAmount) {
        this.baseAmount = baseAmount;
    }

    public DebtType getDebit() {
        return debit;
    }

    public void setDebit(DebtType debit) {
        this.debit = debit;
    }

    public DebtType getCredit() {
        return credit;
    }

    public void setCredit(DebtType credit) {
        this.credit = credit;
    }

    public Boolean getCorrection() {
        return correction;
    }

    public void setCorrection(Boolean correction) {
        this.correction = correction;
    }

    @Override
    public String toString() {
        return "DebtCorrectionDto{" +
                "amount=" + amount +
                ", baseAmount=" + baseAmount +
                ", debit=" + debit +
                ", credit=" + credit +
                ", correction=" + correction +
                "} " + super.toString();
    }
}

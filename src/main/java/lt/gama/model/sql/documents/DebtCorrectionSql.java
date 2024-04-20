package lt.gama.model.sql.documents;

import jakarta.persistence.*;
import lt.gama.model.i.IExchangeAmount;
import lt.gama.model.sql.base.BaseDebtDocumentSql;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.enums.DebtType;


@Entity
@Table(name = "debt_corrections")
@NamedEntityGraph(name = DebtCorrectionSql.GRAPH_ALL, attributeNodes = @NamedAttributeNode(DebtCorrectionSql_.COUNTERPARTY))
public class DebtCorrectionSql extends BaseDebtDocumentSql implements IExchangeAmount {

    public static final String GRAPH_ALL = "graph.DebtCorrection.all";

    @Embedded
    private GamaMoney amount;

    @Embedded
    private GamaMoney baseAmount;

    private DebtType debit;

    private DebtType credit;

    /**
     * Is currency correction record,
     * i.e. sum is zero but baseSum has some value to correct currency exchange rate fluctuations
     */
    private Boolean correction;


    public boolean isCorrection() {
        return correction != null && correction;
    }

    @Override
    public GamaMoney getDebt() {
        return null;
    }

    @Override
    public GamaMoney getBaseDebt() {
        return null;
    }

    // generated
    // except getCorrection()

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

    public void setCorrection(Boolean correction) {
        this.correction = correction;
    }

    @Override
    public String toString() {
        return "DebtCorrectionSql{" +
                "amount=" + amount +
                ", baseAmount=" + baseAmount +
                ", debit=" + debit +
                ", credit=" + credit +
                ", correction=" + correction +
                "} " + super.toString();
    }
}

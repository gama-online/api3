package lt.gama.model.dto.entities;

import lt.gama.helpers.GamaMoneyUtils;
import lt.gama.model.type.doc.Doc;
import lt.gama.model.type.doc.DocDebt;
import lt.gama.model.dto.base.BaseCompanyDto;
import lt.gama.model.i.IDebtDto;
import lt.gama.model.i.IDoc;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.enums.DebtType;

import java.io.Serial;
import java.util.List;

public class DebtCoverageDto extends BaseCompanyDto implements IDebtDto, IDoc {

    @Serial
    private static final long serialVersionUID = -1L;

    private CounterpartyDto counterparty;

    private DebtType type;

    private long companyId;

    /**
     * Source document
     */
    private Doc doc;

    /**
     * Document's total amount
     */
    private GamaMoney amount;

    /**
     * Amount covered (must be not greater than total amount)
     */
    private GamaMoney covered;

    /**
     * Coverage documents
     */
    private List<DocDebt> docs;

    /**
     * Is the document finished? - i.e. covered 100%
     */
    private Boolean finished;

    @Override
    public long getCounterpartyId() {
        return counterparty.getId();
    }

    public String getCurrency() {
        return amount != null ? amount.getCurrency() :
                covered != null ? covered.getCurrency() : null;
    }

    public GamaMoney getRemainder() {
        return GamaMoneyUtils.subtract(amount, covered);
    }

    // generated

    @Override
    public CounterpartyDto getCounterparty() {
        return counterparty;
    }

    @Override
    public void setCounterparty(CounterpartyDto counterparty) {
        this.counterparty = counterparty;
    }

    @Override
    public DebtType getType() {
        return type;
    }

    public void setType(DebtType type) {
        this.type = type;
    }

    @Override
    public long getCompanyId() {
        return companyId;
    }

    @Override
    public void setCompanyId(long companyId) {
        this.companyId = companyId;
    }

    @Override
    public Doc getDoc() {
        return doc;
    }

    @Override
    public void setDoc(Doc doc) {
        this.doc = doc;
    }

    public GamaMoney getAmount() {
        return amount;
    }

    public void setAmount(GamaMoney amount) {
        this.amount = amount;
    }

    public GamaMoney getCovered() {
        return covered;
    }

    public void setCovered(GamaMoney covered) {
        this.covered = covered;
    }

    public List<DocDebt> getDocs() {
        return docs;
    }

    public void setDocs(List<DocDebt> docs) {
        this.docs = docs;
    }

    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    @Override
    public String toString() {
        return "DebtCoverageDto{" +
                "counterparty=" + counterparty +
                ", type=" + type +
                ", companyId=" + companyId +
                ", doc=" + doc +
                ", amount=" + amount +
                ", covered=" + covered +
                ", docs=" + docs +
                ", finished=" + finished +
                "} " + super.toString();
    }
}


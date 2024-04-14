package lt.gama.model.dto.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lt.gama.helpers.GamaMoneyUtils;
import lt.gama.model.type.doc.Doc;
import lt.gama.model.dto.base.BaseCompanyDto;
import lt.gama.model.i.ICurrency;
import lt.gama.model.i.IDebtDto;
import lt.gama.model.i.IDoc;
import lt.gama.model.type.Exchange;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.enums.DebtType;

import java.io.Serial;
import java.time.LocalDate;

public class DebtHistoryDto extends BaseCompanyDto implements IDebtDto, IDoc, ICurrency {

    @Serial
    private static final long serialVersionUID = -1L;

    private CounterpartyDto counterparty;

    private Doc doc;

    /**
     * Debt
     */
    private GamaMoney debt;

    private GamaMoney baseDebt;

    private DebtType type;

    private Exchange exchange;

    private LocalDate dueDate;


    @SuppressWarnings("unused")
    protected DebtHistoryDto() {}

    public DebtHistoryDto(long companyId, CounterpartyDto counterparty, Doc doc, DebtType type, Exchange exchange, GamaMoney debt) {
        setCompanyId(companyId);
        this.counterparty = counterparty;
        this.doc = doc;
        this.type = type;
        this.exchange = exchange;
        this.debt = debt;
    }

    @Override
    public long getCounterpartyId() {
        return counterparty.getId();
    }

    @Override
    public String getCurrency() {
        return exchange.getCurrency();
    }

    @JsonIgnore
    public GamaMoney getDebit() {
        return GamaMoneyUtils.isPositive(debt) ? debt : null;
    }

    @JsonIgnore
    public GamaMoney getBaseDebit() {
        return GamaMoneyUtils.isPositive(baseDebt) ? baseDebt : null;
    }

    @JsonIgnore
    public GamaMoney getCredit() {
        return GamaMoneyUtils.isNegative(debt) ? debt.negated() : null;
    }

    @JsonIgnore
    public GamaMoney getBaseCredit() {
        return GamaMoneyUtils.isNegative(baseDebt) ? baseDebt.negated() : null;
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
    public Doc getDoc() {
        return doc;
    }

    @Override
    public void setDoc(Doc doc) {
        this.doc = doc;
    }

    public GamaMoney getDebt() {
        return debt;
    }

    public void setDebt(GamaMoney debt) {
        this.debt = debt;
    }

    public GamaMoney getBaseDebt() {
        return baseDebt;
    }

    public void setBaseDebt(GamaMoney baseDebt) {
        this.baseDebt = baseDebt;
    }

    @Override
    public DebtType getType() {
        return type;
    }

    public void setType(DebtType type) {
        this.type = type;
    }

    public Exchange getExchange() {
        return exchange;
    }

    public void setExchange(Exchange exchange) {
        this.exchange = exchange;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    @Override
    public String toString() {
        return "DebtHistoryDto{" +
                "counterparty=" + counterparty +
                ", doc=" + doc +
                ", debt=" + debt +
                ", baseDebt=" + baseDebt +
                ", type=" + type +
                ", exchange=" + exchange +
                ", dueDate=" + dueDate +
                "} " + super.toString();
    }
}

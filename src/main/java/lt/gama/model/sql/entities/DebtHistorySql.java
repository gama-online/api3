package lt.gama.model.sql.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lt.gama.helpers.GamaMoneyUtils;
import lt.gama.helpers.Validators;
import lt.gama.model.i.ICurrency;
import lt.gama.model.i.IDebtSql;
import lt.gama.model.i.IDoc;
import lt.gama.model.sql.base.BaseCompanySql;
import lt.gama.model.type.Exchange;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.doc.Doc;
import lt.gama.model.type.enums.DebtType;
import lt.gama.service.TranslationService;

import java.time.LocalDate;

@Entity
@Table(name = "debt_history")
@NamedEntityGraph(name = DebtHistorySql.GRAPH_ALL, attributeNodes = @NamedAttributeNode(DebtHistorySql_.COUNTERPARTY))
public class DebtHistorySql extends BaseCompanySql implements IDebtSql, IDoc, ICurrency {

    public static final String GRAPH_ALL = "graph.DebtHistorySql.all";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counterparty_id")
    private CounterpartySql counterparty;

    @Embedded
    private Doc doc;

    /**
     * Debt
     */
    @Embedded
    private GamaMoney debt;

    @Embedded
    private GamaMoney baseDebt;

    private DebtType type;

    @Embedded
    private Exchange exchange;

    private LocalDate dueDate;


    public DebtHistorySql() {
    }

    public DebtHistorySql(long companyId, CounterpartySql counterparty, DebtType type, Doc doc,
                          GamaMoney debt, GamaMoney baseDebt, Exchange exchange, LocalDate dueDate) {
        setCompanyId(companyId);
        this.counterparty = counterparty;
        this.type = type;
        this.doc = doc;
        this.debt = debt;
        this.baseDebt = baseDebt;
        this.exchange = exchange;
        this.dueDate = dueDate;
    }

    @Override
    public long getCounterpartyId() {
        Validators.checkNotNull(counterparty, TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoCounterparty));
        return counterparty.getId();
    }

    public String getCurrency() {
        return debt != null ? debt.getCurrency() : exchange != null ? exchange.getCurrency() : null;
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

    /**
     * toString except counterparty
     */
    @Override
    public String toString() {
        return "DebtHistorySql{" +
                "doc=" + doc +
                ", debt=" + debt +
                ", baseDebt=" + baseDebt +
                ", type=" + type +
                ", exchange=" + exchange +
                ", dueDate=" + dueDate +
                "} " + super.toString();
    }

    // generated

    @Override
    public CounterpartySql getCounterparty() {
        return counterparty;
    }

    @Override
    public void setCounterparty(CounterpartySql counterparty) {
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
}

package lt.gama.model.sql.entities;

import jakarta.persistence.*;
import lt.gama.helpers.GamaMoneyUtils;
import lt.gama.helpers.Validators;
import lt.gama.model.i.IDebtSql;
import lt.gama.model.i.IDoc;
import lt.gama.model.sql.base.BaseCompanySql;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.doc.Doc;
import lt.gama.model.type.enums.DBType;
import lt.gama.model.type.enums.DebtType;
import lt.gama.service.TranslationService;

import java.time.LocalDate;

@Entity
@Table(name = "debt_now")
@NamedEntityGraph(name = DebtNowSql.GRAPH_ALL, attributeNodes = @NamedAttributeNode(DebtNowSql_.COUNTERPARTY))
public class DebtNowSql extends BaseCompanySql implements IDebtSql, IDoc {

    public static final String GRAPH_ALL = "graph.DebtNowSql.all";

    @Transient
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counterparty_id")
    private CounterpartySql counterparty;

    private DebtType type;

    @Embedded
    private Doc doc;

    @Embedded
    private GamaMoney initial;

    @Embedded
    private GamaMoney remainder;

    private LocalDate dueDate;


    public DebtNowSql() {
    }

    public DebtNowSql(long companyId, CounterpartySql counterparty, DebtType type, Doc doc,
                      LocalDate dueDate, GamaMoney initial, GamaMoney remainder) {
        setCompanyId(companyId);
        this.doc = Validators.checkNotNull(doc, "No doc");
        if (this.doc.getDb() == null) this.doc.setDb(DBType.DATASTORE);
        this.counterparty = counterparty;
        this.type = type;
        this.dueDate = dueDate == null ? doc.getDate() : dueDate;
        this.initial = initial;
        this.remainder = remainder;
    }

    @Override
    public long getCounterpartyId() {
        Validators.checkNotNull(counterparty, TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoCounterparty));
        return counterparty.getId();
    }

    public GamaMoney getCovered() {
        return GamaMoneyUtils.subtract(initial, remainder);
    }

    public String getCurrency() {
        return initial != null ? initial.getCurrency() :
                remainder != null ? remainder.getCurrency() : null;
    }


    /**
     * toString except counterparty
     */
    @Override
    public String toString() {
        return "DebtNowSql{" +
                "token='" + token + '\'' +
                ", type=" + type +
                ", doc=" + doc +
                ", initial=" + initial +
                ", remainder=" + remainder +
                ", dueDate=" + dueDate +
                "} " + super.toString();
    }

    // generated

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public CounterpartySql getCounterparty() {
        return counterparty;
    }

    @Override
    public void setCounterparty(CounterpartySql counterparty) {
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
    public Doc getDoc() {
        return doc;
    }

    @Override
    public void setDoc(Doc doc) {
        this.doc = doc;
    }

    public GamaMoney getInitial() {
        return initial;
    }

    public void setInitial(GamaMoney initial) {
        this.initial = initial;
    }

    public GamaMoney getRemainder() {
        return remainder;
    }

    public void setRemainder(GamaMoney remainder) {
        this.remainder = remainder;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}


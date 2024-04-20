package lt.gama.model.sql.entities;

import jakarta.persistence.*;
import lt.gama.helpers.GamaMoneyUtils;
import lt.gama.helpers.Validators;
import lt.gama.model.i.IDebtSql;
import lt.gama.model.i.IDoc;
import lt.gama.model.sql.base.BaseCompanySql;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.doc.Doc;
import lt.gama.model.type.doc.DocDebt;
import lt.gama.model.type.enums.DebtType;
import lt.gama.service.TranslationService;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "debt_coverage")
@NamedEntityGraph(name = DebtCoverageSql.GRAPH_ALL, attributeNodes = @NamedAttributeNode(DebtCoverageSql_.COUNTERPARTY))
public class DebtCoverageSql extends BaseCompanySql implements IDebtSql, IDoc {

    public static final String GRAPH_ALL = "graph.DebtCoverageSql.all";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counterparty_id")
    private CounterpartySql counterparty;

    private DebtType type;

    /**
     * Source document
     */
    @Embedded
    private Doc doc;

    /**
     * Document's total amount
     */
    @Embedded
    private GamaMoney amount;

    /**
     * Amount covered (must be not greater than total amount)
     */
    @Embedded
    private GamaMoney covered;

    /**
     * Coverage documents
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private List<DocDebt> docs;

    /**
     * Is the document finished? - i.e. covered 100%
     */
    private Boolean finished;


    public DebtCoverageSql() {
    }

    public DebtCoverageSql(long companyId, CounterpartySql counterparty, DebtType type, Doc doc, GamaMoney amount) {
        setCompanyId(companyId);
        this.doc = Validators.checkNotNull(doc, TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDocument));
        this.counterparty = Validators.checkValid(counterparty, TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoCounterparty));
        this.type = type;
        this.amount = amount;
    }

    @Override
    public long getCounterpartyId() {
        return Validators.checkNotNull(counterparty, TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoCounterparty)).getId();
    }

    public String getCurrency() {
        return amount != null ? amount.getCurrency() :
                covered != null ? covered.getCurrency() : null;
    }

    public GamaMoney getRemainder() {
        return GamaMoneyUtils.subtract(amount, covered);
    }

    @SuppressWarnings("unused")
    @PrePersist
    @PreUpdate
    private void onSave() {
        finished = GamaMoneyUtils.isEqual(amount, covered);
    }

    /**
     * toString except counterparty
     * and include getRemainder(), getCounterpartyId()
     */
    @Override
    public String toString() {
        return "DebtCoverageSql{" +
                "counterpartyId=" + getCounterpartyId() +
                ", type=" + type +
                ", doc=" + doc +
                ", amount=" + amount +
                ", covered=" + covered +
                ", docs=" + docs +
                ", finished=" + finished +
                ", remainder=" + getRemainder() +
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
}


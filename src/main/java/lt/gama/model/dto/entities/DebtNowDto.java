package lt.gama.model.dto.entities;

import lt.gama.helpers.GamaMoneyUtils;
import lt.gama.model.type.doc.Doc;
import lt.gama.model.dto.base.BaseCompanyDto;
import lt.gama.model.i.IDebtDto;
import lt.gama.model.i.IDoc;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.enums.DebtType;

import java.time.LocalDate;

public class DebtNowDto extends BaseCompanyDto implements IDebtDto, IDoc {

    private String token;

    private long companyId;

    private CounterpartyDto counterparty;

    private DebtType type;

    private Doc doc;

    private GamaMoney initial;

    private GamaMoney remainder;

    private LocalDate dueDate;

    @Override
    public long getCounterpartyId() {
        return counterparty.getId();
    }

    public String getCurrency() {
        return initial != null ? initial.getCurrency() :
                remainder != null ? remainder.getCurrency() : null;
    }

    public GamaMoney getCovered() {
        return GamaMoneyUtils.subtract(initial, remainder);
    }

    // generated

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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

    @Override
    public String toString() {
        return "DebtNowDto{" +
                "token='" + token + '\'' +
                ", companyId=" + companyId +
                ", counterparty=" + counterparty +
                ", type=" + type +
                ", doc=" + doc +
                ", initial=" + initial +
                ", remainder=" + remainder +
                ", dueDate=" + dueDate +
                "} " + super.toString();
    }
}


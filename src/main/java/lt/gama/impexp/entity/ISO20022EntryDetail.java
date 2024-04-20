package lt.gama.impexp.entity;

import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.doc.DocBankAccount;
import lt.gama.model.type.doc.DocCounterparty;
import lt.gama.model.type.doc.DocEmployee;

import java.io.Serial;
import java.io.Serializable;

/**
 * gama-online
 * Created by valdas on 2015-12-06.
 */
public class ISO20022EntryDetail implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    private String opNumber;

    private String note;

    private GamaMoney amount;

    private GamaMoney proprietaryAmount;

    private String partyName;

    private String partyIBAN;

    private String partyPrivateId;

    private String partyOrgId;

    private boolean linked;

    boolean autoLinked;

    private DocCounterparty counterparty;

    private DocEmployee employee;

    private DocBankAccount account2;

    private Boolean paymentCode;

    private boolean fees;

    // generated

    public String getOpNumber() {
        return opNumber;
    }

    public void setOpNumber(String opNumber) {
        this.opNumber = opNumber;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public GamaMoney getAmount() {
        return amount;
    }

    public void setAmount(GamaMoney amount) {
        this.amount = amount;
    }

    public GamaMoney getProprietaryAmount() {
        return proprietaryAmount;
    }

    public void setProprietaryAmount(GamaMoney proprietaryAmount) {
        this.proprietaryAmount = proprietaryAmount;
    }

    public String getPartyName() {
        return partyName;
    }

    public void setPartyName(String partyName) {
        this.partyName = partyName;
    }

    public String getPartyIBAN() {
        return partyIBAN;
    }

    public void setPartyIBAN(String partyIBAN) {
        this.partyIBAN = partyIBAN;
    }

    public String getPartyPrivateId() {
        return partyPrivateId;
    }

    public void setPartyPrivateId(String partyPrivateId) {
        this.partyPrivateId = partyPrivateId;
    }

    public String getPartyOrgId() {
        return partyOrgId;
    }

    public void setPartyOrgId(String partyOrgId) {
        this.partyOrgId = partyOrgId;
    }

    public boolean isLinked() {
        return linked;
    }

    public void setLinked(boolean linked) {
        this.linked = linked;
    }

    public boolean isAutoLinked() {
        return autoLinked;
    }

    public void setAutoLinked(boolean autoLinked) {
        this.autoLinked = autoLinked;
    }

    public DocCounterparty getCounterparty() {
        return counterparty;
    }

    public void setCounterparty(DocCounterparty counterparty) {
        this.counterparty = counterparty;
    }

    public DocEmployee getEmployee() {
        return employee;
    }

    public void setEmployee(DocEmployee employee) {
        this.employee = employee;
    }

    public DocBankAccount getAccount2() {
        return account2;
    }

    public void setAccount2(DocBankAccount account2) {
        this.account2 = account2;
    }

    public Boolean getPaymentCode() {
        return paymentCode;
    }

    public void setPaymentCode(Boolean paymentCode) {
        this.paymentCode = paymentCode;
    }

    public boolean isFees() {
        return fees;
    }

    public void setFees(boolean fees) {
        this.fees = fees;
    }

    @Override
    public String toString() {
        return "ISO20022EntryDetail{" +
                "opNumber='" + opNumber + '\'' +
                ", note='" + note + '\'' +
                ", amount=" + amount +
                ", proprietaryAmount=" + proprietaryAmount +
                ", partyName='" + partyName + '\'' +
                ", partyIBAN='" + partyIBAN + '\'' +
                ", partyPrivateId='" + partyPrivateId + '\'' +
                ", partyOrgId='" + partyOrgId + '\'' +
                ", linked=" + linked +
                ", autoLinked=" + autoLinked +
                ", counterparty=" + counterparty +
                ", employee=" + employee +
                ", account2=" + account2 +
                ", paymentCode=" + paymentCode +
                ", fees=" + fees +
                '}';
    }
}

package lt.gama.impexp.entity;

import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.doc.DocBankAccount;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * gama-online
 * Created by valdas on 2015-12-06.
 */
public class ISO20022Statement  implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    private LocalDateTime periodFrom;

    private LocalDateTime periodTo;

    private DocBankAccount bankAccount;

    // Bal
    private GamaMoney openingBalanceDebit;

    private GamaMoney openingBalanceCredit;

    private GamaMoney closingBalanceDebit;

    private GamaMoney closingBalanceCredit;

    private LocalDateTime openingDateTime;

    private LocalDateTime closingDateTime;

    // TxsSummry
    private GamaMoney creditTotal;

    private Integer creditCount;

    private GamaMoney debitTotal;

    private Integer debitCount;

    private GamaMoney creditTotalTotal;

    private GamaMoney debitTotalTotal;

    private Integer totalCount;

    // list of entry + detail from client
    private List<ISO20022Record> items;

    // generated

    public LocalDateTime getPeriodFrom() {
        return periodFrom;
    }

    public void setPeriodFrom(LocalDateTime periodFrom) {
        this.periodFrom = periodFrom;
    }

    public LocalDateTime getPeriodTo() {
        return periodTo;
    }

    public void setPeriodTo(LocalDateTime periodTo) {
        this.periodTo = periodTo;
    }

    public DocBankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(DocBankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }

    public GamaMoney getOpeningBalanceDebit() {
        return openingBalanceDebit;
    }

    public void setOpeningBalanceDebit(GamaMoney openingBalanceDebit) {
        this.openingBalanceDebit = openingBalanceDebit;
    }

    public GamaMoney getOpeningBalanceCredit() {
        return openingBalanceCredit;
    }

    public void setOpeningBalanceCredit(GamaMoney openingBalanceCredit) {
        this.openingBalanceCredit = openingBalanceCredit;
    }

    public GamaMoney getClosingBalanceDebit() {
        return closingBalanceDebit;
    }

    public void setClosingBalanceDebit(GamaMoney closingBalanceDebit) {
        this.closingBalanceDebit = closingBalanceDebit;
    }

    public GamaMoney getClosingBalanceCredit() {
        return closingBalanceCredit;
    }

    public void setClosingBalanceCredit(GamaMoney closingBalanceCredit) {
        this.closingBalanceCredit = closingBalanceCredit;
    }

    public LocalDateTime getOpeningDateTime() {
        return openingDateTime;
    }

    public void setOpeningDateTime(LocalDateTime openingDateTime) {
        this.openingDateTime = openingDateTime;
    }

    public LocalDateTime getClosingDateTime() {
        return closingDateTime;
    }

    public void setClosingDateTime(LocalDateTime closingDateTime) {
        this.closingDateTime = closingDateTime;
    }

    public GamaMoney getCreditTotal() {
        return creditTotal;
    }

    public void setCreditTotal(GamaMoney creditTotal) {
        this.creditTotal = creditTotal;
    }

    public Integer getCreditCount() {
        return creditCount;
    }

    public void setCreditCount(Integer creditCount) {
        this.creditCount = creditCount;
    }

    public GamaMoney getDebitTotal() {
        return debitTotal;
    }

    public void setDebitTotal(GamaMoney debitTotal) {
        this.debitTotal = debitTotal;
    }

    public Integer getDebitCount() {
        return debitCount;
    }

    public void setDebitCount(Integer debitCount) {
        this.debitCount = debitCount;
    }

    public GamaMoney getCreditTotalTotal() {
        return creditTotalTotal;
    }

    public void setCreditTotalTotal(GamaMoney creditTotalTotal) {
        this.creditTotalTotal = creditTotalTotal;
    }

    public GamaMoney getDebitTotalTotal() {
        return debitTotalTotal;
    }

    public void setDebitTotalTotal(GamaMoney debitTotalTotal) {
        this.debitTotalTotal = debitTotalTotal;
    }

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public List<ISO20022Record> getItems() {
        return items;
    }

    public void setItems(List<ISO20022Record> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "ISO20022Statement{" +
                "periodFrom=" + periodFrom +
                ", periodTo=" + periodTo +
                ", bankAccount=" + bankAccount +
                ", openingBalanceDebit=" + openingBalanceDebit +
                ", openingBalanceCredit=" + openingBalanceCredit +
                ", closingBalanceDebit=" + closingBalanceDebit +
                ", closingBalanceCredit=" + closingBalanceCredit +
                ", openingDateTime=" + openingDateTime +
                ", closingDateTime=" + closingDateTime +
                ", creditTotal=" + creditTotal +
                ", creditCount=" + creditCount +
                ", debitTotal=" + debitTotal +
                ", debitCount=" + debitCount +
                ", creditTotalTotal=" + creditTotalTotal +
                ", debitTotalTotal=" + debitTotalTotal +
                ", totalCount=" + totalCount +
                ", items=" + items +
                '}';
    }
}

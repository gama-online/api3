package lt.gama.impexp.entity;

import lt.gama.model.type.GamaMoney;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * gama-online
 * Created by valdas on 2015-12-06.
 */
public class ISO20022Entry implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    private GamaMoney credit;

    private GamaMoney debit;

    private LocalDateTime bookingDate;

    private LocalDateTime valueDate;

    private String codeDomain;

    private String codeFamily;

    private String codeSubFamily;

    private List<ISO20022EntryDetail> details;

    private boolean cash;

    private boolean fees;

    // generated

    public GamaMoney getCredit() {
        return credit;
    }

    public void setCredit(GamaMoney credit) {
        this.credit = credit;
    }

    public GamaMoney getDebit() {
        return debit;
    }

    public void setDebit(GamaMoney debit) {
        this.debit = debit;
    }

    public LocalDateTime getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDateTime bookingDate) {
        this.bookingDate = bookingDate;
    }

    public LocalDateTime getValueDate() {
        return valueDate;
    }

    public void setValueDate(LocalDateTime valueDate) {
        this.valueDate = valueDate;
    }

    public String getCodeDomain() {
        return codeDomain;
    }

    public void setCodeDomain(String codeDomain) {
        this.codeDomain = codeDomain;
    }

    public String getCodeFamily() {
        return codeFamily;
    }

    public void setCodeFamily(String codeFamily) {
        this.codeFamily = codeFamily;
    }

    public String getCodeSubFamily() {
        return codeSubFamily;
    }

    public void setCodeSubFamily(String codeSubFamily) {
        this.codeSubFamily = codeSubFamily;
    }

    public List<ISO20022EntryDetail> getDetails() {
        return details;
    }

    public void setDetails(List<ISO20022EntryDetail> details) {
        this.details = details;
    }

    public boolean isCash() {
        return cash;
    }

    public void setCash(boolean cash) {
        this.cash = cash;
    }

    public boolean isFees() {
        return fees;
    }

    public void setFees(boolean fees) {
        this.fees = fees;
    }

    @Override
    public String toString() {
        return "ISO20022Entry{" +
                "credit=" + credit +
                ", debit=" + debit +
                ", bookingDate=" + bookingDate +
                ", valueDate=" + valueDate +
                ", codeDomain='" + codeDomain + '\'' +
                ", codeFamily='" + codeFamily + '\'' +
                ", codeSubFamily='" + codeSubFamily + '\'' +
                ", details=" + details +
                ", cash=" + cash +
                ", fees=" + fees +
                '}';
    }
}

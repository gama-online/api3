package lt.gama.service.sync.scoro.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lt.gama.service.json.deser.LocalDateDeserializer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * gama-online
 * Created by valdas on 2017-10-10.
 */
public class ScoroExpense {

    private String no;

    private long id;

    private String currency;

    private BigDecimal sum;

    @JsonProperty("vat_sum")
    private BigDecimal vatSum;

    private BigDecimal vat;

    @JsonProperty("company_id")
    private long companyId;

    private LocalDate date;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate deadline;

    private List<ScoroExpenseLine> lines;

    // generated

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getSum() {
        return sum;
    }

    public void setSum(BigDecimal sum) {
        this.sum = sum;
    }

    public BigDecimal getVatSum() {
        return vatSum;
    }

    public void setVatSum(BigDecimal vatSum) {
        this.vatSum = vatSum;
    }

    public BigDecimal getVat() {
        return vat;
    }

    public void setVat(BigDecimal vat) {
        this.vat = vat;
    }

    public long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(long companyId) {
        this.companyId = companyId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public List<ScoroExpenseLine> getLines() {
        return lines;
    }

    public void setLines(List<ScoroExpenseLine> lines) {
        this.lines = lines;
    }

    @Override
    public String toString() {
        return "ScoroExpense{" +
                "no='" + no + '\'' +
                ", id=" + id +
                ", currency='" + currency + '\'' +
                ", sum=" + sum +
                ", vatSum=" + vatSum +
                ", vat=" + vat +
                ", companyId=" + companyId +
                ", date=" + date +
                ", deadline=" + deadline +
                ", lines=" + lines +
                '}';
    }
}

package lt.gama.service.sync.scoro.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lt.gama.service.json.deser.LocalDateDeserializer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * gama-online
 * Created by valdas on 2017-10-06.
 */
public class ScoroInvoice {

    private long id;

    private long no;

    @JsonProperty("company_id")
    private long companyId;

    private String currency;

    private BigDecimal sum;

    @JsonProperty("vat_sum")
    private BigDecimal vatSum;

    private LocalDate date;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate deadline;

    private List<ScoroInvoiceLine> lines;

    // generated

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getNo() {
        return no;
    }

    public void setNo(long no) {
        this.no = no;
    }

    public long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(long companyId) {
        this.companyId = companyId;
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

    public List<ScoroInvoiceLine> getLines() {
        return lines;
    }

    public void setLines(List<ScoroInvoiceLine> lines) {
        this.lines = lines;
    }

    @Override
    public String toString() {
        return "ScoroInvoice{" +
                "id=" + id +
                ", no=" + no +
                ", companyId=" + companyId +
                ", currency='" + currency + '\'' +
                ", sum=" + sum +
                ", vatSum=" + vatSum +
                ", date=" + date +
                ", deadline=" + deadline +
                ", lines=" + lines +
                '}';
    }
}

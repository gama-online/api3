package lt.gama.model.sql.documents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lt.gama.helpers.BooleanUtils;
import lt.gama.helpers.DateUtils;
import lt.gama.model.i.IParts;
import lt.gama.model.sql.base.BaseDocumentSql;
import lt.gama.model.sql.documents.items.EstimateBasePartSql;
import lt.gama.model.sql.entities.BankAccountSql;
import lt.gama.model.sql.entities.WarehouseSql;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.Location;
import lt.gama.model.type.NameContact;
import lt.gama.model.type.doc.Doc;
import lt.gama.model.type.enums.EstimateType;
import lt.gama.model.type.enums.PeriodType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

@Entity
@Table(name = "estimate")
@NamedEntityGraph(name = EstimateSql.GRAPH_ALL, attributeNodes = {
        @NamedAttributeNode(EstimateSql_.COUNTERPARTY),
        @NamedAttributeNode(EstimateSql_.WAREHOUSE),
        @NamedAttributeNode(EstimateSql_.ACCOUNT),
        @NamedAttributeNode(EstimateSql_.PARTS)
})
public class EstimateSql extends BaseDocumentSql implements IParts<EstimateBasePartSql> {

    public static final String GRAPH_ALL = "graph.EstimateSql.all";

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("parent")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @OrderBy("sortOrder, id")
    private List<EstimateBasePartSql> parts = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private EstimateType type;

    @Embedded
    private Location location;

    @Embedded
    private NameContact contact;

    /**
     * Document's discount in percent
     */
    private Double discount;

    @Embedded
    private GamaMoney subtotal;

    @Embedded
    private GamaMoney taxTotal;

    @Embedded
    private GamaMoney total;

    @Embedded
    private GamaMoney baseSubtotal;

    @Embedded
    private GamaMoney baseTaxTotal;

    @Embedded
    private GamaMoney baseTotal;

    @Embedded
    private GamaMoney prepayment;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "warehouse_id")
    private WarehouseSql warehouse;

    private String tag;

    private LocalDate dueDate;

    /**
     * For Periodic type
     */
    @Enumerated(EnumType.STRING)
    private PeriodType period;

    private Integer step;

    private LocalDate startDate;

    private LocalDate endDate;

    /**
     * Links to generated invoices
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Doc> docs;

    /**
     * Date of next invoice if periodic
     */
    private LocalDate nextDate;

    /**
     * if bank account is null - all banks account will be printed on invoice
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private BankAccountSql account;

    private Boolean zeroVAT;

    /**
     * Note visible to customer
     */
    private String estimateNote;	// Up to 500 Unicode characters

    /**
     * Reverse VAT?
     */
    private Boolean reverseVAT;


    public boolean isZeroVAT() {
        return zeroVAT != null && zeroVAT;
    }

    public LocalDate getLastDate() {
        if (getType() == EstimateType.PERIODIC) {
            if (getNextDate() == null) calculateNextDate();
            LocalDate date = getNextDate();
            if (date == null && getDocs() != null) date = getDocs().get(getDocs().size() - 1).getDate();
            if (date == null) date = getDate();
            return date;
        } else {
            return getDate();
        }
    }

    public Long getDaysLeft() {
        return getNextDate() != null ? ChronoUnit.DAYS.between(DateUtils.date(), getNextDate()) : null;
    }

    @Override
    public boolean setFullyFinished() {
        boolean changed = BooleanUtils.isNotTrue(getFinished());
        setFinished(true);
        return changed;
    }

    private void calculateNextDate() {
        if (getType() != EstimateType.PERIODIC ||
                getPeriod() == null ||
                getStartDate() == null ||
                getStep() <= 0 ||
                (getEndDate() != null && getEndDate().isAfter(DateUtils.date())) ||
                getNextDate() != null) return;

        LocalDate lastDate = getDocs() == null ?  getDate() : getDocs().get(getDocs().size() - 1).getDate();
        int step = getStep();

        switch (getPeriod()) {
            case DAY -> setNextDate(lastDate.plusDays(step));
            case WEEK -> setNextDate(lastDate.plusDays(step * 7L));
            case MONTH, QUARTER -> {
                if (getPeriod() == PeriodType.QUARTER) step *= 3;
                LocalDate nextDate = lastDate.withDayOfMonth(1).plusMonths(step);
                int day = getStartDate().getDayOfMonth();
                if (day <= nextDate.lengthOfMonth()) {
                    nextDate = nextDate.withDayOfMonth(day);
                } else {
                    nextDate = nextDate.with(lastDayOfMonth());
                }
                setNextDate(nextDate);
            }
            case YEAR -> {
                int monthStart = getStartDate().getMonthValue();
                LocalDate nextDate = lastDate.withDayOfMonth(1).plusYears(step).withMonth(monthStart);
                int day = getStartDate().getDayOfMonth();
                if (day <= nextDate.lengthOfMonth()) {
                    nextDate = nextDate.withDayOfMonth(day);
                } else {
                    nextDate = nextDate.withDayOfMonth(nextDate.lengthOfMonth());
                }
                setNextDate(nextDate);
            }
        }
    }

    /**
     * Next invoice date for periodic estimate will be calculated on save if it is null.
     * So after a new invoice was generated we need to set nextDate to null before save.
     */
    @PrePersist
    @PreUpdate
    private void onSave() {
        calculateNextDate();
    }

    // generated
    // except getZeroVAT()

    public List<EstimateBasePartSql> getParts() {
        return parts;
    }

    public void setParts(List<EstimateBasePartSql> parts) {
        this.parts = parts;
    }

    public EstimateType getType() {
        return type;
    }

    public void setType(EstimateType type) {
        this.type = type;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public NameContact getContact() {
        return contact;
    }

    public void setContact(NameContact contact) {
        this.contact = contact;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public GamaMoney getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(GamaMoney subtotal) {
        this.subtotal = subtotal;
    }

    public GamaMoney getTaxTotal() {
        return taxTotal;
    }

    public void setTaxTotal(GamaMoney taxTotal) {
        this.taxTotal = taxTotal;
    }

    public GamaMoney getTotal() {
        return total;
    }

    public void setTotal(GamaMoney total) {
        this.total = total;
    }

    public GamaMoney getBaseSubtotal() {
        return baseSubtotal;
    }

    public void setBaseSubtotal(GamaMoney baseSubtotal) {
        this.baseSubtotal = baseSubtotal;
    }

    public GamaMoney getBaseTaxTotal() {
        return baseTaxTotal;
    }

    public void setBaseTaxTotal(GamaMoney baseTaxTotal) {
        this.baseTaxTotal = baseTaxTotal;
    }

    public GamaMoney getBaseTotal() {
        return baseTotal;
    }

    public void setBaseTotal(GamaMoney baseTotal) {
        this.baseTotal = baseTotal;
    }

    public GamaMoney getPrepayment() {
        return prepayment;
    }

    public void setPrepayment(GamaMoney prepayment) {
        this.prepayment = prepayment;
    }

    public WarehouseSql getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(WarehouseSql warehouse) {
        this.warehouse = warehouse;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public PeriodType getPeriod() {
        return period;
    }

    public void setPeriod(PeriodType period) {
        this.period = period;
    }

    public Integer getStep() {
        return step;
    }

    public void setStep(Integer step) {
        this.step = step;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<Doc> getDocs() {
        return docs;
    }

    public void setDocs(List<Doc> docs) {
        this.docs = docs;
    }

    public LocalDate getNextDate() {
        return nextDate;
    }

    public void setNextDate(LocalDate nextDate) {
        this.nextDate = nextDate;
    }

    public BankAccountSql getAccount() {
        return account;
    }

    public void setAccount(BankAccountSql account) {
        this.account = account;
    }

    public void setZeroVAT(Boolean zeroVAT) {
        this.zeroVAT = zeroVAT;
    }

    public String getEstimateNote() {
        return estimateNote;
    }

    public void setEstimateNote(String estimateNote) {
        this.estimateNote = estimateNote;
    }

    public Boolean getReverseVAT() {
        return reverseVAT;
    }

    public void setReverseVAT(Boolean reverseVAT) {
        this.reverseVAT = reverseVAT;
    }

    @Override
    public String toString() {
        return "EstimateSql{" +
                "parts=" + parts +
                ", type=" + type +
                ", location=" + location +
                ", contact=" + contact +
                ", discount=" + discount +
                ", subtotal=" + subtotal +
                ", taxTotal=" + taxTotal +
                ", total=" + total +
                ", baseSubtotal=" + baseSubtotal +
                ", baseTaxTotal=" + baseTaxTotal +
                ", baseTotal=" + baseTotal +
                ", prepayment=" + prepayment +
                ", warehouse=" + warehouse +
                ", tag='" + tag + '\'' +
                ", dueDate=" + dueDate +
                ", period=" + period +
                ", step=" + step +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", docs=" + docs +
                ", nextDate=" + nextDate +
                ", zeroVAT=" + zeroVAT +
                ", estimateNote='" + estimateNote + '\'' +
                ", reverseVAT=" + reverseVAT +
                "} " + super.toString();
    }
}

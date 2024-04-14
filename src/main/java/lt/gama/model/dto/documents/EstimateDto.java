package lt.gama.model.dto.documents;

import lt.gama.helpers.BooleanUtils;
import lt.gama.helpers.DateUtils;
import lt.gama.helpers.LocationUtils;
import lt.gama.model.type.Location;
import lt.gama.model.type.NameContact;
import lt.gama.model.type.doc.Doc;
import lt.gama.model.dto.base.BaseDocumentDto;
import lt.gama.model.dto.documents.items.PartEstimateDto;
import lt.gama.model.dto.entities.BankAccountDto;
import lt.gama.model.dto.entities.WarehouseDto;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.enums.EstimateType;
import lt.gama.model.type.enums.PeriodType;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serial;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

@DynamicUpdate
public class EstimateDto extends BaseDocumentDto {

    @Serial
    private static final long serialVersionUID = -1L;

    private EstimateType type;

    private Location location;

    private NameContact contact;

    private List<PartEstimateDto> parts = new ArrayList<>();

    /**
     * Document's discount in percent
     */
    private Double discount;

    private GamaMoney subtotal;

    private GamaMoney taxTotal;

    private GamaMoney total;

    private GamaMoney baseSubtotal;

    private GamaMoney baseTaxTotal;

    private GamaMoney baseTotal;

    private GamaMoney prepayment;

    private WarehouseDto warehouse;

    private String tag;

    private LocalDate dueDate;

    /*
     * For Periodic type
     */

    private PeriodType period;

    private Integer step;

    private LocalDate startDate;

    private LocalDate endDate;

    /**
     * Links to generated invoices
     */
    private List<Doc> docs;

    /**
     * Date of next invoice if periodic
     */
    private LocalDate nextDate;

    /**
     * if bank account is null - all banks account will be printed on invoice
     */
    private BankAccountDto account;

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

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
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

        if (PeriodType.DAY.equals(getPeriod())) {
            setNextDate(lastDate.plusDays(step));

        } else if (PeriodType.WEEK.equals(getPeriod())) {
            setNextDate(lastDate.plusDays(step * 7L));

        } else if (PeriodType.MONTH.equals(getPeriod()) || PeriodType.QUARTER.equals(getPeriod())) {
            if (PeriodType.QUARTER.equals(getPeriod())) step *= 3;
            LocalDate nextDate = lastDate.withDayOfMonth(1).plusMonths(step);
            int day = getStartDate().getDayOfMonth();
            if (day <= nextDate.lengthOfMonth()) {
                nextDate = nextDate.withDayOfMonth(day);
            } else {
                nextDate = nextDate.with(lastDayOfMonth());
            }
            setNextDate(nextDate);

        } else if (PeriodType.YEAR.equals(getPeriod())) {
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

    public String getAddress() {
        return LocationUtils.isValid(location)
                ? location.getAddress()
                : getCounterparty() != null
                ? getCounterparty().getAddress()
                : "";
    }

    // generated
    // except getZeroVAT()

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

    public List<PartEstimateDto> getParts() {
        return parts;
    }

    public void setParts(List<PartEstimateDto> parts) {
        this.parts = parts;
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

    public WarehouseDto getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(WarehouseDto warehouse) {
        this.warehouse = warehouse;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
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

    public BankAccountDto getAccount() {
        return account;
    }

    public void setAccount(BankAccountDto account) {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        EstimateDto that = (EstimateDto) o;
        return type == that.type && Objects.equals(location, that.location) && Objects.equals(contact, that.contact) && Objects.equals(parts, that.parts) && Objects.equals(discount, that.discount) && Objects.equals(subtotal, that.subtotal) && Objects.equals(taxTotal, that.taxTotal) && Objects.equals(total, that.total) && Objects.equals(baseSubtotal, that.baseSubtotal) && Objects.equals(baseTaxTotal, that.baseTaxTotal) && Objects.equals(baseTotal, that.baseTotal) && Objects.equals(prepayment, that.prepayment) && Objects.equals(warehouse, that.warehouse) && Objects.equals(tag, that.tag) && Objects.equals(dueDate, that.dueDate) && period == that.period && Objects.equals(step, that.step) && Objects.equals(startDate, that.startDate) && Objects.equals(endDate, that.endDate) && Objects.equals(docs, that.docs) && Objects.equals(nextDate, that.nextDate) && Objects.equals(account, that.account) && Objects.equals(zeroVAT, that.zeroVAT) && Objects.equals(estimateNote, that.estimateNote) && Objects.equals(reverseVAT, that.reverseVAT);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), type, location, contact, parts, discount, subtotal, taxTotal, total, baseSubtotal, baseTaxTotal, baseTotal, prepayment, warehouse, tag, dueDate, period, step, startDate, endDate, docs, nextDate, account, zeroVAT, estimateNote, reverseVAT);
    }

    @Override
    public String toString() {
        return "EstimateDto{" +
                "type=" + type +
                ", location=" + location +
                ", contact=" + contact +
                ", parts=" + parts +
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
                ", account=" + account +
                ", zeroVAT=" + zeroVAT +
                ", estimateNote='" + estimateNote + '\'' +
                ", reverseVAT=" + reverseVAT +
                "} " + super.toString();
    }
}

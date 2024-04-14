package lt.gama.model.type.asset;

import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.Location;
import lt.gama.model.type.doc.DocEmployee;
import lt.gama.model.type.enums.AssetStatusType;
import lt.gama.model.type.enums.DepreciationType;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 2015-10-28.
 */
public class AssetHistory implements Serializable {

    /**
     * Depreciation start or conservation/write-off date
     */
    private LocalDate date;

    /**
     * end of record period date (automatically update on save)
     */
    private LocalDate endDate;

    private AssetStatusType status;

    /**
     * Beginning Book Value
     */
    private GamaMoney beginning;

    /**
     * Cost Value Correction
     */
    private GamaMoney dtValue;

    /**
     * Depreciation Correction
     */
    private GamaMoney dtExpense;

    /**
     * Depreciation final date
     */
    private LocalDate finalDate;

    /**
     * Ending Book Value
     */
    private GamaMoney ending;

    private DepreciationType type;

    private Double rate;

    private GamaMoney amount;

    private DocEmployee responsible;

    private Location location;


    public String toMessage() {
        return (date == null ? "" : (date + "-")) + (finalDate == null ? "" : finalDate.toString());
    }

    // fluent setters

    public AssetHistory setDate(LocalDate date) {
        this.date = date;
        return this;
    }

    public AssetHistory setEndDate(LocalDate endDate) {
        this.endDate = endDate;
        return this;
    }

    public AssetHistory setStatus(AssetStatusType status) {
        this.status = status;
        return this;
    }

    public AssetHistory setBeginning(GamaMoney beginning) {
        this.beginning = beginning;
        return this;
    }

    public AssetHistory setDtValue(GamaMoney dtValue) {
        this.dtValue = dtValue;
        return this;
    }

    public AssetHistory setDtExpense(GamaMoney dtExpense) {
        this.dtExpense = dtExpense;
        return this;
    }

    public AssetHistory setFinalDate(LocalDate finalDate) {
        this.finalDate = finalDate;
        return this;
    }

    public AssetHistory setEnding(GamaMoney ending) {
        this.ending = ending;
        return this;
    }

    public AssetHistory setType(DepreciationType type) {
        this.type = type;
        return this;
    }

    public AssetHistory setRate(Double rate) {
        this.rate = rate;
        return this;
    }

    public AssetHistory setAmount(GamaMoney amount) {
        this.amount = amount;
        return this;
    }

    public AssetHistory setResponsible(DocEmployee responsible) {
        this.responsible = responsible;
        return this;
    }

    public AssetHistory setLocation(Location location) {
        this.location = location;
        return this;
    }

    // generated
    // except setters


    public LocalDate getDate() {
        return date;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public AssetStatusType getStatus() {
        return status;
    }

    public GamaMoney getBeginning() {
        return beginning;
    }

    public GamaMoney getDtValue() {
        return dtValue;
    }

    public GamaMoney getDtExpense() {
        return dtExpense;
    }

    public LocalDate getFinalDate() {
        return finalDate;
    }

    public GamaMoney getEnding() {
        return ending;
    }

    public DepreciationType getType() {
        return type;
    }

    public Double getRate() {
        return rate;
    }

    public GamaMoney getAmount() {
        return amount;
    }

    public DocEmployee getResponsible() {
        return responsible;
    }

    public Location getLocation() {
        return location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssetHistory that = (AssetHistory) o;
        return Objects.equals(date, that.date) && Objects.equals(endDate, that.endDate) && status == that.status && Objects.equals(beginning, that.beginning) && Objects.equals(dtValue, that.dtValue) && Objects.equals(dtExpense, that.dtExpense) && Objects.equals(finalDate, that.finalDate) && Objects.equals(ending, that.ending) && type == that.type && Objects.equals(rate, that.rate) && Objects.equals(amount, that.amount) && Objects.equals(responsible, that.responsible) && Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, endDate, status, beginning, dtValue, dtExpense, finalDate, ending, type, rate, amount, responsible, location);
    }

    @Override
    public String toString() {
        return "AssetHistory{" +
                "date=" + date +
                ", endDate=" + endDate +
                ", status=" + status +
                ", beginning=" + beginning +
                ", dtValue=" + dtValue +
                ", dtExpense=" + dtExpense +
                ", finalDate=" + finalDate +
                ", ending=" + ending +
                ", type=" + type +
                ", rate=" + rate +
                ", amount=" + amount +
                ", responsible=" + responsible +
                ", location=" + location +
                '}';
    }
}

package lt.gama.model.dto.type;

import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.enums.AssetStatusType;

import java.time.LocalDate;

public class AssetTotal {
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private GamaMoney acquired;     // cost of acquiring per period
    private GamaMoney incoming;     // value of assets incoming per period
    private GamaMoney beginning;    // value at the beginning of period
    private GamaMoney dtValue;      // value correction per period
    private GamaMoney dtExpense;    // depreciation correction per period
    private GamaMoney expense;      // depreciation per period
    private GamaMoney ending;       // ending value at the end of period, i.e. beginning + dtValue - dtExpense - expense
    private GamaMoney depreciation;  // total depreciation up to the end of period
    private AssetStatusType status;  // status at the end of period

    @SuppressWarnings("unused")
    protected AssetTotal() {
    }

    public AssetTotal(LocalDate dateFrom, LocalDate dateTo) {
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
    }

    // generated

    public LocalDate getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(LocalDate dateFrom) {
        this.dateFrom = dateFrom;
    }

    public LocalDate getDateTo() {
        return dateTo;
    }

    public void setDateTo(LocalDate dateTo) {
        this.dateTo = dateTo;
    }

    public GamaMoney getAcquired() {
        return acquired;
    }

    public void setAcquired(GamaMoney acquired) {
        this.acquired = acquired;
    }

    public GamaMoney getIncoming() {
        return incoming;
    }

    public void setIncoming(GamaMoney incoming) {
        this.incoming = incoming;
    }

    public GamaMoney getBeginning() {
        return beginning;
    }

    public void setBeginning(GamaMoney beginning) {
        this.beginning = beginning;
    }

    public GamaMoney getDtValue() {
        return dtValue;
    }

    public void setDtValue(GamaMoney dtValue) {
        this.dtValue = dtValue;
    }

    public GamaMoney getDtExpense() {
        return dtExpense;
    }

    public void setDtExpense(GamaMoney dtExpense) {
        this.dtExpense = dtExpense;
    }

    public GamaMoney getExpense() {
        return expense;
    }

    public void setExpense(GamaMoney expense) {
        this.expense = expense;
    }

    public GamaMoney getEnding() {
        return ending;
    }

    public void setEnding(GamaMoney ending) {
        this.ending = ending;
    }

    public GamaMoney getDepreciation() {
        return depreciation;
    }

    public void setDepreciation(GamaMoney depreciation) {
        this.depreciation = depreciation;
    }

    public AssetStatusType getStatus() {
        return status;
    }

    public void setStatus(AssetStatusType status) {
        this.status = status;
    }
}
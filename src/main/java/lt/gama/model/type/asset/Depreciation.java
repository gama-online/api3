package lt.gama.model.type.asset;

import lt.gama.model.type.GamaMoney;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 2015-10-28.
 */
public class Depreciation implements Serializable {

    /**
     * Month of calculations
     */
    private LocalDate date;

    /**
     * Beginning Book Value
     */
    private GamaMoney beginning;

    /**
     * Depreciation Expense
     */
    private GamaMoney expense;

    /**
     * Cost Value Correction
     */
    private GamaMoney dtValue;

    /**
     * Depreciation Correction
     */
    private GamaMoney dtExpense;

    /**
     * Ending Accumulated Depreciation
     */
    private GamaMoney depreciation;

    /**
     * Ending Book Value
     */
    private GamaMoney ending;


    public Depreciation() {
    }

    public Depreciation(LocalDate date) {
        this.date = date;
    }

    // fluent setters

    public Depreciation setDate(LocalDate date) {
        this.date = date;
        return this;
    }

    public Depreciation setBeginning(GamaMoney beginning) {
        this.beginning = beginning;
        return this;
    }

    public Depreciation setExpense(GamaMoney expense) {
        this.expense = expense;
        return this;
    }

    public Depreciation setDtValue(GamaMoney dtValue) {
        this.dtValue = dtValue;
        return this;
    }

    public Depreciation setDtExpense(GamaMoney dtExpense) {
        this.dtExpense = dtExpense;
        return this;
    }

    public Depreciation setDepreciation(GamaMoney depreciation) {
        this.depreciation = depreciation;
        return this;
    }

    public Depreciation setEnding(GamaMoney ending) {
        this.ending = ending;
        return this;
    }


    // generated
    // except setters

    public LocalDate getDate() {
        return date;
    }

    public GamaMoney getBeginning() {
        return beginning;
    }

    public GamaMoney getExpense() {
        return expense;
    }

    public GamaMoney getDtValue() {
        return dtValue;
    }

    public GamaMoney getDtExpense() {
        return dtExpense;
    }

    public GamaMoney getDepreciation() {
        return depreciation;
    }

    public GamaMoney getEnding() {
        return ending;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Depreciation that = (Depreciation) o;
        return Objects.equals(date, that.date) && Objects.equals(beginning, that.beginning) && Objects.equals(expense, that.expense) && Objects.equals(dtValue, that.dtValue) && Objects.equals(dtExpense, that.dtExpense) && Objects.equals(depreciation, that.depreciation) && Objects.equals(ending, that.ending);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, beginning, expense, dtValue, dtExpense, depreciation, ending);
    }

    @Override
    public String toString() {
        return "Depreciation{" +
                "date=" + date +
                ", beginning=" + beginning +
                ", expense=" + expense +
                ", dtValue=" + dtValue +
                ", dtExpense=" + dtExpense +
                ", depreciation=" + depreciation +
                ", ending=" + ending +
                '}';
    }
}

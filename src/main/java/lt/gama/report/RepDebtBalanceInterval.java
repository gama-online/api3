package lt.gama.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

public class RepDebtBalanceInterval {

    private Collection<String> labels;

    private DatasetData[] dataset;


    @SuppressWarnings("unused")
    protected RepDebtBalanceInterval() {}

    public RepDebtBalanceInterval(Collection<String> labels, DatasetData[] dataset) {
        this.labels = labels;
        this.dataset = dataset;
    }

    public static class DatasetData {

        private LocalDate date;

        private BigDecimal baseBalance;

        private BigDecimal baseDebit;

        private BigDecimal baseCredit;


        @SuppressWarnings("unused")
        protected DatasetData() {}

        public DatasetData(LocalDate date, BigDecimal baseBalance, BigDecimal baseDebit, BigDecimal baseCredit) {
            this.date = date;
            this.baseBalance = baseBalance;
            this.baseDebit = baseDebit;
            this.baseCredit = baseCredit;
        }

        // generated

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public BigDecimal getBaseBalance() {
            return baseBalance;
        }

        public void setBaseBalance(BigDecimal baseBalance) {
            this.baseBalance = baseBalance;
        }

        public BigDecimal getBaseDebit() {
            return baseDebit;
        }

        public void setBaseDebit(BigDecimal baseDebit) {
            this.baseDebit = baseDebit;
        }

        public BigDecimal getBaseCredit() {
            return baseCredit;
        }

        public void setBaseCredit(BigDecimal baseCredit) {
            this.baseCredit = baseCredit;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DatasetData that = (DatasetData) o;
            return Objects.equals(date, that.date) && Objects.equals(baseBalance, that.baseBalance) && Objects.equals(baseDebit, that.baseDebit) && Objects.equals(baseCredit, that.baseCredit);
        }

        @Override
        public int hashCode() {
            return Objects.hash(date, baseBalance, baseDebit, baseCredit);
        }

        @Override
        public String toString() {
            return "DatasetData{" +
                    "date=" + date +
                    ", baseBalance=" + baseBalance +
                    ", baseDebit=" + baseDebit +
                    ", baseCredit=" + baseCredit +
                    '}';
        }
    }

    // generated

    public Collection<String> getLabels() {
        return labels;
    }

    public void setLabels(Collection<String> labels) {
        this.labels = labels;
    }

    public DatasetData[] getDataset() {
        return dataset;
    }

    public void setDataset(DatasetData[] dataset) {
        this.dataset = dataset;
    }

    @Override
    public String toString() {
        return "RepDebtBalanceInterval{" +
                "labels=" + labels +
                ", dataset=" + Arrays.toString(dataset) +
                '}';
    }
}

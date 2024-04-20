package lt.gama.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class RepMoneyBalanceInterval {

    private List<String> labels;

    private List<Dataset> datasets;


    @SuppressWarnings("unused")
    protected RepMoneyBalanceInterval() {}

    public RepMoneyBalanceInterval(List<String> labels, List<Dataset> datasets) {
        this.labels = labels;
        this.datasets = datasets;
    }

    public static class Dataset {

        private long accountId;

        private String accountName;

        private DatasetData[] data;


        @SuppressWarnings("unused")
        protected Dataset() {}

        public Dataset(int size) {
            this.data = new DatasetData[size];
        }

        // generated

        public long getAccountId() {
            return accountId;
        }

        public void setAccountId(long accountId) {
            this.accountId = accountId;
        }

        public String getAccountName() {
            return accountName;
        }

        public void setAccountName(String accountName) {
            this.accountName = accountName;
        }

        public DatasetData[] getData() {
            return data;
        }

        public void setData(DatasetData[] data) {
            this.data = data;
        }

        @Override
        public String toString() {
            return "Dataset{" +
                    "accountId=" + accountId +
                    ", accountName='" + accountName + '\'' +
                    ", data=" + Arrays.toString(data) +
                    '}';
        }
    }

    public static class DatasetData {

        private LocalDate date;

        private BigDecimal baseBalance;

        private BigDecimal baseDebit;

        private BigDecimal baseCredit;

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

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public List<Dataset> getDatasets() {
        return datasets;
    }

    public void setDatasets(List<Dataset> datasets) {
        this.datasets = datasets;
    }

    @Override
    public String toString() {
        return "RepMoneyBalanceInterval{" +
                "labels=" + labels +
                ", datasets=" + datasets +
                '}';
    }
}

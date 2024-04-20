package lt.gama.tasks;

import lt.gama.impexp.entity.ISO20022Record;
import lt.gama.model.type.auth.CompanySettings;

import java.util.List;

public class ImportBankTaskData {
    private CompanySettings companySettings;
    private long bankAccountId;
    private List<ISO20022Record> items;
    private int nr;

    // generated

    public CompanySettings getCompanySettings() {
        return companySettings;
    }

    public void setCompanySettings(CompanySettings companySettings) {
        this.companySettings = companySettings;
    }

    public long getBankAccountId() {
        return bankAccountId;
    }

    public void setBankAccountId(long bankAccountId) {
        this.bankAccountId = bankAccountId;
    }

    public List<ISO20022Record> getItems() {
        return items;
    }

    public void setItems(List<ISO20022Record> items) {
        this.items = items;
    }

    public int getNr() {
        return nr;
    }

    public void setNr(int nr) {
        this.nr = nr;
    }

    @Override
    public String toString() {
        return "ImportBankTaskData{" +
                "companySettings=" + companySettings +
                ", bankAccountId=" + bankAccountId +
                ", items=" + items +
                ", nr=" + nr +
                '}';
    }
}

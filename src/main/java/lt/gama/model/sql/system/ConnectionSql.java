package lt.gama.model.sql.system;

import jakarta.persistence.*;
import lt.gama.model.i.ICompany;
import lt.gama.model.i.IId;
import lt.gama.model.sql.base.BaseEntitySql;
import lt.gama.model.type.auth.CompanyAccount;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.Map;

@Entity
@Table(name = "connections")
public class ConnectionSql extends BaseEntitySql implements ICompany, IId<Long> {

    private static final long serialVersionUID = -1L;

    @Id
    @GeneratedValue(generator = "gama_sequence", strategy = GenerationType.SEQUENCE)
    private Long id;

    private long companyId;

    /**
     * The date when accounts was calculated
     * Combination companyId + date is unique
     */
    private LocalDate date;

    /**
     * Company's active accounts
     */
    private int activeAccounts;

    /**
     * Count of accounts in other companies
     * These accounts will be paid by this company (positive number) or by other company (negative number)
     * This number is decoded in {@link #otherAccounts}.
     */
    private int payerAccounts;

    /**
     * Map of accounts in another companies.
     * These connections will be paid or by this company (positive number) or by another companies (negative number)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<Long, CompanyAccount> otherAccounts;


    @SuppressWarnings("unused")
    protected ConnectionSql() {}

    public ConnectionSql(long companyId, LocalDate date) {
        this.companyId = companyId;
        this.date = date;
    }

    public int getTotalAccounts() {
        return getActiveAccounts() + getPayerAccounts();
    }

    // generated

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public long getCompanyId() {
        return companyId;
    }

    @Override
    public void setCompanyId(long companyId) {
        this.companyId = companyId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getActiveAccounts() {
        return activeAccounts;
    }

    public void setActiveAccounts(int activeAccounts) {
        this.activeAccounts = activeAccounts;
    }

    public int getPayerAccounts() {
        return payerAccounts;
    }

    public void setPayerAccounts(int payerAccounts) {
        this.payerAccounts = payerAccounts;
    }

    public Map<Long, CompanyAccount> getOtherAccounts() {
        return otherAccounts;
    }

    public void setOtherAccounts(Map<Long, CompanyAccount> otherAccounts) {
        this.otherAccounts = otherAccounts;
    }

    @Override
    public String toString() {
        return "ConnectionSql{" +
                "id=" + id +
                ", companyId=" + companyId +
                ", date=" + date +
                ", activeAccounts=" + activeAccounts +
                ", payerAccounts=" + payerAccounts +
                ", otherAccounts=" + otherAccounts +
                "} " + super.toString();
    }
}

package lt.gama.model.type.auth;

import lt.gama.model.sql.system.CompanySql;

import java.io.Serializable;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 2016-07-07.
 */
public class CompanyAccount implements Serializable {

    /**
     * Company id
     */
    private Long id;

    /**
     * Company name
     */
    private String name;

    /**
     * Connections in another companies,
     * i.e. these accounts will be paid or by this company (positive number) or by others companies (negative number)
     */
    private int accounts;

    protected CompanyAccount() {
    }

    public CompanyAccount(CompanySql company, int accounts) {
        if (company != null) {
            this.id = company.getId();
            this.name = company.getName();
        }
        this.accounts = accounts;
    }

    public CompanyAccount(Long companyId, String name, int accounts) {
        this.id = companyId;
        this.name = name;
        this.accounts = accounts;
    }

    // generated

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAccounts() {
        return accounts;
    }

    public void setAccounts(int accounts) {
        this.accounts = accounts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompanyAccount that = (CompanyAccount) o;
        return accounts == that.accounts && Objects.equals(id, that.id) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, accounts);
    }

    @Override
    public String toString() {
        return "CompanyAccount{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", accounts=" + accounts +
                '}';
    }
}

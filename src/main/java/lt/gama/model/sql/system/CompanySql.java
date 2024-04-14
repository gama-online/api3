package lt.gama.model.sql.system;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.*;
import lt.gama.helpers.DateUtils;
import lt.gama.helpers.StringHelper;
import lt.gama.model.i.IId;
import lt.gama.model.i.ILocations;
import lt.gama.model.sql.base.BaseEntitySql;
import lt.gama.model.type.Contact;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.Location;
import lt.gama.model.type.NameContact;
import lt.gama.model.type.auth.CompanyAccount;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.model.type.doc.DocBankAccount;
import lt.gama.model.type.enums.CompanyStatusType;
import lt.gama.model.type.enums.ExCompanyType;
import lt.gama.service.json.ser.LocalDateTimeTZSerializer;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "companies")
@NamedEntityGraph(name = CompanySql.GRAPH_ALL, attributeNodes = @NamedAttributeNode("payer"))
public class CompanySql extends BaseEntitySql implements IId<Long>, ILocations {

    public static final String GRAPH_ALL = "graph.CompanySql.all";

    @Id
    @GeneratedValue(generator = "gama_company_sequence", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "gama_company_sequence", allocationSize = 10)
    private Long id;

    /**
     * Company name - can not (!) be seen and edited by the customer
     */
    private String name;

    /**
     * Contacts - can not (!) be seen and edited by the customer
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private List<NameContact> contacts;

    /**
     * Company name - can be seen and edited by the customer
     */
    private String businessName;

    /**
     * Business registration address - will be printed on invoices - can be changed by customer
     */
    @Embedded
    private Location registrationAddress;

    /**
     * Real business address
     */
    @Embedded
    private Location businessAddress;


    /**
     * All addresses - can be changed by customer
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Location> locations;

    /**
     * Banks accounts - can be changed by customer
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private List<DocBankAccount> banks;

    /**
     * Contacts - can be changed by customer - will be printed on invoices
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Contact> contactsInfo;

    /**
     * Company registration (tax) code
     */
    private String code;

    /**
     * VAT payer code - if empty - no VAT payer
     */
    private String vatCode;

    /**
     * Social Security Code
     */
    private String ssCode;

    /**
     * URL to company image logo file
     */
    private String logo;

    /**
     * email
     */
    private String email;

    /**
     * CC email
     */
    private String ccEmail;

    /**
     * Company settings
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private CompanySettings settings;

    /**
     * Active company's accounts
     */
    private Integer activeAccounts;

    /**
     * Accounts in other companies.
     * These accounts will be paid or by this company (positive number) or by other companies (negative number)
     * This number will be calculated.
     */
    private Integer payerAccounts;

    /**
     * Map of connections in other companies.
     * These accounts will be paid or by this company (positive number) or by other companies (negative number)
     * Map because easy to seek and update. Map keys are companies ids.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<Long, CompanyAccount> otherAccounts;

    private CompanyStatusType status;

    /**
     * Total price can be fixed ('totalPrice' has value) or can be calculated - company's accounts count is multiplied by 'accountPrice'.
     * If 'totalPrice' has value, i.e. is not null, and it is not negative then accounts count do not matter
     */
    @Embedded
    private GamaMoney totalPrice;

    /**
     * Special company's account price.
     * If 'accountPrice' is null then the value will be taken from standard price-list.
     */
    @Embedded
    private GamaMoney accountPrice;

    /**
     * Subscription Start Date - can not (!) be seen and edited by the customer
     */
    private LocalDate subscriptionDate;

    /**
     * Subscriber Name - will be used to send invoices - can not (!) be seen and edited by the customer
     */
    private String subscriberName;

    /**
     * Subscriber Email - will be used to send invoices - can not (!) be seen and edited by the customer
     */
    private String subscriberEmail;

    /**
     * Last login time
     */
    @JsonSerialize(using = LocalDateTimeTZSerializer.class)
    private LocalDateTime lastLogin;

    /**
     * All employees/connections of this company will be paid by payer company
     *
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_id")
    private CompanySql payer;

    /**
     * How company subscriptions in other company will be calculated:
     * <ul>
     *   <li>COMPANY - all connections in other (!) company where this company is a payer will be counted as single</li>
     *   <li>CONNECTION - each connection will be counted (default)</li>
     * </ul>
     */
    private ExCompanyType exCompanies;

    /**
     * Last subscription amount
     */
    @Embedded
    private GamaMoney lastTotal;

    /**
     * Current subscription amount
     */
    @Embedded
    private GamaMoney currentTotal;

    public CompanySql() {
    }

    public CompanySql(String name) {
        this.name = name;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompanySql that = (CompanySql) o;
        return id != null && id.equals(that.id);
    }

    public int getTotalAccounts() {
        return getActiveAccounts() + getPayerAccounts();
    }

    @SuppressWarnings("unused")
    public Collection<CompanyAccount> getOtherAccountsList() {
        return otherAccounts == null ? null : otherAccounts.values();
    }

    /**
     * Do nothing - need for endpoint serialization
     * @param otherAccounts - none
     */
    @SuppressWarnings("unused")
    private void setOtherAccountsList(Collection<CompanyAccount> otherAccounts) {}

    @SuppressWarnings("unused")
    @PrePersist
    @PreUpdate
    private void onSave() {
        if (businessName == null) {
            businessName = name;
        }
        fixCompanySettings();
    }

    @SuppressWarnings("unused")
    @PostLoad
    private void onLoad() {
        fixCompanySettings();
    }

    private void fixCompanySettings() {
        if (settings == null) {
            settings = new CompanySettings();
        }

        boolean isVatPayer = StringHelper.hasValue(vatCode);
        if (isVatPayer != settings.isVatPayer()) {
            settings.setVatPayer(isVatPayer);
        }

        if (settings.getValidUntil() != null && status == null) {
            status = CompanyStatusType.TRIAL;
        }
        if (status == CompanyStatusType.TRIAL && settings.getValidUntil() == null) {
            settings.setValidUntil(DateUtils.date().plusMonths(1));
        } else if (status == CompanyStatusType.SUBSCRIBER) {
            if (settings.getValidUntil() != null) settings.setValidUntil(null);
        }

        if (settings.getAccYear() == 0) settings.setAccYear(DateUtils.date().getYear());
        if (settings.getStartAccYear() == 0) settings.setStartAccYear(settings.getAccYear());
        if (settings.getAccMonth() < 1 || settings.getAccMonth() > 12) settings.setAccMonth(1);
        if (settings.getStartAccounting() == null) {
            settings.setStartAccounting(LocalDate.of(settings.getStartAccYear(), settings.getAccMonth(), 1));
        }
    }

    /**
     * toString without payer
     */
    @Override
    public String toString() {
        return "CompanySql{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", contacts=" + contacts +
                ", businessName='" + businessName + '\'' +
                ", registrationAddress=" + registrationAddress +
                ", businessAddress=" + businessAddress +
                ", locations=" + locations +
                ", banks=" + banks +
                ", contactsInfo=" + contactsInfo +
                ", code='" + code + '\'' +
                ", vatCode='" + vatCode + '\'' +
                ", ssCode='" + ssCode + '\'' +
                ", logo='" + logo + '\'' +
                ", email='" + email + '\'' +
                ", ccEmail='" + ccEmail + '\'' +
                ", settings=" + settings +
                ", activeAccounts=" + activeAccounts +
                ", payerAccounts=" + payerAccounts +
                ", otherAccounts=" + otherAccounts +
                ", status=" + status +
                ", totalPrice=" + totalPrice +
                ", accountPrice=" + accountPrice +
                ", subscriptionDate=" + subscriptionDate +
                ", subscriberName='" + subscriberName + '\'' +
                ", subscriberEmail='" + subscriberEmail + '\'' +
                ", lastLogin=" + lastLogin +
                ", exCompanies=" + exCompanies +
                ", lastTotal=" + lastTotal +
                ", currentTotal=" + currentTotal +
                "} " + super.toString();
    }

    // customized getters

    public int getActiveAccounts() {
        return activeAccounts == null ? 0 : activeAccounts;
    }

    public int getPayerAccounts() {
        return payerAccounts == null ? 0 : payerAccounts;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<NameContact> getContacts() {
        return contacts;
    }

    public void setContacts(List<NameContact> contacts) {
        this.contacts = contacts;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    @Override
    public Location getRegistrationAddress() {
        return registrationAddress;
    }

    public void setRegistrationAddress(Location registrationAddress) {
        this.registrationAddress = registrationAddress;
    }

    @Override
    public Location getBusinessAddress() {
        return businessAddress;
    }

    public void setBusinessAddress(Location businessAddress) {
        this.businessAddress = businessAddress;
    }

    @Override
    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    public List<DocBankAccount> getBanks() {
        return banks;
    }

    public void setBanks(List<DocBankAccount> banks) {
        this.banks = banks;
    }

    public List<Contact> getContactsInfo() {
        return contactsInfo;
    }

    public void setContactsInfo(List<Contact> contactsInfo) {
        this.contactsInfo = contactsInfo;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getVatCode() {
        return vatCode;
    }

    public void setVatCode(String vatCode) {
        this.vatCode = vatCode;
    }

    public String getSsCode() {
        return ssCode;
    }

    public void setSsCode(String ssCode) {
        this.ssCode = ssCode;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCcEmail() {
        return ccEmail;
    }

    public void setCcEmail(String ccEmail) {
        this.ccEmail = ccEmail;
    }

    public CompanySettings getSettings() {
        return settings;
    }

    public void setSettings(CompanySettings settings) {
        this.settings = settings;
    }

    public void setActiveAccounts(Integer activeAccounts) {
        this.activeAccounts = activeAccounts;
    }

    public void setPayerAccounts(Integer payerAccounts) {
        this.payerAccounts = payerAccounts;
    }

    public Map<Long, CompanyAccount> getOtherAccounts() {
        return otherAccounts;
    }

    public void setOtherAccounts(Map<Long, CompanyAccount> otherAccounts) {
        this.otherAccounts = otherAccounts;
    }

    public CompanyStatusType getStatus() {
        return status;
    }

    public void setStatus(CompanyStatusType status) {
        this.status = status;
    }

    public GamaMoney getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(GamaMoney totalPrice) {
        this.totalPrice = totalPrice;
    }

    public GamaMoney getAccountPrice() {
        return accountPrice;
    }

    public void setAccountPrice(GamaMoney accountPrice) {
        this.accountPrice = accountPrice;
    }

    public LocalDate getSubscriptionDate() {
        return subscriptionDate;
    }

    public void setSubscriptionDate(LocalDate subscriptionDate) {
        this.subscriptionDate = subscriptionDate;
    }

    public String getSubscriberName() {
        return subscriberName;
    }

    public void setSubscriberName(String subscriberName) {
        this.subscriberName = subscriberName;
    }

    public String getSubscriberEmail() {
        return subscriberEmail;
    }

    public void setSubscriberEmail(String subscriberEmail) {
        this.subscriberEmail = subscriberEmail;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public CompanySql getPayer() {
        return payer;
    }

    public void setPayer(CompanySql payer) {
        this.payer = payer;
    }

    public ExCompanyType getExCompanies() {
        return exCompanies;
    }

    public void setExCompanies(ExCompanyType exCompanies) {
        this.exCompanies = exCompanies;
    }

    public GamaMoney getLastTotal() {
        return lastTotal;
    }

    public void setLastTotal(GamaMoney lastTotal) {
        this.lastTotal = lastTotal;
    }

    public GamaMoney getCurrentTotal() {
        return currentTotal;
    }

    public void setCurrentTotal(GamaMoney currentTotal) {
        this.currentTotal = currentTotal;
    }
}

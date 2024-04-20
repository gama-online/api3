package lt.gama.model.sql.entities;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lt.gama.helpers.GamaMoneyUtils;
import lt.gama.helpers.StringHelper;
import lt.gama.model.dto.entities.DebtNowDto;
import lt.gama.model.i.ICounterparty;
import lt.gama.model.sql.base.BaseCompanySql;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.Location;
import lt.gama.model.type.NameContact;
import lt.gama.model.type.doc.DocBankAccount;
import lt.gama.model.type.enums.DebtType;
import lt.gama.model.type.enums.TaxpayerType;
import lt.gama.model.type.gl.GLOperationAccount;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.*;

@Entity
@Table(name = "counterparties")
public class CounterpartySql extends BaseCompanySql implements ICounterparty {

    private String name;

    /**
     * Business registration address
     */
    @Embedded
    private Location registrationAddress;

    /**
     * Real business address
     */
    @Embedded
    private Location businessAddress;

    /**
     * Address for correspondence if not the same as businessAddress
     */
    @Embedded
    private Location postAddress;

    /**
     * Other addresses
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Location> locations;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<NameContact> contacts;

    /**
     * short name or internal code of customer/vendor for use locally only
     */
    private String shortName;

    /**
     * registration code
     */
    private String comCode;

    /**
     * VAT code
     */
    private String vatCode;

    /**
     * Bank.s accounts
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private List<DocBankAccount> banks;

    /**
     * note about customer or vendor
     */
    private String note;

    private Integer creditTerm; // credit term in days

    @Embedded
    private GamaMoney credit; // credit amount

    private Double discount; // client discount %

    private String category; // client category - not used yet

    /**
     * Counterparty accounts as Vendor or Customer
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, GLOperationAccount> accounts = new HashMap<>();

    /**
     * G.L. Account used in bank/cash operations with 'noDebt' set to true
     */
    @Embedded
    private GLOperationAccount noDebtAccount;

    /**
     * Counterparty debt by Vendor or Customer and by currency
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, List<GamaMoney>> debts = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    private Set<String> usedCurrencies = new HashSet<>();

    /**
     * Not DB field - used in reports only
     */
    @Transient
    private List<DebtNowDto> debtsNow;

    /**
     * Bank, cash and advances operations default noDebt value
     */
    private Boolean noDebt;

    /**
     * Counterparty tax type: legal party / farmer / physical person
     */
    private TaxpayerType taxpayerType;

    public CounterpartySql() {
    }

    public CounterpartySql(String name) {
        this.name = name;
    }

    public GLOperationAccount getAccount(DebtType type) {
        return type == null || accounts == null ? null : accounts.get(type.toString());
    }

	public void setAccount(DebtType type, GLOperationAccount account) {
		if (accounts == null) accounts = new HashMap<>();
		if (account != null) accounts.put(type.toString(), account);
		else accounts.remove(type.toString());
	}

	public void updateDebt(DebtType type, GamaMoney amount) {
        if (GamaMoneyUtils.isZero(amount)) return;
		String currency = amount.getCurrency();
		// remember currency
		if (getUsedCurrencies() == null) setUsedCurrencies(new HashSet<>());
		getUsedCurrencies().add(currency);
		// update debt
		if (debts == null) debts = new HashMap<>();
		List<GamaMoney> debtRemainders = debts.get(type.toString());
        if (debtRemainders == null) {
            debtRemainders = new ArrayList<>();
            debtRemainders.add(amount);
            debts.put(type.toString(), debtRemainders);
        } else {
            List<GamaMoney> debtRemaindersNew = new ArrayList<>();
            boolean found = false;
            for (GamaMoney remainder : debtRemainders) {
                if (remainder.getCurrency().equals(currency)) {
                    found = true;
					amount = GamaMoneyUtils.add(remainder, amount);
                    if (GamaMoneyUtils.isNonZero(amount)) {
                        debtRemaindersNew.add(amount);
                    }
                } else {
                    debtRemaindersNew.add(remainder);
                }
            }
            if (!found) debtRemaindersNew.add(amount);
            if (debtRemaindersNew.isEmpty()) {
                debts.remove(type.toString());
            } else {
                debts.put(type.toString(), debtRemaindersNew);
            }
        }
    }

    public void setName(String name) {
        this.name = StringHelper.trimNormalize2null(name);
    }

    public void setComCode(String comCode) {
        this.comCode = StringHelper.trim2null(comCode);
    }

    public void setVatCode(String vatCode) {
        this.vatCode = StringHelper.trim2null(vatCode);
    }

    // generated

    @Override
    public String getName() {
        return name;
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
    public Location getPostAddress() {
        return postAddress;
    }

    public void setPostAddress(Location postAddress) {
        this.postAddress = postAddress;
    }

    @Override
    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    @Override
    public List<NameContact> getContacts() {
        return contacts;
    }

    public void setContacts(List<NameContact> contacts) {
        this.contacts = contacts;
    }

    @Override
    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    @Override
    public String getComCode() {
        return comCode;
    }

    @Override
    public String getVatCode() {
        return vatCode;
    }

    @Override
    public List<DocBankAccount> getBanks() {
        return banks;
    }

    public void setBanks(List<DocBankAccount> banks) {
        this.banks = banks;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public Integer getCreditTerm() {
        return creditTerm;
    }

    public void setCreditTerm(Integer creditTerm) {
        this.creditTerm = creditTerm;
    }

    public GamaMoney getCredit() {
        return credit;
    }

    public void setCredit(GamaMoney credit) {
        this.credit = credit;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public Map<String, GLOperationAccount> getAccounts() {
        return accounts;
    }

    public void setAccounts(Map<String, GLOperationAccount> accounts) {
        this.accounts = accounts;
    }

    @Override
    public GLOperationAccount getNoDebtAccount() {
        return noDebtAccount;
    }

    public void setNoDebtAccount(GLOperationAccount noDebtAccount) {
        this.noDebtAccount = noDebtAccount;
    }

    public Map<String, List<GamaMoney>> getDebts() {
        return debts;
    }

    public void setDebts(Map<String, List<GamaMoney>> debts) {
        this.debts = debts;
    }

    public Set<String> getUsedCurrencies() {
        return usedCurrencies;
    }

    public void setUsedCurrencies(Set<String> usedCurrencies) {
        this.usedCurrencies = usedCurrencies;
    }

    public List<DebtNowDto> getDebtsNow() {
        return debtsNow;
    }

    public void setDebtsNow(List<DebtNowDto> debtsNow) {
        this.debtsNow = debtsNow;
    }

    @Override
    public Boolean getNoDebt() {
        return noDebt;
    }

    public void setNoDebt(Boolean noDebt) {
        this.noDebt = noDebt;
    }

    @Override
    public TaxpayerType getTaxpayerType() {
        return taxpayerType;
    }

    public void setTaxpayerType(TaxpayerType taxpayerType) {
        this.taxpayerType = taxpayerType;
    }

    @Override
    public String toString() {
        return "CounterpartySql{" +
                "name='" + name + '\'' +
                ", registrationAddress=" + registrationAddress +
                ", businessAddress=" + businessAddress +
                ", postAddress=" + postAddress +
                ", locations=" + locations +
                ", contacts=" + contacts +
                ", shortName='" + shortName + '\'' +
                ", comCode='" + comCode + '\'' +
                ", vatCode='" + vatCode + '\'' +
                ", banks=" + banks +
                ", note='" + note + '\'' +
                ", creditTerm=" + creditTerm +
                ", credit=" + credit +
                ", discount=" + discount +
                ", category='" + category + '\'' +
                ", accounts=" + accounts +
                ", noDebtAccount=" + noDebtAccount +
                ", debts=" + debts +
                ", usedCurrencies=" + usedCurrencies +
                ", debtsNow=" + debtsNow +
                ", noDebt=" + noDebt +
                ", taxpayerType=" + taxpayerType +
                "} " + super.toString();
    }
}

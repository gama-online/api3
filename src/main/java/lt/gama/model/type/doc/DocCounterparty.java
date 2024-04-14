package lt.gama.model.type.doc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.Hidden;
import lt.gama.model.i.ICounterparty;
import lt.gama.model.type.Location;
import lt.gama.model.type.NameContact;
import lt.gama.model.type.base.BaseDocEntity;
import lt.gama.model.type.enums.DBType;
import lt.gama.model.type.enums.DebtType;
import lt.gama.model.type.enums.TaxpayerType;
import lt.gama.model.type.gl.GLOperationAccount;

import java.io.Serial;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DocCounterparty extends BaseDocEntity implements ICounterparty {

	@Serial
    private static final long serialVersionUID = -1L;

	private String name;

	private String shortName;

	private String comCode;

	private String vatCode;

    /**
     * Real business address
     */
	private Location businessAddress;

	/**
	 * Business registration address - will be printed on invoices - can be changed by customer
	 */
	private Location registrationAddress;

    /**
     * Address for correspondence if not the same as businessAddress
     */
	private Location postAddress;

	private List<Location> locations;

	private List<NameContact> contacts;

	private DebtType debtType;

	private Integer creditTerm; // credit term in days

	private List<DocBankAccount> banks;

	/**
	 * Counterparty accounts as Vendor or Customer
	 */
	private Map<String, GLOperationAccount> accounts = new HashMap<>();

	/**
	 * G.L. Account used in bank/cash operations with 'noDebt' set to true
	 */
	private GLOperationAccount noDebtAccount;

	/**
	 * Bank, cash and advances operations default noDebt value
	 */
	private Boolean noDebt;

	private TaxpayerType taxpayerType;

	public DocCounterparty() {
	}

	public DocCounterparty(Long id) {
		super(id);
	}

	public DocCounterparty(Long id, String name) {
		super(id);
		this.name = name;
	}

	public DocCounterparty(Long id, DBType db, String name) {
		super(id);
		this.setDb(db);
		this.name = name;
	}


	/**
     * make DocCounterparty without bank and location info, i.e. with info significant for reports only
     */
    public DocCounterparty mainView() {
        DocCounterparty result = new DocCounterparty();
        result.setId(this.getId());
        result.setName(this.getName());
        result.setShortName(this.getShortName());
        result.setComCode(this.getComCode());
        result.setVatCode(this.getVatCode());
        result.setDebtType(this.getDebtType());
        result.setAccounts(this.getAccounts());
		result.setNoDebtAccount(this.getNoDebtAccount());
        return result;
    }

	public DocCounterparty(ICounterparty counterparty) {
		if (counterparty != null) {
			setId(counterparty.getId());
			this.name = counterparty.getName();
			this.shortName = counterparty.getShortName();
			this.comCode = counterparty.getComCode();
			this.vatCode = counterparty.getVatCode();
			this.businessAddress = counterparty.getBusinessAddress();
			this.postAddress = counterparty.getPostAddress();
			this.registrationAddress = counterparty.getRegistrationAddress();
			this.locations = counterparty.getLocations();
			this.contacts = counterparty.getContacts();
			this.banks = counterparty.getBanks();
			if (counterparty.getAccount(DebtType.CUSTOMER) != null && counterparty.getAccount(DebtType.VENDOR) == null) {
				this.debtType = DebtType.CUSTOMER;
			} else if (counterparty.getAccount(DebtType.CUSTOMER) == null && counterparty.getAccount(DebtType.VENDOR) != null) {
				this.debtType = DebtType.VENDOR;
			}
            this.accounts = counterparty.getAccounts();
			this.noDebtAccount = counterparty.getNoDebtAccount();
            this.creditTerm = counterparty.getCreditTerm();
            this.noDebt = counterparty.getNoDebt();
			this.taxpayerType = counterparty.getTaxpayerType();
			this.setDb(counterparty.getDb());
		}
	}

	@Hidden
	@JsonIgnore
	@Override
	public String getAddress() {
		return ICounterparty.super.getAddress();
	}

    public GLOperationAccount getAccount(DebtType type) {
        return type == null || accounts == null ? null : accounts.get(type.toString());
    }

	// generated

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
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

	public void setComCode(String comCode) {
		this.comCode = comCode;
	}

	@Override
	public String getVatCode() {
		return vatCode;
	}

	public void setVatCode(String vatCode) {
		this.vatCode = vatCode;
	}

	@Override
	public Location getBusinessAddress() {
		return businessAddress;
	}

	public void setBusinessAddress(Location businessAddress) {
		this.businessAddress = businessAddress;
	}

	@Override
	public Location getRegistrationAddress() {
		return registrationAddress;
	}

	public void setRegistrationAddress(Location registrationAddress) {
		this.registrationAddress = registrationAddress;
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

	public DebtType getDebtType() {
		return debtType;
	}

	public void setDebtType(DebtType debtType) {
		this.debtType = debtType;
	}

	@Override
	public Integer getCreditTerm() {
		return creditTerm;
	}

	public void setCreditTerm(Integer creditTerm) {
		this.creditTerm = creditTerm;
	}

	@Override
	public List<DocBankAccount> getBanks() {
		return banks;
	}

	public void setBanks(List<DocBankAccount> banks) {
		this.banks = banks;
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
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		DocCounterparty that = (DocCounterparty) o;
		return Objects.equals(name, that.name) && Objects.equals(shortName, that.shortName) && Objects.equals(comCode, that.comCode) && Objects.equals(vatCode, that.vatCode) && Objects.equals(businessAddress, that.businessAddress) && Objects.equals(registrationAddress, that.registrationAddress) && Objects.equals(postAddress, that.postAddress) && Objects.equals(locations, that.locations) && Objects.equals(contacts, that.contacts) && debtType == that.debtType && Objects.equals(creditTerm, that.creditTerm) && Objects.equals(banks, that.banks) && Objects.equals(accounts, that.accounts) && Objects.equals(noDebtAccount, that.noDebtAccount) && Objects.equals(noDebt, that.noDebt) && taxpayerType == that.taxpayerType;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), name, shortName, comCode, vatCode, businessAddress, registrationAddress, postAddress, locations, contacts, debtType, creditTerm, banks, accounts, noDebtAccount, noDebt, taxpayerType);
	}

	@Override
	public String toString() {
		return "DocCounterparty{" +
				"name='" + name + '\'' +
				", shortName='" + shortName + '\'' +
				", comCode='" + comCode + '\'' +
				", vatCode='" + vatCode + '\'' +
				", businessAddress=" + businessAddress +
				", registrationAddress=" + registrationAddress +
				", postAddress=" + postAddress +
				", locations=" + locations +
				", contacts=" + contacts +
				", debtType=" + debtType +
				", creditTerm=" + creditTerm +
				", banks=" + banks +
				", accounts=" + accounts +
				", noDebtAccount=" + noDebtAccount +
				", noDebt=" + noDebt +
				", taxpayerType=" + taxpayerType +
				"} " + super.toString();
	}
}

package lt.gama.model.dto.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lt.gama.model.type.Location;
import lt.gama.model.type.NameContact;
import lt.gama.model.type.doc.DocBankAccount;
import lt.gama.model.type.gl.GLOperationAccount;
import lt.gama.model.dto.base.BaseCompanyDto;
import lt.gama.model.i.ICounterparty;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.enums.DBType;
import lt.gama.model.type.enums.DebtType;
import lt.gama.model.type.enums.TaxpayerType;

import java.io.Serial;
import java.util.*;

public class CounterpartyDto extends BaseCompanyDto implements ICounterparty {

    @Serial
    private static final long serialVersionUID = -1L;

    private String name;

    /**
     * Business registration address
     */
    private Location registrationAddress;

    /**
     * Real business address
     */
    private Location businessAddress;

    /**
     * Address for correspondence if not the same as businessAddress
     */
    private Location postAddress;

    /**
     * Other addresses
     */
    private List<Location> locations;


    private List<NameContact> contacts;

    /**
     * short name of customer/vendor for use locally only
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
    private List<DocBankAccount> banks;

    /**
     * note about customer or vendor
     */
    private String note;

    private Integer creditTerm; // credit term in days

    private GamaMoney credit; // credit amount

    private Double discount; // client discount %

    private String category; // client category - not used yet

    /**
     * Counterparty accounts as Vendor or Customer
     */
    private Map<String, GLOperationAccount> accounts = new HashMap<>();

    /**
     * G.L. Account used in bank/cash operations with 'noDebt' set to true
     */
    private GLOperationAccount noDebtAccount;

    /**
     * Counterparty debt by Vendor or Customer and by currency
     */
    private Map<String, List<GamaMoney>> debts = new HashMap<>();

    private Set<String> usedCurrencies = new HashSet<>();

    /**
     * Not DB field - used in reports only
     */
    private List<DebtNowDto> debtsNow;

    /**
     * Bank, cash and advances operations default noDebt value
     */
    private Boolean noDebt;

    /**
     * Counterparty tax type: legal party / farmer / physical person
     */
    private TaxpayerType taxpayerType;

    /**
     * For import only
     */
    @JsonProperty("debtType")
    private DebtType debtTypeImport;


    public CounterpartyDto() {
    }

    public CounterpartyDto(long id, DBType db) {
        setId(id);
        setDb(db);
    }

    public CounterpartyDto(ICounterparty counterparty) {
        if (counterparty == null) return;
        setId(counterparty.getId());
        setDb(counterparty.getDb());
        this.name = counterparty.getName();
        this.shortName = counterparty.getShortName();
        this.comCode = counterparty.getComCode();
        this.vatCode = counterparty.getVatCode();
    }

    public CounterpartyDto(long companyId, long id, DBType db, String name, String shortName, String comCode, String vatCode) {
        super(companyId, id, db);
        this.name = name;
        this.shortName = shortName;
        this.comCode = comCode;
        this.vatCode = vatCode;
    }

    public GLOperationAccount getAccount(DebtType type) {
        return type == null || accounts == null ? null : accounts.get(type.toString());
    }

    public void setAccount(DebtType type, GLOperationAccount account) {
        if (accounts == null) accounts = new HashMap<>();
        if (account != null) accounts.put(type.toString(), account);
        else accounts.remove(type.toString());
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

    public DebtType getDebtTypeImport() {
        return debtTypeImport;
    }

    public void setDebtTypeImport(DebtType debtTypeImport) {
        this.debtTypeImport = debtTypeImport;
    }

    @Override
    public String toString() {
        return "CounterpartyDto{" +
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
                ", debtTypeImport=" + debtTypeImport +
                "} " + super.toString();
    }
}

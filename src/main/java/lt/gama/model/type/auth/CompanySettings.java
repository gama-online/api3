package lt.gama.model.type.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.Hidden;
import lt.gama.helpers.EntityUtils;
import lt.gama.helpers.StringHelper;
import lt.gama.model.sql.documents.*;
import lt.gama.model.sql.entities.AssetSql;
import lt.gama.model.type.cf.CFDescription;
import lt.gama.model.type.doc.DocBankAccount;
import lt.gama.model.type.doc.DocCash;
import lt.gama.model.type.doc.DocCharge;
import lt.gama.model.type.doc.DocWarehouse;
import lt.gama.model.type.sync.SyncSettings;
import lt.gama.model.i.ILanguage;
import lt.gama.model.type.inventory.InvoiceNote;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.*;

import static java.util.Map.entry;

/**
 * Gama
 * Created by valdas on 15-02-17.
 */
public class CompanySettings implements ILanguage, Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    /**
     * If not null - this is the End of trial period. After it the login into the company will be disabled.
     */
    @Hidden
    private LocalDate validUntil;

    /**
     * Start accounting year.
     * @deprecated
     * use {@link #startAccounting} instead
     */
    @Hidden
    @Deprecated
    private int startAccYear;

    /**
     * Start accounting date,
     * i.e. opening balances are in one day before this date
     */
    private LocalDate startAccounting;

    /**
     * Current accounting year
     */
    private int accYear;

    /**
     * Accounting month - can't be changed after start.
     */
    private int accMonth;

    /**
     * used currency
     */
    private CurrencySettings currency;

    /**
     * Basic money calculations precision, i.e. numbers count after decimal point
     */
    private int decimal;

    /**
     * Price precision, i.e. numbers count after decimal point
     */
    private int decimalPrice;

    /**
     * Cost precision - used in purchases and invoices for calculated items price
     */
    private int decimalCost;

    /**
     * Company region for currency rates:
     * EU - European Central Bank exchange rates
     * LT - Lithuanian accounting exchanges rates (the same as EU but one day later)
     */
    private String region;

    /**
     * Language code (ISO 639-1): lt, en, ...
     */
    private String language;

    /**
     * Country code (ISO 3166-1 alpha-2): LT, US, ...
     */
    private String country;

    /**
     * Company TimeZone from com.ibm.icu.util.TimeZone,
     * i.e. it recognize various formats like "America/Los_Angeles", "Europe/Vilnius" or
     * GMT+14:30, GMT-2:00
     */
    private String timeZone;

    /**
     * Enable of use Tax Free declarations
     */
    private Boolean enableTaxFree;

    /**
     * G.L. disabled, i.e. no G.L. double records will be generated on documents finishing
     */
    private Boolean disableGL;

    private CompanySettingsGL gl;

    /**
     * List of company taxes by dates
     */
    private List<CompanyTaxSettings> taxes;

    private Boolean vatPayer;

    private Map<String, String> docType = new HashMap<>();

    /*
     * Custom and documents counters
     */

    public static final String CounterBarcode = "_Barcode";

    private Map<String, CounterDesc> counter = new HashMap<>();

    /*
     * Custom fields
     */

    private List<CFDescription> cfPartSN;

    private List<CFDescription> cfPart;

    private List<CFDescription> cfCounterparty;

    private List<CFDescription> cfEmployee;


    /**
     * Default warehouse - the first one by default
     */
    private DocWarehouse warehouse;

    /**
     * Default bank account - the first one by default
     */
    private DocBankAccount account;

    /**
     * Default cash - the first one by default
     */
    private DocCash cash;

    /*
     * Charges for use in salary calculation
     */

    private DocCharge chargeAdvance;

    private DocCharge chargeWork;

    private DocCharge chargeIllness;

    private DocCharge chargeVacation;

    private DocCharge chargeChildDays;

    /**
     * List of salary settings by dates
     */
    private List<CompanySalarySettings> salary;

    @Hidden
    private SyncSettings sync;

    private SalesSettings sales;

    /**
     * Custom Invoice notes
     */
    private List<InvoiceNote> invoiceNotes;


    private GpaisSettings gpais;


    private static final Map<String, String> docTypeDefault = Map.ofEntries(
            entry(EntityUtils.normalizeEntityClassName(DebtOpeningBalanceSql.class), "Debt Opening Balance"),
            entry(EntityUtils.normalizeEntityClassName(DebtCorrectionSql.class), "Debt Correction"),
            entry(EntityUtils.normalizeEntityClassName(DebtRateInfluenceSql.class), "Debt's $ Rate Influence"),

            entry(EntityUtils.normalizeEntityClassName(EmployeeOpeningBalanceSql.class), "Advances Opening Balance"),
            entry(EntityUtils.normalizeEntityClassName(EmployeeOperationSql.class), "Advances Operation"),
            entry(EntityUtils.normalizeEntityClassName(EmployeeOperationSql.class) + "+", "Cash Receipt"),
            entry(EntityUtils.normalizeEntityClassName(EmployeeOperationSql.class) + "-", "Cash Payment Receipt"),
            entry(EntityUtils.normalizeEntityClassName(EmployeeRateInfluenceSql.class), "Advances $ Rate Influence"),

            entry(EntityUtils.normalizeEntityClassName(BankOpeningBalanceSql.class), "Bank's Accounts Opening Balance"),
            entry(EntityUtils.normalizeEntityClassName(BankOperationSql.class), "Bank's Operation"),
            entry(EntityUtils.normalizeEntityClassName(BankRateInfluenceSql.class), "Bank's $ Rate Influence"),

            entry(EntityUtils.normalizeEntityClassName(CashOpeningBalanceSql.class), "Cash Opening Balance"),
            entry(EntityUtils.normalizeEntityClassName(CashOperationSql.class), "Cash Order"),
            entry(EntityUtils.normalizeEntityClassName(CashOperationSql.class) + "+", "Cash Income Order"),
            entry(EntityUtils.normalizeEntityClassName(CashOperationSql.class) + "-", "Cash Expense Order"),
            entry(EntityUtils.normalizeEntityClassName(CashRateInfluenceSql.class), "Cash $ Rate Influence"),

            entry(EntityUtils.normalizeEntityClassName(InventoryOpeningBalanceSql.class), "Inventory Opening Balance"),
            entry(EntityUtils.normalizeEntityClassName(InvoiceSql.class), "Invoice"),
            entry(EntityUtils.normalizeEntityClassName(PurchaseSql.class), "Purchase"),
            entry(EntityUtils.normalizeEntityClassName(EstimateSql.class), "Estimate"),
            entry(EntityUtils.normalizeEntityClassName(OrderSql.class), "Order"),
            entry(EntityUtils.normalizeEntityClassName(TransProdSql.class), "Transport/Production"),
            entry(EntityUtils.normalizeEntityClassName(InventorySql.class), "Inventory"),

            entry(EntityUtils.normalizeEntityClassName(AssetSql.class), "Asset"),
            entry(EntityUtils.normalizeEntityClassName(SalarySql.class), "Salary"));

    public static final Set<String> docHasCounter = Set.of(
            EntityUtils.normalizeEntityClassName(DebtOpeningBalanceSql.class),
            EntityUtils.normalizeEntityClassName(DebtCorrectionSql.class),
            EntityUtils.normalizeEntityClassName(DebtRateInfluenceSql.class),

            EntityUtils.normalizeEntityClassName(EmployeeOpeningBalanceSql.class),
            EntityUtils.normalizeEntityClassName(EmployeeOperationSql.class) + "+",
            EntityUtils.normalizeEntityClassName(EmployeeOperationSql.class) + "-",
            EntityUtils.normalizeEntityClassName(EmployeeRateInfluenceSql.class),

            EntityUtils.normalizeEntityClassName(BankOpeningBalanceSql.class),
            EntityUtils.normalizeEntityClassName(BankOperationSql.class),
            EntityUtils.normalizeEntityClassName(BankRateInfluenceSql.class),

            EntityUtils.normalizeEntityClassName(CashOpeningBalanceSql.class),
            EntityUtils.normalizeEntityClassName(CashOperationSql.class) + "+",
            EntityUtils.normalizeEntityClassName(CashOperationSql.class) + "-",
            EntityUtils.normalizeEntityClassName(CashRateInfluenceSql.class),

            EntityUtils.normalizeEntityClassName(InventoryOpeningBalanceSql.class),
            EntityUtils.normalizeEntityClassName(InvoiceSql.class),
            EntityUtils.normalizeEntityClassName(EstimateSql.class),
            EntityUtils.normalizeEntityClassName(OrderSql.class),
            EntityUtils.normalizeEntityClassName(TransProdSql.class),
            EntityUtils.normalizeEntityClassName(InventorySql.class),

            EntityUtils.normalizeEntityClassName(AssetSql.class),
            EntityUtils.normalizeEntityClassName(SalarySql.class));


    private static final Map<String, String> docLT = Map.ofEntries(
            entry(EntityUtils.normalizeEntityClassName(DebtOpeningBalanceSql.class), "Skolos lik."),
            entry(EntityUtils.normalizeEntityClassName(DebtCorrectionSql.class), "Skolų korekcija"),
            entry(EntityUtils.normalizeEntityClassName(DebtRateInfluenceSql.class), "Skolų $ kurso kor."),

            entry(EntityUtils.normalizeEntityClassName(EmployeeOpeningBalanceSql.class), "Avans. lik."),
            entry(EntityUtils.normalizeEntityClassName(EmployeeOperationSql.class), "Pinigų pr./iš. kvitas"),
            entry(EntityUtils.normalizeEntityClassName(EmployeeOperationSql.class) + "+", "Pinigų priėmimo kvitas"),
            entry(EntityUtils.normalizeEntityClassName(EmployeeOperationSql.class) + "-", "Pinigų išmokėjimo kvitas"),
            entry(EntityUtils.normalizeEntityClassName(EmployeeRateInfluenceSql.class), "Avans. $ kurso kor."),

            entry(EntityUtils.normalizeEntityClassName(BankOpeningBalanceSql.class), "Banko lik."),
            entry(EntityUtils.normalizeEntityClassName(BankOperationSql.class), "Bankas"),
            entry(EntityUtils.normalizeEntityClassName(BankRateInfluenceSql.class), "Banko $ kurso kor."),

            entry(EntityUtils.normalizeEntityClassName(CashOpeningBalanceSql.class), "Kasos lik."),
            entry(EntityUtils.normalizeEntityClassName(CashOperationSql.class), "Kasa"),
            entry(EntityUtils.normalizeEntityClassName(CashOperationSql.class) + "+", "Kasos pajamų orderis"),
            entry(EntityUtils.normalizeEntityClassName(CashOperationSql.class) + "-", "Kasos išlaidų orderis"),
            entry(EntityUtils.normalizeEntityClassName(CashRateInfluenceSql.class), "Kasos $ kurso kor."),

            entry(EntityUtils.normalizeEntityClassName(InventoryOpeningBalanceSql.class), "Atsargų lik."),
            entry(EntityUtils.normalizeEntityClassName(InvoiceSql.class), "Pardavimas"),
            entry(EntityUtils.normalizeEntityClassName(PurchaseSql.class), "Pirkimas"),
            entry(EntityUtils.normalizeEntityClassName(EstimateSql.class), "Planuojamas pardavimas"),
            entry(EntityUtils.normalizeEntityClassName(OrderSql.class), "Užsakymas"),
            entry(EntityUtils.normalizeEntityClassName(TransProdSql.class), "Transp./Gamyba"),
            entry(EntityUtils.normalizeEntityClassName(InventorySql.class), "Inventorizacija"),

            entry(EntityUtils.normalizeEntityClassName(AssetSql.class), "Turtas"),
            entry(EntityUtils.normalizeEntityClassName(SalarySql.class), "Mėn. atlyginimų lapas"));


    public boolean isDisableGL() {
        return disableGL != null && disableGL;
    }

    public boolean isVatPayer() {
        return vatPayer != null && vatPayer;
    }

    public LocalDate getStartAccounting() {
        return startAccounting != null ? startAccounting : LocalDate.of(getStartAccYear(), getAccMonth(), 1);
    }

    public int getAccMonth() {
        return accMonth > 0 && accMonth <= 12 ? accMonth : 1;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public LocalDate getStartAccountingPeriod() {
        return LocalDate.of(getAccYear(), getAccMonth(), 1);
    }

    public String getDocName(String key) {
        String value = getDocName().get(EntityUtils.normalizeEntityName(key));
        if (value == null || value.isEmpty()) value = key;
        return value;
    }

    public Map<String, String> getDocName() {
        if (docType == null) {
            docType = new HashMap<>(docTypeDefault);
        } else {
            for (String key : docTypeDefault.keySet()) {
                if (!docType.containsKey(key)) {
                    if ("lt".equals(getLanguage())) {
                        docType.put(key, docLT.get(key));
                    } else{
                        docType.put(key, docTypeDefault.get(key));
                    }
                }
            }
        }
        return docType;
    }

    public void setDocNames(String language) {
        if ("lt".equals(language)) {
            docType = new HashMap<>(docLT);
        } else {
            docType = new HashMap<>(docTypeDefault);
        }
    }

    public Map<String, String> docNamesByLanguage(String language) {
        return Objects.equals(language, getLanguage()) ? getDocName() : docTypeDefault;
    }

    public Map<String, CounterDesc> getCounter() {
        if (counter == null) counter = new HashMap<>();
        for (String key : docTypeDefault.keySet()) {
            if (!docHasCounter.contains(key)) {
                counter.remove(key);
                continue;
            }
            CounterDesc desc = counter.get(key);
            if (desc == null) {
                counter.put(key, new CounterDesc(key));
            } else {
                desc.setLabel(key);
            }
        }

        CounterDesc desc = counter.get(CounterBarcode);
        if (desc == null) {
            counter.put(CounterBarcode, new CounterDesc(CounterBarcode));
        } else {
            desc.setLabel(CounterBarcode);
        }
        return counter;
    }

    public CounterDesc getCounterByClass(Class<?> classType) {
        if (getCounter() == null || classType == null) return null;
        String label = EntityUtils.normalizeEntityClassName(classType);
        if (docHasCounter.contains(label)) return getCounter().get(label);
        return null;
    }

    public CounterDesc getCounterByClass(Class<?> classType, String suffix) {
        if (getCounter() == null || classType == null) return null;
        String label = EntityUtils.normalizeEntityClassName(classType) + StringHelper.trim(suffix);
        if (docHasCounter.contains(label)) return getCounter().get(label);
        return null;
    }

    // generated
    // disabled:
    //  - getDisableGL()
    //  - getVatPayer()

    public LocalDate getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDate validUntil) {
        this.validUntil = validUntil;
    }

    public int getStartAccYear() {
        return startAccYear;
    }

    public void setStartAccYear(int startAccYear) {
        this.startAccYear = startAccYear;
    }

    public void setStartAccounting(LocalDate startAccounting) {
        this.startAccounting = startAccounting;
    }

    public int getAccYear() {
        return accYear;
    }

    public void setAccYear(int accYear) {
        this.accYear = accYear;
    }

    public void setAccMonth(int accMonth) {
        this.accMonth = accMonth;
    }

    public CurrencySettings getCurrency() {
        return currency;
    }

    public void setCurrency(CurrencySettings currency) {
        this.currency = currency;
    }

    public int getDecimal() {
        return decimal;
    }

    public void setDecimal(int decimal) {
        this.decimal = decimal;
    }

    public int getDecimalPrice() {
        return decimalPrice;
    }

    public void setDecimalPrice(int decimalPrice) {
        this.decimalPrice = decimalPrice;
    }

    public int getDecimalCost() {
        return decimalCost;
    }

    public void setDecimalCost(int decimalCost) {
        this.decimalCost = decimalCost;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    @Override
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public Boolean getEnableTaxFree() {
        return enableTaxFree;
    }

    public void setEnableTaxFree(Boolean enableTaxFree) {
        this.enableTaxFree = enableTaxFree;
    }

//    public Boolean getDisableGL() {
//        return disableGL;
//    }

    public void setDisableGL(Boolean disableGL) {
        this.disableGL = disableGL;
    }

    public CompanySettingsGL getGl() {
        return gl;
    }

    public void setGl(CompanySettingsGL gl) {
        this.gl = gl;
    }

    public List<CompanyTaxSettings> getTaxes() {
        return taxes;
    }

    public void setTaxes(List<CompanyTaxSettings> taxes) {
        this.taxes = taxes;
    }

//    public Boolean getVatPayer() {
//        return vatPayer;
//    }

    public void setVatPayer(Boolean vatPayer) {
        this.vatPayer = vatPayer;
    }

    public Map<String, String> getDocType() {
        return docType;
    }

    public void setDocType(Map<String, String> docType) {
        this.docType = docType;
    }

    public void setCounter(Map<String, CounterDesc> counter) {
        this.counter = counter;
    }

    public List<CFDescription> getCfPartSN() {
        return cfPartSN;
    }

    public void setCfPartSN(List<CFDescription> cfPartSN) {
        this.cfPartSN = cfPartSN;
    }

    public List<CFDescription> getCfPart() {
        return cfPart;
    }

    public void setCfPart(List<CFDescription> cfPart) {
        this.cfPart = cfPart;
    }

    public List<CFDescription> getCfCounterparty() {
        return cfCounterparty;
    }

    public void setCfCounterparty(List<CFDescription> cfCounterparty) {
        this.cfCounterparty = cfCounterparty;
    }

    public List<CFDescription> getCfEmployee() {
        return cfEmployee;
    }

    public void setCfEmployee(List<CFDescription> cfEmployee) {
        this.cfEmployee = cfEmployee;
    }

    public DocWarehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(DocWarehouse warehouse) {
        this.warehouse = warehouse;
    }

    public DocBankAccount getAccount() {
        return account;
    }

    public void setAccount(DocBankAccount account) {
        this.account = account;
    }

    public DocCash getCash() {
        return cash;
    }

    public void setCash(DocCash cash) {
        this.cash = cash;
    }

    public DocCharge getChargeAdvance() {
        return chargeAdvance;
    }

    public void setChargeAdvance(DocCharge chargeAdvance) {
        this.chargeAdvance = chargeAdvance;
    }

    public DocCharge getChargeWork() {
        return chargeWork;
    }

    public void setChargeWork(DocCharge chargeWork) {
        this.chargeWork = chargeWork;
    }

    public DocCharge getChargeIllness() {
        return chargeIllness;
    }

    public void setChargeIllness(DocCharge chargeIllness) {
        this.chargeIllness = chargeIllness;
    }

    public DocCharge getChargeVacation() {
        return chargeVacation;
    }

    public void setChargeVacation(DocCharge chargeVacation) {
        this.chargeVacation = chargeVacation;
    }

    public DocCharge getChargeChildDays() {
        return chargeChildDays;
    }

    public void setChargeChildDays(DocCharge chargeChildDays) {
        this.chargeChildDays = chargeChildDays;
    }

    public List<CompanySalarySettings> getSalary() {
        return salary;
    }

    public void setSalary(List<CompanySalarySettings> salary) {
        this.salary = salary;
    }

    public SyncSettings getSync() {
        return sync;
    }

    public void setSync(SyncSettings sync) {
        this.sync = sync;
    }

    public SalesSettings getSales() {
        return sales;
    }

    public void setSales(SalesSettings sales) {
        this.sales = sales;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Boolean getMigratedDebt() {
        return true;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Boolean getMigratedDebtDocs() {
        return true;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Boolean getMigratedMoney() {
        return true;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Boolean getMigratedMoneyDocs() {
        return true;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Boolean getMigratedParts() {
        return true;
    }

    public List<InvoiceNote> getInvoiceNotes() {
        return invoiceNotes;
    }

    public void setInvoiceNotes(List<InvoiceNote> invoiceNotes) {
        this.invoiceNotes = invoiceNotes;
    }

    public GpaisSettings getGpais() {
        return gpais;
    }

    public void setGpais(GpaisSettings gpais) {
        this.gpais = gpais;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompanySettings that = (CompanySettings) o;
        return startAccYear == that.startAccYear && accYear == that.accYear && accMonth == that.accMonth && decimal == that.decimal && decimalPrice == that.decimalPrice && decimalCost == that.decimalCost && Objects.equals(validUntil, that.validUntil) && Objects.equals(startAccounting, that.startAccounting) && Objects.equals(currency, that.currency) && Objects.equals(region, that.region) && Objects.equals(language, that.language) && Objects.equals(country, that.country) && Objects.equals(timeZone, that.timeZone) && Objects.equals(enableTaxFree, that.enableTaxFree) && Objects.equals(disableGL, that.disableGL) && Objects.equals(gl, that.gl) && Objects.equals(taxes, that.taxes) && Objects.equals(vatPayer, that.vatPayer) && Objects.equals(docType, that.docType) && Objects.equals(counter, that.counter) && Objects.equals(cfPartSN, that.cfPartSN) && Objects.equals(cfPart, that.cfPart) && Objects.equals(cfCounterparty, that.cfCounterparty) && Objects.equals(cfEmployee, that.cfEmployee) && Objects.equals(warehouse, that.warehouse) && Objects.equals(account, that.account) && Objects.equals(cash, that.cash) && Objects.equals(chargeAdvance, that.chargeAdvance) && Objects.equals(chargeWork, that.chargeWork) && Objects.equals(chargeIllness, that.chargeIllness) && Objects.equals(chargeVacation, that.chargeVacation) && Objects.equals(chargeChildDays, that.chargeChildDays) && Objects.equals(salary, that.salary) && Objects.equals(sync, that.sync) && Objects.equals(sales, that.sales) && Objects.equals(invoiceNotes, that.invoiceNotes) && Objects.equals(gpais, that.gpais);
    }

    @Override
    public int hashCode() {
        return Objects.hash(validUntil, startAccYear, startAccounting, accYear, accMonth, currency, decimal, decimalPrice, decimalCost, region, language, country, timeZone, enableTaxFree, disableGL, gl, taxes, vatPayer, docType, counter, cfPartSN, cfPart, cfCounterparty, cfEmployee, warehouse, account, cash, chargeAdvance, chargeWork, chargeIllness, chargeVacation, chargeChildDays, salary, sync, sales, invoiceNotes, gpais);
    }


    @Override
    public String toString() {
        return "CompanySettings{" +
                "validUntil=" + validUntil +
                ", startAccYear=" + startAccYear +
                ", startAccounting=" + startAccounting +
                ", accYear=" + accYear +
                ", accMonth=" + accMonth +
                ", currency=" + currency +
                ", decimal=" + decimal +
                ", decimalPrice=" + decimalPrice +
                ", decimalCost=" + decimalCost +
                ", region='" + region + '\'' +
                ", language='" + language + '\'' +
                ", country='" + country + '\'' +
                ", timeZone='" + timeZone + '\'' +
                ", enableTaxFree=" + enableTaxFree +
                ", disableGL=" + disableGL +
                ", gl=" + gl +
                ", taxes=" + taxes +
                ", vatPayer=" + vatPayer +
                ", docType=" + docType +
                ", counter=" + counter +
                ", cfPartSN=" + cfPartSN +
                ", cfPart=" + cfPart +
                ", cfCounterparty=" + cfCounterparty +
                ", cfEmployee=" + cfEmployee +
                ", warehouse=" + warehouse +
                ", account=" + account +
                ", cash=" + cash +
                ", chargeAdvance=" + chargeAdvance +
                ", chargeWork=" + chargeWork +
                ", chargeIllness=" + chargeIllness +
                ", chargeVacation=" + chargeVacation +
                ", chargeChildDays=" + chargeChildDays +
                ", salary=" + salary +
                ", sync=" + sync +
                ", sales=" + sales +
                ", invoiceNotes=" + invoiceNotes +
                ", gpaisSettings=" + gpais +
                '}';
    }
}

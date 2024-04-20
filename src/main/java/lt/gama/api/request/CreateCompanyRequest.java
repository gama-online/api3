package lt.gama.api.request;

import lt.gama.model.type.Location;

/**
 * gama-online
 * Created by valdas on 2015-11-26.
 */
public class CreateCompanyRequest {

    private String companyName;

    private String name;

    private String login;

    private String language;

    private String country;

    private String timeZone;

    private String code;

    private String vatCode;

    private Boolean useStdChartOfAccounts;

    private Boolean accountant;

    private Boolean activateSubscription;

    /**
     * Business registration address - can be seen and edited by the customer
     */
    private Location registrationAddress;

    /**
     * Real business address
     */
    private Location businessAddress;

    // generated

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

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

    public Boolean getUseStdChartOfAccounts() {
        return useStdChartOfAccounts;
    }

    public void setUseStdChartOfAccounts(Boolean useStdChartOfAccounts) {
        this.useStdChartOfAccounts = useStdChartOfAccounts;
    }

    public Boolean getAccountant() {
        return accountant;
    }

    public void setAccountant(Boolean accountant) {
        this.accountant = accountant;
    }

    public Boolean getActivateSubscription() {
        return activateSubscription;
    }

    public void setActivateSubscription(Boolean activateSubscription) {
        this.activateSubscription = activateSubscription;
    }

    public Location getRegistrationAddress() {
        return registrationAddress;
    }

    public void setRegistrationAddress(Location registrationAddress) {
        this.registrationAddress = registrationAddress;
    }

    public Location getBusinessAddress() {
        return businessAddress;
    }

    public void setBusinessAddress(Location businessAddress) {
        this.businessAddress = businessAddress;
    }

    @Override
    public String toString() {
        return "CreateCompanyRequest{" +
                "companyName='" + companyName + '\'' +
                ", name='" + name + '\'' +
                ", login='" + login + '\'' +
                ", language='" + language + '\'' +
                ", country='" + country + '\'' +
                ", timeZone='" + timeZone + '\'' +
                ", code='" + code + '\'' +
                ", vatCode='" + vatCode + '\'' +
                ", useStdChartOfAccounts=" + useStdChartOfAccounts +
                ", accountant=" + accountant +
                ", activateSubscription=" + activateSubscription +
                ", registrationAddress=" + registrationAddress +
                ", businessAddress=" + businessAddress +
                '}';
    }
}

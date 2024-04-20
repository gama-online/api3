package lt.gama.api.response;

import lt.gama.model.sql.system.CompanySql;
import lt.gama.model.type.Contact;
import lt.gama.model.type.Location;
import lt.gama.model.type.auth.CompanySettings;

import java.util.List;

/**
 * Gama
 * Created by valdas on 15-05-26.
 */
public class CompanyResponse {

    private String name;

    private Location businessAddress;

    private Location registrationAddress;

    private List<Location> locations;

    private String code;

    /**
     * VAT payer code - if empty - no VAT payer
     */
    private String vatCode;

    private String logo;

    private CompanySettings settings;

    private List<Contact> contactsInfo;


    public CompanyResponse() {
    }

    public CompanyResponse(CompanySql company) {
        if (company == null) return;

        name = company.getName();
        businessAddress = company.getBusinessAddress();
        registrationAddress = company.getRegistrationAddress();
        locations = company.getLocations();
        if (businessAddress == null && locations != null && locations.size() > 0) {
            businessAddress = locations.get(0);
        }
        contactsInfo = company.getContactsInfo();

        code = company.getCode();
        vatCode = company.getVatCode();
        logo = company.getLogo();

        if (company.getSettings() != null) {
            settings = new CompanySettings();
            settings.setDecimal(company.getSettings().getDecimal());
            settings.setDecimalCost(company.getSettings().getDecimalCost());
            settings.setDecimalPrice(company.getSettings().getDecimalPrice());
            settings.setCurrency(company.getSettings().getCurrency());
            settings.setAccYear(company.getSettings().getAccYear());
            settings.setAccMonth(company.getSettings().getAccMonth());
        }
    }

    // generated

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getBusinessAddress() {
        return businessAddress;
    }

    public void setBusinessAddress(Location businessAddress) {
        this.businessAddress = businessAddress;
    }

    public Location getRegistrationAddress() {
        return registrationAddress;
    }

    public void setRegistrationAddress(Location registrationAddress) {
        this.registrationAddress = registrationAddress;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
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

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public CompanySettings getSettings() {
        return settings;
    }

    public void setSettings(CompanySettings settings) {
        this.settings = settings;
    }

    public List<Contact> getContactsInfo() {
        return contactsInfo;
    }

    public void setContactsInfo(List<Contact> contactsInfo) {
        this.contactsInfo = contactsInfo;
    }

    @Override
    public String toString() {
        return "CompanyResponse{" +
                "name='" + name + '\'' +
                ", businessAddress=" + businessAddress +
                ", registrationAddress=" + registrationAddress +
                ", locations=" + locations +
                ", code='" + code + '\'' +
                ", vatCode='" + vatCode + '\'' +
                ", logo='" + logo + '\'' +
                ", settings=" + settings +
                ", contactsInfo=" + contactsInfo +
                '}';
    }
}

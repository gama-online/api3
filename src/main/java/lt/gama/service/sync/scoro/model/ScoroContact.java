package lt.gama.service.sync.scoro.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * gama-online
 * Created by valdas on 2017-10-11.
 */
public class ScoroContact {

    @JsonProperty("contact_id")
    private long contactId;

    private String name;

    private String lastname;

    @JsonProperty("id_code")
    private String idCode;

    private String bankaccount;

    private String vatno;

    private List<ScoroAddress> addresses;

    /*
     * "company" or "person"
     */
    @JsonProperty("contact_type")
    private String contactType;

    // generated

    public long getContactId() {
        return contactId;
    }

    public void setContactId(long contactId) {
        this.contactId = contactId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getIdCode() {
        return idCode;
    }

    public void setIdCode(String idCode) {
        this.idCode = idCode;
    }

    public String getBankaccount() {
        return bankaccount;
    }

    public void setBankaccount(String bankaccount) {
        this.bankaccount = bankaccount;
    }

    public String getVatno() {
        return vatno;
    }

    public void setVatno(String vatno) {
        this.vatno = vatno;
    }

    public List<ScoroAddress> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<ScoroAddress> addresses) {
        this.addresses = addresses;
    }

    public String getContactType() {
        return contactType;
    }

    public void setContactType(String contactType) {
        this.contactType = contactType;
    }

    @Override
    public String toString() {
        return "ScoroContact{" +
                "contactId=" + contactId +
                ", name='" + name + '\'' +
                ", lastname='" + lastname + '\'' +
                ", idCode='" + idCode + '\'' +
                ", bankaccount='" + bankaccount + '\'' +
                ", vatno='" + vatno + '\'' +
                ", addresses=" + addresses +
                ", contactType='" + contactType + '\'' +
                '}';
    }
}

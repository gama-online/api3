package lt.gama.model.type.inventory.taxfree;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class Customer implements Serializable {
    @Serial
    private static final long serialVersionUID = -1L;

    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String idDocType;
    private String idDocNo;
    private String issuedBy;
    private String resCountryCode;

    // following fields are required if id document issued By EU or GB
    private String otherDocType; // max length = 100
    private String otherDocNo; // max length = 50
    private String otherIssuedBy; // ISO 3166-1 alpha 2

    // optional
    private String email;

    // generated

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getIdDocType() {
        return idDocType;
    }

    public void setIdDocType(String idDocType) {
        this.idDocType = idDocType;
    }

    public String getIdDocNo() {
        return idDocNo;
    }

    public void setIdDocNo(String idDocNo) {
        this.idDocNo = idDocNo;
    }

    public String getIssuedBy() {
        return issuedBy;
    }

    public void setIssuedBy(String issuedBy) {
        this.issuedBy = issuedBy;
    }

    public String getResCountryCode() {
        return resCountryCode;
    }

    public void setResCountryCode(String resCountryCode) {
        this.resCountryCode = resCountryCode;
    }

    public String getOtherDocType() {
        return otherDocType;
    }

    public void setOtherDocType(String otherDocType) {
        this.otherDocType = otherDocType;
    }

    public String getOtherDocNo() {
        return otherDocNo;
    }

    public void setOtherDocNo(String otherDocNo) {
        this.otherDocNo = otherDocNo;
    }

    public String getOtherIssuedBy() {
        return otherIssuedBy;
    }

    public void setOtherIssuedBy(String otherIssuedBy) {
        this.otherIssuedBy = otherIssuedBy;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(firstName, customer.firstName) && Objects.equals(lastName, customer.lastName) && Objects.equals(birthDate, customer.birthDate) && Objects.equals(idDocType, customer.idDocType) && Objects.equals(idDocNo, customer.idDocNo) && Objects.equals(issuedBy, customer.issuedBy) && Objects.equals(resCountryCode, customer.resCountryCode) && Objects.equals(otherDocType, customer.otherDocType) && Objects.equals(otherDocNo, customer.otherDocNo) && Objects.equals(otherIssuedBy, customer.otherIssuedBy) && Objects.equals(email, customer.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, birthDate, idDocType, idDocNo, issuedBy, resCountryCode, otherDocType, otherDocNo, otherIssuedBy, email);
    }

    @Override
    public String toString() {
        return "Customer{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", birthDate=" + birthDate +
                ", idDocType='" + idDocType + '\'' +
                ", idDocNo='" + idDocNo + '\'' +
                ", issuedBy='" + issuedBy + '\'' +
                ", resCountryCode='" + resCountryCode + '\'' +
                ", otherDocType='" + otherDocType + '\'' +
                ", otherDocNo='" + otherDocNo + '\'' +
                ", otherIssuedBy='" + otherIssuedBy + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}

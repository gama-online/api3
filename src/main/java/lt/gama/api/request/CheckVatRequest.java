package lt.gama.api.request;

public class CheckVatRequest {

    private String countryCode;

    private String vatNumber;

    // generated

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getVatNumber() {
        return vatNumber;
    }

    public void setVatNumber(String vatNumber) {
        this.vatNumber = vatNumber;
    }

    @Override
    public String toString() {
        return "CheckVATRequest{" +
                "countryCode='" + countryCode + '\'' +
                ", vatNumber='" + vatNumber + '\'' +
                '}';
    }
}

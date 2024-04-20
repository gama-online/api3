package lt.gama.api.response;

public class CheckVatResponse {

    private String error;

    private String countryCode;

    private String vatNumber;

    private boolean valid;

    private String name;

    private String address;

    @SuppressWarnings("unused")
    protected CheckVatResponse() {}

    public static CheckVatResponse error(String error) {
        var response = new CheckVatResponse();
        response.error = error;
        return response;
    }

    public CheckVatResponse(String countryCode, String vatNumber, boolean valid, String name, String address) {
        this.countryCode = countryCode;
        this.vatNumber = vatNumber;
        this.valid = valid;
        this.name = name;
        this.address = address;
    }

    // generated

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

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

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "CheckVATResponse{" +
                "error='" + error + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", vatNumber='" + vatNumber + '\'' +
                ", valid=" + valid +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}

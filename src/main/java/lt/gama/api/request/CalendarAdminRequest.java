package lt.gama.api.request;

public class CalendarAdminRequest extends CalendarRequest {

    private String country;

    // generated

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public String toString() {
        return "CalendarAdminRequest{" +
                "country='" + country + '\'' +
                "} " + super.toString();
    }
}

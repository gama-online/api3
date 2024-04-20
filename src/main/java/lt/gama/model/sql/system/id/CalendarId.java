package lt.gama.model.sql.system.id;

import jakarta.persistence.Embeddable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CalendarId implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Country code (ISO 3166-1 alpha-2): LT, US, ...
     */
    private String country;

    private int year;

    protected CalendarId() {
    }

    public CalendarId(String country, int year) {
        this.country = country;
        this.year = year;
    }

    // generated

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CalendarId that = (CalendarId) o;
        return year == that.year && Objects.equals(country, that.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(country, year);
    }

    @Override
    public String toString() {
        return "CalendarId{" +
                "country='" + country + '\'' +
                ", year=" + year +
                '}';
    }
}

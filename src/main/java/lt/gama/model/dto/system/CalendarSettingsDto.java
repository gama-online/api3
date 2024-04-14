package lt.gama.model.dto.system;

import lt.gama.model.dto.base.BaseEntityDto;
import lt.gama.model.type.salary.HolidaySettings;

import java.util.List;
import java.util.Objects;

public class CalendarSettingsDto extends BaseEntityDto {

    private String country;

    private int year;

    private List<HolidaySettings> holidays;

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

    public List<HolidaySettings> getHolidays() {
        return holidays;
    }

    public void setHolidays(List<HolidaySettings> holidays) {
        this.holidays = holidays;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CalendarSettingsDto that = (CalendarSettingsDto) o;
        return year == that.year && Objects.equals(country, that.country) && Objects.equals(holidays, that.holidays);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), country, year, holidays);
    }

    @Override
    public String toString() {
        return "CalendarSettingsDto{" +
                "country='" + country + '\'' +
                ", year=" + year +
                ", holidays=" + holidays +
                "} " + super.toString();
    }
}

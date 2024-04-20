package lt.gama.api.request;

import lt.gama.model.type.CalendarMonth;

/**
 * gama-online
 * Created by valdas on 2016-01-07.
 */
public class CalendarRequest {

    private int year;

    private int month;

    private Boolean refresh;

    private CalendarMonth calendarMonth;


    @SuppressWarnings("unused")
    protected CalendarRequest() {}

    public CalendarRequest(int year, int month, Boolean refresh) {
        this.year = year;
        this.month = month;
        this.refresh = refresh;
    }

    // generated

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public Boolean getRefresh() {
        return refresh;
    }

    public void setRefresh(Boolean refresh) {
        this.refresh = refresh;
    }

    public CalendarMonth getCalendarMonth() {
        return calendarMonth;
    }

    public void setCalendarMonth(CalendarMonth calendarMonth) {
        this.calendarMonth = calendarMonth;
    }

    @Override
    public String toString() {
        return "CalendarRequest{" +
                "year=" + year +
                ", month=" + month +
                ", refresh=" + refresh +
                ", calendarMonth=" + calendarMonth +
                '}';
    }
}

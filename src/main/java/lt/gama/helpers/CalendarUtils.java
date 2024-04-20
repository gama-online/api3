package lt.gama.helpers;

import lt.gama.model.sql.system.CalendarSql;
import lt.gama.model.type.CalendarDay;
import lt.gama.model.type.CalendarMonth;

import java.time.LocalDate;

/**
 * gama-online
 * Created by valdas on 2016-05-26.
 */
public final class CalendarUtils {

    private CalendarUtils() {}

    public static CalendarDay getDay(CalendarSql calendar, LocalDate date) {
        return getDay(calendar, date.getMonthValue(), date.getDayOfMonth());
    }

    public static CalendarDay getDay(CalendarSql calendar, int month, int day) {
        Validators.checkNotNull(calendar, "No Calendar");
        Validators.checkArgument(month >= 1 && month <= 12, "Invalid month - must be between 1 and 12");

        int daysInMonth = LocalDate.of(calendar.getId().getYear(), month, 1).lengthOfMonth();
        Validators.checkArgument(day >= 1 && day <= daysInMonth, "Invalid day - must be between 1 and 31");
        Validators.checkNotNull(calendar.getMonths(), "No Calendar months");
        Validators.checkArgument(calendar.getMonths().size() == 12, "Invalid months count");

        CalendarMonth calendarMonth = calendar.getMonths().get(month - 1);
        Validators.checkNotNull(calendarMonth.getDays(), "No month days");
        Validators.checkArgument(calendarMonth.getDays().size() == daysInMonth, "Invalid days count in the Calendar month");

        return calendarMonth.getDays().get(day - 1);
    }
}

package lt.gama.service;

import lt.gama.auth.impl.Auth;
import lt.gama.helpers.CalendarUtils;
import lt.gama.helpers.Validators;
import lt.gama.model.sql.system.CalendarSettingsSql;
import lt.gama.model.sql.system.CalendarSql;
import lt.gama.model.sql.system.id.CalendarId;
import lt.gama.model.type.CalendarDay;
import lt.gama.model.type.CalendarMonth;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.model.type.enums.HolidayType;
import lt.gama.model.type.salary.HolidaySettings;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * gama-online
 * Created by valdas on 2016-01-07.
 */
@Service
public class CalendarService {

    private final DBServiceSQL dbServiceSQL;
    private final Auth auth;


    public CalendarService(DBServiceSQL dbServiceSQL, Auth auth) {
        this.dbServiceSQL = dbServiceSQL;
        this.auth = auth;
    }

    public CalendarSql getYear(int year, boolean refresh) {
        return dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
            String country = Validators.checkNotNull(companySettings.getCountry(), "No country in settings");
            boolean needSave = false;
            CalendarSql calendar = dbServiceSQL.getById(CalendarSql.class, new CalendarId(country, year));

            if (calendar == null) {
                needSave = true;
                calendar = new CalendarSql(country, year);
            }

            if (calendar.getMonths() == null || calendar.getMonths().size() != 12 || refresh) {
                needSave = true;
                calendar.setMonths(new ArrayList<>());

                List<CalendarSettingsSql> settingsList = entityManager.createQuery(
                                "SELECT c FROM " + CalendarSettingsSql.class.getName() + " c" +
                                        " WHERE id.country = :country AND id.year <= :year" +
                                        " ORDER BY id.year DESC", CalendarSettingsSql.class)
                        .setParameter("country", country)
                        .setParameter("year", year)
                        .setMaxResults(1)
                        .getResultList();

                for (int month = 1; month <= 12; month++) {
                    CalendarMonth calendarMonth = new CalendarMonth();
                    calendar.getMonths().add(calendarMonth);
                    calendarMonth.setDays(new ArrayList<>());

                    int daysInMonth = LocalDate.of(year, month, 1).lengthOfMonth();
                    for (int day = 1; day <= daysInMonth; day++) {
                        CalendarDay calendarDay = new CalendarDay();
                        calendarDay.setWeekend(LocalDate.of(year, month, day).getDayOfWeek().getValue() >= 6);
                        calendarMonth.getDays().add(calendarDay);
                    }
                }

                if (settingsList != null && settingsList.size() > 0 && settingsList.get(0).getHolidays() != null) {
                    for (HolidaySettings holidaySettings : settingsList.get(0).getHolidays()) {

                        LocalDate holiday = holidayInYear(holidaySettings, year);
                        CalendarDay calendarDay = CalendarUtils.getDay(calendar, holiday);

                        calendarDay.setHoliday(true);
                        String dayName = (calendarDay.getName() != null ? calendarDay.getName() + "; " : "") + holidaySettings.getName();
                        calendarDay.setName(dayName);
                    }
                }

                for (int month = 0; month < 12; month++) {
                    CalendarMonth calendarMonth = calendar.getMonths().get(month);
                    int workingDays = 0;
                    int workingHours = 0;
                    int daysCount = calendarMonth.getDays().size();
                    for (int i = 0; i < daysCount; i++) {
                        CalendarDay day = calendarMonth.getDays().get(i);
                        if (!day.isWeekend() && !day.isHoliday()) {
                            workingDays++;
                            workingHours += 8;
                            if (i + 1 < daysCount && calendarMonth.getDays().get(i + 1).isHoliday()) workingHours--;
                        }
                    }
                    calendarMonth.setWorkingDays(workingDays);
                    calendarMonth.setWorkingHours(workingHours);
                }
            }
            return needSave ? dbServiceSQL.saveEntity(calendar) : calendar;
        });
    }

    public CalendarSql getYearInCountry(String country, int year, boolean refresh) {
        auth.getSettings().setCountry(country);
        return getYear(year, refresh);
    }

    public CalendarMonth getMonth(int year, int month, boolean refresh) {
        CalendarSql calendar = getYear(year, refresh);
        return calendar.getMonths().get(month - 1);
    }

    public CalendarMonth getMonthInCountry(String country, int year, int month, boolean refresh) {
        auth.getSettings().setCountry(country);
        return getMonth(year, month, refresh);
    }

    public void saveMonth(int year, int month, CalendarMonth calendarMonth) {
        Validators.checkArgument(month >= 1 && month <= 12, "Invalid month, must be between 1 and 12");
        Validators.checkArgument(calendarMonth.getDays() != null &&
                calendarMonth.getDays().size() == LocalDate.of(year, month, 1).lengthOfMonth(), "Invalid month's days count");

        dbServiceSQL.executeInTransaction(entityManager -> {
            CalendarSql calendar = getYear(year, false);
            calendar.getMonths().set(month - 1, calendarMonth);
            dbServiceSQL.saveEntity(calendar);
        });
    }

    public void saveMonthInCountry(String country, int year, int month, CalendarMonth calendarMonth) {
        Validators.checkArgument(month >= 1 && month <= 12, "Invalid month, must be between 1 and 12");
        Validators.checkArgument(calendarMonth.getDays() != null &&
                calendarMonth.getDays().size() == LocalDate.of(year, month, 1).lengthOfMonth(), "Invalid month's days count");

        dbServiceSQL.executeInTransaction(entityManager -> {
            auth.getSettings().setCountry(country);
            CalendarSql calendar = getYear(year, false);
            calendar.getMonths().set(month - 1, calendarMonth);
            dbServiceSQL.saveEntity(calendar);
        });
    }

    public LocalDate holidayInYear(HolidaySettings holidaySettings, int year) {
        if (HolidayType.DAY.equals(holidaySettings.getType())) {
            return LocalDate.of(year, Validators.checkNotNull(holidaySettings.getMonth(), "No Month"), Validators.checkNotNull(holidaySettings.getDay(), "No Day of Month"));
        }
        if (HolidayType.WEEK.equals(holidaySettings.getType())) {
            Validators.checkNotNull(holidaySettings.getMonth(), "No Month");
            Validators.checkNotNull(holidaySettings.getWeekDay(), "No Weekday");
            Validators.checkArgument(holidaySettings.getWeekDay() >= 1 && holidaySettings.getWeekDay() <= 7, "Wrong weekday number, mus be between 1 (monday) and 7 (sunday)");
            Validators.checkNotNull(holidaySettings.getN(), "No Week number");
            Validators.checkArgument(holidaySettings.getN() != 0, "Week number must be not 0");

            LocalDate date;
            if (holidaySettings.getN() > 0) {
                date = LocalDate.of(year, holidaySettings.getMonth(), 1);
                int weekDay = date.getDayOfWeek().getValue();  // 1(Monday)..7(Sunday)
                // find week day
                if (weekDay <= holidaySettings.getWeekDay())
                    date = date.plusDays(holidaySettings.getWeekDay() - weekDay);
                else date = date.plusDays(holidaySettings.getWeekDay() - weekDay + 7);
                // add number of weeks
                if (holidaySettings.getN() != 1) date = date.plusWeeks(holidaySettings.getN() - 1);
            } else {
                date = LocalDate.of(year, holidaySettings.getMonth(), 1).plusMonths(1).minusDays(1);
                int weekDay = date.getDayOfWeek().getValue();  // 1(Monday)..7(Sunday)
                // find week day
                if (weekDay < holidaySettings.getWeekDay())
                    date = date.plusDays(holidaySettings.getWeekDay() - weekDay - 7);
                else date = date.plusDays(holidaySettings.getWeekDay() - weekDay);
                // subtract number of weeks
                if (holidaySettings.getN() != -1) date = date.plusWeeks(holidaySettings.getN() + 1);
            }
            return date;
        }
        if (HolidayType.OTHER.equals(holidaySettings.getType())) {
            Validators.checkNotNull(holidaySettings.getCode(), "No special day code");
            Validators.checkArgument(holidaySettings.getCode().equals("E"), "Wrong special day code");
            Validators.checkNotNull(holidaySettings.getN(), "No day number");
            return easter(year).plusDays(holidaySettings.getN());
        }

        return null;
    }

    public LocalDate easter(int year) {
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = ((19 * a) + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + (2 * e) + (2 * i) - h - k) % 7;
        int m = (a + (11 * h) + (22 * l)) / 451;
        int t = (h + l - (7 * m) + 114);
        int n = t / 31;
        int p = t % 31 + 1;
        return LocalDate.of(year, n, p);
    }
}

package lt.gama.helpers;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 2018-02-09.
 */
public final class DateUtils {

    private DateUtils() {}

    public static final Comparator<LocalDate> comparatorNullsLast = Comparator.nullsLast(LocalDate::compareTo);

    public static final Comparator<LocalDate> comparatorNullsFirst = Comparator.nullsFirst(LocalDate::compareTo);

    public static boolean isEqual(LocalDate date1, LocalDate date2) {
        return (date1 == null && date2 == null) || (date1 != null && date2 != null && date1.isEqual(date2));
    }

    public static int compare(LocalDate date1, boolean nullFirst1, LocalDate date2, boolean nullFirst2) {
        return compare(toLocalDateTime(date1), nullFirst1, toLocalDateTime(date2), nullFirst2);
    }

    public static int compare(LocalDateTime date1, boolean nullFirst1, LocalDateTime date2, boolean nullFirst2) {
        if (date1 != null && date2 != null) return date1.compareTo(date2);
        if (date1 == null && date2 == null) return nullFirst1 == nullFirst2 ? 0 : nullFirst1 ? -1 : 1;

        if (date1 == null) return nullFirst1 ? -1 : 1;
        return nullFirst2 ? 1 : -1;
    }

    public static LocalDate plusDay(LocalDate date) {
        return date == null ? null : date.plusDays(1);
    }

    public static LocalDate minusDay(LocalDate date) {
        return date == null ? null : date.minusDays(1);
    }

    public static LocalDate max(LocalDate date1, LocalDate date2) {
        return date1 == null ? date2 : date2 == null ? date1 : date1.isAfter(date2) ? date1 : date2;
    }

    public static LocalDateTime toLocalDateTime(LocalDate date) {
        return date == null ? null : LocalDateTime.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), 0, 0, 0);
    }

    public static Date toDate(LocalDate date) {
        return toDate(date.atStartOfDay());
    }

    public static Date toDate(LocalDateTime date) {
        return Date.from(date.toInstant(ZoneOffset.UTC));
    }

    public static Clock mockClock = null;

    public static LocalDate date() {
        return mockClock != null ? LocalDate.now(mockClock) : LocalDate.now();
    }

    public static LocalDate date(String timeZone) {
        if (StringHelper.isEmpty(timeZone)) return date();
        try {
            ZoneId tzId = ZoneId.of(timeZone);
            return mockClock != null ? LocalDate.now(mockClock.withZone(tzId)) : LocalDate.now(tzId);

        } catch (DateTimeException ignored) {
        }

        return date();
    }

    public static LocalDateTime now() {
        return now(ZoneOffset.UTC);
    }

    public static LocalDateTime now(String timeZone) {
        if (StringHelper.isEmpty(timeZone)) return now();
        try {
            ZoneId tzId = ZoneId.of(timeZone);
            return mockClock != null ? LocalDateTime.now(mockClock.withZone(tzId)) : LocalDateTime.now(tzId);

        } catch (DateTimeException ignored) {
        }

        return now();
    }

    public static LocalDateTime now(ZoneId timeZone) {
        if (timeZone == null) return now();
        return mockClock != null ? LocalDateTime.now(mockClock.withZone(timeZone)) : LocalDateTime.now(timeZone);
    }

    public static Instant instant() {
        return mockClock != null ? Instant.now(mockClock) : Instant.now();
    }

    public static LocalDateTime adjust(LocalDateTime localDateTime, String timeZone) {
        if (localDateTime == null || StringHelper.isEmpty(timeZone)) return localDateTime;
        try {
            ZoneId tzId = ZoneId.of(timeZone);
            return adjust(localDateTime, tzId);

        } catch (DateTimeException ignored) {
        }

        return localDateTime;
    }

    public static LocalDateTime adjust(LocalDateTime localDateTime, ZoneId timeZone) {
        if (localDateTime == null || timeZone == null) return localDateTime;
        try {
            return localDateTime.atZone(ZoneId.of("UTC")).withZoneSameInstant(timeZone).toLocalDateTime();

        } catch (DateTimeException ignored) {
        }

        return localDateTime;
    }

    public static LocalDate parseLocalDate(String p) {
        Objects.requireNonNull(p, "Empty string to parse");
        if (StringHelper.isEmpty(p) || p.length() < 10) throw new DateTimeParseException("parser", p, 0);
        if (p.length() == 10) return LocalDate.parse(p, DateTimeFormatter.ISO_DATE);
        return LocalDate.parse(p, DateTimeFormatter.ISO_DATE_TIME);
    }

    public static LocalDateTime parseLocalDateTime(String p) {
        Objects.requireNonNull(p, "parser");
        if (StringHelper.isEmpty(p) || p.length() < 10) throw new DateTimeParseException("parser", p, 0);
        if (p.length() == 10) return LocalDateTime.parse(p + "T00:00:00", DateTimeFormatter.ISO_DATE_TIME);
        if (p.charAt(10) == ' ') {
            char[] buf = p.toCharArray();
            buf[10] = 'T';
            p = String.valueOf(buf);
        }
        return LocalDateTime.parse(p, DateTimeFormatter.ISO_DATE_TIME);
    }

    public static LocalDate firstDayOfYear(LocalDate date) {
        return date == null ? null : date.withDayOfYear(1);
    }
}

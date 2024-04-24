package lt.gama.test.tools;

import lt.gama.helpers.DateUtils;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * gama-online
 * Created by valdas on 2017-06-01.
 */
class DateUtilsTest {

    @Test
    void testLocalDateParser() {
        assertThat(DateUtils.parseLocalDate("2015-12-31")).isEqualTo(LocalDate.of(2015, 12, 31));
        assertThat(DateUtils.parseLocalDateTime("2015-12-31")).isEqualTo(LocalDateTime.of(2015, 12, 31, 0, 0));

        assertThat(DateUtils.parseLocalDate("2015-12-31T22:00:00")).isEqualTo(LocalDate.of(2015, 12, 31));
        assertThat(DateUtils.parseLocalDateTime("2015-12-31T22:00:00")).isEqualTo(LocalDateTime.of(2015, 12, 31, 22, 0, 0));
        assertThat(DateUtils.parseLocalDateTime("2015-12-31 22:00:00")).isEqualTo(LocalDateTime.of(2015, 12, 31, 22, 0, 0));

        assertThat(DateUtils.parseLocalDate("2015-12-31T22:00:00.000")).isEqualTo(LocalDate.of(2015, 12, 31));
        assertThat(DateUtils.parseLocalDateTime("2015-12-31T22:00:00.000")).isEqualTo(LocalDateTime.of(2015, 12, 31, 22, 0, 0));
        assertThat(DateUtils.parseLocalDateTime("2015-12-31 22:00:00.000")).isEqualTo(LocalDateTime.of(2015, 12, 31, 22, 0, 0));

        assertThat(DateUtils.parseLocalDate("2015-12-31T23:59:59.999")).isEqualTo(LocalDate.of(2015, 12, 31));
        assertThat(DateUtils.parseLocalDateTime("2015-12-31T23:59:59.999")).isEqualTo(LocalDateTime.of(2015, 12, 31, 23, 59, 59, 999000000));
        assertThat(DateUtils.parseLocalDateTime("2015-12-31 23:59:59.999")).isEqualTo(LocalDateTime.of(2015, 12, 31, 23, 59, 59, 999000000));

        assertThat(DateUtils.parseLocalDate("2015-12-31T22:00:00.000Z")).isEqualTo(LocalDate.of(2015, 12, 31));
        assertThat(DateUtils.parseLocalDateTime("2015-12-31T22:00:00.000Z")).isEqualTo(LocalDateTime.of(2015, 12, 31, 22, 0, 0));
        assertThat(DateUtils.parseLocalDateTime("2015-12-31 22:00:00.000Z")).isEqualTo(LocalDateTime.of(2015, 12, 31, 22, 0, 0));

        assertThat(DateUtils.parseLocalDate("2015-12-31T23:59:59.999Z")).isEqualTo(LocalDate.of(2015, 12, 31));
        assertThat(DateUtils.parseLocalDateTime("2015-12-31T23:59:59.999Z")).isEqualTo(LocalDateTime.of(2015, 12, 31, 23, 59, 59, 999000000));
        assertThat(DateUtils.parseLocalDateTime("2015-12-31 23:59:59.999Z")).isEqualTo(LocalDateTime.of(2015, 12, 31, 23, 59, 59, 999000000));
    }

    record LocalDateObj(LocalDate date, String name) {}

    @Test
    void testComparator() {
        List<LocalDateObj> lists = Arrays.asList(
                new LocalDateObj(LocalDate.of(2001, 2, 1), "b"),
                new LocalDateObj(LocalDate.of(2000, 1, 1), "a"),
                new LocalDateObj(LocalDate.of(2002, 1, 2), "d"),
                null,
                new LocalDateObj(LocalDate.of(2002, 1, 1), "c")
        );

        lists.sort(Comparator.nullsLast(Comparator.comparing(LocalDateObj::date)));
        assertThat(lists.get(0).name).isEqualTo("a");
        assertThat(lists.get(1).name).isEqualTo("b");
        assertThat(lists.get(2).name).isEqualTo("c");
        assertThat(lists.get(3).name).isEqualTo("d");
        assertThat(lists.get(4)).isNull();

        lists.sort(Comparator.nullsLast(Comparator.comparing(LocalDateObj::date)).reversed());
        assertThat(lists.get(0)).isNull();
        assertThat(lists.get(1).name).isEqualTo("d");
        assertThat(lists.get(2).name).isEqualTo("c");
        assertThat(lists.get(3).name).isEqualTo("b");
        assertThat(lists.get(4).name).isEqualTo("a");
    }

    @Test
    void testCompare() {
        assertThat(DateUtils.compare((LocalDate) null, false, null, false)).isEqualTo(0);
        assertThat(DateUtils.compare((LocalDate) null, true, null, true)).isEqualTo(0);
        assertThat(DateUtils.compare((LocalDate) null, true, null, false)).isEqualTo(-1);
        assertThat(DateUtils.compare((LocalDate) null, false, null, true)).isEqualTo(1);

        assertThat(DateUtils.compare(LocalDate.of(2001, 2, 1), true, null, true)).isEqualTo(1);
        assertThat(DateUtils.compare(LocalDate.of(2001, 2, 1), false, null, true)).isEqualTo(1);
        assertThat(DateUtils.compare(LocalDate.of(2001, 2, 1), true, null, false)).isEqualTo(-1);
        assertThat(DateUtils.compare(LocalDate.of(2001, 2, 1), false, null, false)).isEqualTo(-1);

        assertThat(DateUtils.compare(null, true, LocalDate.of(2001, 2, 1), true)).isEqualTo(-1);
        assertThat(DateUtils.compare(null, true, LocalDate.of(2001, 2, 1), false)).isEqualTo(-1);
        assertThat(DateUtils.compare(null, false, LocalDate.of(2001, 2, 1), true)).isEqualTo(1);
        assertThat(DateUtils.compare(null, false, LocalDate.of(2001, 2, 1), false)).isEqualTo(1);

        assertThat(DateUtils.compare(LocalDate.of(2001, 2, 2), true, LocalDate.of(2001, 2, 1), true)).isEqualTo(1);
        assertThat(DateUtils.compare(LocalDate.of(2001, 2, 1), true, LocalDate.of(2001, 2, 1), true)).isEqualTo(0);
        assertThat(DateUtils.compare(LocalDate.of(2001, 2, 1), true, LocalDate.of(2001, 2, 2), true)).isEqualTo(-1);
        assertThat(DateUtils.compare(LocalDate.of(2001, 2, 1), false, LocalDate.of(2001, 2, 2), true)).isEqualTo(-1);
        assertThat(DateUtils.compare(LocalDate.of(2001, 2, 2), true, LocalDate.of(2001, 2, 1), false)).isEqualTo(1);
    }

    @Test
    void testMax() {
        assertThat(DateUtils.max(null, null)).isNull();
        assertThat(DateUtils.max(null, LocalDate.of(2001, 2, 2))).isEqualTo(LocalDate.of(2001, 2, 2));
        assertThat(DateUtils.max(LocalDate.of(2001, 2, 2), null)).isEqualTo(LocalDate.of(2001, 2, 2));
        assertThat(DateUtils.max(LocalDate.of(2001, 2, 2), LocalDate.of(2001, 2, 1))).isEqualTo(LocalDate.of(2001, 2, 2));
        assertThat(DateUtils.max(LocalDate.of(2001, 2, 1), LocalDate.of(2001, 2, 2))).isEqualTo(LocalDate.of(2001, 2, 2));
    }

    @Test
    void testLocalDateTimeTimeZones() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        //  Cloud       Backend     other external system
        //  UTC         UTC         local time
        //  15:00       15:00       ??:??

        DateUtils.mockClock = Clock.fixed(LocalDateTime.of(2015, 4, 15, 15, 0).toInstant(ZoneOffset.UTC), ZoneId.of("UTC"));

        assertThat(DateUtils.now("Europe/Vilnius")).isEqualTo(LocalDateTime.of(2015, 4, 15, 18, 0));
        assertThat(DateUtils.adjust(DateUtils.now(), "Europe/Vilnius")).isEqualTo(LocalDateTime.of(2015, 4, 15, 18, 0));

        assertThat(DateUtils.now("Wrong zone")).isEqualTo(LocalDateTime.of(2015, 4, 15, 15, 0));
        assertThat(DateUtils.adjust(DateUtils.now(), "Wrong zone")).isEqualTo(LocalDateTime.of(2015, 4, 15, 15, 0));
    }

    @Test
    void testFunctions() {
        assertThat(DateUtils.firstDayOfYear(null)).isNull();
        assertThat(DateUtils.firstDayOfYear(LocalDate.of(2000, 5, 15))).isEqualTo(LocalDate.of(2000, 1, 1));
    }
}



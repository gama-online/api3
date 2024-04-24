package lt.gama.test.tools;

import org.junit.jupiter.api.Test;

import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * gama-online
 * Created by valdas on 2018-01-30.
 */
public class TimeZoneTest {

    @Test
    public void testTimeZone() {

        // test on January 1, 1970 0:00 GMT

        TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
        assertThat(tz.getID()).isEqualTo("America/Los_Angeles");
        assertThat(tz.getOffset(0)).isEqualTo(-8 * 60 * 60 * 1000);

        tz = TimeZone.getTimeZone("Europe/Vilnius");
        assertThat(tz.getID()).isEqualTo("Europe/Vilnius");
        assertThat(tz.getOffset(0)).isEqualTo(3 * 60 * 60 * 1000);

        tz = TimeZone.getTimeZone("EET");
        assertThat(tz.getID()).isEqualTo("EET");
        assertThat(tz.getOffset(0)).isEqualTo(2 * 60 * 60 * 1000);

        tz = TimeZone.getTimeZone("GMT+14:30");
        assertThat(tz.getID()).isEqualTo("GMT+14:30");
        assertThat(tz.getOffset(0)).isEqualTo(((14 * 60) + 30) * 60 * 1000);

        tz = TimeZone.getTimeZone("GMT-2:00");
        assertThat(tz.getID()).isEqualTo("GMT-02:00");
        assertThat(tz.getOffset(0)).isEqualTo(-2 * 60 * 60 * 1000);

        tz = TimeZone.getTimeZone("Europe/Riga");
        assertThat(tz.getID()).isEqualTo("Europe/Riga");
        assertThat(tz.getOffset(1517320183365L)).isEqualTo(2 * 60 * 60 * 1000);

        tz = TimeZone.getTimeZone("UTC");
        assertThat(tz.getID()).isEqualTo("UTC");
        assertThat(tz.getOffset(1517320183365L)).isEqualTo(0);

        tz = TimeZone.getTimeZone("Europe/Balbieriškis");
        assertThat(tz.getID()).isEqualTo("GMT");

//        String[] z = TimeZone.getAvailableIDs();
//        assertThat(z.length).isEqualTo(632);
    }
}

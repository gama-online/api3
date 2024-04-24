package lt.gama.test.tools;

import lt.gama.helpers.DateUtils;
import lt.gama.helpers.EntityUtils;
import lt.gama.model.type.doc.DocCompany;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * gama-online
 * Created by valdas on 2016-06-07.
 */
class EntityUtilsTest {

    @Test
    void testIIdNull() {
        assertThat(EntityUtils.isIdNull(null)).isTrue();
        assertThat(EntityUtils.isIdNull(new DocCompany())).isTrue();
        assertThat(EntityUtils.isIdNull(new DocCompany(0, "a"))).isEqualTo(false);
        assertThat(EntityUtils.isIdNull(new DocCompany(1, "a"))).isEqualTo(false);
    }

    @Test
    void testIdEquals() {
        assertThat(EntityUtils.isIdEquals(null, null)).isTrue();
        assertThat(EntityUtils.isIdEquals(new DocCompany(), null)).isTrue();
        assertThat(EntityUtils.isIdEquals(null, new DocCompany())).isTrue();

        assertThat(EntityUtils.isIdEquals(new DocCompany(1, "a"), null)).isEqualTo(false);
        assertThat(EntityUtils.isIdEquals(null, new DocCompany(1, "a"))).isEqualTo(false);
        assertThat(EntityUtils.isIdEquals(new DocCompany(1, "a"), new DocCompany(1, "b"))).isTrue();
        assertThat(EntityUtils.isIdEquals(new DocCompany(1, "a"), new DocCompany(2, "a"))).isEqualTo(false);
    }

    @Test
    void unicodeTest() {
        assertThat(StringUtils.stripAccents("123abc-ąčęėèéêẽįšųū„“-žźẑż")).isEqualTo("123abc-aceeeeeeisuu„“-zzzz");
        assertThat(StringUtils.stripAccents("ABC-ĄÀÁČĆĈĘĖĮŠŲŪ()_Ž")).isEqualTo("ABC-AAACCCEEISUU()_Z");
    }

    @Test
    void prepareNameTest() {
        assertThat(EntityUtils.prepareName("abc deefiuu \n ss\tzzg1-23")).isEqualTo("abc deefiuu ss zzg1 23");
        assertThat(EntityUtils.prepareName("Abc deefIUu Ss 'ZzG1'23")).isEqualTo("abc deefiuu ss zzg1 23");
        assertThat(EntityUtils.prepareName("\"Abc_deefiUu?& !sS.'zZg1'/23")).isEqualTo("abc deefiuu ss zzg1 23");
        assertThat(EntityUtils.prepareName("Ąbč dėęfįŪų::šs*Žzg1'+23")).isEqualTo("abc deefiuu ss zzg1 23");
    }

    @Test
    void testLocalDateCoding() {
        LocalDate localDate = LocalDate.of(1234, 1, 1);
        assertThat(EntityUtils.decodeLocalDate(EntityUtils.encodeLocalDate(localDate))).isEqualTo(localDate);

        localDate = LocalDate.of(2109, 12, 31);
        assertThat(EntityUtils.decodeLocalDate(EntityUtils.encodeLocalDate(localDate))).isEqualTo(localDate);

        localDate = DateUtils.date();
        assertThat(EntityUtils.decodeLocalDate(EntityUtils.encodeLocalDate(localDate))).isEqualTo(localDate);
    }

}

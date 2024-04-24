package lt.gama.test.tools;

import com.google.common.collect.Lists;
import lt.gama.helpers.StringHelper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * gama-online
 * Created by valdas on 2018-07-20.
 */
public class StringHelperTest {

    @Test
    void trim() {
        assertThat(StringHelper.trim(null)).isEqualTo("");
        assertThat(StringHelper.trim("")).isEqualTo("");
        assertThat(StringHelper.trim("  ")).isEqualTo("");
        assertThat(StringHelper.trim(" 12 3   ")).isEqualTo("12 3");
    }

    @Test
    void trim2null() {
        assertThat(StringHelper.trim2null(null)).isNull();
        assertThat(StringHelper.trim2null("")).isNull();
        assertThat(StringHelper.trim2null("  ")).isNull();
        assertThat(StringHelper.trim2null(" 12 3   ")).isEqualTo("12 3");
    }

    @Test
    void trimNormalize2null() {
        assertThat(StringHelper.trimNormalize2null(null)).isNull();
        assertThat(StringHelper.trimNormalize2null("")).isNull();
        assertThat(StringHelper.trimNormalize2null(" \r\n\t ")).isNull();
        assertThat(StringHelper.trimNormalize2null(" 12   \t\n3  ")).isEqualTo("12 3");
    }

    @Test
    void isEmpty() {
        assertThat(StringHelper.isEmpty("1")).isEqualTo(false);
        assertThat(StringHelper.isEmpty("")).isTrue();
        assertThat(StringHelper.isEmpty("  ")).isTrue();
        assertThat(StringHelper.isEmpty(null)).isTrue();
    }

    @Test
    void deleteSpaces() {
        assertThat(StringHelper.deleteSpaces(null)).isEqualTo("");
        assertThat(StringHelper.deleteSpaces("")).isEqualTo("");
        assertThat(StringHelper.deleteSpaces("12 34 5")).isEqualTo("12345");
        assertThat(StringHelper.deleteSpaces("ač12  x34\t5\n\r6")).isEqualTo("ač12x3456");
    }

    @Test
    void normalizeBankAccount() {
        assertThat(StringHelper.normalizeBankAccount(null)).isEqualTo("");
        assertThat(StringHelper.normalizeBankAccount("")).isEqualTo("");
        assertThat(StringHelper.normalizeBankAccount("12345")).isEqualTo("12345");
        assertThat(StringHelper.normalizeBankAccount("123-45")).isEqualTo("123-45");
        assertThat(StringHelper.normalizeBankAccount("ač12  x34\t5\n\r6")).isEqualTo("AČ12X3456");
    }

    @Test
    void concatenate() {
        assertThat(StringHelper.concatenate(null, null)).isNull();
        assertThat(StringHelper.concatenate(" 12 3 ", null)).isEqualTo(" 12 3 ");
        assertThat(StringHelper.concatenate(null, " 12 3 ")).isEqualTo(" 12 3 ");
        assertThat(StringHelper.concatenate(" 12 3 ", " 4 5 ")).isEqualTo(" 12 3   4 5 ");
    }

    @Test
    void splitTokens() {
        assertThat(StringHelper.splitTokens("123 456")).isEqualTo(Lists.newArrayList("123", "456"));
        assertThat(StringHelper.splitTokens("ąbc:123 456?")).isEqualTo(Lists.newArrayList("ąbc:123", "456?"));
        assertThat(StringHelper.splitTokens("123 \" 1-3 \" 456")).isEqualTo(Lists.newArrayList("123", "1-3", "456"));
        assertThat(StringHelper.splitTokens("123 \"1-'a3\" \"456\"7:")).isEqualTo(Lists.newArrayList("123", "1-'a3", "456", "7:"));
        assertThat(StringHelper.splitTokens("123 '1-3\"' 456")).isEqualTo(Lists.newArrayList("123", "1-3\"", "456"));
    }

    @Test
    void firstWord() {
        assertThat(StringHelper.firstWord(" 12\t3 ")).isEqualTo("12");
        assertThat(StringHelper.firstWord("\n12-A3  3 ")).isEqualTo("12-A3");
        assertThat(StringHelper.firstWord("\n12?Ž3. 3 ")).isEqualTo("12?Ž3.");
    }

    @Test
    void parseDocNumber() {
        assertThat(StringHelper.parseDocNumber(" ").getSeries()).isNull();
        assertThat(StringHelper.parseDocNumber(" ").getOrdinal()).isNull();

        assertThat(StringHelper.parseDocNumber(null).getSeries()).isNull();
        assertThat(StringHelper.parseDocNumber(null).getOrdinal()).isNull();

        assertThat(StringHelper.parseDocNumber("123").getSeries()).isNull();
        assertThat(StringHelper.parseDocNumber("123").getOrdinal()).isEqualTo(123L);

        assertThat(StringHelper.parseDocNumber("abc").getSeries()).isEqualTo("abc");
        assertThat(StringHelper.parseDocNumber("abc").getOrdinal()).isNull();

        assertThat(StringHelper.parseDocNumber("a123").getSeries()).isEqualTo("a");
        assertThat(StringHelper.parseDocNumber("a123").getOrdinal()).isEqualTo(123L);

        assertThat(StringHelper.parseDocNumber("a123x").getSeries()).isEqualTo("a");
        assertThat(StringHelper.parseDocNumber("a123x").getOrdinal()).isEqualTo(123L);

        assertThat(StringHelper.parseDocNumber("abc 123").getSeries()).isEqualTo("abc");
        assertThat(StringHelper.parseDocNumber("abc 123").getOrdinal()).isEqualTo(123L);

        assertThat(StringHelper.parseDocNumber("abc-123").getSeries()).isEqualTo("abc-");
        assertThat(StringHelper.parseDocNumber("abc-123").getOrdinal()).isEqualTo(123L);

        assertThat(StringHelper.parseDocNumber("abc-123/2").getSeries()).isEqualTo("abc-");
        assertThat(StringHelper.parseDocNumber("abc-123/2").getOrdinal()).isEqualTo(123L);

        assertThat(StringHelper.parseDocNumber("abc def 123x").getSeries()).isEqualTo("abc def");
        assertThat(StringHelper.parseDocNumber("abc def 123x").getOrdinal()).isEqualTo(123L);

        assertThat(StringHelper.parseDocNumber("a123 456").getSeries()).isEqualTo("a123");
        assertThat(StringHelper.parseDocNumber("a123 456").getOrdinal()).isEqualTo(456L);

        assertThat(StringHelper.parseDocNumber("a123 456 789").getSeries()).isEqualTo("a123 456");
        assertThat(StringHelper.parseDocNumber("a123 456 789").getOrdinal()).isEqualTo(789L);
    }

    @Test
    void parseDocLargeNumbers() {
        // 9 digits number - OK
        assertThat(StringHelper.parseDocNumber("ABC0000000123456789").getSeries()).isEqualTo("ABC");
        assertThat(StringHelper.parseDocNumber("ABC0000000123456789").getOrdinal()).isEqualTo(123456789L);

        // 10 and more digits number - null
        assertThat(StringHelper.parseDocNumber("ABC00000001234567890").getSeries()).isEqualTo("ABC");
        assertThat(StringHelper.parseDocNumber("ABC00000001234567890").getOrdinal()).isEqualTo(null);

        assertThat(StringHelper.parseDocNumber("ABC00000001234567890123456789").getSeries()).isEqualTo("ABC");
        assertThat(StringHelper.parseDocNumber("ABC00000001234567890123456789").getOrdinal()).isEqualTo(null);
    }

    @Test
    void normalizeFileName() {
        assertThat(StringHelper.normalizeFileName(" \" '' \t _    \n\r _::\\ / ")).isEqualTo("_");
        assertThat(StringHelper.normalizeFileName(" \" '' \t _  x \n\r _::\\ / ")).isEqualTo("_x_");
    }

    @Test
    void toRfc5987() {
        assertThat(StringHelper.toRfc5987(";,/?:@&=+$")).isEqualTo("%3b%2c%2f%3f%3a%40&%3d+$");
        assertThat(StringHelper.toRfc5987("-_.!~*'()")).isEqualTo("-_.!~%2a%27%28%29");
        assertThat(StringHelper.toRfc5987("#")).isEqualTo("#");
        assertThat(StringHelper.toRfc5987("ABC abc 123")).isEqualTo("ABC%20abc%20123");
        assertThat(StringHelper.toRfc5987("\u0104")).isEqualTo("%c4%84");       // Ą (Latin Capital Letter A with ogonek)
        assertThat(StringHelper.toRfc5987("\u2021")).isEqualTo("%e2%80%a1");    // ‡ (Double dagger)
    }
}

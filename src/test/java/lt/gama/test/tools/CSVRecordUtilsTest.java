package lt.gama.test.tools;

import lt.gama.helpers.CSVRecordUtils;
import lt.gama.model.type.GamaBigMoney;
import lt.gama.model.type.GamaMoney;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * gama-online
 * Created by valdas on 2018-06-12.
 */
public class CSVRecordUtilsTest {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private void test(CSVParser csvParser) {
        for (CSVRecord record : csvParser) {
            assertThat(CSVRecordUtils.getString(record, "Name")).isNull();
            assertThat(CSVRecordUtils.getString(record, "Name", "abc")).isEqualTo("abc");
            assertThat(CSVRecordUtils.getString(record, "Time Zone")).isEqualTo("PDT");
            assertThat(CSVRecordUtils.getInt(record, "Nr", 0)).isEqualTo(45);
            assertThat(CSVRecordUtils.getInt(record, "Name", 10)).isEqualTo(10);
            assertThat(CSVRecordUtils.getInteger(record, "Nr")).isEqualTo(45);
            assertThat(CSVRecordUtils.getInteger(record, "Name")).isNull();
            assertThat(CSVRecordUtils.getLocalDateMDY(record, "Date")).isEqualTo(LocalDate.of(2018, 5, 31));
            assertThat(CSVRecordUtils.getLocalDateDMY(record, "Date2")).isEqualTo(LocalDate.of(2018, 5, 31));
            assertThat(CSVRecordUtils.getLocalTime(record, "Time")).isEqualTo(LocalTime.of(8, 46, 35));
            assertThat(CSVRecordUtils.getDecimalMoneyPart(record, "Gross", "EUR")).isEqualTo(new BigDecimal("-1234.56"));
            assertThat(CSVRecordUtils.getDecimalMoneyPart(record, "Balance", "USD")).isEqualTo(new BigDecimal("45678.90"));
            assertThat(CSVRecordUtils.getMoney(record, "Balance", "EUR")).isEqualTo(GamaMoney.parse("EUR 45678.90"));
            assertThat(CSVRecordUtils.getBigMoney(record, "Money")).isEqualTo(GamaBigMoney.parse("USD -12345678.90"));
            assertThat(CSVRecordUtils.getDecimalMoneyPart(record, "Money0", "JPY")).isEqualTo(new BigDecimal("-12345678"));
        }
    }

    @Test
    public void testCommaSep() {
        String buf = "Date, Date2, Time, Time Zone, Name, Gross, Balance, Nr, Money, Money0" + "\n" +   // IgnoreSurroundingSpaces()
                "\"5/31/2018\",\"31/5/2018\",\"08:46:35\",\"PDT\",\"\",\"-1 234.56\",\"45,678.90\",\"45\",\"USD -12345678.90\",\"-12,345 678\"";

        try {
            InputStream is = new ByteArrayInputStream(buf.getBytes(StandardCharsets.UTF_8));
            Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader().withIgnoreSurroundingSpaces());

            test(csvParser);

        } catch (IOException e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    @Test
    public void testCommaSepNumbersWithComma() {
        String buf = "Date, Date2, Time, Time Zone, Name, Gross, Balance, Nr, Money, Money0" + "\n" +   // IgnoreSurroundingSpaces()
                "\"5/31/2018\",\"31/5/2018\",\"08:46:35\",\"PDT\",\"\",\"-1 234,56\",\"45.678,90\",\"45\",\"USD -12345678,90\",\"-12,345 678\"";

        try {
            InputStream is = new ByteArrayInputStream(buf.getBytes(StandardCharsets.UTF_8));
            Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader().withIgnoreSurroundingSpaces());

            test(csvParser);

        } catch (IOException e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    @Test
    public void testTabSep() {
        String buf = "Date\tDate2\tTime\tTime Zone\tName\tGross\tBalance\tNr\tMoney\tMoney0" + "\r\n" +
                "\"5/31/2018\"\t\"31/5/2018\"\t\"08:46:35\"\t\"PDT\"\t\"\"\t\"-1,234.56\"\t\"45,678.90\"\t\"45\"\t\"USD -12345678.90\"\t\"-12.345 678\"";

        try {
            InputStream is = new ByteArrayInputStream(buf.getBytes(StandardCharsets.UTF_8));
            Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withHeader().withDelimiter('\t'));

            test(csvParser);

        } catch (IOException e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }
}

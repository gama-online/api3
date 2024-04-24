package lt.gama.test.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lt.gama.AppConfiguration;
import lt.gama.JsonConfiguration;
import lt.gama.model.dto.documents.InvoiceDto;
import lt.gama.model.type.GamaBigMoney;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.auth.SalesSettings;
import lt.gama.model.type.enums.PartSortOrderType;
import lt.gama.model.type.enums.PartType;
import lt.gama.service.AppPropService;
import lt.gama.service.json.ser.LocalDateTimeTZSerializer;
import lt.gama.service.sync.openCart.model.OCResponse;
import lt.gama.service.sync.scoro.model.ScoroInvoice;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Gama
 * Created by valdas on 15-09-29.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = JsonConfiguration.class)
@TestPropertySource(locations = "classpath:application.properties")
public class JacksonTest {
    
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testInvoice() throws IOException, JSONException {
        var entity = new InvoiceDto();
        entity.setCompanyId(3);
        entity.setDate(LocalDate.of(2014, 12, 31));
        entity.setTotal(GamaMoney.parse("EUR 1.23"));

        String json = objectMapper.writeValueAsString(entity);
        JSONAssert.assertEquals("{\"@class\":\"Invoice\",\"version\":0,\"companyId\":3,\"date\":\"2014-12-31\",\"total\":{\"currency\":\"EUR\",\"amount\":1.23},\"zeroVAT\":false,\"isafSpecialTaxation\":false,\"fullyFinished\":false,\"debt\":{\"currency\":\"EUR\",\"amount\":1.23},\"documentType\":\"Invoice\"}",
            json, false);

        var entity2 = objectMapper.readValue(json, InvoiceDto.class);
        assertThat(entity.getCompanyId()).isEqualTo(entity2.getCompanyId());
        assertThat(entity.getDate()).isEqualTo(entity2.getDate());
        assertThat(entity.getTotal()).isEqualTo(entity2.getTotal());
        assertThat(entity.getDueDate()).isNull();
    }

    @Test
    public void testScoroInvoice() throws IOException {
        ScoroInvoice entity = objectMapper.readValue(
                "{\"date\":\"2017-12-31\"}",
                ScoroInvoice.class);

        assertThat(entity.getDate()).isEqualTo(LocalDate.of(2017, 12, 31));
        assertThat(entity.getDeadline()).isNull();

        entity = objectMapper.readValue(
                "{\"date\":\"2017-12-31\",\"deadline\":\"\"}",
                ScoroInvoice.class);

        assertThat(entity.getDate()).isEqualTo(LocalDate.of(2017, 12, 31));
        assertThat(entity.getDeadline()).isNull();

        entity = objectMapper.readValue(
                "{\"date\":\"2017-12-31\",\"deadline\":\"2018-01-15\"}",
                ScoroInvoice.class);

        assertThat(entity.getDate()).isEqualTo(LocalDate.of(2017, 12, 31));
        assertThat(entity.getDeadline()).isEqualTo(LocalDate.of(2018, 1, 15));
    }

    @Test
    public void testLocalDate() throws IOException {
        LocalDate localDate = LocalDate.of(2014, 12, 31);
        String json = objectMapper.writeValueAsString(localDate);
        assertThat(json).isEqualTo("\"2014-12-31\"");

        LocalDate localDate2 = objectMapper.readValue(json, LocalDate.class);
        assertThat(localDate2).isEqualTo(localDate);
    }

    @Test
    public void testLocalDateTime() throws IOException {
        LocalDateTime localDateTime = LocalDateTime.of(2018, 1, 9, 12, 16, 33, 217000000);
        String json = objectMapper.writeValueAsString(localDateTime);
        assertThat(json).isEqualTo("\"2018-01-09T12:16:33.217\"");

        LocalDateTime localDateTime2 = objectMapper.readValue(json, LocalDateTime.class);
        assertThat(localDateTime).isEqualTo(localDateTime2);
    }

    @Test
    public void testBigMoney() throws IOException {
        GamaBigMoney money = GamaBigMoney.parse("USD 3.45");
        String json = objectMapper.writeValueAsString(money);
        assertThat(json).isEqualTo("{\"currency\":\"USD\",\"amount\":3.45}");

        GamaBigMoney money2 = objectMapper.readValue(json, GamaBigMoney.class);
        assertThat(money).isEqualTo(money2);
    }

    @Test
    void testSimpleLocalDateTime() throws JsonProcessingException, JSONException {
        final LocalDateTime DATE = LocalDateTime.of(1999, 12, 31, 23, 59, 58, 123_000_000);
        Obj obj = new Obj();
        obj.time1 = DATE;
        obj.time2 = DATE;
        obj.date = java.sql.Timestamp.valueOf(DATE);

        String json = objectMapper.writeValueAsString(obj);
        JSONAssert.assertEquals("""
        {
            "time1":"1999-12-31T23:59:58.123Z",
            "time2":"1999-12-31T23:59:58.123",
            "date":"1999-12-31T23:59:58Z"
        }
        """, json, true);

        Obj des = objectMapper.readValue(json, Obj.class);
        assertThat(des.time1).isEqualTo(DATE);
        assertThat(des.time2).isEqualTo(DATE);
        assertThat(des.date).isEqualTo(java.sql.Timestamp.valueOf(DATE.truncatedTo(ChronoUnit.SECONDS)));
    }

    @Test
    void testPartTypeEnum() throws JsonProcessingException, JSONException {
        Obj obj = new Obj();
        obj.partType = PartType.SERVICE;

        String json = objectMapper.writeValueAsString(obj);
        JSONAssert.assertEquals("{\"partType\":\"N\"}", json, true);

        Obj des = objectMapper.readValue(json, Obj.class);
        assertThat(des.partType).isEqualTo(PartType.SERVICE);
    }

    public static class Obj {

        @JsonSerialize(using = LocalDateTimeTZSerializer.class)
        public LocalDateTime time1;

        public LocalDateTime time2;

        public Date date;

        public PartType partType;
    }

    @Test
    public void testPartSortOrderType() throws JsonProcessingException {

        SalesSettings salesSettings = new SalesSettings();
        salesSettings.setDefaultPartSortOrder(PartSortOrderType.NAME);
        assertThat(objectMapper.writeValueAsString(salesSettings)).isEqualTo("{\"defaultPartSortOrder\":\"N\"}");

        salesSettings.setDefaultPartSortOrder(PartSortOrderType.SKU);
        assertThat(objectMapper.writeValueAsString(salesSettings)).isEqualTo("{\"defaultPartSortOrder\":\"S\"}");
    }

    @Test
    void testOCResponseDecode() throws IOException {
        InputStream is = new ByteArrayInputStream("[]".getBytes(StandardCharsets.UTF_8));
        OCResponse ocResponse = objectMapper.readValue(is, OCResponse.class);
        assertThat(ocResponse).isNull();
    }
}

package lt.gama.test.tools;

import lt.gama.api.ex.GamaApiException;
import lt.gama.model.dto.documents.InvoiceDto;
import lt.gama.model.dto.documents.PurchaseDto;
import lt.gama.model.dto.documents.items.PartInvoiceDto;
import lt.gama.model.dto.entities.CounterpartyDto;
import lt.gama.model.sql.entities.CounterpartySql;
import lt.gama.model.sql.system.ExchangeRateSql;
import lt.gama.model.type.Exchange;
import lt.gama.model.type.GamaBigMoney;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.enums.DebtType;
import lt.gama.model.type.enums.PartType;
import lt.gama.test.base.BaseDBTest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Gama
 * Created by valdas on 15-04-13.
 */
public class ExchangeTest extends BaseDBTest {

    @Test
    public void testPurchase() throws GamaApiException {
        ExchangeRateSql exchangeRate = new ExchangeRateSql("LT", "USD", LocalDate.of(2015, 1, 15),
                new Exchange("EUR", new BigDecimal("2"), "USD", new BigDecimal("2.3"), LocalDate.of(2015, 1, 15)));
        dbServiceSQL.saveEntity(exchangeRate);
        clearCaches();

        var warehouse = createWarehouse("W1");
        var counterparty = createCounterparty("Counterparty A", DebtType.VENDOR, "41");
        var part1 = createPart("P1", null, PartType.SERVICE, true, 21.0, "11", "51", "61");

        var docPart1 = createPartPurchase(part1, new BigDecimal("1.3"), GamaBigMoney.parse("USD 12.34"));

        var document = new PurchaseDto();
        document.setDate(LocalDate.of(2015, 1, 15));
        document.setNumber("1");
        document.setCounterparty(counterparty);
        document.setWarehouse(warehouse);
        document.setParts(new ArrayList<>());
        document.getParts().add(docPart1);
        document.setSubtotal(docPart1.getTotal());
        document.setTaxTotal(document.getSubtotal().multipliedBy(21.0 / 100.0));
        document.setTotal(document.getSubtotal().plus(document.getTaxTotal()));
        document.setExchange(new Exchange());
        document.getExchange().setCurrency("USD");

        document = inventoryApi.savePurchase(document).getData();
        assertThat(document.getId()).isPositive();
        assertThat(document.getExchange()).isNotNull();
        assertThat(document.getExchange().getBase()).isEqualTo("EUR");
        assertThat(document.getExchange().getBaseAmount()).isEqualTo(new BigDecimal("2"));
        assertThat(document.getExchange().getAmount()).isEqualTo(new BigDecimal("2.3"));
        assertThat(document.getExchange().conversionMultiplier()).isEqualTo(new BigDecimal("0.86956522"));
        assertThat(document.getParts().get(0).getBaseTotal()).isEqualTo(GamaMoney.parse("EUR 13.95"));
        assertThat(document.getBaseSubtotal()).isEqualTo(GamaMoney.parse("EUR 13.95"));
        assertThat(document.getBaseTaxTotal()).isEqualTo(GamaMoney.parse("EUR 2.93"));
        assertThat(document.getBaseTotal()).isEqualTo(GamaMoney.parse("EUR 16.88"));
    }

    @Test
    public void testInvoice() {
        ExchangeRateSql exchangeRate = new ExchangeRateSql("LT", "USD", LocalDate.of(2015, 1, 15),
                new Exchange("EUR", new BigDecimal("2"), "USD", new BigDecimal("2.3"), LocalDate.of(2015, 1, 15)));
        dbServiceSQL.saveEntity(exchangeRate);
        clearCaches();

        var warehouse = createWarehouse("W1");
        var counterparty = createCounterparty("Counterparty A", DebtType.CUSTOMER, "21");
        var part1 = createPart("P1", null, PartType.SERVICE, true, 21.0, "11", "51", "61");

        var docPart1 = new PartInvoiceDto();
        docPart1.setId(part1.getId());
        docPart1.setName("P1");
        docPart1.setTaxable(true);
        docPart1.setVat(getVatRate(21.0));
        docPart1.setQuantity(new BigDecimal("1.3"));
        docPart1.setPrice(GamaBigMoney.parse("USD 12.340"));
        docPart1.setDiscount(10.0);

        var docPart2 = new PartInvoiceDto();
        docPart2.setId(part1.getId());
        docPart2.setName("P1");
        docPart2.setTaxable(true);
        docPart2.setVat(getVatRate(21.0));
        docPart2.setQuantity(new BigDecimal("10.0"));
        docPart2.setPrice(GamaBigMoney.parse("USD 9.980"));
        docPart2.setDiscount(25.0);

        var document = new InvoiceDto();
        document.setDate(LocalDate.of(2015, 1, 15));
        document.setNumber("1");
        document.setCompanyId(getCompanyId());
        document.setCounterparty(counterparty);
        document.setWarehouse(warehouse);
        document.setDiscount(5.0);
        document.setParts(new ArrayList<>());
        document.getParts().add(docPart1);      //  1.3 x $12.34 x 10% | 1.3 * $11.106 = $14.44 | $0.72
        document.getParts().add(docPart2);      //  10.0 x $9.98 x 25% | 10.0 * $7.485 = $74.85 | $3.74

        document.setExchange(new Exchange());
        document.getExchange().setCurrency("USD");


        document = tradeService.saveInvoice(document);

        assertThat(document.getId()).isPositive();
        assertThat(document.getExchange()).isNotNull();
        assertThat(document.getExchange().getBase()).isEqualTo("EUR");
        assertThat(document.getExchange().getBaseAmount()).isEqualTo(new BigDecimal("2"));
        assertThat(document.getExchange().getAmount()).isEqualTo(new BigDecimal("2.3"));
        assertThat(document.getExchange().conversionMultiplier()).isEqualTo(new BigDecimal("0.86956522"));

        assertThat(document.getParts().size()).isEqualTo(2);
        assertThat(document.getParts().get(0).getDiscount()).isEqualTo(10.0);
        assertThat(document.getParts().get(0).getDiscountDoc()).isEqualTo(5.0);
        assertThat(document.getParts().get(0).getDiscountedPrice()).isEqualTo(GamaBigMoney.parse("USD 11.106"));
        assertThat(document.getParts().get(0).getDiscountDocTotal()).isEqualTo(GamaMoney.parse("USD 0.72"));
//        assertThat(document.getParts().get(0).getTaxTotal()).isEqualTo(GamaMoney.parse("USD 2.88"));
        assertThat(document.getParts().get(0).getTotal()).isEqualTo(GamaMoney.parse("USD 13.72"));
        assertThat(document.getParts().get(0).getBaseTotal()).isEqualTo(GamaMoney.parse("EUR 11.93"));

        assertThat(document.getParts().get(1).getDiscount()).isEqualTo(25.0);
        assertThat(document.getParts().get(1).getDiscountDoc()).isEqualTo(5.0);
        assertThat(document.getParts().get(1).getDiscountedPrice()).isEqualTo(GamaBigMoney.parse("USD 7.485"));
        assertThat(document.getParts().get(1).getDiscountDocTotal()).isEqualTo(GamaMoney.parse("USD 3.74"));
//        assertThat(document.getParts().get(1).getTaxTotal()).isEqualTo(GamaMoney.parse("USD 14.93"));
        assertThat(document.getParts().get(1).getTotal()).isEqualTo(GamaMoney.parse("USD 71.11"));
        assertThat(document.getParts().get(1).getBaseTotal()).isEqualTo(GamaMoney.parse("EUR 61.84"));

        assertThat(document.getPartsTotal()).isEqualTo(GamaMoney.parse("USD 89.29"));
        assertThat(document.getDiscountTotal()).isEqualTo(GamaMoney.parse("USD 4.46"));
        assertThat(document.getSubtotal()).isEqualTo(GamaMoney.parse("USD 84.83"));
        assertThat(document.getTaxTotal()).isEqualTo(GamaMoney.parse("USD 17.81"));
        assertThat(document.getTotal()).isEqualTo(GamaMoney.parse("USD 102.64"));

        assertThat(document.getBaseSubtotal()).isEqualTo(GamaMoney.parse("EUR 73.77"));
        assertThat(document.getBaseTaxTotal()).isEqualTo(GamaMoney.parse("EUR 15.49"));
        assertThat(document.getBaseTotal()).isEqualTo(GamaMoney.parse("EUR 89.26"));
    }
}

package lt.gama.service;

import lt.gama.auth.impl.Auth;
import lt.gama.helpers.*;
import lt.gama.model.i.IExchangeAmount;
import lt.gama.model.sql.system.ExchangeRateSql;
import lt.gama.model.sql.system.id.ExchangeRateId;
import lt.gama.model.type.Exchange;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.model.type.auth.CurrencySettings;
import lt.gama.service.ex.rt.GamaExchangeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Gama
 * Created by valdas on 15-03-26.
 */
@Service
public class CurrencyService {

    private final DBServiceSQL dbServiceSQL;
    private final Auth auth;

    /**
     * if testmode do try to access external exchange rates APIs
     */
    private boolean testMode = false;

    @Autowired
    CurrencyService(DBServiceSQL dbServiceSQL, Auth auth) {
        this.dbServiceSQL = dbServiceSQL;
        this.auth = auth;
    }

    public boolean isTestMode() {
        return testMode;
    }

    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
    }

    public Exchange exchangeRate(String currency, LocalDate date) {
        Validators.checkArgument(currency != null && !currency.isEmpty(), "No currency");
        Validators.checkNotNull(date, "No date");
        LocalDate today = DateUtils.date();
        Validators.checkArgument(!date.isAfter(today),
                MessageFormat.format("Wrong date {0} - it should be today, i.e. {1} or from past", date, today));

        CompanySettings settings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        String region = Validators.checkNotNull(settings.getRegion(), "No company region");
        CurrencySettings currencySettings = Validators.checkNotNull(settings.getCurrency(), "No company currency settings");
        String base = Validators.checkNotNull(currencySettings.getCode(), "No company base currency");
        Validators.checkArgument(!region.isEmpty(), "No company region");

        if (base.equals(currency))
            return new Exchange(base, BigDecimal.ONE, currency, BigDecimal.ONE, null);
        if (base.equals("EUR") && currency.equals("LTL"))
            return new Exchange(base, BigDecimal.ONE, currency, new BigDecimal("3.4528"), null);

        Validators.checkArgument(region.equals("LT") || region.equals("EU"), "Region not LT nor EU");
        ExchangeRateSql exchangeRate = Validators.checkNotNull(getExchangeRate(region, base, currency, date),
                MessageFormat.format("No {0} exchange rate for {1}", currency, date));

        return exchangeRate.getExchange();
    }


    private ExchangeRateSql getExchangeRate(String region, String base, String currency, LocalDate date) {
        ExchangeRateSql exchangeRate = dbServiceSQL.getById(ExchangeRateSql.class, new ExchangeRateId(region, currency, date));
        if (exchangeRate != null) return exchangeRate;

        // if not found - retrieve from internet
        if (!isTestMode() && (region.equals("LT") || region.equals("EU"))) {
            Exchange exchange = getExchangeRateLB(region, currency, date);
            exchangeRate = new ExchangeRateSql(region, currency, date, exchange);
            if (base.equals("EUR") && exchangeRate.getExchange().getBase().equals("LTL")) {
                exchangeRate.getExchange().setBase(base);
                exchangeRate.getExchange().setBaseAmount(exchangeRate.getExchange().getBaseAmount().divide(new BigDecimal("3.4528"), 5, RoundingMode.HALF_UP));
            }
            dbServiceSQL.saveEntity(exchangeRate);
        }
        return exchangeRate;
    }

    /**
     * Get exchange rate in Lietuvos Bankas<br>
     * <br>
     * HOST: lb.lt<br>
     * GET: /webservices/fxrates/FxRates.asmx/getFxRatesForCurrency?tp=string&ccy=string&dtFrom=string&dtTo=string<br>
     * Parameter in:
     * <ul>
     *   <li>tp - exchange rate type. Possible types:
     *     <ul>
     *       <li>EU — the exchange rates of the euro against foreign currencies of the European Central Bank (data in the base since 30/09/2014)
     *          and of Lietuvos bankas (data in the base since 30/12/2014) are presented according to their announcement date.</li>
     *       <li>LT — the exchange rates of the litas against foreign currencies are presented until the euro adoption date
     *          (data in the base since 25/06/1993). As of the euro adoption date the exchange rates of the euro against foreign currencies,
     *          announced by the European Central Bank and Lietuvos bankas (data in the base since 1/1/2015), are presented in accordance
     *          with Article 5 of the Law on Accounting (draft law in Lithuanian).</li>
     *     </ul>
     *   </li>
     *   <li>ccy - ISO 4217 Currency code, e.g. USD</li>
     *   <li>dtFrom - exchange rate date from (ISO 8601), e.g. 2015-01-02</li>
     *   <li>dtTo - exchange rate date to (ISO 8601), e.g. 2015-01-15</li>
     * </ul>
     *
     * result something like that:
     * <pre>
     * &lt;?xml version="1.0" encoding="utf-8"?&gt;
     * &lt;FxRates xmlns="..."&gt;
     *   &lt;FxRate&gt;
     *     &lt;Tp&gt;LT&lt;/Tp&gt;
     *     &lt;Dt&gt;2015-01-15&lt;/Dt&gt;
     *     &lt;CcyAmt&gt;
     *       &lt;Ccy&gt;EUR&lt;/Ccy&gt;
     *       &lt;Amt&gt;1&lt;/Amt&gt;
     *     &lt;/CcyAmt&gt;
     *     &lt;CcyAmt&gt;
     *       &lt;Ccy&gt;USD&lt;/Ccy&gt;
     *       &lt;Amt&gt;1.1775&lt;/Amt&gt;
     *     &lt;/CcyAmt&gt;
     *   &lt;/FxRate&gt;
     * &lt;/FxRates&gt;
     * </pre>
     *
     * @param tp exchange rate type:
     * @param currency ISO 4217 Currency code
     * @param date exchange rate date
     * @return exchange data
     */
    private Exchange getExchangeRateLB(String tp, String currency, LocalDate date)  {

        // check special cases
        if ("LTL".equals(currency)) {
            return new Exchange("EUR", BigDecimal.ONE, "LTL", new BigDecimal("3.4528"), date);
        }
        if ("EUR".equals(currency)) {
            return new Exchange("EUR", date);
        }

        try {
            URI uri = new URI("http://lb.lt/webservices/fxrates/FxRates.asmx/getFxRatesForCurrency" +
                    "?tp=" + tp +
                    "&ccy=" + currency +
                    "&dtFrom=" + date.toString() +
                    "&dtTo=" + date);

            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.addRequestProperty("User-Agent", "Chrome");

            InputStream is = connection.getInputStream();
            Document doc = XMLUtils.document(is);

            is.close();

            doc.getDocumentElement().normalize();

            NodeList fxRateList = Validators.checkNotNull(doc.getElementsByTagName("FxRate"), "XML error - no FxRate");
            Validators.checkArgument(fxRateList.getLength() == 1, "XML error - FxRate appears more than once - {0}", fxRateList.getLength());

            Node node = fxRateList.item(0);
            Validators.checkArgument(node.getNodeType() == Node.ELEMENT_NODE, "XML error - no element node");
            Element element = (Element) node;
            Validators.checkArgument(tp.equals(element.getElementsByTagName("Tp").item(0).getTextContent()), "XML error - not the same tp");
            Validators.checkArgument(date.toString().equals(element.getElementsByTagName("Dt").item(0).getTextContent()), "XML error - not the same date");

            NodeList ccyAmtList = Validators.checkNotNull(doc.getElementsByTagName("CcyAmt"), "XML error - no CcyAmt");
            Validators.checkArgument(ccyAmtList.getLength() == 2, "XML error - CcyAmt must appears 2 times, but appear {0}", ccyAmtList.getLength());

            node = ccyAmtList.item(0);
            Validators.checkArgument(node.getNodeType() == Node.ELEMENT_NODE, "XML error - no element node");
            element = (Element) node;
            String respBaseCurrency = Validators.checkNotNull(element.getElementsByTagName("Ccy").item(0).getTextContent(), "XML error - no base currency");
            BigDecimal respBaseAmount = new BigDecimal(Validators.checkNotNull(element.getElementsByTagName("Amt").item(0).getTextContent(), "XML error - no base amount"));

            node = ccyAmtList.item(1);
            Validators.checkArgument(node.getNodeType() == Node.ELEMENT_NODE, "XML error - no element node");
            element = (Element) node;
            String respCurrency = Validators.checkNotNull(element.getElementsByTagName("Ccy").item(0).getTextContent(), "XML error - no currency");
            BigDecimal respAmount = new BigDecimal(Validators.checkNotNull(element.getElementsByTagName("Amt").item(0).getTextContent(), "XML error - no amount"));

            if ("LTL".equals(respBaseCurrency)) {
                // change currency from LTL to EUR
                respBaseCurrency = "EUR";
                respBaseAmount = respBaseAmount.divide(new BigDecimal("3.4528"), Math.max(4, respBaseAmount.scale()) * 2, RoundingMode.HALF_UP);
            }
            return new Exchange(respBaseCurrency, respBaseAmount, respCurrency, respAmount, date);

        } catch (URISyntaxException | IOException | SAXException |
                ParserConfigurationException | NullPointerException | IllegalArgumentException e) {
            throw new GamaExchangeException(MessageFormat.format("Can''t get exchange rate for {0} at {1}", currency, date), e);
        }
    }

    public Exchange currencyExchange(CompanySettings settings, Exchange exchange, LocalDate date) {
        try {
            Validators.checkNotNull(settings, "No company settings");
            CurrencySettings currencySettings = Validators.checkNotNull(settings.getCurrency(), "No company currency settings");
            String base = Validators.checkNotNull(currencySettings.getCode(), "No company base currency");

            if (exchange == null || exchange.getCurrency() == null || exchange.getCurrency().isEmpty())
                return new Exchange(base, BigDecimal.ONE, base, BigDecimal.ONE, null);

            if (StringHelper.isEmpty(exchange.getBase())) exchange.setBase(base);
            if (BigDecimalUtils.isZero(exchange.getBaseAmount())) exchange.setBaseAmount(BigDecimal.ONE);


            if (exchange.getBase().equals(exchange.getCurrency())) {
                if (!BigDecimalUtils.isEqual(exchange.getBaseAmount(), BigDecimal.ONE))
                    exchange.setBaseAmount(BigDecimal.ONE);
                if (!BigDecimalUtils.isEqual(exchange.getAmount(), BigDecimal.ONE)) exchange.setAmount(BigDecimal.ONE);
                if (exchange.getDate() != null) exchange.setDate(null);
                return exchange;
            }

            if (exchange.getDate() == null || !exchange.getDate().equals(date) || BigDecimalUtils.isZero(exchange.getAmount())) {
                String region = Validators.checkNotNull(settings.getRegion(), "No company region");
                ExchangeRateSql exchangeRate = Validators.checkNotNull(getExchangeRate(region, base, exchange.getCurrency(), date),
                        "No {0} exchange rate for {1}", exchange.getCurrency(), date);
                return exchangeRate.getExchange();
            }
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new GamaExchangeException(e.getMessage());
        }

        return exchange;
    }

    public GamaMoney exchange(Exchange exchange, GamaMoney amount) {
        return exchange == null ? amount : exchange.exchange(amount);
    }

    public void checkBaseMoneyDocumentExchange(LocalDate date, IExchangeAmount document, Boolean skipBase) {
        if ((document.getExchange() == null && document.getAmount() != null) ||
                (document.getExchange() != null && document.getAmount() != null &&
                        !Objects.equals(document.getExchange().getCurrency(),
                                document.getAmount().getCurrency()))) {
            document.setExchange(new Exchange(document.getAmount().getCurrency()));
        }
        Exchange exchange = Validators.checkNotNull(currencyExchange(auth.getSettings(),
                document.getExchange(), date), "No exchange");
        document.setExchange(exchange);

        // convert to base currency if not 'skipBase'
        if (BooleanUtils.isNotTrue(skipBase)) {
            if (exchange.getBase().equals(exchange.getCurrency())) {
                document.setBaseAmount(document.getAmount());
            } else {
                if (GamaMoneyUtils.isNonZero(document.getAmount())) {
                    document.setBaseAmount(exchange.exchange(document.getAmount()));
                } else {
                    document.setBaseAmount(null);
                }
            }
        }
    }

    public void checkBaseMoneyDocumentExchange(LocalDate date, IExchangeAmount document) {
        checkBaseMoneyDocumentExchange(date, document, false);
    }
}

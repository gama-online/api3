package lt.gama.api.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.auth.annotation.RequiresPermissions;
import lt.gama.model.type.Exchange;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;

import static lt.gama.api.service.Api.APP_API_3_PATH;

/**
 * Gama
 * Created by valdas on 15-03-26.
 */
@RequestMapping(APP_API_3_PATH + "currency")
@RequiresPermissions
public interface CurrencyApi extends Api {

    @PostMapping("/currencyExchange")
    APIResult<Exchange> currencyExchange(CurrencyExchangeRequest request) throws GamaApiException;

    @SuppressWarnings("unused")
    class CurrencyExchangeRequest {
        @JsonProperty("c") public String currency;
        @JsonProperty("dt") public LocalDate date;
        public CurrencyExchangeRequest() {}
        public CurrencyExchangeRequest(String currency, LocalDate date) {
            this.currency = currency;
            this.date = date;
        }
    }
}

package lt.gama.api.impl;

import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.service.CurrencyApi;
import lt.gama.model.type.Exchange;
import lt.gama.service.APIResultService;
import lt.gama.service.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

/**
 * Gama
 * Created by valdas on 15-03-26.
 */
@RestController
public class CurrencyApiImpl implements CurrencyApi {

    private final CurrencyService currencyService;
    private final APIResultService apiResultService;

    public CurrencyApiImpl(CurrencyService currencyService, APIResultService apiResultService) {
        this.currencyService = currencyService;
        this.apiResultService = apiResultService;
    }

    @Override
    public APIResult<Exchange> currencyExchange(CurrencyExchangeRequest request) throws GamaApiException {
        return apiResultService.result(() -> currencyService.exchangeRate(request.currency, request.date));
    }
}

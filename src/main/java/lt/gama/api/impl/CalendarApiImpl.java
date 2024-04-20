package lt.gama.api.impl;

import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.CalendarRequest;
import lt.gama.api.service.CalendarApi;
import lt.gama.helpers.BooleanUtils;
import lt.gama.model.type.CalendarMonth;
import lt.gama.service.APIResultService;
import lt.gama.service.CalendarService;
import org.springframework.web.bind.annotation.RestController;

/**
 * gama-online
 * Created by valdas on 2016-01-07.
 */
@RestController
public class CalendarApiImpl implements CalendarApi {

    private final CalendarService calendarService;
    private final APIResultService apiResultService;


    public CalendarApiImpl(CalendarService calendarService, APIResultService apiResultService) {
        this.calendarService = calendarService;
        this.apiResultService = apiResultService;
    }


    @Override
    public APIResult<CalendarMonth> getMonth(CalendarRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                calendarService.getMonth(request.getYear(), request.getMonth(), BooleanUtils.isTrue(request.getRefresh())));
    }

    @Override
    public APIResult<Void> saveMonth(CalendarRequest request) throws GamaApiException {
        return apiResultService.result(() ->
            calendarService.saveMonth(request.getYear(), request.getMonth(), request.getCalendarMonth()));
    }
}

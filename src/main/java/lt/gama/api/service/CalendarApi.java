package lt.gama.api.service;

import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.CalendarRequest;
import lt.gama.auth.annotation.RequiresPermissions;
import lt.gama.model.type.CalendarMonth;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static lt.gama.api.service.Api.APP_API_3_PATH;

/**
 * gama-online
 * Created by valdas on 2016-01-07.
 */
@RequestMapping(APP_API_3_PATH + "calendar")
@RequiresPermissions
public interface CalendarApi extends Api {

    @PostMapping("/getMonth")
    APIResult<CalendarMonth> getMonth(CalendarRequest request) throws GamaApiException;

    @PostMapping("/saveMonth")
    APIResult<Void> saveMonth(CalendarRequest request) throws GamaApiException;
}

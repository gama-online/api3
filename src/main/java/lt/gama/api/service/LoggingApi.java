package lt.gama.api.service;

import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.auth.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static lt.gama.api.service.Api.APP_API_3_PATH;

@RequestMapping(APP_API_3_PATH + "log")
@RequiresPermissions
public interface LoggingApi extends Api {

    @PostMapping("/front")
    APIResult<Void> logFront(LogFrontRequest logFrontRequest) throws GamaApiException;

    @SuppressWarnings("unused")
    class LogFrontRequest {
        public String message;
        public String version;
        public String url;
    }
}

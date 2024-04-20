package lt.gama.api.impl;

import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.service.LoggingApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;

import static lt.gama.Constants.*;

@RestController
public class LoggingApiImpl implements LoggingApi {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public APIResult<Void> logFront(LogFrontRequest logFrontRequest) throws GamaApiException {
        MDC.put(LOG_LABEL_FRONT, "true");
        MDC.put(LOG_FRONT_VERSION, logFrontRequest.version);
        MDC.put(LOG_FRONT_URL, logFrontRequest.url);
        log.error(logFrontRequest.message);
        return APIResult.Data();
    }
}

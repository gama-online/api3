package lt.gama.service.sync.openCart;

import lt.gama.service.sync.i.ISyncOpenCartUtilsService;
import lt.gama.service.sync.SyncHttpService;
import lt.gama.service.sync.openCart.model.OCLogin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

@Service
public class SyncOpenCartUtilsService implements ISyncOpenCartUtilsService {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    private final SyncHttpService syncHttpService;

    public SyncOpenCartUtilsService(SyncHttpService syncHttpService) {
        this.syncHttpService = syncHttpService;
    }

    @Override
    public OCLogin login(String api, String key, String username) {
        try {
            Map<String, String> httpCookies = new HashMap<>();
            OCLogin login = syncHttpService.getRequestData(
                    SyncHttpService.HttpMethod.POST,
                    api,
                    Map.of("route", "api/login"),
                    SyncHttpService.ContentType.FORM,
                    Map.of("key", key, "username", username),
                    OCLogin.class,
                    null,
                    httpCookies);
            login.setSession(getSessionCookie(httpCookies));
            return login;

        } catch (Exception e) {
            log.error(this.getClass().getSimpleName() + ": " +
                    " api=" + api +
                    " key=" + key +
                    " username=" + username +
                    " message=" + e.getMessage(), e);
            return null;
        }
    }

    @Override
    public SyncHttpService.Cookie getSessionCookie(Map<String, String> httpCookies) {
        if (httpCookies != null) {
            String name = "PHPSESSID";
            String value = httpCookies.get(name);
            if (value != null) return new SyncHttpService.Cookie(name, value);
        }
        return null;
    }
}

package lt.gama.service.sync.i;

import lt.gama.service.sync.SyncHttpService;
import lt.gama.service.sync.openCart.model.OCLogin;

import java.util.Map;

public interface ISyncOpenCartUtilsService {

    OCLogin login(String api, String key, String username);

    SyncHttpService.Cookie getSessionCookie(Map<String, String> httpCookies);
}

package lt.gama.api.service;

import lt.gama.api.APIResult;
import lt.gama.auth.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static lt.gama.api.service.Api.APP_API_3_PATH;

/**
 * gama-online
 * Created by valdas on 2016-03-20.
 */
@RequestMapping(APP_API_3_PATH + "dashboard")
@RequiresPermissions
public interface DashboardApi extends Api {

    @PostMapping("/listReleaseNotes")
    APIResult<Void> listReleaseNotes();

}

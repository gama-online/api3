package lt.gama.api.service.maintenance;

import io.swagger.v3.oas.annotations.tags.Tag;
import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.auth.annotation.MaintenancePermissions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static lt.gama.api.service.maintenance.ApiMaintenance.MAINTENANCE_PATH;

/**
 * gama-online
 * Created by valdas on 2017-01-26.
 */
@RequestMapping(MAINTENANCE_PATH + "salary")
@Tag(name = "salary")
@MaintenancePermissions
public interface FixSalaryApi extends ApiMaintenance {

    @PostMapping("/refreshVacations")
    APIResult<String> refreshVacations() throws GamaApiException;
}

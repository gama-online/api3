package lt.gama.api.service.maintenance;

import io.swagger.v3.oas.annotations.tags.Tag;
import lt.gama.api.APIResult;
import lt.gama.auth.annotation.MaintenancePermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static lt.gama.api.service.maintenance.ApiMaintenance.MAINTENANCE_PATH;

@RequestMapping(MAINTENANCE_PATH + "migration")
@Tag(name = "migration")
@MaintenancePermissions
public interface MigrationApi extends ApiMaintenance {

    @GetMapping("/echo")
    APIResult<String> echo();
}



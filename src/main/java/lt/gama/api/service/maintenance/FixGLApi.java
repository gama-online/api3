package lt.gama.api.service.maintenance;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.tags.Tag;
import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.auth.annotation.MaintenancePermissions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static lt.gama.api.service.maintenance.ApiMaintenance.MAINTENANCE_PATH;

/**
 * gama-online
 * Created by valdas on 2016-05-09.
 */
@RequestMapping(MAINTENANCE_PATH + "gl")
@Tag(name = "gl")
@MaintenancePermissions
public interface FixGLApi extends ApiMaintenance {

    @PostMapping("/deleteGLAccount")
    APIResult<Void> deleteGLAccount(DeleteGLAccountRequest request) throws GamaApiException;

    class DeleteGLAccountRequest {
        @JsonProperty("company") public long companyId;
        public long id;
    }
}

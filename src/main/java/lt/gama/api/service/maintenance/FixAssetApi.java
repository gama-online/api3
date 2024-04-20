package lt.gama.api.service.maintenance;

import io.swagger.v3.oas.annotations.tags.Tag;
import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.auth.annotation.MaintenancePermissions;
import lt.gama.model.sql.entities.AssetSql;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static lt.gama.api.service.maintenance.ApiMaintenance.MAINTENANCE_PATH;

/**
 * gama-online
 * Created by valdas on 2017-04-03.
 */
@RequestMapping(MAINTENANCE_PATH + "asset")
@Tag(name = "asset")
@MaintenancePermissions
public interface FixAssetApi extends ApiMaintenance {

    /**
     * Fix asset updatedFrom field vale, i.e. recalculate and assign
     * Fix asset lastDate field value
     * @return none
     */
    @PostMapping("/fixAsset")
    APIResult<String> fixAsset() throws GamaApiException;

    @PostMapping("/refreshAsset")
    APIResult<AssetSql> refreshAsset(RefreshAssetRequest request) throws GamaApiException;

    class RefreshAssetRequest {
        public long id;
    }
}

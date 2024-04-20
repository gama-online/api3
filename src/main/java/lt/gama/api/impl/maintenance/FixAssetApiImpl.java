package lt.gama.api.impl.maintenance;

import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.service.maintenance.FixAssetApi;
import lt.gama.auth.impl.Auth;
import lt.gama.model.sql.entities.AssetSql;
import lt.gama.service.*;
import lt.gama.service.ex.rt.GamaNotFoundException;
import lt.gama.tasks.maintenance.FixAssetTask;
import org.springframework.web.bind.annotation.RestController;

/**
 * gama-online
 * Created by valdas on 2017-04-03.
 */
@RestController
public class FixAssetApiImpl implements FixAssetApi {

    private final DepreciationService depreciationService;
    private final DBServiceSQL dbServiceSQL;
    private final Auth auth;
    private final AuthSettingsCacheService authSettingsCacheService;
    private final APIResultService apiResultService;
    private final TaskQueueService taskQueueService;

    public FixAssetApiImpl(DepreciationService depreciationService, DBServiceSQL dbServiceSQL, Auth auth, AuthSettingsCacheService authSettingsCacheService, APIResultService apiResultService, TaskQueueService taskQueueService) {
        this.depreciationService = depreciationService;
        this.dbServiceSQL = dbServiceSQL;
        this.auth = auth;
        this.authSettingsCacheService = authSettingsCacheService;
        this.apiResultService = apiResultService;
        this.taskQueueService = taskQueueService;
    }


    @Override
    public APIResult<String> fixAsset() throws GamaApiException {
        return apiResultService.result(() -> taskQueueService.queueTask(new FixAssetTask()));
    }

    @Override
    public APIResult<AssetSql> refreshAsset(RefreshAssetRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            AssetSql asset = dbServiceSQL.getById(AssetSql.class, request.id);
            if (asset == null) throw new GamaNotFoundException("Assets " + request.id + " not found");
            auth.setCompanyId(asset.getCompanyId());
            auth.setSettings(authSettingsCacheService.get(auth.getCompanyId()));

            depreciationService.reset(asset);
            dbServiceSQL.saveEntityInCompany(asset);
            return asset;
        });
    }
}

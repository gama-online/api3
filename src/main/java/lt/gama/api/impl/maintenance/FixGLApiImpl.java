package lt.gama.api.impl.maintenance;

import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.service.maintenance.FixGLApi;
import lt.gama.auth.impl.Auth;
import lt.gama.model.sql.entities.GLAccountSql;
import lt.gama.service.APIResultService;
import lt.gama.service.AuthSettingsCacheService;
import lt.gama.service.DBServiceSQL;
import org.springframework.web.bind.annotation.RestController;

/**
 * gama-online
 * Created by valdas on 2016-05-09.
 */
@RestController
public class FixGLApiImpl implements FixGLApi {

    private final DBServiceSQL dbServiceSQL;
    private final Auth auth;
    private final AuthSettingsCacheService authSettingsCacheService;
    private final APIResultService apiResultService;

    public FixGLApiImpl(DBServiceSQL dbServiceSQL, Auth auth, AuthSettingsCacheService authSettingsCacheService, APIResultService apiResultService) {
        this.dbServiceSQL = dbServiceSQL;
        this.auth = auth;
        this.authSettingsCacheService = authSettingsCacheService;
        this.apiResultService = apiResultService;
    }

    @Override
    public APIResult<Void> deleteGLAccount(DeleteGLAccountRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            auth.setCompanyId(request.companyId);
            auth.setSettings(authSettingsCacheService.get(auth.getCompanyId()));

            dbServiceSQL.deleteById(GLAccountSql.class, request.id);
        });
    }
}

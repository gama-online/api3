package lt.gama.api.impl.maintenance;

import lt.gama.api.APIResult;
import lt.gama.api.service.maintenance.MigrationApi;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MigrationApiImpl implements MigrationApi {

    @Override
    public APIResult<String> echo() {
        return APIResult.Data( "o-o-o");
    }
}

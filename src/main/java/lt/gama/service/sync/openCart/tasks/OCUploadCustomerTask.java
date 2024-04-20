package lt.gama.service.sync.openCart.tasks;

import lt.gama.api.response.TaskResponse;
import lt.gama.helpers.BooleanUtils;
import lt.gama.helpers.Validators;
import lt.gama.model.sql.system.SyncSql;
import lt.gama.model.type.enums.SyncType;
import lt.gama.model.type.sync.SyncSettings;
import lt.gama.service.sync.i.ISyncOpenCartAService;
import lt.gama.service.sync.openCart.SyncOpenCartAService;
import lt.gama.tasks.BaseDeferredTask;
import lt.gama.tasks.sync.SyncCompanyTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.invoke.MethodHandles;

/**
 * gama-online
 * Created by valdas on 2019-03-15.
 */
public class OCUploadCustomerTask extends BaseDeferredTask {

    private final long counterpartyId;
    private final String email;


    @Autowired
    transient protected SyncOpenCartAService openCartAService;


    public OCUploadCustomerTask(long companyId, long counterpartyId, String email) {
        super(companyId);
        this.counterpartyId = counterpartyId;
        this.email = email;
    }

    @Override
    public void execute() {
        try {
            SyncSettings syncSettings = Validators.checkNotNull(auth.getSettings().getSync(), "No Sync settings");
            Validators.checkArgument(BooleanUtils.isTrue(auth.getSettings().getSync().getSyncActive()), "No Sync active");
            Validators.checkArgument(syncSettings.getType() == SyncType.OPENCART_A, "Wrong Sync type");
            Validators.checkNotNull(syncSettings.getUrl(), "No URL in Sync Settings");
            Validators.checkNotNull(dbServiceSQL.getById(SyncSql.class, getCompanyId()), "No Sync entity");

            final String api = Validators.checkNotNull(syncSettings.getUrl(), "No api url");
            final String key = Validators.checkNotNull(syncSettings.getKey(), "No api key");
            final String username = Validators.checkNotNull(syncSettings.getId(), "No api username");

            boolean isNewCustomer = openCartAService.uploadCustomer(api, key, username, counterpartyId, email);

            if (isNewCustomer && BooleanUtils.isTrue(syncSettings.getAbilities().price().fromGama())) {
                openCartAService.syncSpecialPricesForCustomer(api, key, username, counterpartyId);
            }

            taskQueueService.queueTask(new SyncCompanyTask(getCompanyId()));

            finish(TaskResponse.success());

        } catch (Exception e) {
            log.error(className + ": " + e.getMessage(), e);
            finish(TaskResponse.error(e.getMessage()));
        }
    }


    @Override
    public String toString() {
        return "counterpartyId=" + counterpartyId +
                " email='" + email + '\'' +
                ' ' + super.toString();
    }
}

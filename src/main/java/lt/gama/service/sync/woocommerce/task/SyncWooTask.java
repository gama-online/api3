package lt.gama.service.sync.woocommerce.task;

import lt.gama.api.response.TaskResponse;
import lt.gama.helpers.BooleanUtils;
import lt.gama.helpers.DateUtils;
import lt.gama.helpers.Validators;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.tasks.BigDataDeferredTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serial;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;

public class SyncWooTask extends BigDataDeferredTask<SyncWooData> {

    @Serial
    private static final long serialVersionUID = -1L;


    private final LocalDateTime date;


    public SyncWooTask(long companyId, LocalDateTime date) {
        super(companyId);
        this.date = date != null ? date : DateUtils.now().minusYears(100);
    }

    @Override
    public void execute() {
        CompanySettings companySettings = auth.getSettings();
        Validators.checkNotNull(companySettings.getSync(), "No sync settings");
        if (BooleanUtils.isNotTrue(companySettings.getSync().getSyncActive())) {
            log.info(className + ": Sync is not active for company " + getCompanyId());
            finish(TaskResponse.success());
            return;
        }
        if (BooleanUtils.isTrue(companySettings.getSync().getAbilities().product().toGama())) {
            taskQueueService.queueTask(new SyncWooProductsTask(getToken(), getCompanyId(), date));
        } else if (BooleanUtils.isTrue(companySettings.getSync().getAbilities().order().toGama())) {
            taskQueueService.queueTask(new SyncWooOrdersTask(getToken(), getCompanyId(), date));
        }
    }
}

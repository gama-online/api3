package lt.gama.service.sync.woocommerce;

import lt.gama.auth.impl.Auth;
import lt.gama.helpers.BooleanUtils;
import lt.gama.helpers.DateUtils;
import lt.gama.helpers.StringHelper;
import lt.gama.helpers.Validators;
import lt.gama.model.sql.system.CompanySql;
import lt.gama.model.sql.system.SyncSql;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.model.type.enums.CompanyStatusType;
import lt.gama.model.type.enums.SyncType;
import lt.gama.model.type.sync.SyncSettings;
import lt.gama.service.AuthSettingsCacheService;
import lt.gama.service.DBServiceSQL;
import lt.gama.service.TaskQueueService;
import lt.gama.service.ex.rt.GamaException;
import lt.gama.service.sync.i.ISyncWoocommerceService;
import lt.gama.service.sync.SyncResult;
import lt.gama.service.sync.woocommerce.task.SyncWooTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SyncWoocommerceService implements ISyncWoocommerceService {

    private static final Logger log = LoggerFactory.getLogger(SyncWoocommerceService.class);


    private final Auth auth;
    private final AuthSettingsCacheService authSettingsCacheService;
    private final DBServiceSQL dbServiceSQL;
    private final TaskQueueService taskQueueService;


    public SyncWoocommerceService(Auth auth, 
                                  AuthSettingsCacheService authSettingsCacheService, 
                                  DBServiceSQL dbServiceSQL, 
                                  TaskQueueService taskQueueService) {
        this.auth = auth;
        this.authSettingsCacheService = authSettingsCacheService;
        this.dbServiceSQL = dbServiceSQL;
        this.taskQueueService = taskQueueService;
    }


    @Override
    public SyncResult sync(long companyId) {
        auth.setCompanyId(companyId);
        auth.setSettings(authSettingsCacheService.get(auth.getCompanyId()));
        try {
            return dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
                Validators.checkArgument(BooleanUtils.isTrue(auth.getSettings().getMigratedParts()), "Company " + companyId + " has not migrated parts yet");

                SyncSql sync = dbServiceSQL.getById(SyncSql.class, companyId);
                if (sync == null) return null;

                CompanySql company = Validators.checkNotNull(dbServiceSQL.getById(CompanySql.class, auth.getCompanyId()), "No company with id=" + companyId);

                if (company.getStatus() == null || company.getStatus() == CompanyStatusType.INACTIVE) {
                    log.info(this.getClass().getSimpleName() + ": company=" + companyId + " status: null or INACTIVE");
                    return null;
                }

                CompanySettings companySettings = Validators.checkNotNull(company.getSettings(), "Company " + companyId + " has no settings");

                SyncSettings syncSettings = Validators.checkNotNull(companySettings.getSync(), "Company " + companyId + " has no Sync settings");
                Validators.checkArgument(syncSettings.getType() == SyncType.WOOCOMMERCE, "Company " + companyId + " has wrong Sync type");

                final LocalDateTime dateFrom = sync.getDate();
                final LocalDateTime dateTo = DateUtils.now(); // UTC !!!

                final LocalDateTime localDateFrom = DateUtils.adjust(dateFrom, companySettings.getTimeZone());
                final LocalDateTime localDateTo = DateUtils.adjust(dateTo, companySettings.getTimeZone());

                log.info(this.getClass().getSimpleName() + ": Syncing " + dateFrom + " - " + dateTo + " (" + localDateFrom + " - " + localDateTo + ")");

                Validators.checkArgument(StringHelper.hasValue(syncSettings.getUrl()), "No api url");
                Validators.checkArgument(StringHelper.hasValue(syncSettings.getKey()), "No api secret");
                Validators.checkArgument(StringHelper.hasValue(syncSettings.getId()), "No api user");

                SyncResult syncResult = new SyncResult();

                syncResult.getTasksIds().add(taskQueueService.queueTask(new SyncWooTask(companyId, dateFrom)));

                // Update sync date
                sync.setDate(dateTo);

                log.info(this.getClass().getSimpleName() + ": " + syncResult);
                return syncResult;
            });
        } catch (GamaException | NullPointerException | IllegalArgumentException e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
        return null;
    }
}

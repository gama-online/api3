package lt.gama.tasks.sync;

import lt.gama.model.sql.system.SyncSql;
import lt.gama.tasks.BaseDeferredTask;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serial;

//TODO finish
public class SyncCompanyTask extends BaseDeferredTask {

    @Serial
    private static final long serialVersionUID = -1L;


//    @Autowired
//    transient protected SyncTypeService syncTypeService;


    public SyncCompanyTask(long companyId) {
        super(companyId);
    }

    @Override
    public void execute() {
//        try {
//            SyncSql sync = dbServiceSQL.getById(SyncSql.class, getCompanyId());
//            if (sync == null) {
//                log.error(className + ": Syncing company done - Error: no syncing data - " + this);
//                return;
//            }
//            if (sync.getSettings() == null || sync.getSettings().getType() == null) {
//                log.warn(className + ": Syncing company done - Error: no settings or settings type - " + this);
//                return;
//            }
//
//            // check if time is about 0:30, i.e between 0:15 and 0:45
////            LocalTime now = DateUtils.now(sync.getSettings().getTimeZone()).toLocalTime();
////            if (now.isBefore(LocalTime.of(0, 15)) || now.isAfter(LocalTime.of(0, 45))) {
////                logger.log(Level.FINE, "Syncing: not time yet " + now);
////                return;
////            }
//
//            ISyncService syncService = syncTypeService.getSyncTypeService(sync.getSettings().getType());
//            if (syncService != null) {
//                syncService.sync(getCompanyId());
//                log.info(className + ": Syncing company done - " + this);
//            } else {
//                log.warn(className + ": Syncing company done - Error: no sync type - " + this);
//            }
//        } catch (Exception e) {
//            log.error(className + ": " + e.getMessage(), e);
//        }
    }
}

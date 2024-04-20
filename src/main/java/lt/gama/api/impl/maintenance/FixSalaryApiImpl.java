package lt.gama.api.impl.maintenance;

import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.service.maintenance.FixSalaryApi;
import lt.gama.helpers.BooleanUtils;
import lt.gama.model.sql.entities.WorkHoursSql;
import lt.gama.service.APIResultService;
import lt.gama.service.DBServiceSQL;
import lt.gama.service.TaskQueueService;
import lt.gama.tasks.UpdateEmployeeVacationTask;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * gama-online
 * Created by valdas on 2017-01-26.
 */
@RestController
public class FixSalaryApiImpl implements FixSalaryApi {

    private final DBServiceSQL dbServiceSQL;
    private final APIResultService apiResultService;
    private final TaskQueueService taskQueueService;

    public FixSalaryApiImpl(DBServiceSQL dbServiceSQL, APIResultService apiResultService, TaskQueueService taskQueueService) {
        this.dbServiceSQL = dbServiceSQL;
        this.apiResultService = apiResultService;
        this.taskQueueService = taskQueueService;
    }


    @Override
    public APIResult<String> refreshVacations() throws GamaApiException {
        return apiResultService.result(() -> {
            List<WorkHoursSql> list = dbServiceSQL.makeQueryInCompany(WorkHoursSql.class).getResultList();
            int refreshed = 0;
            for (WorkHoursSql wh : list) {
                if (BooleanUtils.isTrue(wh.getArchive())) continue;
                if (wh.getCompanyId() == 0) continue;

                refreshed++;
                taskQueueService.queueTask(new UpdateEmployeeVacationTask(wh.getCompanyId(), wh.getDate().getYear(), wh.getDate().getMonthValue(), wh.getId()));
            }
            return "Total: " + list.size() + ", refreshed: " + refreshed;
        });
    }
}

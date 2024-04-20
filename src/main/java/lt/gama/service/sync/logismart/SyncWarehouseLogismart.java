package lt.gama.service.sync.logismart;

import lt.gama.service.TaskQueueService;
import lt.gama.service.sync.i.ISyncWarehouseLogismart;
import lt.gama.service.sync.logismart.tasks.SyncWarehouseArrivalTask;
import lt.gama.service.sync.logismart.tasks.SyncWarehouseOrderTask;
import lt.gama.service.sync.logismart.tasks.SyncWarehousePartsTask;
import org.springframework.stereotype.Service;

@Service
public class SyncWarehouseLogismart implements ISyncWarehouseLogismart {

    private final TaskQueueService taskQueueService;

    public SyncWarehouseLogismart(TaskQueueService taskQueueService) {
        this.taskQueueService = taskQueueService;
    }

    @Override
    public String uploadProducts(long companyId) {
        return taskQueueService.queueTask(new SyncWarehousePartsTask(companyId));
    }

    @Override
    public String uploadArrival(long companyId, long documentId) {
        return taskQueueService.queueTask(new SyncWarehouseArrivalTask(companyId, documentId));
    }

    @Override
    public String uploadOrder(long companyId, long documentId) {
        return taskQueueService.queueTask(new SyncWarehouseOrderTask(companyId, documentId));
    }
}

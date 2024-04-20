package lt.gama.service.sync;

import lt.gama.model.type.enums.SyncType;
import lt.gama.model.type.enums.SyncWarehouseType;
import lt.gama.service.sync.i.ISyncWarehouseService;
import lt.gama.service.sync.i.base.ISyncService;
import org.springframework.stereotype.Service;

@Service
public class SyncTypeService {
    public ISyncWarehouseService getSyncWarehouseTypeService(SyncWarehouseType type) {
        return null;
    }

    public ISyncService getSyncTypeService(SyncType type) {
        return null;
    }
}

package lt.gama.service.sync.i;

import lt.gama.model.type.enums.SyncType;
import lt.gama.model.type.enums.SyncWarehouseType;
import lt.gama.service.sync.i.base.ISyncService;
import lt.gama.service.sync.i.base.ISyncWarehouseService;

public interface ISyncTypeService {

    ISyncService getSyncTypeService(SyncType syncType);

    ISyncWarehouseService getSyncWarehouseTypeService(SyncWarehouseType syncWarehouseType);

}

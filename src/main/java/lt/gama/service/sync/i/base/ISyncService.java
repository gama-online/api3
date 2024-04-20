package lt.gama.service.sync.i.base;


import lt.gama.service.sync.SyncResult;

/**
 * gama-online
 * Created by valdas on 2017-10-03.
 */
public interface ISyncService {

    SyncResult sync(long companyId);

}

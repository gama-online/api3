package lt.gama.service.sync.i;

import lt.gama.service.sync.i.base.ISyncService;
import lt.gama.service.sync.SyncResult;

import java.time.LocalDateTime;

/**
 * gama-online
 * Created by valdas on 2019-03-02.
 */
public interface ISyncOpenCartAService extends ISyncService {

    /**
     * Upload customer into OpenCart
     *
     * @return true - if new customer, false - if customer already exists in OpenCart
     */
    boolean uploadCustomer(String api, String key, String username, long counterpartyId, String email);

    void syncSpecialPrices(SyncResult syncResult, String api, String key, String username, LocalDateTime dateFrom);

    void syncSpecialPricesForCustomer(String api, String key, String username, long counterpartyId);
}

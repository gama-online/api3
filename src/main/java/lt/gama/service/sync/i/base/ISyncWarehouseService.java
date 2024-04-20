package lt.gama.service.sync.i.base;

public interface ISyncWarehouseService {

    default String uploadProducts(long companyId) { return ""; }

    default String uploadArrival(long companyId, long documentId) { return ""; };

    default String uploadOrder(long companyId, long documentId) { return ""; };

}

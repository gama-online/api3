package lt.gama.api.service.maintenance;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.tags.Tag;
import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.auth.annotation.MaintenancePermissions;
import lt.gama.model.sql.documents.PurchaseSql;
import lt.gama.model.sql.entities.InventoryHistorySql;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.enums.InventoryType;
import lt.gama.model.type.part.PartSN;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static lt.gama.api.service.maintenance.ApiMaintenance.MAINTENANCE_PATH;

/**
 * gama-online
 * Created by valdas on 2016-03-21.
 */
@RequestMapping(MAINTENANCE_PATH + "inventory")
@Tag(name = "inventory")
@MaintenancePermissions
public interface FixInventoryApi extends ApiMaintenance {

    /**
     * Delete inventory and everything related to it from DB
     * @return none
     */
    @PostMapping("/deleteInventory")
    APIResult<Map<String, Integer>> deleteInventory(DeleteInventoryRequest request) throws GamaApiException;

    class DeleteInventoryRequest {
        @JsonProperty("company") public long companyId;
        @JsonProperty("part") public long partId;
    }

    /**
     * Create InventoryNow
     * @param request CreateInventoryNowRequest
     * @return none
     */
    @PostMapping("/createInventoryNow")
    APIResult<String> createInventoryNow(CreateInventoryNowRequest request) throws GamaApiException;

    class CreateInventoryNowRequest {
        @JsonProperty("company") public long companyId;
        @JsonProperty("warehouse") public long warehouseId;
        @JsonProperty("part") public long partId;
        public PartSN sn;
        @JsonProperty("doc") public long docId;
        public InventoryType inventoryType;
        public String quantity;
        public String costTotal;
    }

    /**
     * Retrieve retrieveInventoryHistory records from period
     * @param request RetrieveInventoryHistoryRequest
     * @return array of InventoryBalance records
     */
    @PostMapping("/retrieveInventoryHistory")
    APIResult<List<InventoryHistorySql>> retrieveInventoryHistory(RetrieveInventoryHistoryRequest request) throws GamaApiException;

    class RetrieveInventoryHistoryRequest {
        @JsonProperty("company") public long companyId;
        @JsonProperty("warehouse") public long warehouseId;
        @JsonProperty("part") public long partId;
        public LocalDate dateFrom;
        public LocalDate dateTo;
    }

    @PostMapping("/createInventoryHistoryFromCopy")
    APIResult<InventoryHistorySql> createInventoryHistoryFromCopy(CreateInventoryHistoryFromCopyRequest request) throws GamaApiException;

    class CreateInventoryHistoryFromCopyRequest {
        public long id;
        public BigDecimal quantity;
        public GamaMoney cost;
    }

    @PostMapping("/createInventoryHistoryFromDoc")
    APIResult<List<InventoryHistorySql>> createInventoryHistoryFromDoc(CreateInventoryHistoryFromDocRequest request) throws GamaApiException;

    class CreateInventoryHistoryFromDocRequest {
        @JsonProperty("company") public long companyId;
        @JsonProperty("warehouse") public long warehouseId;
        @JsonProperty("part") public long partId;
        @JsonProperty("doc") public long docId;
        @JsonProperty("uuid") public UUID uuid;
    }

    /**
     * Fix TransProd document status - i.e. clear document fix flag if not all items are fixed
     * @return none
     */
    @PostMapping("/fixTransProdDocumentStatus")
    APIResult<String> fixTransProdDocumentStatus(FixTransProdDocumentStatusRequest request) throws GamaApiException;

    class FixTransProdDocumentStatusRequest {
        @JsonProperty("doc") public long documentId;
    }

    /**
     * Recall inventory without transaction
     * @return  task id
     */
    @PostMapping("/recallInventory")
    APIResult<String> recallInventory(RecallInventoryRequest request) throws GamaApiException;

    class RecallInventoryRequest {
        @JsonProperty("company") public long companyId;
        @JsonProperty("part") public long inventoryId;
    }

    /**
     * Clear Document's inventory finished mark
     * @return none
     */
    @PostMapping("/fixClearPurchaseInventoryFinishedMark")
    APIResult<PurchaseSql> fixClearPurchaseInventoryFinishedMark(FixClearPurchaseInventoryFinishedMarkRequest request) throws GamaApiException;

    class FixClearPurchaseInventoryFinishedMarkRequest {
        @JsonProperty("doc") public long docId;
    }

    /**
     * Create random Invoices in company for testing
     * @return none
     */
    @PostMapping("/createInvoices")
    APIResult<String> createInvoices(CreateInvoicesRequest request) throws GamaApiException;

    class CreateInvoicesRequest {
        @JsonProperty("company") public long companyId;
        public int count;
        public LocalDate dateFrom;
        public LocalDate dateTo;
    }
}

package lt.gama.api.service;

import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.*;
import lt.gama.api.response.*;
import lt.gama.auth.annotation.RequiresPermissions;
import lt.gama.model.dto.documents.*;
import lt.gama.model.dto.documents.items.PartPurchaseDto;
import lt.gama.model.dto.entities.InventoryHistoryDto;
import lt.gama.model.sql.system.CountryVatCodeSql;
import lt.gama.model.sql.system.CountryVatNoteSql;
import lt.gama.model.type.enums.DBType;
import lt.gama.model.type.enums.Permission;
import lt.gama.model.type.gl.GLDC;
import lt.gama.model.type.inventory.InvoiceNote;
import lt.gama.model.type.inventory.TaxFree;
import lt.gama.report.RepInventoryBalance;
import lt.gama.report.RepInventoryDetail;
import lt.gama.report.RepInvoice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;
import java.util.Map;

import static lt.gama.api.service.Api.APP_API_3_PATH;

/**
 * Gama
 * Created by valdas on 15-04-20.
 */
@RequestMapping(APP_API_3_PATH + "inventory")
@RequiresPermissions
public interface InventoryApi extends Api {

    /*
     * Opening Balance
     */

    @PostMapping("/listOpeningBalance")
    @RequiresPermissions({Permission.DOCUMENT_R, Permission.DOCUMENT_M, Permission.GL})
    APIResult<PageResponse<InventoryOpeningBalanceDto, Void>> listOpeningBalance(PageRequest request) throws GamaApiException;

    @PostMapping("/saveOpeningBalance")
    @RequiresPermissions({Permission.DOCUMENT_M, Permission.GL})
    APIResult<InventoryOpeningBalanceDto> saveOpeningBalance(InventoryOpeningBalanceDto request) throws GamaApiException;

    @PostMapping("/getOpeningBalance")
    @RequiresPermissions({Permission.DOCUMENT_R, Permission.DOCUMENT_M, Permission.GL})
    APIResult<InventoryOpeningBalanceDto> getOpeningBalance(IdRequest request) throws GamaApiException;

    @PostMapping("/finishOpeningBalanceTask")
    @RequiresPermissions({Permission.DOCUMENT_M, Permission.GL})
    APIResult<String> finishOpeningBalanceTask(IdRequest request) throws GamaApiException;

    @PostMapping("/importOpeningBalance")
    @RequiresPermissions({Permission.DOCUMENT_M, Permission.GL})
    APIResult<InventoryOpeningBalanceDto> importOpeningBalance(ImportDocRequest request) throws GamaApiException;

    @PostMapping("/deleteOpeningBalance")
    @RequiresPermissions({Permission.DOCUMENT_M, Permission.GL})
    APIResult<Void> deleteOpeningBalance(IdRequest request) throws GamaApiException;

    /*
     * Invoice
     */

    @PostMapping("/listInvoice")
    @RequiresPermissions({Permission.INVOICE_R, Permission.INVOICE_M, Permission.GL})
    ResponseEntity<StreamingResponseBody> listInvoice(PageRequest request) throws GamaApiException;

    @PostMapping("/saveInvoice")
    @RequiresPermissions({Permission.INVOICE_M, Permission.GL})
    APIResult<InvoiceDto> saveInvoice(InvoiceDto request) throws GamaApiException;

    @PostMapping("/getInvoice")
    @RequiresPermissions({Permission.INVOICE_R, Permission.INVOICE_M, Permission.GL})
    APIResult<InvoiceDto> getInvoice(IdRequest request) throws GamaApiException;

    @PostMapping("/finishInvoice")
    @RequiresPermissions({Permission.INVOICE_M, Permission.GL})
    APIResult<InvoiceDto> finishInvoice(FinishRequest request) throws GamaApiException;

    @PostMapping("/reportInvoiceSql")
    @RequiresPermissions({Permission.PART_S, Permission.INVOICE_R, Permission.INVOICE_M, Permission.GL})
    APIResult<PageResponse<RepInvoice, Void>> reportInvoiceSQL(PageRequest request) throws GamaApiException;

    @PostMapping("/deleteInvoice")
    @RequiresPermissions({Permission.INVOICE_M, Permission.GL})
    APIResult<Void> deleteInvoice(IdRequest request) throws GamaApiException;

    @PostMapping("/recallInvoice")
    @RequiresPermissions({Permission.INVOICE_M, Permission.GL})
    APIResult<InvoiceDto> recallInvoice(IdRequest request) throws GamaApiException;

    @PostMapping("/updateInvoiceISAF")
    @RequiresPermissions({Permission.GL})
    APIResult<InvoiceDto> updateInvoiceISAF(InvoiceDto request) throws GamaApiException;

    @PostMapping("/getLastInvoicePrice")
    @RequiresPermissions({Permission.INVOICE_M, Permission.GL})
    APIResult<LastInvoicePriceResponse> getLastInvoicePrice(GetLastInvoicePriceRequest request) throws GamaApiException;

    @SuppressWarnings("unused")
    class GetLastInvoicePriceRequest {
        public long counterpartyId;
        public DBType counterpartyDb;
        public long partId;
        public GetLastInvoicePriceRequest() {}
        public GetLastInvoicePriceRequest(long counterpartyId, DBType counterpartyDb, long partId) {
            this.counterpartyId = counterpartyId;
            this.counterpartyDb = counterpartyDb;
            this.partId = partId;
        }
    }

    @PostMapping("/syncInvoiceTask")
    @RequiresPermissions({Permission.INVOICE_M, Permission.GL})
    APIResult<String> syncInvoiceTask() throws GamaApiException;

    @SuppressWarnings("unused")
    class TaxFreeRequest {
        public long id;
        public DBType db;
        public TaxFree taxFree;
        public TaxFreeRequest() {}
        public TaxFreeRequest(long id, DBType db, TaxFree taxFree) {
            this.id = id;
            this.db = db;
            this.taxFree = taxFree;
        }
    }

    @PostMapping("/saveInvoiceTaxFree")
    @RequiresPermissions({Permission.INVOICE_M, Permission.GL})
    APIResult<InvoiceDto> saveInvoiceTaxFree(TaxFreeRequest request) throws GamaApiException;

    @PostMapping("/generateInvoiceTaxFree")
    APIResult<TaxFree> generateInvoiceTaxFree(IdRequest request) throws GamaApiException;

    @PostMapping("/submitInvoiceTaxFree")
    APIResult<InvoiceDto> submitInvoiceTaxFree(IdRequest request) throws GamaApiException;

    @PostMapping("/cancelInvoiceTaxFree")
    APIResult<InvoiceDto> cancelInvoiceTaxFree(IdRequest request) throws GamaApiException;

    @PostMapping("/submitInvoiceTaxFreePaymentInfo")
    APIResult<InvoiceDto> submitInvoiceTaxFreePaymentInfo(IdRequest request) throws GamaApiException;

    @PostMapping("/emailInvoiceTaxFree")
    APIResult<Void> emailInvoiceTaxFree(IdRequest request) throws GamaApiException;

    @PostMapping("/getInvoiceNotes")
    APIResult<List<InvoiceNote>> getInvoiceNotes() throws GamaApiException;

    @PostMapping("/syncWarehouseInvoiceTask")
    @RequiresPermissions({Permission.INVOICE_M, Permission.GL})
    APIResult<String> syncWarehouseInvoiceTask(IdRequest request) throws GamaApiException;

    /*
     * Purchase
     */

    @PostMapping("/listPurchase")
    @RequiresPermissions({Permission.PURCHASE_R, Permission.PURCHASE_M, Permission.GL})
    ResponseEntity<StreamingResponseBody> listPurchase(PageRequest request) throws GamaApiException;

    @PostMapping("/savePurchase")
    @RequiresPermissions({Permission.PURCHASE_M, Permission.GL})
    APIResult<PurchaseDto> savePurchase(PurchaseDto request) throws GamaApiException;

    @PostMapping("/getPurchase")
    @RequiresPermissions({Permission.PURCHASE_R, Permission.PURCHASE_M, Permission.GL})
    APIResult<PurchaseDto> getPurchase(IdRequest request) throws GamaApiException;

    @PostMapping("/finishPurchase")
    @RequiresPermissions({Permission.PURCHASE_M, Permission.GL})
    APIResult<PurchaseDto> finishPurchase(FinishRequest request) throws GamaApiException;

    @PostMapping("/deletePurchase")
    @RequiresPermissions({Permission.PURCHASE_M, Permission.GL})
    APIResult<Void> deletePurchase(IdRequest request) throws GamaApiException;

    @PostMapping("/recallPurchase")
    @RequiresPermissions({Permission.PURCHASE_M, Permission.GL})
    APIResult<PurchaseDto> recallPurchase(IdRequest request) throws GamaApiException;

    @PostMapping("/updatePurchaseISAF")
    @RequiresPermissions({Permission.GL})
    APIResult<PurchaseDto> updatePurchaseISAF(PurchaseDto request) throws GamaApiException;

    @PostMapping("/syncWarehousePurchaseTask")
    @RequiresPermissions({Permission.PURCHASE_M, Permission.GL})
    APIResult<String> syncWarehousePurchaseTask(IdRequest request) throws GamaApiException;

    @PostMapping("/importPurchase")
    @RequiresPermissions({Permission.PURCHASE_M, Permission.GL})
    APIResult<List<PartPurchaseDto>> importPurchase(ImportDocRequest request) throws GamaApiException;


    /*
     * Transportation - Production
     */

    @PostMapping("/listTransProd")
    @RequiresPermissions({Permission.DOCUMENT_R, Permission.DOCUMENT_M, Permission.GL})
    APIResult<PageResponse<TransProdDto, Void>> listTransProd(PageRequest request) throws GamaApiException;

    @PostMapping("/saveTransProd")
    @RequiresPermissions({Permission.DOCUMENT_M, Permission.GL})
    APIResult<TransProdDto> saveTransProd(TransProdDto request) throws GamaApiException;

    @PostMapping("/getTransProd")
    @RequiresPermissions({Permission.DOCUMENT_R, Permission.DOCUMENT_M, Permission.GL})
    APIResult<TransProdDto> getTransProd(IdRequest request) throws GamaApiException;

    @PostMapping("/finishTransProd")
    @RequiresPermissions({Permission.DOCUMENT_M, Permission.GL})
    APIResult<TransProdDto> finishTransProd(FinishRequest request) throws GamaApiException;

    @PostMapping("/deleteTransProd")
    @RequiresPermissions({Permission.DOCUMENT_M, Permission.GL})
    APIResult<Void> deleteTransProd(IdRequest request) throws GamaApiException;

    @PostMapping("/recallTransProd")
    @RequiresPermissions({Permission.DOCUMENT_M, Permission.GL})
    APIResult<TransProdDto> recallTransProd(IdRequest request) throws GamaApiException;

    @PostMapping("/importTransProd")
    @RequiresPermissions({Permission.DOCUMENT_M, Permission.GL})
    APIResult<TransProdDto> importTransProd(ImportDocRequest request) throws GamaApiException;

    @PostMapping("/reserveTransProd")
    @RequiresPermissions({Permission.DOCUMENT_M, Permission.GL})
    APIResult<TransProdDto> reserveTransProd(IdRequest request) throws GamaApiException;

    @PostMapping("/recallReserveTransProd")
    @RequiresPermissions({Permission.DOCUMENT_M, Permission.GL})
    APIResult<TransProdDto> recallReserveTransProd(IdRequest request) throws GamaApiException;

    /*
     * Inventory
     */

    @PostMapping("/listInventory")
    @RequiresPermissions
    APIResult<PageResponse<InventoryDto, Void>> listInventory(PageRequest request) throws GamaApiException;

    @PostMapping("/saveInventory")
    @RequiresPermissions({Permission.DOCUMENT_M, Permission.GL})
    APIResult<InventoryDto> saveInventory(InventoryDto request) throws GamaApiException;

    @PostMapping("/getInventory")
    @RequiresPermissions
    APIResult<InventoryDto> getInventory(IdRequest request) throws GamaApiException;

    @PostMapping("/finishInventoryTask")
    @RequiresPermissions({Permission.DOCUMENT_M, Permission.GL})
    APIResult<String> finishInventoryTask(FinishRequest request) throws GamaApiException;

    @PostMapping("/deleteInventory")
    @RequiresPermissions({Permission.DOCUMENT_M, Permission.GL})
    APIResult<Void> deleteInventory(IdRequest request) throws GamaApiException;

    @PostMapping("/recallInventoryTask")
    @RequiresPermissions({Permission.DOCUMENT_M, Permission.GL})
    APIResult<String> recallInventoryTask(IdRequest request) throws GamaApiException;

    @PostMapping("/importInventory")
    @RequiresPermissions({Permission.DOCUMENT_M, Permission.GL})
    APIResult<InventoryDto> importInventory(ImportDocRequest request) throws GamaApiException;

    /*
     * Estimate
     */

    @PostMapping("/listEstimate")
    @RequiresPermissions({Permission.INVOICE_R, Permission.INVOICE_M, Permission.GL})
    APIResult<PageResponse<EstimateDto, Void>> listEstimate(PageRequest request) throws GamaApiException;

    @PostMapping("/saveEstimate")
    @RequiresPermissions({Permission.INVOICE_M, Permission.GL})
    APIResult<EstimateDto> saveEstimate(EstimateDto request) throws GamaApiException;

    @PostMapping("/getEstimate")
    @RequiresPermissions({Permission.INVOICE_R, Permission.INVOICE_M, Permission.GL})
    APIResult<EstimateDto> getEstimate(IdRequest request) throws GamaApiException;

    @PostMapping("/finishEstimate")
    @RequiresPermissions({Permission.INVOICE_M, Permission.GL})
    APIResult<EstimateDto> finishEstimate(IdRequest request) throws GamaApiException;

    @PostMapping("/deleteEstimate")
    @RequiresPermissions({Permission.DOCUMENT_M, Permission.GL})
    APIResult<Void> deleteEstimate(IdRequest request) throws GamaApiException;

    /*
     * Order
     */

    @PostMapping("/listOrder")
    @RequiresPermissions({Permission.PURCHASE_R, Permission.PURCHASE_M, Permission.GL})
    APIResult<PageResponse<OrderDto, Void>> listOrder(PageRequest request) throws GamaApiException;

    @PostMapping("/saveOrder")
    @RequiresPermissions({Permission.PURCHASE_M, Permission.GL})
    APIResult<OrderDto> saveOrder(OrderDto request) throws GamaApiException;

    @PostMapping("/getOrder")
    @RequiresPermissions({Permission.PURCHASE_R, Permission.PURCHASE_M, Permission.GL})
    APIResult<OrderDto> getOrder(IdRequest request) throws GamaApiException;

    @PostMapping("/finishOrder")
    @RequiresPermissions({Permission.PURCHASE_M, Permission.GL})
    APIResult<OrderDto> finishOrder(IdRequest request) throws GamaApiException;

    /*
     * Reports
     */

    @PostMapping("/reportBalance")
    @RequiresPermissions({Permission.PART_S, Permission.PART_B, Permission.GL})
    APIResult<PageResponse<RepInventoryBalance, Void>> reportBalance(PageRequest request) throws GamaApiException;

    @PostMapping("/getBalance")
    @RequiresPermissions({Permission.PART_S, Permission.PART_B, Permission.GL})
    APIResult<InventoryBalanceResponse> getBalance(InventoryBalanceRequest request) throws GamaApiException;

    @PostMapping("/reportDetail")
    @RequiresPermissions({Permission.PART_B, Permission.GL})
    APIResult<PageResponse<InventoryHistoryDto, RepInventoryDetail>> reportDetail(PageRequest request) throws GamaApiException;

    @PostMapping("/reportGpais")
    @RequiresPermissions({Permission.PART_B, Permission.GL})
    APIResult<PageResponse<InventoryGpais, Void>> reportGpais(PageRequest request) throws GamaApiException;

    /*
     * Mail
     */

    @PostMapping("/sendInvoice")
    @RequiresPermissions({Permission.INVOICE_R, Permission.INVOICE_M, Permission.GL})
    APIResult<Void> sendInvoice(MailRequest request) throws GamaApiException;

    /*
     * VAT codes (for iSAF)
     */

    @PostMapping("/getVatCode")
    APIResult<CountryVatCodeSql> getVatCode() throws GamaApiException;

    @PostMapping("/getVatCodeGLSettings")
    APIResult<Map<String, GLDC>> getVatCodeGLSettings() throws GamaApiException;


    @PostMapping("/getVatNote")
    APIResult<CountryVatNoteSql> getVatNote() throws GamaApiException;

    @PostMapping("/{a:checkV[aA][tT]}")
    APIResult<CheckVatResponse> checkVat(CheckVatRequest request) throws GamaApiException;

}

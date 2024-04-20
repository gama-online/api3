package lt.gama.api.service;

import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.response.UploadResponse;
import lt.gama.auth.annotation.MaintenancePermissions;
import lt.gama.integrations.vmi.ws.DeclStateForQueryType;
import lt.gama.model.sql.documents.DoubleEntrySql;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static lt.gama.api.service.Api.APP_API_3_PATH;

@RequestMapping(APP_API_3_PATH + "test")
@MaintenancePermissions
public interface TestApi extends Api {

	@GetMapping("/createTestDoubleEntry")
	APIResult<DoubleEntrySql> createTestDoubleEntry(@RequestParam("company") long companyId) throws GamaApiException;

	@PostMapping("/testCreateStorage")
	APIResult<String> testCreateStorage(@RequestParam("company") Long companyId,
										@RequestParam("folder") String folder,
										@RequestParam("fileName") String fileName,
										@RequestParam("contentType") String contentType,
										@RequestParam("content") String content) throws GamaApiException;

	@PostMapping("/testReadStorage")
	APIResult<String> testReadStorage(@RequestParam("company") Long companyId,
									  @RequestParam("folder") String folder,
									  @RequestParam("fileName") String fileName) throws GamaApiException;

	@PostMapping("/testGetUploadUrl")
	APIResult<UploadResponse> testGetUploadUrl(@RequestParam("company") Long companyId,
											   @RequestParam("folder") String folder,
											   @RequestParam("fileName") String fileName,
											   @RequestParam("contentType") String contentType,
											   @RequestParam("isPublic") boolean isPublic,
											   @RequestParam("sourceFileName")  String sourceFileName) throws GamaApiException;

	@PostMapping("/testBlobList")
	APIResult<List<String>> testBlobList(@RequestParam("company") Long companyId, @RequestParam("folder") String folder) throws GamaApiException;

	@PostMapping("/testVMICert")
	APIResult<String> testVMICert() throws GamaApiException;

	@PostMapping("/testVMISubmitDeclaration")
	APIResult<String> testVMISubmitDeclaration(@RequestParam("docId") String docId) throws GamaApiException;

	@PostMapping("/testVMICancelDeclaration")
	APIResult<String> testVMICancelDeclaration(@RequestParam("docId") String docId) throws GamaApiException;

	@PostMapping("/testVMIQueryDeclarations")
	APIResult<String> testVMIQueryDeclarations(@RequestParam("docId") String docId,
											   @RequestParam("from") LocalDateTime timestampFrom,
											   @RequestParam("to") LocalDateTime timestampTo,
											   @RequestParam("state") DeclStateForQueryType state) throws GamaApiException;

	@PostMapping("/testVMIGetInfoOnExportedGoods")
	APIResult<String> testVMIGetInfoOnExportedGoods(@RequestParam("docId") String docId) throws GamaApiException;

	@PostMapping("/testVMISubmitPaymentInfo")
	APIResult<String> testVMISubmitPaymentInfo(@RequestParam("docId") String docId) throws GamaApiException;

	@PostMapping("/testVMITaxFreeUpdateTask")
	APIResult<String> testVMITaxFreeUpdateTask(@RequestParam("docId") String docId, 
											   @RequestParam("date") LocalDateTime timestamp, 
											   @RequestParam("id")long invoiceId) throws GamaApiException;

	@GetMapping("/getSystemInfo")
	APIResult<Map<String, Object>> getSystemInfo();
}

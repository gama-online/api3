package lt.gama.api.impl;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import freemarker.template.Configuration;
import freemarker.template.Version;
import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.response.UploadResponse;
import lt.gama.api.service.TestApi;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.DateUtils;
import lt.gama.helpers.StringHelper;
import lt.gama.integrations.vmi.ITaxRefundService;
import lt.gama.integrations.vmi.ws.DeclStateForQueryType;
import lt.gama.model.sql.documents.DoubleEntrySql;
import lt.gama.model.sql.documents.items.GLOperationSql;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.gl.GLOperationAccount;
import lt.gama.service.DBServiceSQL;
import lt.gama.service.StorageService;
import lt.gama.service.TaskQueueService;
import lt.gama.service.ex.rt.GamaException;
import lt.gama.tasks.TaxFreeUpdateTask;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
public class TestApiImpl implements TestApi {

	@Value("${gama.version}") private String appVersion;
	
	private final DBServiceSQL dbServiceSQL;
	private final StorageService storageService;
	private final ITaxRefundService taxRefundService;
	private final Auth auth;
	private final TaskQueueService taskQueueService;

	private static final Version VERSION = Configuration.VERSION_2_3_30;

    public TestApiImpl(DBServiceSQL dbServiceSQL, StorageService storageService, ITaxRefundService taxRefundService, Auth auth, TaskQueueService taskQueueService) {
        this.dbServiceSQL = dbServiceSQL;
        this.storageService = storageService;
        this.taxRefundService = taxRefundService;
        this.auth = auth;
        this.taskQueueService = taskQueueService;
    }

    @Override
	public APIResult<DoubleEntrySql> createTestDoubleEntry(long companyId) throws GamaApiException {
		DoubleEntrySql de = new DoubleEntrySql();
		de.setCompanyId(companyId);
		de.setDate(DateUtils.date());
		de.setNumber("?");
		de.setOperations(new ArrayList<>());
		de.getOperations().add(new GLOperationSql(new GLOperationAccount("1", "A"), new GLOperationAccount("2", "B"), GamaMoney.parse("EUR 123.45")));
		dbServiceSQL.saveWithCounter(de);
		return APIResult.Data(de);
	}

	@Override
	public APIResult<String> testCreateStorage(Long companyId, String folder, String fileName, String contentType, String content) throws GamaApiException {
		return APIResult.Data(storageService.upload(content, folder, fileName, contentType));
	}

	@Override
	public APIResult<String> testReadStorage(Long companyId, String folder, String filename) throws GamaApiException {
		return APIResult.Data(storageService.getContent(folder, filename));
	}

	@Override
	public APIResult<UploadResponse> testGetUploadUrl(Long companyId, String folder, String filename, String contentType, boolean isPublic, String sourceFileName) throws GamaApiException {
		return APIResult.Data(storageService.getUploadUrlv4(contentType, folder, filename, isPublic, sourceFileName));
	}

	@Override
	public APIResult<List<String>> testBlobList(Long companyId, String folder) throws GamaApiException {
		if (StringHelper.isEmpty(folder)) throw new GamaException("No folder");

		String folderFixed = folder.charAt(folder.length() - 1) != '/' ? folder + '/' : folder;

		Bucket bucket = storageService.defaultBucket();
		Page<Blob> blobs = bucket.list(
				Storage.BlobListOption.prefix(folderFixed),
				Storage.BlobListOption.currentDirectory());

		List<String> list = new ArrayList<>();

		for (Blob blob : blobs.iterateAll()) {
			list.add(blob.getName() + ", UpdateTime=" + new Date(blob.getUpdateTime()) + ", CreateTime=" + new Date(blob.getCreateTime()));
		}
		return APIResult.Data(list);
	}

	@Override
	public APIResult<String> testVMICert() throws GamaApiException {
		return APIResult.Data(taxRefundService.testCert());
	}

	@Override
	public APIResult<String> testVMISubmitDeclaration(String docId) throws GamaApiException {
		return APIResult.Data(taxRefundService.testVMISubmitDeclaration(docId));
	}

	@Override
	public APIResult<String> testVMICancelDeclaration(String docId) throws GamaApiException {
		return APIResult.Data(taxRefundService.testVMICancelDeclaration(docId));
	}

	@Override
	public APIResult<String> testVMIQueryDeclarations(String docId, LocalDateTime timestampFrom, LocalDateTime timestampTo, DeclStateForQueryType state) throws GamaApiException {
		return APIResult.Data(taxRefundService.testVMIQueryDeclarations(docId, timestampFrom, timestampTo, state));
	}

	@Override
	public APIResult<String> testVMIGetInfoOnExportedGoods(String docId) throws GamaApiException {
		return APIResult.Data(taxRefundService.testVMIGetInfoOnExportedGoods(docId));
	}

	@Override
	public APIResult<String> testVMISubmitPaymentInfo(String docId) throws GamaApiException {
		return APIResult.Data(taxRefundService.testVMISubmitPaymentInfo(docId));
	}

	@Override
	public APIResult<String> testVMITaxFreeUpdateTask(String docId, LocalDateTime timestamp, long invoiceId) throws GamaApiException {
		return APIResult.Data(taskQueueService.queueTask(new TaxFreeUpdateTask(auth.getCompanyId(), docId, timestamp, invoiceId)));
	}

	@Override
	public APIResult<Map<String, Object>> getSystemInfo() {
		return APIResult.Data(Map.of(
				"Gama version", appVersion,
				"java.version", System.getProperty("java.version"),
				"Runtime version", Runtime.version().toString(),
				"Available processors (cores)", Runtime.getRuntime().availableProcessors(),
				"Free memory (MB)", Runtime.getRuntime().freeMemory() / 1024 / 1024,
				"Maximum memory (MB)", Runtime.getRuntime().maxMemory() == Long.MAX_VALUE ? "no limit" : Runtime.getRuntime().maxMemory() / 1024 / 1024,
				"Total memory available to JVM (MB)", Runtime.getRuntime().totalMemory() / 1024 / 1024,
				"Environment", System.getenv(),
				"System properties", System.getProperties().stringPropertyNames().stream()
						.collect(Collectors.toMap(Function.identity(), System::getProperty))));
	}
}

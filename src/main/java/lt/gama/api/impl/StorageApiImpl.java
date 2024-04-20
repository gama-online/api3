package lt.gama.api.impl;

import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.GetUploadUrlRequest;
import lt.gama.api.request.StartImportRequest;
import lt.gama.api.request.UploadDataRequest;
import lt.gama.api.response.UploadResponse;
import lt.gama.api.service.StorageApi;
import lt.gama.helpers.BooleanUtils;
import lt.gama.service.StorageService;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StorageApiImpl implements StorageApi {

	private final StorageService storageService;

    public StorageApiImpl(StorageService storageService) {
        this.storageService = storageService;
    }

    @Override
	public APIResult<UploadResponse> getUploadUrl(GetUploadUrlRequest request) throws GamaApiException {
		return APIResult.Data(
				storageService.getUploadUrlv4(request.getContentType(), request.getFolder(), request.getFileName(),
						BooleanUtils.isTrue(request.getAccessPublic()), request.getSourceFileName()));
	}

	@Override
	public APIResult<String> startImport(StartImportRequest request) throws GamaApiException {
		return APIResult.Data(
				storageService.startImport(request.getFileName(), request.getEntity(),
						request.isDelete(), request.getFormat()));
	}

    @Override
    public APIResult<String> upload(UploadDataRequest request) throws GamaApiException {
		return APIResult.Data(
				storageService.upload(request.getData(), null, null, request.getContentType()));
    }
}

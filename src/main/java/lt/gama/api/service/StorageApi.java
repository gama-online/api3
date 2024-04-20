package lt.gama.api.service;

import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.GetUploadUrlRequest;
import lt.gama.api.request.StartImportRequest;
import lt.gama.api.request.UploadDataRequest;
import lt.gama.api.response.UploadResponse;
import lt.gama.auth.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static lt.gama.api.service.Api.APP_API_3_PATH;

/**
 * Google Cloud/Blob Storage API interface
 *
 * @author valdas
 *
 */
@RequestMapping(APP_API_3_PATH + "storage")
@RequiresPermissions
public interface StorageApi extends Api {

    @PostMapping("/getUploadUrl")
    APIResult<UploadResponse> getUploadUrl(GetUploadUrlRequest request) throws GamaApiException;

    @PostMapping("/startImport")
	APIResult<String> startImport(StartImportRequest request) throws GamaApiException;

    @PostMapping("/upload")
    APIResult<String> upload(UploadDataRequest request) throws GamaApiException;
}

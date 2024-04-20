package lt.gama.api.service;

import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.DocumentRequest;
import lt.gama.api.request.HtmlTemplateRequest;
import lt.gama.api.request.IdRequest;
import lt.gama.api.request.TaskStatusRequest;
import lt.gama.api.response.TaskResponse;
import lt.gama.auth.annotation.RequiresPermissions;
import lt.gama.model.i.base.IBaseDocument;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static lt.gama.api.service.Api.APP_API_3_PATH;

/**
 * Gama
 * Created by valdas on 15-07-31.
 */
@RequestMapping(APP_API_3_PATH + "document")
@RequiresPermissions
public interface DocumentApi extends Api {

    @PostMapping("/get")
    APIResult<IBaseDocument> get(DocumentRequest request) throws GamaApiException;

    @PostMapping("/getByDE")
    APIResult<IBaseDocument> getByDE(IdRequest request) throws GamaApiException;

    @PostMapping("/getTaskStatus")
    APIResult<TaskResponse<Object>> getTaskStatus(TaskStatusRequest request) throws GamaApiException;

    @PostMapping("/getHtmlTemplate")
    APIResult<String> getHtmlTemplate(HtmlTemplateRequest request) throws GamaApiException;
}

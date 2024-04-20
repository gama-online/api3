package lt.gama.api.service;

import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.IdRequest;
import lt.gama.api.request.LabelsRequest;
import lt.gama.api.request.PageRequest;
import lt.gama.api.request.UpdateLabelsRequest;
import lt.gama.api.response.PageResponse;
import lt.gama.auth.annotation.RequiresPermissions;
import lt.gama.model.dto.entities.LabelDto;
import lt.gama.model.i.base.IBaseCompany;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

import static lt.gama.api.service.Api.APP_API_3_PATH;

/**
 * gama-online
 * Created by valdas on 2016-03-10.
 */
@RequestMapping(APP_API_3_PATH + "label")
@RequiresPermissions
public interface LabelApi extends Api {

    @PostMapping("/listLabel")
    APIResult<PageResponse<LabelDto, Void>> listLabel(PageRequest request) throws GamaApiException;

    @PostMapping("/saveLabel")
    APIResult<LabelDto> saveLabel(LabelDto request) throws GamaApiException;

    @PostMapping("/saveLabelsList")
    APIResult<List<LabelDto>> saveLabelsList(LabelsRequest request) throws GamaApiException;

    @PostMapping("/getLabel")
    APIResult<LabelDto> getLabel(IdRequest request) throws GamaApiException;

    @PostMapping("/deleteLabel")
    APIResult<Void> deleteLabel(IdRequest request) throws GamaApiException;

    @PostMapping("/updateLabels")
    APIResult<IBaseCompany> updateLabels(UpdateLabelsRequest request) throws GamaApiException;
}

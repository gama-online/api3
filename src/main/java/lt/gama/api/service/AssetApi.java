package lt.gama.api.service;

import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.GenerateDERequest;
import lt.gama.api.request.IdRequest;
import lt.gama.api.request.PageRequest;
import lt.gama.api.response.PageResponse;
import lt.gama.auth.annotation.RequiresPermissions;
import lt.gama.model.dto.documents.DoubleEntryDto;
import lt.gama.model.dto.entities.AssetDto;
import lt.gama.model.dto.type.AssetTotal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static lt.gama.api.service.Api.APP_API_3_PATH;

/**
 * gama-online
 * Created by valdas on 2015-10-30.
 */
@RequestMapping(APP_API_3_PATH + "asset")
@RequiresPermissions
public interface AssetApi extends Api {

    @PostMapping("/listAsset")
    APIResult<PageResponse<AssetDto, AssetTotal>> listAsset(PageRequest request) throws GamaApiException;

    @PostMapping("/saveAsset")
    APIResult<AssetDto> saveAsset(AssetDto request) throws GamaApiException;

    @PostMapping("/deleteAsset")
    APIResult<Void> deleteAsset(IdRequest request) throws GamaApiException;

    @PostMapping("/getAsset")
    APIResult<AssetDto> getAsset(IdRequest request) throws GamaApiException;

    @PostMapping("/periodListAsset")
    APIResult<PageResponse<AssetDto, AssetTotal>> periodListAsset(PageRequest request) throws GamaApiException;

    /**
     * Recalculate depreciation
     */
    @PostMapping("/reset")
    APIResult<AssetDto> reset(AssetDto request) throws GamaApiException;

    /**
     * generate G.L. double-entry record
     */
    @PostMapping("/generateDE")
    APIResult<DoubleEntryDto> generateDE(GenerateDERequest request) throws GamaApiException;

}

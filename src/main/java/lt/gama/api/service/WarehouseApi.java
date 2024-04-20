package lt.gama.api.service;

import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.IdRequest;
import lt.gama.api.request.PageRequest;
import lt.gama.api.response.PageResponse;
import lt.gama.auth.annotation.RequiresPermissions;
import lt.gama.model.dto.entities.WarehouseDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static lt.gama.api.service.Api.APP_API_3_PATH;

/**
 * Gama
 * Created by valdas on 15-04-07.
 */
@RequestMapping(APP_API_3_PATH + "warehouse")
@RequiresPermissions
public interface WarehouseApi extends Api {

    @PostMapping("/listWarehouse")
    APIResult<PageResponse<WarehouseDto, Void>> listWarehouse(PageRequest request) throws GamaApiException;

    @PostMapping("/saveWarehouse")
    APIResult<WarehouseDto> saveWarehouse(WarehouseDto request) throws GamaApiException;

    @PostMapping("/getWarehouse")
    APIResult<WarehouseDto> getWarehouse(IdRequest request) throws GamaApiException;
}

package lt.gama.api.service.v4;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.response.PageResponse;
import lt.gama.auth.annotation.RequiresPermissions;
import lt.gama.model.dto.entities.WarehouseDto;
import lt.gama.model.type.enums.Permission;
import org.springframework.web.bind.annotation.*;

import static lt.gama.api.service.v4.Api4.API_4_PATH;

@RequestMapping(API_4_PATH + "warehouses")
@Tag(name = "warehouses")
public interface WarehouseApi extends Api4 {

    class PageResponseWarehouse extends PageResponse<WarehouseDto, Void> {}

    @GetMapping
    @Operation(operationId = "warehouseList", responses = @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = PageResponseWarehouse.class))))
    @RequiresPermissions(Permission.DOCUMENT_R)
    PageResponse<WarehouseDto, Void> list(
            @RequestParam("cursor") Integer cursor,
            @RequestParam(name = "limit", defaultValue = "10") int pageSize) throws GamaApiException;

    @GetMapping("{id}")
    @Operation(operationId = "warehouseGet")
    @RequiresPermissions(Permission.DOCUMENT_R)
    WarehouseDto get(@PathVariable("id") long id) throws GamaApiException;

    @PostMapping
    @Operation(operationId = "warehouseCreate")
    @RequiresPermissions(Permission.DOCUMENT_M)
    WarehouseDto create(WarehouseDto warehouseDto) throws GamaApiException;

    @PutMapping
    @Operation(operationId = "warehouseUpdate")
    @RequiresPermissions(Permission.DOCUMENT_M)
    WarehouseDto update(WarehouseDto warehouseDto) throws GamaApiException;
}

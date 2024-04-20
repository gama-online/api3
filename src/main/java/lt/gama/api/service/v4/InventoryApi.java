package lt.gama.api.service.v4;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.response.PageResponse;
import lt.gama.auth.annotation.RequiresPermissions;
import lt.gama.model.dto.documents.InventoryApiDto;
import lt.gama.model.type.enums.Permission;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static lt.gama.api.service.v4.Api4.API_4_PATH;

@RequestMapping(API_4_PATH + "inventories")
@Tag(name = "inventories")
public interface InventoryApi extends Api4 {

    class PageResponseInventory extends PageResponse<InventoryApiDto, Void> {}

    @GetMapping
    @Operation(operationId = "inventoryList", responses = @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = PageResponseInventory.class))))
    @RequiresPermissions(Permission.DOCUMENT_R)
    PageResponse<InventoryApiDto, Void> list(
            @RequestParam("dateFrom") LocalDate dateFrom,
            @RequestParam("dateTo") LocalDate dateTo,
            @RequestParam("cursor") Integer cursor,
            @RequestParam(name = "limit", defaultValue = "10") int pageSize) throws GamaApiException;

    @GetMapping("{id}")
    @Operation(operationId = "inventoryGet")
    @RequiresPermissions(Permission.DOCUMENT_R)
    InventoryApiDto get(@PathVariable("id") long id) throws GamaApiException;

    @PostMapping
    @Operation(operationId = "inventoryCreate")
    @RequiresPermissions(Permission.DOCUMENT_M)
    InventoryApiDto create(InventoryApiDto documentDto) throws GamaApiException;

    @PutMapping
    @Operation(operationId = "inventoryUpdate")
    @RequiresPermissions(Permission.DOCUMENT_M)
    InventoryApiDto update(InventoryApiDto documentDto) throws GamaApiException;

    @DeleteMapping("{id}")
    @Operation(operationId = "inventoryDelete")
    @RequiresPermissions(Permission.DOCUMENT_M)
    void delete(@PathVariable("id") long id) throws GamaApiException;
}

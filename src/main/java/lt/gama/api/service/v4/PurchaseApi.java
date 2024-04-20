package lt.gama.api.service.v4;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.response.PageResponse;
import lt.gama.auth.annotation.RequiresPermissions;
import lt.gama.model.dto.documents.PurchaseApiDto;
import lt.gama.model.type.enums.Permission;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static lt.gama.api.service.v4.Api4.API_4_PATH;

@RequestMapping(API_4_PATH + "purchases")
@Tag(name = "purchases")
public interface PurchaseApi extends Api4 {

    class PageResponsePurchase extends PageResponse<PurchaseApiDto, Void> {}

    @GetMapping
    @Operation(operationId = "purchaseList", responses = @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = PageResponsePurchase.class))))
    @RequiresPermissions(Permission.DOCUMENT_R)
    PageResponse<PurchaseApiDto, Void> list(
            @RequestParam("dateFrom") LocalDate dateFrom,
            @RequestParam("dateTo") LocalDate dateTo,
            @RequestParam("cursor") Integer cursor,
            @RequestParam(name = "limit", defaultValue = "10") int pageSize) throws GamaApiException;

    @GetMapping("{id}")
    @Operation(operationId = "purchaseGet")
    @RequiresPermissions(Permission.DOCUMENT_R)
    PurchaseApiDto get(@PathVariable("id") long id) throws GamaApiException;

    @PostMapping
    @Operation(operationId = "purchaseCreate")
    @RequiresPermissions(Permission.DOCUMENT_M)
    PurchaseApiDto create(PurchaseApiDto documentDto) throws GamaApiException;

    @PutMapping
    @Operation(operationId = "purchaseUpdate")
    @RequiresPermissions(Permission.DOCUMENT_M)
    PurchaseApiDto update(PurchaseApiDto documentDto) throws GamaApiException;

    @DeleteMapping("{id}")
    @Operation(operationId = "purchaseDelete")
    @RequiresPermissions(Permission.DOCUMENT_M)
    void delete(@PathVariable("id") long id) throws GamaApiException;
}

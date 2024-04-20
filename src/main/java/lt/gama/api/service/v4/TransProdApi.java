package lt.gama.api.service.v4;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.websocket.server.PathParam;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.response.PageResponse;
import lt.gama.auth.annotation.RequiresPermissions;
import lt.gama.model.dto.documents.TransProdApiDto;
import lt.gama.model.type.enums.Permission;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static lt.gama.api.service.v4.Api4.API_4_PATH;

@RequestMapping(API_4_PATH + "transProd")
@Tag(name = "transProd")
public interface TransProdApi extends Api4 {

    class PageResponseTransProd extends PageResponse<TransProdApiDto, Void> {}

    @GetMapping
    @Operation(operationId = "transProdList", responses = @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = PageResponseTransProd.class))))
    @RequiresPermissions(Permission.DOCUMENT_R)
    PageResponse<TransProdApiDto, Void> list(
            @RequestParam("dateFrom") LocalDate dateFrom,
            @RequestParam("dateTo") LocalDate dateTo,
            @RequestParam("cursor") Integer cursor,
            @RequestParam(name = "limit", defaultValue = "10") int pageSize) throws GamaApiException;

    @GetMapping("{id}")
    @Operation(operationId = "transProdGet")
    @RequiresPermissions(Permission.DOCUMENT_R)
    TransProdApiDto get(@PathParam("id") long id) throws GamaApiException;

    @PostMapping
    @Operation(operationId = "transProdCreate")
    @RequiresPermissions(Permission.DOCUMENT_M)
    TransProdApiDto create(TransProdApiDto documentDto) throws GamaApiException;

    @PutMapping
    @Operation(operationId = "transProdUpdate")
    @RequiresPermissions(Permission.DOCUMENT_M)
    TransProdApiDto update(TransProdApiDto documentDto) throws GamaApiException;

    @DeleteMapping("{id}")
    @Operation(operationId = "transProdDelete")
    @RequiresPermissions(Permission.DOCUMENT_M)
    void delete(@PathParam("id") long id) throws GamaApiException;
}

package lt.gama.api.service.v4;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.response.PageResponse;
import lt.gama.auth.annotation.RequiresPermissions;
import lt.gama.model.dto.entities.PartApiDto;
import lt.gama.model.type.enums.Permission;
import org.springframework.web.bind.annotation.*;

import static lt.gama.api.service.v4.Api4.API_4_PATH;

@RequestMapping(API_4_PATH + "parts")
@Tag(name = "parts")
public interface PartApi extends Api4 {

    class PageResponsePart extends PageResponse<PartApiDto, Void> {}

    @GetMapping
    @Operation(operationId = "partList", responses = @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = PageResponsePart.class))))
    @RequiresPermissions(Permission.PART_R)
    PageResponse<PartApiDto, Void> list(
            @RequestParam("cursor") Integer cursor,
            @RequestParam(name = "limit", defaultValue = "10") int pageSize) throws GamaApiException;

    @GetMapping("{id}")
    @Operation(operationId = "partGet")
    @RequiresPermissions(Permission.PART_R)
    PartApiDto get(@PathVariable("id") long id) throws GamaApiException;

    @GetMapping("findBy")
    @Operation(operationId = "partFindBy", responses = @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = PageResponsePart.class))),
            description = "Find product/service by SKU or/and start of any word in name, sku, barcode, G.L. accounts")
    @RequiresPermissions(Permission.PART_R)
    PageResponse<PartApiDto, Void> findBy(
            @RequestParam("sku") String sku,
            @RequestParam("filter") String filter,
            @RequestParam("cursor") Integer cursor,
            @RequestParam(name = "limit", defaultValue = "10") int pageSize) throws GamaApiException;

    @PostMapping
    @Operation(operationId = "partCreate")
    @RequiresPermissions(Permission.PART_M)
    PartApiDto create(PartApiDto part) throws GamaApiException;

    @PutMapping
    @Operation(operationId = "partUpdate")
    @RequiresPermissions(Permission.PART_M)
    PartApiDto update(PartApiDto partDto) throws GamaApiException;
}

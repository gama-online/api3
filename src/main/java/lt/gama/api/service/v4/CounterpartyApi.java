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
import lt.gama.model.dto.entities.CounterpartyDto;
import lt.gama.model.type.enums.Permission;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.web.bind.annotation.*;

import static lt.gama.api.service.v4.Api4.API_4_PATH;

@RequestMapping(API_4_PATH + "counterparties")
@Tag(name = "counterparties")
public interface CounterpartyApi extends Api4 {

    class PageResponseCounterparty extends PageResponse<CounterpartyDto, Void> {}

    @GetMapping
    @Operation(operationId = "counterpartyList", responses = @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = PageResponseCounterparty.class))))
    @RequiresPermissions(Permission.COUNTERPARTY_R)
    PageResponse<CounterpartyDto, Void> list(
            @RequestParam("cursor") Integer cursor,
            @RequestParam("limit") @DefaultValue("10") int pageSize) throws GamaApiException;

    @GetMapping("{id}")
    @Operation(operationId = "counterpartyGet")
    @RequiresPermissions(Permission.COUNTERPARTY_R)
    CounterpartyDto get(@PathParam("id") long id) throws GamaApiException;

    @GetMapping("findBy")
    @Operation(operationId = "counterpartyFindBy", responses = @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = PageResponseCounterparty.class))),
            description = "Find client/vendor by company/registration code or/and name")
    @RequiresPermissions(Permission.COUNTERPARTY_R)
    PageResponse<CounterpartyDto, Void> findBy(
            @RequestParam("code") String code,
            @RequestParam("name") String name,
            @RequestParam("cursor") Integer cursor,
            @RequestParam("limit") @DefaultValue("10") int pageSize) throws GamaApiException;

    @GetMapping("getByForeignId/{id}")
    @Operation(operationId = "counterpartyGetByForeignId")
    @RequiresPermissions(Permission.COUNTERPARTY_R)
    CounterpartyDto getByForeignId(@PathVariable("id") long foreignId) throws GamaApiException;

    @PostMapping
    @Operation(operationId = "counterpartyCreate")
    @RequiresPermissions(Permission.COUNTERPARTY_M)
    CounterpartyDto create(CounterpartyDto part) throws GamaApiException;

    @PutMapping
    @Operation(operationId = "counterpartyUpdate")
    @RequiresPermissions(Permission.COUNTERPARTY_M)
    CounterpartyDto update(CounterpartyDto partDto) throws GamaApiException;
}

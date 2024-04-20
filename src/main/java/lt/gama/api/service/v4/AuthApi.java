package lt.gama.api.service.v4;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.LoginRequest;
import lt.gama.api.response.ApiErrorResponse;
import lt.gama.api.response.ApiLoginResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static lt.gama.api.service.v4.Api4.API_4_PATH;

@RequestMapping(API_4_PATH + "auth")
@Tag(name = "auth")
public interface AuthApi extends Api4 {

    @PostMapping("login")
    @ApiResponse(responseCode = "200", description = "Logged in",
            content = @Content(schema = @Schema(implementation = ApiLoginResponse.class)))
    @ApiResponse(responseCode = "401", description = "Invalid credentials",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    ApiLoginResponse login(LoginRequest request) throws GamaApiException;

    @GetMapping("refresh")
    @ApiResponse(responseCode = "200", description = "New tokens generated",
            content = @Content(schema = @Schema(implementation = ApiLoginResponse.class)))
    @ApiResponse(responseCode = "401", description = "Invalid credentials",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    ApiLoginResponse refresh() throws GamaApiException;

    @GetMapping("logout")
    void logout();

}


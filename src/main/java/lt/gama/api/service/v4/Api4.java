package lt.gama.api.service.v4;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;


@OpenAPIDefinition(
        servers = {
                @Server(url = "https://gama-online.appspot.com/_ah/api"),
                @Server(url = "https://test-dot-gama-online.appspot.com/_ah/api",
                        description = "Production beta server (real data!!!)"),
                @Server(url = "http://localhost:8080/_ah/api", description = "Local test server")
        },
        info = @Info(
                title = "Gama-online",
                description = "Gama-online integration API",
                version = "4.0",
                contact = @Contact(
                        name = "Gama-online team",
                        email = "gama-online@e-servisas.lt",
                        url = "https://www.gama-online.lt/"
                )
        ),
        security = @SecurityRequirement(name = "accessToken")
)
@SecurityScheme(
        name = "accessToken",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "Paseto"
)
public interface Api4 {
        String API_4_PATH = "gama/v4.0/";
}

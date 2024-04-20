package lt.gama.api.impl;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lt.gama.Constants;
import lt.gama.api.APIResult;
import lt.gama.api.request.LoginRequest;
import lt.gama.api.response.LoginResponse;
import lt.gama.api.service.TestAuthApi;
import lt.gama.auth.service.TokenService;
import lt.gama.helpers.Validators;
import lt.gama.model.sql.system.AccountSql;
import lt.gama.service.AccountService;
import lt.gama.service.AppPropService;
import lt.gama.service.TaskQueueService;
import lt.gama.tasks.UpdateLastLoginTimeTask;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
public class TestAuthApiImpl implements TestAuthApi {

    private final TokenService tokenService;
    private final AccountService accountService;
    private final AppPropService appPropService;
    private final TaskQueueService taskQueueService;

    public TestAuthApiImpl(TokenService tokenService, AccountService accountService, AppPropService appPropService, TaskQueueService taskQueueService) {
        this.tokenService = tokenService;
        this.accountService = accountService;
        this.appPropService = appPropService;
        this.taskQueueService = taskQueueService;
    }

    @Transactional
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        try {
            AccountSql account = Validators.checkNotNull(accountService.checkAccount(loginRequest.getName(), loginRequest.getPassword()), "No account");
            Integer companyIndex = loginRequest.getCompanyIndex() == null ? account.getCompanyIndex() : loginRequest.getCompanyIndex();
            if (companyIndex == null || companyIndex < 0 || companyIndex >= account.getCompanies().size())
                companyIndex = 0;
            LoginResponse loginResponse = accountService.login(account, companyIndex);

            Cookie cookie = new Cookie(Constants.TOKEN_COOKIE_NAME, loginResponse.getRefreshToken());
            cookie.setPath("/auth");
            cookie.setHttpOnly(true);
            cookie.setSecure(appPropService.isProduction());
            cookie.setMaxAge(tokenService.params().refreshTokenInSeconds());
            response.addCookie(cookie);

            taskQueueService.queueTask(new UpdateLastLoginTimeTask(account.getCompanies().get(companyIndex).getCompanyId(), account.getId()));

            loginResponse.setRefreshToken(null); // hide refresh token
            return ResponseEntity.ok(loginResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(APIResult.Error("Unauthorized", HttpStatus.UNAUTHORIZED.value()));
        }
    }
}

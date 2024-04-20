package lt.gama.api.impl.v4;

import lt.gama.api.ex.GamaApiException;
import lt.gama.api.ex.GamaApiUnauthorizedException;
import lt.gama.api.request.LoginRequest;
import lt.gama.api.response.ApiLoginResponse;
import lt.gama.api.service.v4.AuthApi;
import lt.gama.auth.i.IAuth;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.StringHelper;
import lt.gama.helpers.Validators;
import lt.gama.model.sql.system.AccountSql;
import lt.gama.service.AccountService;
import lt.gama.service.DBServiceSQL;
import lt.gama.service.TranslationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

import java.lang.invoke.MethodHandles;

@RestController("AuthApiImplv4")
public class AuthApiImpl implements AuthApi {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    private final AccountService accountService;
    private final Auth auth;
    private final DBServiceSQL dbServiceSQL;

    public AuthApiImpl(AccountService accountService, Auth auth, DBServiceSQL dbServiceSQL) {
        this.accountService = accountService;
        this.auth = auth;
        this.dbServiceSQL = dbServiceSQL;
    }


    @Override
    public ApiLoginResponse login(LoginRequest request) throws GamaApiException {
        try {
            Validators.checkNotNull(request.getName());
            Validators.checkNotNull(request.getPassword());

            return accountService.apiLogin(request);

        } catch (Exception e) {
            log.warn(request.getName() + ": " + e);
            throw new GamaApiUnauthorizedException(TranslationService.getInstance().translate(TranslationService.DB.WrongLogin, auth.getLanguage()));
        }
    }

    @Override
    public ApiLoginResponse refresh() throws GamaApiException {
        try {
            return accountService.apiRefresh();

        } catch (Exception e) {
            throw new GamaApiUnauthorizedException(TranslationService.getInstance().translate(TranslationService.DB.Unauthorized, auth.getLanguage()));
        }
    }

    @Override
    public void logout() {
        IAuth authRefresh = auth;
        if (authRefresh == null || StringHelper.isEmpty(authRefresh.getId())) {
            return;
        }
        dbServiceSQL.executeInTransaction(entityManager -> {
            AccountSql account = accountService.findAccount(authRefresh.getId());
            if (account == null || StringHelper.isEmpty(account.getRefreshToken())) {
                return;
            }
            account.setRefreshToken(null);
            account.setRefreshTokenDate(null);
        });
    }
}

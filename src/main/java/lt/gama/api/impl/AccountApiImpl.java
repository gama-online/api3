package lt.gama.api.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.PasswordRequest;
import lt.gama.api.response.CompanyResponse;
import lt.gama.api.response.LoginResponse;
import lt.gama.api.service.AccountApi;
import lt.gama.auth.i.IAuth;
import lt.gama.auth.impl.Auth;
import lt.gama.auth.service.AESCipherService;
import lt.gama.auth.service.TokenService;
import lt.gama.helpers.BooleanUtils;
import lt.gama.helpers.Validators;
import lt.gama.model.sql.system.AccountSql;
import lt.gama.model.sql.system.CompanySql;
import lt.gama.service.APIResultService;
import lt.gama.service.AccountService;
import lt.gama.service.DBServiceSQL;
import lt.gama.service.TranslationService;
import lt.gama.service.ex.rt.GamaUnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountApiImpl implements AccountApi {

    private static final Logger log = LoggerFactory.getLogger(AccountApi.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final AccountService accountService;
    private final Auth auth;
    private final DBServiceSQL dbServiceSQL;
    private final APIResultService apiResultService;
    private final TokenService tokenService;
    private final AESCipherService aesCipherService;

    public AccountApiImpl(AccountService accountService, Auth auth, DBServiceSQL dbServiceSQL, APIResultService apiResultService, TokenService tokenService, AESCipherService aesCipherService) {
        this.accountService = accountService;
        this.auth = auth;
        this.dbServiceSQL = dbServiceSQL;
        this.apiResultService = apiResultService;
        this.tokenService = tokenService;
        this.aesCipherService = aesCipherService;
    }

    @Override
    public APIResult<LoginResponse> refreshAccess(RefreshAccessRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            try {
                IAuth auth = Validators.checkNotNull(tokenService.getAuthentication(request.refreshToken), "Wrong token");
                AccountSql account = accountService.findAccountByRefreshToken(auth.getUuid());
                return accountService.login(account, request.companyIndex);

            } catch (Exception e) {
                throw new GamaUnauthorizedException(e);
            }
        });
    }

    @Override
    public APIResult<Void> forgotPassword(ForgotPasswordRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            accountService.resetPassword(request.login);
        });
    }

    @Override
    public APIResult<LoginResponse> resetPassword(ResetPasswordRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
                    AccountSql account = accountService.findAccountByResetToken(request.resetToken);
                    if (account == null || account.getCompanies() == null || account.getCompanies().size() == 0) {
                        if (account != null) { // wrong account - without companies
                            entityManager.remove(account);
                        }
                        throw new GamaUnauthorizedException(TranslationService.getInstance().translate(TranslationService.DB.WrongToken, auth.getLanguage()));
                    }
                    return accountService.login(account, null, true);
                }));
    }

    @Override
    public APIResult<String> createPassword(PasswordRequest loginParam) throws GamaApiException {
        return apiResultService.result(() -> {
            accountService.createPassword(auth.getId(), loginParam.getPassword());
            return "OK";
        });
    }

    @Override
    public APIResult<LoginResponse> changeCompany(ChangeCompanyRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            try {
                return accountService.changeCompany(request.companyIndex);

            } catch (Exception e) {
                throw new GamaUnauthorizedException(e);
            }
        });
    }

    @Override
    public APIResult<LoginResponse> setDefaultCompany(SetDefaultCompanyRequest request) throws GamaApiException {
        return apiResultService.result(() -> dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            try {
                AccountSql account = accountService.setDefaultCompany(auth.getId(), request.companyIndex);
                // do not generate refresh token if impersonated
                return accountService.login(account, request.companyIndex, !BooleanUtils.isTrue(auth.getImpersonated()));

            } catch (Exception e) {
                throw new GamaUnauthorizedException(e);
            }
        }));
    }

    @Override
    public APIResult<CompanyResponse> getCompany(GetCompanyRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            try {
                Validators.checkArgument(request.companyIndex >= 0, "Invalid company index (#400)");
                AccountSql account = Validators.checkNotNull(dbServiceSQL.getById(AccountSql.class, auth.getId()),
                        "No account exists for " + auth.getId());
                Validators.checkArgument(account.getCompanies() != null && request.companyIndex < account.getCompanies().size(),
                        "Invalid company index");
                long companyId = account.getCompanies().get(request.companyIndex).getCompanyId();
                CompanySql company = dbServiceSQL.getById(CompanySql.class, companyId);
                return new CompanyResponse(company);

            } catch (Exception e) {
                throw new GamaUnauthorizedException(e);
            }
        });
    }

    @Override
    public APIResult<String> signParams(SignParamsRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            try {
                return aesCipherService.sign(request.params);
            } catch (Exception e) {
                throw new GamaUnauthorizedException(e);
            }
        });
    }
}

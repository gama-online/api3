package lt.gama.api.impl.maintenance;

import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.PageRequest;
import lt.gama.api.request.PageRequestCondition;
import lt.gama.api.response.PageResponse;
import lt.gama.api.service.maintenance.FixMoneyApi;
import lt.gama.auth.impl.Auth;
import lt.gama.model.dto.entities.MoneyHistoryDto;
import lt.gama.model.type.enums.CustomSearchType;
import lt.gama.report.RepMoneyDetail;
import lt.gama.service.APIResultService;
import lt.gama.service.AuthSettingsCacheService;
import lt.gama.service.MoneyAccountService;
import lt.gama.service.ex.GamaServerErrorException;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

/**
 * gama-online
 * Created by valdas on 2016-03-21.
 */
@RestController
public class FixMoneyApiImpl implements FixMoneyApi {

    private final MoneyAccountService moneyAccountService;
    private final Auth auth;
    private final AuthSettingsCacheService authSettingsCacheService;
    private final APIResultService apiResultService;

    public FixMoneyApiImpl(MoneyAccountService moneyAccountService, Auth auth, AuthSettingsCacheService authSettingsCacheService, APIResultService apiResultService) {
        this.moneyAccountService = moneyAccountService;
        this.auth = auth;
        this.authSettingsCacheService = authSettingsCacheService;
        this.apiResultService = apiResultService;
    }


    @Override
    public APIResult<PageResponse<MoneyHistoryDto, RepMoneyDetail<Object>>>
    retrieveMoneyHistory(RetrieveMoneyHistoryRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            auth.setCompanyId(request.companyId);
            auth.setSettings(authSettingsCacheService.get(auth.getCompanyId()));

            PageRequest pageRequest = new PageRequest();
            pageRequest.setDateRange(true);
            pageRequest.setDateFrom(request.dateFrom);
            pageRequest.setDateTo(request.dateTo);
            pageRequest.setOrder("mainIndex");
            pageRequest.setPageSize(request.pageSize);
            pageRequest.setConditions(new ArrayList<>());
            pageRequest.getConditions().add(new PageRequestCondition(CustomSearchType.ORIGIN_ID, request.accountId));
            pageRequest.getConditions().add(new PageRequestCondition(CustomSearchType.CURRENCY, request.currency));
            return moneyAccountService.reportFlow(pageRequest, request.type);
        });
    }

    @Override
    public APIResult<Void> fixMoneyAccount(FixMoneyAccountRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            throw new GamaServerErrorException("Not implemented");
        });
    }
}

package lt.gama.api.impl.maintenance;

import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.service.maintenance.FixDebtApi;
import lt.gama.auth.impl.Auth;
import lt.gama.model.dto.entities.DebtNowDto;
import lt.gama.service.APIResultService;
import lt.gama.service.AuthSettingsCacheService;
import lt.gama.service.DebtService;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class FixDebtApiImpl implements FixDebtApi {

    private final Auth auth;
    private final AuthSettingsCacheService authSettingsCacheService;
    private final APIResultService apiResultService;
    private final DebtService debtService;

    public FixDebtApiImpl(Auth auth, AuthSettingsCacheService authSettingsCacheService, APIResultService apiResultService, DebtService debtService) {
        this.auth = auth;
        this.authSettingsCacheService = authSettingsCacheService;
        this.apiResultService = apiResultService;
        this.debtService = debtService;
    }


    @Override
    public APIResult<DebtNowDto> createDebtNowFromDoc(CreateDebtNowFromDocRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            auth.setCompanyId(request.companyId);
            auth.setSettings(authSettingsCacheService.get(auth.getCompanyId()));

            return debtService.createDebtNowFromDoc(request.docId);
        });
    }

    @Override
    public APIResult<Map<String, Integer>> rebuildDebtCoveragesForCounterparty(RebuildDebtCoveragesForCounterpartyRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            auth.setCompanyId(request.companyId);
            auth.setSettings(authSettingsCacheService.get(auth.getCompanyId()));

            return debtService.rebuildDebtCoveragesForCounterparty(request.counterpartyId, request.debtType);
        });
    }
}

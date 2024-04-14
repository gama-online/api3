package lt.gama.api;

import jakarta.annotation.security.PermitAll;
import lt.gama.api.ex.GamaApiBadRequestException;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.ex.GamaApiServerErrorException;
import lt.gama.api.request.PageRequest;
import lt.gama.model.dto.entities.CompanyDto;
import lt.gama.model.sql.system.CompanySql;
import lt.gama.service.APIResultService;
import lt.gama.service.ex.rt.GamaException;
import lt.gama.service.repo.CompanyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import static lt.gama.api.IApi.APP_API_PATH;

@RestController
@RequestMapping(APP_API_PATH + "admin")
public class AdminApi {

    private final APIResultService apiResultService;
    private final CompanyRepository companyRepository;

    public AdminApi(APIResultService apiResultService, CompanyRepository companyRepository) {
        this.apiResultService = apiResultService;
        this.companyRepository = companyRepository;
    }

    @GetMapping("/testBadRequest")
    @PermitAll
    public APIResult<String> testBadRequest() throws GamaApiException {
        return apiResultService.result(() -> {
            throw new GamaApiBadRequestException("Testing Bad request exception");
        });
    }

    @GetMapping("/testServerError")
    @PermitAll
    public APIResult<String> testServerError() throws GamaApiException {
        return apiResultService.result(() -> {
            throw new GamaApiServerErrorException("Testing Server error exception", new GamaException("Some server error"));
        });
    }

    @GetMapping("/version")
    @PermitAll
    public APIResult<String> getVersion() throws GamaApiException {
        return apiResultService.result(() -> "123"); // AppPropService.prop().getProperty(AppPropService.Prop.GAMA_VERSION));
    }

    @GetMapping("/company")
    public Page<CompanySql> companyList(PageRequest request) throws GamaApiException {
        return companyRepository.findAll(org.springframework.data.domain.PageRequest.of(0, 10));
    }
}

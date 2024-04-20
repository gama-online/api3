package lt.gama.api.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.IdRequest;
import lt.gama.auth.annotation.RequiresPermissions;
import lt.gama.service.sync.openCart.model.OCImportStep;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static lt.gama.api.service.Api.APP_API_3_PATH;

/**
 * gama-online
 * Created by valdas on 2019-03-02.
 */
@RequestMapping(APP_API_3_PATH + "oca")
@RequiresPermissions
public interface OCApi extends Api {

    @PostMapping("/adminUploadProduct")
    APIResult<OCImportStep> adminUploadProduct(AdminUploadProductRequest request) throws GamaApiException;

    class AdminUploadProductRequest {
        @JsonProperty("company") public long companyId;
        public long partId;

        // generated

        public AdminUploadProductRequest(long companyId, long partId) {
            this.companyId = companyId;
            this.partId = partId;
        }
    }

    @PostMapping("/adminUploadProducts")
    APIResult<String> adminUploadProducts(AdminUploadProductsRequest request) throws GamaApiException;

    class AdminUploadProductsRequest {
        @JsonProperty("company") public long companyId;
    }

    @PostMapping("/adminAddCustomer")
    APIResult<String> adminAddCustomer(AdminAddCustomerRequest request) throws GamaApiException;

    class AdminAddCustomerRequest {
        @JsonProperty("company") public long companyId;
        public long customerId;
    }

    @PostMapping("/uploadCustomer")
    APIResult<String> uploadCustomer(IdRequest request) throws GamaApiException;
}

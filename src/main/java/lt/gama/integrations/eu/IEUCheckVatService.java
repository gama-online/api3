package lt.gama.integrations.eu;

import lt.gama.api.request.CheckVatRequest;
import lt.gama.api.response.CheckVatResponse;

public interface IEUCheckVatService {

    CheckVatResponse checkVat(CheckVatRequest request);
}

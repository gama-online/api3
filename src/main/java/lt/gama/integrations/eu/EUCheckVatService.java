package lt.gama.integrations.eu;

import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Holder;
import jakarta.xml.ws.handler.Handler;
import lt.gama.api.request.CheckVatRequest;
import lt.gama.api.response.CheckVatResponse;
import lt.gama.helpers.BooleanUtils;
import lt.gama.integrations.SOAPLogHandler;
import lt.gama.integrations.eu.ws.CheckVatPortType;
import lt.gama.integrations.eu.ws.CheckVatService;
import lt.gama.service.AppPropService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.List;

@Service
public class EUCheckVatService implements IEUCheckVatService {

    private static final Logger log = LoggerFactory.getLogger(EUCheckVatService.class);

    private static final ThreadLocal<CheckVatPortType> ports = new ThreadLocal<>();

    private final AppPropService appPropService;

    public EUCheckVatService(AppPropService appPropService) {
        this.appPropService = appPropService;
    }

    private CheckVatPortType getService() {
        CheckVatPortType port = ports.get();
        if (port == null) {
            CheckVatService service = new CheckVatService();
            port = service.getPort(CheckVatPortType.class);

            final BindingProvider bindingProvider = (BindingProvider) port;
            if (appPropService.isDevelopment()) {
                bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, EUCheckVatConst.TEST_URL);
            } else {
                bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, EUCheckVatConst.PROD_URL);
            }

            @SuppressWarnings("rawtypes") List<Handler> chain = bindingProvider.getBinding().getHandlerChain();
            chain.add(new SOAPLogHandler());
            bindingProvider.getBinding().setHandlerChain(chain);

            ports.set(port);
        }
        return port;
    }

    @Override
    public CheckVatResponse checkVat(CheckVatRequest request) {
        CheckVatPortType port = getService();

        final Holder<XMLGregorianCalendar> requestDate = new Holder<>();
        final Holder<Boolean> valid = new Holder<>();
        final Holder<String> name = new Holder<>();
        final Holder<String> address = new Holder<>();

        try {
            port.checkVat(new Holder<>(request.getCountryCode()), new Holder<>(request.getVatNumber()), requestDate, valid, name, address);

            return new CheckVatResponse(request.getCountryCode(), request.getVatNumber(),
                    BooleanUtils.isTrue(valid.value), name.value, address.value);
        } catch (Exception e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
            return CheckVatResponse.error(e.getMessage());
        }
    }
}

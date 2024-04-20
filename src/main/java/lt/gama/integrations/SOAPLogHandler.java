package lt.gama.integrations;

import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Set;

public class SOAPLogHandler implements SOAPHandler<SOAPMessageContext> {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Override
    public Set<QName> getHeaders() {
        return null;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        log.info("SOAP.handleMessage");
        Boolean isRequest = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if (isRequest) {
            SOAPMessage soapMsg = context.getMessage();
            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                soapMsg.writeTo(os);
                log.info(os.toString());
            } catch (SOAPException | IOException e) {
                log.error(e.toString(), e);
            }
        }
        return true;
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        log.error("SOAP.handleFault");
        SOAPMessage soapMsg = context.getMessage();
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            soapMsg.writeTo(os);
            log.error(os.toString());
        } catch (SOAPException | IOException e) {
            log.error(e.toString(), e);
        }
        return true;
    }

    @Override
    public void close(MessageContext context) {
        log.info("SOAP.close");
    }
}

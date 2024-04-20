package lt.gama.service;

//import com.google.api.client.http.*;

import com.google.common.primitives.Bytes;
import lt.gama.api.request.MailRequestContact;
import lt.gama.helpers.StringHelper;
import net.htmlparser.jericho.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;

import static java.time.temporal.ChronoUnit.SECONDS;


@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);


    private static final String MAILGUN_API_KEY = System.getProperty("mailgun.api.key");
    private static final String MAILGUN_DOMAIN = System.getProperty("mailgun.domain");

    private final Environment environment;

    public MailService(Environment environment) {
        this.environment = environment;
    }

    public void sendMail(String senderAddress, String senderName, String recipientAddress, String recipientName,
						 String subject, String msgBody, String htmlMsgBody,
                         byte[] attachment, String attachmentName, String cc) {

        List<MailRequestContact> recipients = List.of(new MailRequestContact(recipientAddress, recipientName));
        sendMails(senderAddress, senderName, recipients, subject, msgBody, htmlMsgBody, attachment, attachmentName, cc);
    }

    private String makeAddress(String name, String email) {
        return (name != null ? name.replace(',', ' ').replace('<', ' ').replace('>', ' ') + " " : "") + "<" + email + ">";
    }

    private String makeAddress(MailRequestContact address) {
        return makeAddress(address.getName(), address.getEmail());
    }

    private void testSendMails(String senderAddress, String senderName, Collection<MailRequestContact> recipients,
                               String subject, String msgBody, String htmlMsgBody, String bcc) {
        log.info("Sending test email" +
                "from: " + senderName + " <" + senderAddress + ">" +
                ", to: " + recipients +
                ", bcc: " + (bcc == null ? "" : bcc) +
                ", subject: " + subject +
                ", msgBody: " + msgBody +
                ", htmlMsgBody: " + htmlMsgBody);
    }

    /**
     * If msgBody and htmlMsgBody is empty then use subject as message text body
     */
	public void sendMails(String senderAddress, String senderName, Collection<MailRequestContact> recipients,
						  String subject, String msgBody, String htmlMsgBody,
                          byte[] attachment, String attachmentName,
                          String bcc) {

        if (StringHelper.isEmpty(msgBody) && StringHelper.hasValue(htmlMsgBody)) {
            Source source = new Source(htmlMsgBody);
            msgBody = source.getRenderer().toString();
        } else if (StringHelper.isEmpty(msgBody) && StringHelper.isEmpty(htmlMsgBody)) {
            msgBody = subject;
        }

        // check if unit-test?
        if (environment.matchesProfiles("!prod & !dev")) {
            testSendMails(senderAddress, senderName, recipients, subject, msgBody, htmlMsgBody, bcc);
            return;
        }

        List<String> toList = new ArrayList<>();
        List<String> ccList = new ArrayList<>();
        List<String> bccList = new ArrayList<>();

        for (MailRequestContact address : recipients) {
            String email = makeAddress(address);
            if ("CC".equalsIgnoreCase(address.getType())) ccList.add(email);
            else if ("BCC".equalsIgnoreCase(address.getType())) bccList.add(email);
            else toList.add(email);
        }

        if (toList.isEmpty()) {
            log.error("Recipients list is empty");
            return;
        }

        try {
            URI uri = new URI("https://api.mailgun.net/v3/" + MAILGUN_DOMAIN + "/messages");

            MultiPartBodyPublisher publisher = new MultiPartBodyPublisher();
            publisher.addPart("from", senderAddress);
            toList.forEach(e -> publisher.addPart("to", e));
            ccList.forEach(e -> publisher.addPart("cc", e));
            bccList.forEach(e -> publisher.addPart("bcc", e));
            publisher.addPart("subject", subject);

            if (StringHelper.hasValue(msgBody)) publisher.addPart("text", msgBody);
            if (StringHelper.hasValue(htmlMsgBody)) publisher.addPart("html", htmlMsgBody);
            if (attachment != null && attachment.length > 0) {
                publisher.addPart("attachment", attachment, attachmentName, "application/pdf; name=\"" + attachmentName + "\"");
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", "multipart/form-data; boundary=" + publisher.getBoundary())
                    .header("User-Agent", "Mozilla/5.0 GamaOnline/1.0")
                    .header("Authorization", "Basic " + Base64.getEncoder().encodeToString(("api:" + MAILGUN_API_KEY).getBytes()))
                    .timeout(Duration.of(60, SECONDS))
                    .POST(publisher.build())
                    .build();

            try (HttpClient client = HttpClient.newHttpClient()) {

                var response = client.send(request, HttpResponse.BodyHandlers.ofString());

                String msg = "email from: " + senderName + " <" + senderAddress + ">" +
                        " to: " + recipients + ", bcc: " + (bcc == null ? "" : bcc) + ", subject: " + subject +
                        ", status: " + response.statusCode();

                if (response.statusCode() == 200) {
                    log.info(msg);
                } else {
                    log.error(msg);
                }
            }

        } catch (IOException | URISyntaxException | InterruptedException e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    static class MultiPartBodyPublisher {
        private final List<PartsSpecification> partsSpecificationList = new ArrayList<>();
        private final String boundary = UUID.randomUUID().toString();

        public HttpRequest.BodyPublisher build() {
            if (partsSpecificationList.isEmpty()) {
                throw new IllegalStateException("Must have at least one part to build multipart message.");
            }
            addFinalBoundaryPart();
            return HttpRequest.BodyPublishers.ofByteArrays(PartsIterator::new);
        }

        public String getBoundary() {
            return boundary;
        }

        public MultiPartBodyPublisher addPart(String name, String value) {
            PartsSpecification newPart = new PartsSpecification();
            newPart.type = PartsSpecification.TYPE.STRING;
            newPart.name = name;
            newPart.value = value;
            partsSpecificationList.add(newPart);
            return this;
        }

        public MultiPartBodyPublisher addPart(String name, Supplier<InputStream> value, String filename, String contentType) {
            PartsSpecification newPart = new PartsSpecification();
            newPart.type = PartsSpecification.TYPE.STREAM;
            newPart.name = name;
            newPart.stream = value;
            newPart.filename = filename;
            newPart.contentType = contentType;
            partsSpecificationList.add(newPart);
            return this;
        }

        public MultiPartBodyPublisher addPart(String name, byte[] value, String filename, String contentType) {
            PartsSpecification newPart = new PartsSpecification();
            newPart.type = PartsSpecification.TYPE.BYTES;
            newPart.name = name;
            newPart.bytes = value;
            newPart.filename = filename;
            newPart.contentType = contentType;
            partsSpecificationList.add(newPart);
            return this;
        }
        
        private void addFinalBoundaryPart() {
            PartsSpecification newPart = new PartsSpecification();
            newPart.type = PartsSpecification.TYPE.FINAL_BOUNDARY;
            newPart.value = "--" + boundary + "--";
            partsSpecificationList.add(newPart);
        }

        static class PartsSpecification {

            public enum TYPE {
                BYTES, STRING, FILE, STREAM, FINAL_BOUNDARY
            }

            TYPE type;
            String name;
            String value;
            Path path;
            Supplier<InputStream> stream;
            byte[] bytes;
            String filename;
            String contentType;

        }

        class PartsIterator implements Iterator<byte[]> {

            private final Iterator<PartsSpecification> iter;
            private InputStream currentFileInput;

            private boolean done;
            private byte[] next;

            PartsIterator() {
                iter = partsSpecificationList.iterator();
            }

            @Override
            public boolean hasNext() {
                if (done) return false;
                if (next != null) return true;
                try {
                    next = computeNext();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
                if (next == null) {
                    done = true;
                    return false;
                }
                return true;
            }

            @Override
            public byte[] next() {
                if (!hasNext()) throw new NoSuchElementException();
                byte[] res = next;
                next = null;
                return res;
            }

            private byte[] computeNext() throws IOException {
                if (currentFileInput == null) {
                    if (!iter.hasNext()) return null;
                    PartsSpecification nextPart = iter.next();
                    return switch (nextPart.type) {
                        case FINAL_BOUNDARY -> nextPart.value.getBytes(StandardCharsets.UTF_8);
                        case STRING -> {
                            String part = "--" + boundary + "\r\n" +
                                    "Content-Disposition: form-data; name=" + nextPart.name + "\r\n" +
                                    "Content-Type: text/plain; charset=UTF-8\r\n\r\n" +
                                    nextPart.value + "\r\n";
                            yield part.getBytes(StandardCharsets.UTF_8);
                        }
                        case FILE -> {
                            Path path = nextPart.path;
                            String filename = path.getFileName().toString();
                            String contentType = Files.probeContentType(path);
                            if (contentType == null) contentType = "application/octet-stream";
                            currentFileInput = Files.newInputStream(path);
                            String partHeader = "--" + boundary + "\r\n" +
                                    "Content-Disposition: form-data; name=" + nextPart.name + "; filename=\"" + filename + "\"\r\n" +
                                    "Content-Type: " + contentType + "\r\n\r\n";
                            yield partHeader.getBytes(StandardCharsets.UTF_8);
                        }
                        case STREAM -> {
                            String filename = nextPart.filename;
                            String contentType = nextPart.contentType;
                            if (contentType == null) contentType = "application/octet-stream";
                            currentFileInput = nextPart.stream.get();
                            String partHeader = "--" + boundary + "\r\n" +
                                    "Content-Disposition: form-data; name=" + nextPart.name + "; filename=\"" + filename + "\"\r\n" +
                                    "Content-Type: " + contentType + "\r\n\r\n";
                            yield partHeader.getBytes(StandardCharsets.UTF_8);
                        }
                        case BYTES -> {
                            String filename = nextPart.filename;
                            String contentType = nextPart.contentType;
                            if (contentType == null) contentType = "application/octet-stream";
                            String partHeader = "--" + boundary + "\r\n" +
                                    "Content-Disposition: form-data; name=" + nextPart.name + "; filename=\"" + filename + "\"\r\n" +
                                    "Content-Type: " + contentType + "\r\n\r\n";
                            yield Bytes.concat(partHeader.getBytes(StandardCharsets.UTF_8), nextPart.bytes, "\r\n".getBytes(StandardCharsets.UTF_8));
                        }
                    };
                } else {
                    byte[] buf = new byte[8192];
                    int r = currentFileInput.read(buf);
                    if (r > 0) {
                        byte[] actualBytes = new byte[r];
                        System.arraycopy(buf, 0, actualBytes, 0, r);
                        return actualBytes;
                    } else {
                        currentFileInput.close();
                        currentFileInput = null;
                        return "\r\n".getBytes(StandardCharsets.UTF_8);
                    }
                }
            }
        }
    }
}

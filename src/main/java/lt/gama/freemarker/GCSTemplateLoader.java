package lt.gama.freemarker;

import com.google.cloud.storage.Blob;
import freemarker.cache.TemplateLoader;
import lt.gama.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.lang.invoke.MethodHandles;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;

/**
 * Gama
 * Created by valdas on 15-07-10.
 */
public class GCSTemplateLoader implements TemplateLoader {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final StorageService storageService;


    public GCSTemplateLoader(StorageService storageService) {
        this.storageService = storageService;
    }

    @Override
    public Object findTemplateSource(String fileName) {
        try {
            Blob blob = storageService.getBlob(fileName);
            return blob != null && blob.exists() ? blob : null;
        } catch (Throwable t) {
            log.error(t.toString());
            return null;
        }
    }

    @Override
    public long getLastModified(Object templateSource) {
        if (!(templateSource instanceof Blob)) {
            log.error("Object is not Blob: " + templateSource);
            return -1;
        }
        try {
            Blob blob = (Blob)templateSource;
            if (!blob.exists()) {
                log.error("No template: " + blob.getName());
                return -1;
            }
            return blob.getUpdateTime();

        } catch (Exception e) {
            log.error(e.toString());
            return -1;
        }
    }

    @Override
    public Reader getReader(Object templateSource, String encoding) {
        if (!(templateSource instanceof Blob blob)) {
            log.error("Object is not Blob: " + templateSource);
            return null;
        }
        return Channels.newReader(blob.reader(), StandardCharsets.UTF_8);
    }

    @Override
    public void closeTemplateSource(Object templateSource) {
    }
}

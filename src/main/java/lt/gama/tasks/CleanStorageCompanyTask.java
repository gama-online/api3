package lt.gama.tasks;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import lt.gama.ConstWorkers;
import lt.gama.helpers.StringHelper;

import java.io.Serial;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class CleanStorageCompanyTask extends BaseDeferredTask {

    @Serial
    private static final long serialVersionUID = -1L;

    final private String folder;
    final private int days;

    public CleanStorageCompanyTask() {
        this(-1, null, 3);
    }

    public CleanStorageCompanyTask(long companyId, String folder, int days) {
        super(companyId);
        this.folder = folder;
        this.days = days <= 0 ? 3 : days;
    }

    @Override
    public void execute() {
        try {
            String prefix = (getCompanyId() >= 0 ? getCompanyId() + "/" : "") + (StringHelper.hasValue(folder) ? folder : ConstWorkers.IMPORT_FOLDER) + "/";

            int deleted = 0;
            int total = 0;

            Bucket bucket = storageService.defaultBucket();
            Page<Blob> blobs = bucket.list(
                    Storage.BlobListOption.prefix(prefix),
                    Storage.BlobListOption.currentDirectory());

            for (Blob blob : blobs.iterateAll()) {
                total++;
                if (blob.getUpdateTimeOffsetDateTime().toEpochSecond() < Instant.now().minus(days, ChronoUnit.DAYS).getEpochSecond()) {
                    blob.delete();
                    deleted++;
                }
            }

            log.info(prefix + " - deleted " + deleted + " of " + total);

        } catch (Exception e) {
            log.error(className + ": " + e.getMessage(), e);
        }
    }


    @Override
    public String toString() {
        return "folder='" + folder + '\'' +
                ' ' + super.toString();
    }
}

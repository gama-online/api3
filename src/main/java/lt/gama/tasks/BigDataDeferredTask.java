package lt.gama.tasks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import lt.gama.api.response.TaskResponse;
import lt.gama.helpers.StringHelper;
import lt.gama.service.ex.rt.GamaException;
import lt.gama.ConstWorkers;

import java.io.IOException;

public abstract class BigDataDeferredTask<T> extends BaseDeferredTask {

    private final String fileName;

    protected BigDataDeferredTask(long companyId) {
        this(null, companyId, null, null);
    }

    protected BigDataDeferredTask(long companyId, String fileName) {
        this(null, companyId, fileName, null);
    }

    protected BigDataDeferredTask(long companyId, String fileName, String queueName) {
        this(null, companyId, fileName, queueName);
    }

    protected BigDataDeferredTask(String token, long companyId, String fileName) {
        this(token, companyId, fileName, null);
    }

    protected BigDataDeferredTask(String token, long companyId, String fileName, String queueName) {
        super(token, companyId, queueName);
        this.fileName = StringHelper.hasValue(fileName) ? fileName : className + "-" + getToken();
    }

    protected String getFileName() {
        return fileName;
    }

    protected void saveData(T obj) {
        try {
            String json = objectMapper.writeValueAsString(obj);
            if (StringHelper.hasValue(json)) {
                storageService.upload(json, ConstWorkers.TASKS_FOLDER, getFileName(), "application/json");
            } else {
                log.warn("No external data");
            }
        } catch (JsonProcessingException e) {
            log.error(className + ": " + e.getMessage(), e);
            throw new GamaException(e.getMessage(), e);
        }
    }

    protected <S extends T> S loadData(TypeReference<S> typeRef) {
        String json = storageService.getContent(ConstWorkers.TASKS_FOLDER, getFileName());
        if (StringHelper.hasValue(json)) {
            try {
                return objectMapper.readValue(json, typeRef);

            } catch (IOException e) {
                log.error(className + ": " + e.getMessage(), e);
            }
        }
        return null;
    }

    protected void deleteData() {
        storageService.deleteFile(ConstWorkers.TASKS_FOLDER, getFileName());
    }

    @Override
    protected void finish(TaskResponse<?> response) {
        super.finish(response);
        deleteData();
    }

}

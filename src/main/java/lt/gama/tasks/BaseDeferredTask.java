package lt.gama.tasks;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.ConstWorkers;
import lt.gama.api.APIResult;
import lt.gama.api.response.TaskResponse;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.StringHelper;
import lt.gama.service.AuthSettingsCacheService;
import lt.gama.service.DBServiceSQL;
import lt.gama.service.StorageService;
import lt.gama.service.TaskQueueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

import static lt.gama.Constants.*;
import static lt.gama.ConstWorkers.DEFAULT_QUEUE_NAME;

/**
 * gama-online
 * Created by valdas on 2018-06-29.
 * p.s.
 * - 60 seconds for frontend request
 * - 600 seconds for cron/task handlers
 */
public abstract class BaseDeferredTask implements Runnable, Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @PersistenceContext
    transient protected EntityManager entityManager;

    @Autowired transient protected StorageService storageService;
    @Autowired transient protected Auth auth;
    @Autowired transient protected AuthSettingsCacheService authSettingsCacheService;
    @Autowired transient protected ObjectMapper objectMapper;
    @Autowired transient protected DBServiceSQL dbServiceSQL;
    @Autowired transient protected TaskQueueService taskQueueService;


    transient protected final String className = this.getClass().getSimpleName();


    private final String token;
    private final long companyId;
    private final String queueName;
    private final int retryNumber;

    private final String traceId;

    private final String userId;
    private final String userName;
    private final Set<String> permissions;


    protected BaseDeferredTask(long companyId) {
        this(null, companyId, null, 0);
    }

    protected BaseDeferredTask(String token, long companyId) {
        this(token, companyId, null, 0);
    }

    protected BaseDeferredTask(long companyId, int retryNumber) {
        this(null, companyId, null, retryNumber);
    }

    protected BaseDeferredTask(long companyId, String queueName) {
        this(null, companyId, queueName, 0);
    }

    protected BaseDeferredTask(String token, long companyId, String queueName) {
        this(token, companyId, queueName, 0);
    }

    protected BaseDeferredTask(long companyId, String queueName, int retryNumber) {
        this(null, companyId, queueName, retryNumber);
    }

    protected BaseDeferredTask(String token, long companyId, String queueName, int retryNumber) {
        this.token = StringHelper.hasValue(token) ? token : UUID.randomUUID().toString();
        this.companyId = companyId;
        this.queueName = StringHelper.hasValue(queueName) ? queueName : DEFAULT_QUEUE_NAME;
        this.retryNumber = retryNumber;

        this.traceId = MDC.get(LOG_TRACE_ID);
        this.userId = MDC.get(LOG_LABEL_LOGIN);
        this.userName = MDC.get(LOG_LABEL_USER_NAME);
        var perm = MDC.get(LOG_LABEL_PERMISSIONS);
        if (StringHelper.hasValue(perm)) {
            this.permissions = Set.copyOf(Arrays.asList(perm.split(",")));
        } else {
            this.permissions = null;
        }
    }

    public long getCompanyId() {
        return companyId;
    }

    public String getToken() {
        return token;
    }

    public int getRetryNumber() {
        return retryNumber;
    }

    @Override
    public final void run() {
        auth.clear();
        auth.setId(userId);
        auth.setName(userName);
        auth.setCompanyId(companyId);
        auth.setSettings(authSettingsCacheService.get(companyId));
        auth.setPermissions(permissions);

        // prepare data for logger
        MDC.put(LOG_TRACE_ID, traceId);
        MDC.put(LOG_LABEL_LOGIN, userId);
        MDC.put(LOG_LABEL_COMPANY, String.valueOf(companyId));
        MDC.put(LOG_LABEL_TASK_ID, token);

        log.info(className + ": *** Start *** " + this);
        execute();
        log.info(className + ": *** Done *** " + this);
    }

    public abstract void execute();

    protected void finish(TaskResponse<?> response) {
        String jsonResp = null;
        try {
            try {
                jsonResp = objectMapper.writeValueAsString(APIResult.Data(response));
            } catch (JsonProcessingException e) {
                log.error(className + ": " + e.getMessage(), e);
                if (companyId > 0) {
                    jsonResp = objectMapper.writeValueAsString(APIResult.Error(e.getMessage()));
                }
            }
        } catch (JsonProcessingException e) {
            log.error(className + ": " + e.getMessage(), e);
        }
        if (jsonResp != null) {
            storageService.upload(jsonResp, ConstWorkers.TASKS_FOLDER, token, "application/json");
            log.info(className + ": finished " + this);
        } else {
            log.warn(className + ": finished without response " + this);
        }
    }

    @Override
    public String toString() {
        return "companyId=" + companyId +
                " token=" + token +
                " queueName='" + queueName + '\'' +
                " retryNumber=" + retryNumber +
                (traceId == null ? "" : " traceId='" + traceId + '\'') +
                (userId == null ? "" : " userId='" + userId + '\'') +
                (userName == null ? "" : " userName='" + userName + '\'') +
                " class=" + className;
    }
}

package lt.gama.service;

import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.InstantiatingGrpcChannelProvider;
import com.google.cloud.tasks.v2.*;
import com.google.protobuf.ByteString;
import com.google.protobuf.Timestamp;
import io.grpc.ManagedChannelBuilder;
import lt.gama.helpers.StringHelper;
import lt.gama.service.ex.rt.GamaException;
import lt.gama.ConstWorkers;
import lt.gama.tasks.BaseDeferredTask;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;

import static lt.gama.Constants.PROJECT_ID;
import static lt.gama.Constants.QUEUE_LOCATION;
import static lt.gama.ConstWorkers.DEFAULT_QUEUE_NAME;

@Service
public class TaskQueueServiceImpl implements TaskQueueService {

    private static final Logger log = LoggerFactory.getLogger(TaskQueueService.class);

    private final AppPropService appPropService;

    public TaskQueueServiceImpl(AppPropService appPropService) {
        this.appPropService = appPropService;
    }

    public String queueTask(BaseDeferredTask task, String queueName, int delayInSeconds) {
        try (CloudTasksClient client = create()) {
            String queuePath = QueueName.of(PROJECT_ID, QUEUE_LOCATION,
                    StringHelper.hasValue(queueName) ? queueName : DEFAULT_QUEUE_NAME).toString();
            // Construct the task body:
            Task.Builder taskBuilder = Task.newBuilder().setAppEngineHttpRequest(
                    AppEngineHttpRequest.newBuilder()
                            .setBody(ByteString.copyFrom(SerializationUtils.serialize(task)))
                            .setRelativeUri(ConstWorkers.TASKS_QUEUE_PATH)
                            .setHttpMethod(HttpMethod.POST)
                            .putHeaders("content-type", "application/x-binary-app-engine-java-runnable-task")
                            .build());
            // Add the scheduled time to the request
            if (delayInSeconds > 0) {
                taskBuilder.setScheduleTime(Timestamp.newBuilder()
                        .setSeconds(Instant.now(Clock.systemUTC()).plusSeconds(delayInSeconds).getEpochSecond()));
            }
            // Send create task request
            var tsk = client.createTask(queuePath, taskBuilder.build());
            log.info(this.getClass().getSimpleName() + ": Task created" +
                    ", name=" + tsk.getName() +
                    ", queuePath=" + queuePath +
                    ", task=" + this);
            return task.getToken();

        } catch (IOException e) {
            log.error(this.getClass().getSimpleName() + ": Task creation failed", e);
            throw new GamaException(e);
        }
    }

    private CloudTasksClient create() throws IOException {
        if (appPropService.isProduction()) {
            return CloudTasksClient.create();
        } else if (appPropService.isDevelopment()) {
            CloudTasksSettings settings = CloudTasksSettings.newBuilder()
                    .setCredentialsProvider(NoCredentialsProvider.create())
                    .setTransportChannelProvider(
                            InstantiatingGrpcChannelProvider.newBuilder()
                                    .setEndpoint("localhost:9090")
                                    .setChannelConfigurator(ManagedChannelBuilder::usePlaintext)
                                    .build()
                    )
                    .build();
            return CloudTasksClient.create(settings);
        } else {
            throw new GamaException("Unknown environment");
        }
    }
}

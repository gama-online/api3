package lt.gama.service;

import lt.gama.tasks.BaseDeferredTask;

import static lt.gama.ConstWorkers.DEFAULT_QUEUE_NAME;

public interface TaskQueueService {

    String queueTask(BaseDeferredTask task, String queueName, int delayInSeconds);

    default String queueTask(BaseDeferredTask task) {
        return queueTask(task, DEFAULT_QUEUE_NAME, 0);
    }

    default String queueTask(BaseDeferredTask task, int delayInSeconds) {
        return queueTask(task, DEFAULT_QUEUE_NAME, delayInSeconds);
    }
}

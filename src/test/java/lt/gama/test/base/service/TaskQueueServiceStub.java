package lt.gama.test.base.service;

import lt.gama.service.TaskQueueService;
import lt.gama.tasks.BaseDeferredTask;
import org.springframework.beans.factory.annotation.Autowired;


public class TaskQueueServiceStub implements TaskQueueService {

    @Autowired private TasksQueue tasksQueue;

    @Override
    public String queueTask(BaseDeferredTask task, String queueName, int delayInSeconds) {
        return tasksQueue.queueTask(task, queueName);
    }
}

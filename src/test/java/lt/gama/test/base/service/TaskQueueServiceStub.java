package lt.gama.test.base.service;

import lt.gama.service.TaskQueueService;
import lt.gama.tasks.BaseDeferredTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class TaskQueueServiceStub implements TaskQueueService {

    @Autowired private final TasksQueue tasksQueue;

    public TaskQueueServiceStub(TasksQueue tasksQueue) {
        this.tasksQueue = tasksQueue;
    }

    @Override
    public String queueTask(BaseDeferredTask task, String queueName, int delayInSeconds) {
        return tasksQueue.queueTask(task, queueName);
    }
}

package lt.gama.test.base.service;

import lt.gama.tasks.BaseDeferredTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

@Service
public class TasksQueue {

    private @Autowired AutowireCapableBeanFactory beanFactory;

    private Map<String, LinkedList<TaskInfoAndBody>> tasksQueue = new HashMap<>();

    public String queueTask(BaseDeferredTask task, String queueName) {
        beanFactory.autowireBean(task);
        var queue = tasksQueue.computeIfAbsent(queueName, key -> new LinkedList<>());
        var taskInfo = new TaskInfoAndBody(task);
        queue.add(taskInfo);
        return taskInfo.info().taskName();
    }

    public int getCountTasks(String queueName) {
        return tasksQueue.get(queueName).size();
    }

    public TaskInfoAndBody getTask(String queueName, int index) {
        return tasksQueue.get(queueName).get(index);
    }

    public void deleteTask(String queueName, int index) {
        tasksQueue.get(queueName).remove(index);
    }

    public void deleteTask(String queueName, String taskName) {
        tasksQueue.get(queueName).removeIf(t -> t.info().taskName().equals(taskName));
    }
}

package lt.gama.test.base.service;

import lt.gama.tasks.BaseDeferredTask;

import java.util.UUID;

public record TaskInfoAndBody(TaskStateInfo info, BaseDeferredTask body) {
    TaskInfoAndBody(BaseDeferredTask body) {
        this(new TaskStateInfo(UUID.randomUUID().toString()), body);
    }
}

package lt.gama.api.response;

import lt.gama.model.type.enums.ProcessingStatusType;

import java.util.Collection;
import java.util.Collections;

/**
 * gama-online
 * Created by valdas on 2017-05-01.
 */
public class TaskResponse<T> {

    private ProcessingStatusType status;

    private Collection<String> warnings;

    private Collection<String> errors;

    private T data;


    public static <T> TaskResponse<T> success() {
        return new TaskResponse<T>().withStatus(ProcessingStatusType.COMPLETED);
    }

    public static <T> TaskResponse<T> success(T data) {
        return TaskResponse.<T>success().withData(data);
    }

    public static <T> TaskResponse<T> error(String error) {
        return TaskResponse.<T>status(ProcessingStatusType.ERROR).withError(error);
    }

    public static <T> TaskResponse<T> errors(Collection<String> errors) {
        return TaskResponse.<T>status(ProcessingStatusType.ERROR).withErrors(errors);
    }

    public static <T> TaskResponse<T> status(ProcessingStatusType status) {
        return new TaskResponse<T>().withStatus(status);
    }

    public TaskResponse<T> withStatus(ProcessingStatusType status) {
        setStatus(status);
        return this;
    }

    public TaskResponse<T> withError(String error) {
        return withErrors(Collections.singletonList(error));
    }

    public TaskResponse<T> withErrors(Collection<String> errors) {
        setStatus(ProcessingStatusType.ERROR);
        setErrors(errors);
        return this;
    }

    public TaskResponse<T> withWarnings(Collection<String> warnings) {
        setWarnings(warnings);
        return this;
    }

    public TaskResponse<T> withData(T data) {
        setData(data);
        return this;
    }

    // generated

    public ProcessingStatusType getStatus() {
        return status;
    }

    public void setStatus(ProcessingStatusType status) {
        this.status = status;
    }

    public Collection<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(Collection<String> warnings) {
        this.warnings = warnings;
    }

    public Collection<String> getErrors() {
        return errors;
    }

    public void setErrors(Collection<String> errors) {
        this.errors = errors;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "TaskResponse{" +
                "status=" + status +
                ", warnings=" + warnings +
                ", errors=" + errors +
                ", data=" + data +
                '}';
    }
}

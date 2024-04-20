package lt.gama.impexp.entity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * gama-online
 * Created by valdas on 2015-12-06.
 */
public class ISO20022Statements {

    /**
     * Background tasks (for creating bank documents) ids list
     */
    private List<String> taskIds;

    private String msgId; // Message Identification

    private LocalDateTime createdOn; // Creation DateTime

    private List<ISO20022Statement> statements;

    // generated

    public List<String> getTaskIds() {
        return taskIds;
    }

    public void setTaskIds(List<String> taskIds) {
        this.taskIds = taskIds;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public List<ISO20022Statement> getStatements() {
        return statements;
    }

    public void setStatements(List<ISO20022Statement> statements) {
        this.statements = statements;
    }

    @Override
    public String toString() {
        return "ISO20022Statements{" +
                "taskIds=" + taskIds +
                ", msgId='" + msgId + '\'' +
                ", createdOn=" + createdOn +
                ", statements=" + statements +
                '}';
    }
}

package lt.gama.api.request;

import lt.gama.model.type.enums.DBType;
import lt.gama.model.type.enums.LabelType;

import java.util.Set;

/**
 * gama-online
 * Created by valdas on 2016-03-14.
 */
public class UpdateLabelsRequest {

    private LabelType type;

    private long id;

    private DBType db;

    private Set<String> labels;


    @SuppressWarnings("unused")
    protected UpdateLabelsRequest() {}

    public UpdateLabelsRequest(LabelType type, long id, DBType db, Set<String> labels) {
        this.type = type;
        this.id = id;
        this.db = db;
        this.labels = labels;
    }

    // generated

    public LabelType getType() {
        return type;
    }

    public void setType(LabelType type) {
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public DBType getDb() {
        return db;
    }

    public void setDb(DBType db) {
        this.db = db;
    }

    public Set<String> getLabels() {
        return labels;
    }

    public void setLabels(Set<String> labels) {
        this.labels = labels;
    }

    @Override
    public String toString() {
        return "UpdateLabelsRequest{" +
                "type=" + type +
                ", id=" + id +
                ", db=" + db +
                ", labels=" + labels +
                '}';
    }
}

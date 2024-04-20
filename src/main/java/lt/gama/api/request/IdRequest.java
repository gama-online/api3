package lt.gama.api.request;

import lt.gama.model.type.enums.DBType;

import java.util.Objects;

/**
 * Gama
 * Created by valdas on 15-09-22.
 */
public class IdRequest {

    private long id;

    private DBType db;

    private Long parentId;

    private String parentName;

    private Boolean parent;

    private DBType parentDb;


    public IdRequest() {
    }

    public IdRequest(long id) {
        this.id = id;
    }

    public IdRequest(long id, DBType dbType) {
        this(id);
        this.db = dbType;
    }

    public IdRequest(long id, Long parentId) {
        this(id);
        this.parentId = parentId;
    }

    public IdRequest(long id, Long parentId, DBType parentDb) {
        this(id);
        this.parentId = parentId;
        this.parentDb = parentDb;
    }

    public IdRequest(long id, String parentName) {
        this(id);
        this.parentName = parentName;
    }

    public IdRequest(long id, Boolean parent) {
        this(id);
        if (parent) this.parentId = id;
        this.parent = parent;
    }

    public IdRequest(long id, DBType dbType, Boolean parent) {
        this(id, dbType);
        if (parent) this.parentId = id;
        this.parent = parent;
    }

    // generated

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

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public Boolean getParent() {
        return parent;
    }

    public void setParent(Boolean parent) {
        this.parent = parent;
    }

    public DBType getParentDb() {
        return parentDb;
    }

    public void setParentDb(DBType parentDb) {
        this.parentDb = parentDb;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IdRequest idRequest = (IdRequest) o;
        return id == idRequest.id && db == idRequest.db && Objects.equals(parentId, idRequest.parentId) && Objects.equals(parentName, idRequest.parentName) && Objects.equals(parent, idRequest.parent) && parentDb == idRequest.parentDb;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, db, parentId, parentName, parent, parentDb);
    }

    @Override
    public String toString() {
        return "IdRequest{" +
                "id=" + id +
                ", db=" + db +
                ", parentId=" + parentId +
                ", parentName='" + parentName + '\'' +
                ", parent=" + parent +
                ", parentDb=" + parentDb +
                '}';
    }
}

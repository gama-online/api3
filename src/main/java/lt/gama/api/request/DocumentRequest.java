package lt.gama.api.request;

import lt.gama.model.type.enums.DBType;

public class DocumentRequest {

    private long id;

    private DBType db;

    private String documentType;

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

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    @Override
    public String toString() {
        return "DocumentRequest{" +
                "id=" + id +
                ", db=" + db +
                ", documentType='" + documentType + '\'' +
                '}';
    }
}

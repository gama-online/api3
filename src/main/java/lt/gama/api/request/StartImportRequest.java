package lt.gama.api.request;

import lt.gama.model.type.enums.DataFormatType;

/**
 * gama-online
 * Created by valdas on 2017-04-24.
 */
public class StartImportRequest {

    private String fileName;

    private String entity;

    private boolean delete;

    private DataFormatType format;

    // generated

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    public DataFormatType getFormat() {
        return format;
    }

    public void setFormat(DataFormatType format) {
        this.format = format;
    }

    @Override
    public String toString() {
        return "StartImportRequest{" +
                "fileName='" + fileName + '\'' +
                ", entity='" + entity + '\'' +
                ", delete=" + delete +
                ", format='" + format + '\'' +
                '}';
    }
}

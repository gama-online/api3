package lt.gama.api.request;

/**
 * gama-online
 * Created by valdas on 2017-04-24.
 */
public class GetUploadUrlRequest {

    private String contentType;

    private String folder;

    private String fileName;

    private Boolean accessPublic;

    private String sourceFileName;

    // generated

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Boolean getAccessPublic() {
        return accessPublic;
    }

    public void setAccessPublic(Boolean accessPublic) {
        this.accessPublic = accessPublic;
    }

    public String getSourceFileName() {
        return sourceFileName;
    }

    public void setSourceFileName(String sourceFileName) {
        this.sourceFileName = sourceFileName;
    }

    @Override
    public String toString() {
        return "GetUploadUrlRequest{" +
                "contentType='" + contentType + '\'' +
                ", folder='" + folder + '\'' +
                ", fileName='" + fileName + '\'' +
                ", accessPublic=" + accessPublic +
                ", sourceFileName='" + sourceFileName + '\'' +
                '}';
    }
}

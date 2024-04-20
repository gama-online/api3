package lt.gama.api.request;

/**
 * gama-online
 * Created by valdas on 2015-12-04.
 */
public class ImportFileRequest {

    private String fileName;


    @SuppressWarnings("unused")
    protected ImportFileRequest() {}

    public ImportFileRequest(String fileName) {
        this.fileName = fileName;
    }

    // generated

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return "ImportFileRequest{" +
                "fileName='" + fileName + '\'' +
                '}';
    }
}

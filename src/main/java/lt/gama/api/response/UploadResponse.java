package lt.gama.api.response;

import java.util.Map;

/**
 * Gama
 * Created by valdas on 15-05-20.
 */
public class UploadResponse {

    /**
     * file access full url
     */
    private String url;

    /**
     * file upload url
     */
    private String upload;

    /**
     * Storage file name in the cloud default bucket
     */
    private String filename;

    private String version;

    private Map<String, String> fields;

    // generated

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUpload() {
        return upload;
    }

    public void setUpload(String upload) {
        this.upload = upload;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, String> getFields() {
        return fields;
    }

    public void setFields(Map<String, String> fields) {
        this.fields = fields;
    }

    @Override
    public String toString() {
        return "UploadResponse{" +
                "url='" + url + '\'' +
                ", upload='" + upload + '\'' +
                ", filename='" + filename + '\'' +
                ", version='" + version + '\'' +
                ", fields='" + fields + '\'' +
                '}';
    }
}

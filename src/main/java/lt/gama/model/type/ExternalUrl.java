package lt.gama.model.type;

import java.io.Serializable;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 22/11/2018.
 */
public class ExternalUrl implements Serializable {

    private String url;

    private String mime;

    /**
     * Original file name
     */
    private String filename;

    /**
     * File name in cloud storage
     */
    private String storageFilename;

    /**
     * true - If file uploaded, so on delete we need to remove it from cloud also
     * false/null - remove this record about file only
     */
    private Boolean uploaded;

    /**
     * true - if file should be removed on save or update
     */
    private Boolean archive;

    // generated

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getStorageFilename() {
        return storageFilename;
    }

    public void setStorageFilename(String storageFilename) {
        this.storageFilename = storageFilename;
    }

    public Boolean getUploaded() {
        return uploaded;
    }

    public void setUploaded(Boolean uploaded) {
        this.uploaded = uploaded;
    }

    public Boolean getArchive() {
        return archive;
    }

    public void setArchive(Boolean archive) {
        this.archive = archive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExternalUrl that = (ExternalUrl) o;
        return Objects.equals(url, that.url) && Objects.equals(mime, that.mime) && Objects.equals(filename, that.filename) && Objects.equals(storageFilename, that.storageFilename) && Objects.equals(uploaded, that.uploaded) && Objects.equals(archive, that.archive);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, mime, filename, storageFilename, uploaded, archive);
    }

    @Override
    public String toString() {
        return "ExternalUrl{" +
                "url='" + url + '\'' +
                ", mime='" + mime + '\'' +
                ", filename='" + filename + '\'' +
                ", storageFilename='" + storageFilename + '\'' +
                ", uploaded=" + uploaded +
                ", archive=" + archive +
                '}';
    }
}

package lt.gama.api.request;

import lt.gama.model.type.enums.DataFormatType;

/**
 * Gama
 * Created by valdas on 15-09-22.
 */
public class ImportDocRequest extends ImportFileRequest {

    private long id;
    private DataFormatType format;
    private String currency;


    @SuppressWarnings("unused")
    protected ImportDocRequest() {}

    public ImportDocRequest(String fileName) {
        super(fileName);
    }

    public ImportDocRequest(long id, String fileName) {
        super(fileName);
        this.id = id;
    }

    // generated

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public DataFormatType getFormat() {
        return format;
    }

    public void setFormat(DataFormatType format) {
        this.format = format;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Override
    public String toString() {
        return "ImportDocRequest{" +
                "id=" + id +
                ", format=" + format +
                ", currency=" + currency +
                "} " + super.toString();
    }
}

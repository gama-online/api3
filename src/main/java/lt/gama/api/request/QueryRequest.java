package lt.gama.api.request;

/**
 * gama-online
 * Created by valdas on 2016-01-07.
 */
public class QueryRequest {

    private String datasetId;

    private String tableId;

    private long pageSize;

    private long startIndex;

    private String pageToken;

    // generated

    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

    public String getTableId() {
        return tableId;
    }

    public void setTableId(String tableId) {
        this.tableId = tableId;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }

    public long getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(long startIndex) {
        this.startIndex = startIndex;
    }

    public String getPageToken() {
        return pageToken;
    }

    public void setPageToken(String pageToken) {
        this.pageToken = pageToken;
    }

    @Override
    public String toString() {
        return "QueryRequest{" +
                "datasetId='" + datasetId + '\'' +
                ", tableId='" + tableId + '\'' +
                ", pageSize=" + pageSize +
                ", startIndex=" + startIndex +
                ", pageToken='" + pageToken + '\'' +
                '}';
    }
}

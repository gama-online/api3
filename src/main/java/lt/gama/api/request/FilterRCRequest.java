package lt.gama.api.request;

public class FilterRCRequest {

    private String filter;

    private int maxItems;


    @SuppressWarnings("unused")
    protected FilterRCRequest() {}

    public FilterRCRequest(String filter, int maxItems) {
        this.filter = filter;
        this.maxItems = maxItems;
    }

    // generated

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public int getMaxItems() {
        return maxItems;
    }

    public void setMaxItems(int maxItems) {
        this.maxItems = maxItems;
    }

    @Override
    public String toString() {
        return "FilterRCRequest{" +
                "filter='" + filter + '\'' +
                ", maxItems=" + maxItems +
                '}';
    }
}

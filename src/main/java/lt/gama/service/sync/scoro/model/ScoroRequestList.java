package lt.gama.service.sync.scoro.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * gama-online
 * Created by valdas on 2017-10-06.
 */
public class ScoroRequestList extends ScoroRequest {

    @JsonProperty("per_page")
    private int perPage;

    /**
     * page number from 1
     */
    private int page;

    private ScoroRequestFilter filter;


    @SuppressWarnings("unused")
    protected ScoroRequestList() {}

    public ScoroRequestList(String apiKey, String companyAccountId, String language, int perPage, int page, ScoroRequestFilter filter) {
        super(apiKey, companyAccountId, language);
        this.perPage = perPage;
        this.page = page;
        this.filter = filter;
    }

    // generated

    public int getPerPage() {
        return perPage;
    }

    public void setPerPage(int perPage) {
        this.perPage = perPage;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public ScoroRequestFilter getFilter() {
        return filter;
    }

    public void setFilter(ScoroRequestFilter filter) {
        this.filter = filter;
    }

    @Override
    public String toString() {
        return "ScoroRequestList{" +
                "perPage=" + perPage +
                ", page=" + page +
                ", filter=" + filter +
                "} " + super.toString();
    }
}

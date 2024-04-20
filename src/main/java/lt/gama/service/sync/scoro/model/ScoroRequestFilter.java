package lt.gama.service.sync.scoro.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * gama-online
 * Created by valdas on 2017-10-06.
 */
public class ScoroRequestFilter {

    @JsonProperty("modified_date")
    private ScoroRequestFilterDate modifiedDate;


    @SuppressWarnings("unused")
    protected ScoroRequestFilter() {}

    public ScoroRequestFilter(ScoroRequestFilterDate modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    // generated

    public ScoroRequestFilterDate getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(ScoroRequestFilterDate modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    @Override
    public String toString() {
        return "ScoroRequestFilter{" +
                "modifiedDate=" + modifiedDate +
                '}';
    }
}

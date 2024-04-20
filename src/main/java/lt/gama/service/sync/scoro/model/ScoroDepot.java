package lt.gama.service.sync.scoro.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * gama-online
 * Created by valdas on 2017-10-06.
 */
public class ScoroDepot {

    @JsonProperty("depot_id")
    private int id;

    @JsonProperty("depot_name")
    private String name;

    private String comments;

    // generated

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    @Override
    public String toString() {
        return "ScoroDepot{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", comments='" + comments + '\'' +
                '}';
    }
}

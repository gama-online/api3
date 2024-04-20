package lt.gama.api.request;

public class GetJsonRequest {

    private String language;

    private String name;

    // generated

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "GetJsonRequest{" +
                "language='" + language + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}

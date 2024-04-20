package lt.gama.api.request;

/**
 * gama-online
 * Created by valdas on 2019-02-01.
 */
public class HtmlTemplateRequest {

    private String template;

    private String language;

    // generated

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    public String toString() {
        return "HtmlTemplateRequest{" +
                "template='" + template + '\'' +
                ", language='" + language + '\'' +
                '}';
    }
}

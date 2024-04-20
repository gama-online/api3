package lt.gama.service.sync.scoro.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.neovisionaries.i18n.LanguageCode;

/**
 * gama-online
 * Created by valdas on 2017-10-12.
 */
public class ScoroRequest {

    private String apiKey;

    private String lang;    //  "eng" / "lit"

    @JsonProperty("company_account_id")
    private String companyAccountId;

    public ScoroRequest() {
    }

    public ScoroRequest(String apiKey, String companyAccountId, String language) {
        this.apiKey = apiKey;
        this.companyAccountId = companyAccountId;
        if (language == null) this.lang = "eng";
        else if (language.length() == 3) this.lang = language.toLowerCase();
        else if (language.length() == 2) this.lang = LanguageCode.getByCodeIgnoreCase(language).getAlpha3().name().toLowerCase();
        else this.lang = "eng";
    }

    // generated

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getCompanyAccountId() {
        return companyAccountId;
    }

    public void setCompanyAccountId(String companyAccountId) {
        this.companyAccountId = companyAccountId;
    }

    @Override
    public String toString() {
        return "ScoroRequest{" +
                "apiKey='" + apiKey + '\'' +
                ", lang='" + lang + '\'' +
                ", companyAccountId='" + companyAccountId + '\'' +
                '}';
    }
}

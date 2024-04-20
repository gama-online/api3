package lt.gama.service.sync.scoro.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * gama-online
 * Created by valdas on 2017-10-12.
 */
public class ScoroVatCodes {

    @JsonProperty("vat_code_id")
    private long vatCodeId;

    @JsonProperty("vat_code")
    private String vatCode;

    private BigDecimal percent;

    @JsonProperty("vat_name")
    private String vatName;

    // generated

    public long getVatCodeId() {
        return vatCodeId;
    }

    public void setVatCodeId(long vatCodeId) {
        this.vatCodeId = vatCodeId;
    }

    public String getVatCode() {
        return vatCode;
    }

    public void setVatCode(String vatCode) {
        this.vatCode = vatCode;
    }

    public BigDecimal getPercent() {
        return percent;
    }

    public void setPercent(BigDecimal percent) {
        this.percent = percent;
    }

    public String getVatName() {
        return vatName;
    }

    public void setVatName(String vatName) {
        this.vatName = vatName;
    }

    @Override
    public String toString() {
        return "ScoroVatCodes{" +
                "vatCodeId=" + vatCodeId +
                ", vatCode='" + vatCode + '\'' +
                ", percent=" + percent +
                ", vatName='" + vatName + '\'' +
                '}';
    }
}

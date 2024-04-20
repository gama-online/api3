package lt.gama.impexp.entity;

import lt.gama.model.type.doc.DocCounterparty;

/**
 * gama-online
 * Created by valdas on 2016-04-03.
 */
public class DocCounterpartyExport {

    private Long id;

    private String name;

    private String shortName;

    private String comCode;

    private String vatCode;

    private String debtType;


    @SuppressWarnings("unused")
    protected DocCounterpartyExport() {}

    public DocCounterpartyExport(DocCounterparty src) {
        if (src == null) return;
        id = src.getId();
        name = src.getName();
        shortName = src.getShortName();
        comCode = src.getComCode();
        vatCode = src.getVatCode();
        debtType = src.getDebtType() != null ? src.getDebtType().toString() : null;
    }

    // generated

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getComCode() {
        return comCode;
    }

    public void setComCode(String comCode) {
        this.comCode = comCode;
    }

    public String getVatCode() {
        return vatCode;
    }

    public void setVatCode(String vatCode) {
        this.vatCode = vatCode;
    }

    public String getDebtType() {
        return debtType;
    }

    public void setDebtType(String debtType) {
        this.debtType = debtType;
    }

    @Override
    public String toString() {
        return "DocCounterpartyExport{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", shortName='" + shortName + '\'' +
                ", comCode='" + comCode + '\'' +
                ", vatCode='" + vatCode + '\'' +
                ", debtType='" + debtType + '\'' +
                '}';
    }
}

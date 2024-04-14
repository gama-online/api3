package lt.gama.model.type.doc;

import jakarta.persistence.Embeddable;
import lt.gama.model.type.Location;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Gama
 * Created by valdas on 15-05-30.
 */
@Embeddable
public class DocBank extends Location implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    private String swift;

    private String code;

    public DocBank() {
    }

    public DocBank(String name, String address1, String address2, String address3, String zip, String city,
                   String municipality, String country, String swift, String code) {
        super(name, address1, address2, address3, zip, city, municipality, country);
        this.swift = swift;
        this.code = code;
    }

    public DocBank(String name, String swift, String code) {
        setName(name);
        this.swift = swift;
        this.code = code;
    }

    // generated

    public String getSwift() {
        return swift;
    }

    public void setSwift(String swift) {
        this.swift = swift;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DocBank docBank = (DocBank) o;
        return Objects.equals(swift, docBank.swift) && Objects.equals(code, docBank.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), swift, code);
    }

    @Override
    public String toString() {
        return "DocBank{" +
                "swift='" + swift + '\'' +
                ", code='" + code + '\'' +
                "} " + super.toString();
    }
}

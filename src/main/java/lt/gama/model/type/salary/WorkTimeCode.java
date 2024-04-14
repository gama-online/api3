package lt.gama.model.type.salary;

import jakarta.persistence.Embeddable;
import lt.gama.model.type.enums.WorkTimeCodeType;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 2016-05-13.
 */
@Embeddable
public class WorkTimeCode implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    private String code;

    private String name;

    private WorkTimeCodeType type;

    private Boolean avgPay;


    protected WorkTimeCode() {
    }

    public WorkTimeCode(String code, String name, WorkTimeCodeType type, Boolean avgPay) {
        this.code = code;
        this.name = name;
        this.type = type;
        this.avgPay = avgPay;
    }

    public boolean isAvgPay() {
        return avgPay != null && avgPay;
    }

    // generated
    // except getAvgPay()

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public WorkTimeCodeType getType() {
        return type;
    }

    public void setType(WorkTimeCodeType type) {
        this.type = type;
    }

    public void setAvgPay(Boolean avgPay) {
        this.avgPay = avgPay;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkTimeCode that = (WorkTimeCode) o;
        return Objects.equals(code, that.code) && Objects.equals(name, that.name) && type == that.type && Objects.equals(avgPay, that.avgPay);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, name, type, avgPay);
    }

    @Override
    public String toString() {
        return "WorkTimeCode{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", avgPay=" + avgPay +
                '}';
    }
}



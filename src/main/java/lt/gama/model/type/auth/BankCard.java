package lt.gama.model.type.auth;

import lt.gama.model.type.doc.DocEmployee;

import java.io.Serializable;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 17/10/2018.
 */
public class BankCard implements Serializable {

    private String number1;

    private String number2;

    private DocEmployee employee;


    public BankCard() {
    }

    public BankCard(String number1, String number2, DocEmployee employee) {
        this.number1 = number1;
        this.number2 = number2;
        this.employee = employee;
    }

    // generated

    public String getNumber1() {
        return number1;
    }

    public void setNumber1(String number1) {
        this.number1 = number1;
    }

    public String getNumber2() {
        return number2;
    }

    public void setNumber2(String number2) {
        this.number2 = number2;
    }

    public DocEmployee getEmployee() {
        return employee;
    }

    public void setEmployee(DocEmployee employee) {
        this.employee = employee;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BankCard bankCard = (BankCard) o;
        return Objects.equals(number1, bankCard.number1) && Objects.equals(number2, bankCard.number2) && Objects.equals(employee, bankCard.employee);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number1, number2, employee);
    }

    @Override
    public String toString() {
        return "BankCard{" +
                "number1='" + number1 + '\'' +
                ", number2='" + number2 + '\'' +
                ", employee=" + employee +
                '}';
    }
}

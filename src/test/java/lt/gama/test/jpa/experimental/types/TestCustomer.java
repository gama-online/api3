package lt.gama.test.jpa.experimental.types;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

public class TestCustomer implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    private int id;

    private String name;

    private String address;

    private LocalDate date;

    public TestCustomer() {
    }

    public TestCustomer(int id, String name, String address) {
        this.id = id;
        this.name = name;
        this.address = address;
    }

    public TestCustomer(int id, String name, String address, LocalDate date) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.date = date;
    }

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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "TestCustomer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}

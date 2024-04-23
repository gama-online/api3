package lt.gama.test.jpa.experimental.types;

import java.io.Serial;
import java.io.Serializable;

public class TestCustomer implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    private int id;

    private String name;

    private String address;

    public TestCustomer() {
    }

    public TestCustomer(int id, String name, String address) {
        this.id = id;
        this.name = name;
        this.address = address;
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

    @Override
    public String toString() {
        return "TestCustomer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}

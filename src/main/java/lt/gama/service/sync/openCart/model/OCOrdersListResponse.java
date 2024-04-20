package lt.gama.service.sync.openCart.model;

import java.util.List;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 11/11/2018.
 */
public class OCOrdersListResponse extends OCResponse {

    private List<OCOrder> orders;

    // generated

    public List<OCOrder> getOrders() {
        return orders;
    }

    public void setOrders(List<OCOrder> orders) {
        this.orders = orders;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        OCOrdersListResponse that = (OCOrdersListResponse) o;
        return Objects.equals(orders, that.orders);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), orders);
    }

    @Override
    public String toString() {
        return "OCOrdersListResponse{" +
                "orders=" + orders +
                "} " + super.toString();
    }
}

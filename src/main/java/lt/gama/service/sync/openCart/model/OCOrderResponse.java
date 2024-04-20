package lt.gama.service.sync.openCart.model;

import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 11/11/2018.
 */
public class OCOrderResponse extends OCResponse {

    private OCOrder order;

    // generated

    public OCOrder getOrder() {
        return order;
    }

    public void setOrder(OCOrder order) {
        this.order = order;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        OCOrderResponse that = (OCOrderResponse) o;
        return Objects.equals(order, that.order);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), order);
    }

    @Override
    public String toString() {
        return "OCOrderResponse{" +
                "order=" + order +
                "} " + super.toString();
    }
}

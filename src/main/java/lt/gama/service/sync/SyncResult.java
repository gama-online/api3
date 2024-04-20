package lt.gama.service.sync;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SyncResult implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    private int orders;

    private int parts;

    private int specialPrices;

    private List<String> tasksIds = new ArrayList<>();

    // generated

    public int getOrders() {
        return orders;
    }

    public void setOrders(int orders) {
        this.orders = orders;
    }

    public int getParts() {
        return parts;
    }

    public void setParts(int parts) {
        this.parts = parts;
    }

    public int getSpecialPrices() {
        return specialPrices;
    }

    public void setSpecialPrices(int specialPrices) {
        this.specialPrices = specialPrices;
    }

    public List<String> getTasksIds() {
        return tasksIds;
    }

    public void setTasksIds(List<String> tasksIds) {
        this.tasksIds = tasksIds;
    }

    @Override
    public String toString() {
        return "SyncResult{" +
                "orders=" + orders +
                ", parts=" + parts +
                ", specialPrices=" + specialPrices +
                ", tasksIds=" + tasksIds +
                '}';
    }
}

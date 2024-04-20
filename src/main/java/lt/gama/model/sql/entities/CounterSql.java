package lt.gama.model.sql.entities;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lt.gama.model.sql.base.BaseEntitySql;
import lt.gama.model.sql.entities.id.CounterId;
import lt.gama.model.type.auth.CounterDesc;


@Entity
@Table(name = "counter")
public class CounterSql extends BaseEntitySql {

    /**
     * company id + label + prefix
     */
    @EmbeddedId
    private CounterId id;

    private int count;


    @SuppressWarnings("unused")
    protected CounterSql() {}

    public CounterSql(long companyId, @NotNull CounterDesc desc, int count) {
        this.id = new CounterId(companyId, desc);
        this.count = count;
    }

    public CounterSql increment() {
        count++;
        return this;
    }

    public CounterSql increment(int step) {
        count += step;
        return this;
    }

    public CounterSql update(int count) {
        this.count = count;
        return this;
    }

    // generated

    public CounterId getId() {
        return id;
    }

    public void setId(CounterId id) {
        this.id = id;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "CounterSql{" +
                "id=" + id +
                ", count=" + count +
                "} " + super.toString();
    }
}

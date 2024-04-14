package lt.gama.api.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.Hidden;
import lt.gama.model.type.enums.CustomSearchType;

import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

/**
 * Gama
 * Created by valdas on 15-04-16.
 */
public class PageRequestCondition {

    private String field;

    private ConditionFieldType type;

    private Object value;

    public PageRequestCondition() {
    }

    public PageRequestCondition(String field, Object value) {
        this.field = field;
        this.value = value;
    }

    public PageRequestCondition(CustomSearchType searchType, Object value) {
        this(searchType.getField(), value);
    }

    public PageRequestCondition(CustomSearchType searchType, Object value, ConditionFieldType type) {
        this(searchType.getField(), value);
        this.type = type;
    }

    public boolean isList() {
        return value instanceof List;
    }

    @Hidden
    @JsonIgnore
    public List<?> getListValue() {
        if (value instanceof List) return (List<?>) getValue();
        return null;
    }

    public int size() {
        return value instanceof List ? ((List)value).size() : 0;
    }

    private Object transform(Object v) {
        // fix (Integer !!!!)
        if (getType() != null) {
            switch (getType()) {
                case Integer -> {
                    if (v instanceof String) return Long.valueOf((String) v);
                    else if (v instanceof Integer) return Long.valueOf((Integer) v);
                }
                case Float -> {
                    if (v instanceof String) return Double.valueOf((String) v);
                }
                case Boolean -> {
                    if (v instanceof Long) return ((Long) v) != 0;
                    else if (v instanceof Integer) return ((Integer) v) != 0;
                    else if (v instanceof String) return Boolean.valueOf((String) v);
                }
            }
        }
        return v;
    }

    public Object getValue() {
        if (!isList()) return transform(value);
        else {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>)value;
            ListIterator<Object> iterator = list.listIterator();
            while (iterator.hasNext()) {
                Object v = iterator.next();
                Object vTrans = transform(v);
                if (!Objects.equals(v, vTrans)) {
                    iterator.set(vTrans);
                }
            }
            return value;
        }
    }

    // generated

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public ConditionFieldType getType() {
        return type;
    }

    public void setType(ConditionFieldType type) {
        this.type = type;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PageRequestCondition that = (PageRequestCondition) o;
        return Objects.equals(field, that.field) && type == that.type && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field, type, value);
    }

    @Override
    public String toString() {
        return "PageRequestCondition{" +
                "field='" + field + '\'' +
                ", type=" + type +
                ", value=" + value +
                '}';
    }
}

package lt.gama.model.type.base;

import lt.gama.model.i.IId;
import lt.gama.model.i.IParentId;

import java.io.Serializable;

public abstract class BaseChild implements IId<Long>, IParentId, Serializable {

    /**
     * Object/entity id.
     * Not used in Google Cloud Datastore, but can be used anywhere else.
     */
    private Long id;

    /**
     * Object/entity parent id.
     * Not used in Google Cloud Datastore, but can be used anywhere else.
     */
    private Long parentId;

    // generated

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Long getParentId() {
        return parentId;
    }

    @Override
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    @Override
    public String toString() {
        return "BaseChild{" +
                "id=" + id +
                ", parentId=" + parentId +
                '}';
    }
}

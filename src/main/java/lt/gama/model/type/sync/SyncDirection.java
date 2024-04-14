package lt.gama.model.type.sync;

import java.io.Serializable;

public record SyncDirection(
        Boolean toGama,
        Boolean fromGama
) implements Serializable {
    public SyncDirection {
        if (toGama == null) toGama = false;
        if (fromGama == null) fromGama = false;
    }
    public SyncDirection() {
        this(false, false);
    }
    public SyncDirection(boolean isEnabled) {
        this(isEnabled, isEnabled);
    }
}

package lt.gama.model.type.sync;

import java.io.Serializable;

public record SyncAbilities(
        SyncDirection product,
        SyncDirection quantity,
        SyncDirection price,
        SyncDirection customer,
        SyncDirection order
) implements Serializable {
    public SyncAbilities() {
        this(new SyncDirection(), new SyncDirection(), new SyncDirection(), new SyncDirection(), new SyncDirection());
    }
    public SyncAbilities(boolean isAllEnabled) {
        this(new SyncDirection(isAllEnabled), new SyncDirection(isAllEnabled), new SyncDirection(isAllEnabled),
                new SyncDirection(isAllEnabled), new SyncDirection(isAllEnabled));
    }
}

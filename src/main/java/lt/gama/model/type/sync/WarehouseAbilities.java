package lt.gama.model.type.sync;

import java.io.Serializable;

public record WarehouseAbilities(
        SyncDirection arrival,  // sync arrival document in external warehouse with purchase or transportation document from gama
        SyncDirection product,  // sync products in external warehouse with gama products (sku, title and quantity)
        SyncDirection order     // sync order document in external warehouse with invoice document from gama
) implements Serializable {
    public WarehouseAbilities() {
        this(new SyncDirection(), new SyncDirection(), new SyncDirection());
    }
    public WarehouseAbilities(boolean isAllEnabled) {
        this(new SyncDirection(isAllEnabled), new SyncDirection(isAllEnabled), new SyncDirection(isAllEnabled));
    }
}

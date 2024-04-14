package lt.gama.model.type.sync;

import lt.gama.model.type.doc.DocWarehouse;
import lt.gama.model.type.enums.SyncWarehouseType;

import java.io.Serializable;

public record SyncWarehouse(
        SyncWarehouseType type,
        String url,
        String username,
        String password,
        String key,
        DocWarehouse warehouse
) implements Serializable {}

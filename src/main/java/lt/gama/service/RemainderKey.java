package lt.gama.service;

import lt.gama.model.type.part.PartSN;

import java.util.Objects;

public class RemainderKey {
    private long partId;
    private long warehouseId;
    private PartSN partSN;
    private long docId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RemainderKey that = (RemainderKey) o;
        return partId == that.partId && warehouseId == that.warehouseId && docId == that.docId && Objects.equals(partSN, that.partSN);
    }

    @Override
    public int hashCode() {
        return Objects.hash(partId, warehouseId, partSN, docId);
    }

    public RemainderKey setPartId(long partId) {
        this.partId = partId;
        return this;
    }

    public RemainderKey setWarehouseId(long warehouseId) {
        this.warehouseId = warehouseId;
        return this;
    }

    public RemainderKey setPartSN(PartSN partSN) {
        this.partSN = partSN;
        return this;
    }

    public RemainderKey setDocId(long docId) {
        this.docId = docId;
        return this;
    }
}

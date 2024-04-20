package lt.gama.api.request;

import lt.gama.model.type.part.PartSN;

public class InventoryBalanceRequest {

	private long partId;

	private long warehouseId;

	private PartSN sn;

	// generated

	public long getPartId() {
		return partId;
	}

	public void setPartId(long partId) {
		this.partId = partId;
	}

	public long getWarehouseId() {
		return warehouseId;
	}

	public void setWarehouseId(long warehouseId) {
		this.warehouseId = warehouseId;
	}

	public PartSN getSn() {
		return sn;
	}

	public void setSn(PartSN sn) {
		this.sn = sn;
	}

	@Override
	public String toString() {
		return "InventoryBalanceRequest{" +
				"partId=" + partId +
				", warehouseId=" + warehouseId +
				", sn=" + sn +
				'}';
	}
}

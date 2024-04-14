package lt.gama.model.type.sync;

import lt.gama.model.type.doc.DocCounterparty;
import lt.gama.model.type.doc.DocPartSync;
import lt.gama.model.type.doc.DocWarehouse;
import lt.gama.model.type.enums.SyncType;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 2017-10-03.
 */
public class SyncSettings implements Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    /**
     * Sync with external system (e-shop)
     */
    private Boolean syncActive;

    /**
     * Sync with external warehouse
     */
    private Boolean syncWarehouseActive;


    private SyncType type;

    private String url;

    private String id;

    private String key;

    private String timeZone;

    private LocalDate date; // last sync date

    private String statusIds;

    /**
     * Transportation service
     */
    private DocPartSync transportation;

    /**
     * Warehouse for sync, if not set - get from main company settings
     */
    private DocWarehouse warehouse;

    /**
     * The single gama-online client for all e-shop clients.
     * if not specified then different e-shop clients will be imported as different gama-online clients
     */
    private DocCounterparty counterparty;

    /**
     * Part used for sync from eshop to gama, if not found by sku
     */
    private DocPartSync part;

    /**
     * Mark synced Orders with label
     */
    private String label;


    private SyncAbilities abilities;


    private SyncWarehouse syncWarehouse;
    private WarehouseAbilities warehouseAbilities;


    // generated

    public SyncType getType() {
        return type;
    }

    public void setType(SyncType type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getStatusIds() {
        return statusIds;
    }

    public void setStatusIds(String statusIds) {
        this.statusIds = statusIds;
    }

    public DocPartSync getTransportation() {
        return transportation;
    }

    public void setTransportation(DocPartSync transportation) {
        this.transportation = transportation;
    }

    public DocWarehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(DocWarehouse warehouse) {
        this.warehouse = warehouse;
    }

    public DocCounterparty getCounterparty() {
        return counterparty;
    }

    public void setCounterparty(DocCounterparty counterparty) {
        this.counterparty = counterparty;
    }

    public DocPartSync getPart() {
        return part;
    }

    public void setPart(DocPartSync part) {
        this.part = part;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public SyncAbilities getAbilities() {
        return abilities;
    }

    public void setAbilities(SyncAbilities abilities) {
        this.abilities = abilities;
    }

    public SyncWarehouse getSyncWarehouse() {
        return syncWarehouse;
    }

    public void setSyncWarehouse(SyncWarehouse syncWarehouse) {
        this.syncWarehouse = syncWarehouse;
    }

    public WarehouseAbilities getWarehouseAbilities() {
        return warehouseAbilities;
    }

    public void setWarehouseAbilities(WarehouseAbilities warehouseAbilities) {
        this.warehouseAbilities = warehouseAbilities;
    }

    public Boolean getSyncActive() {
        return syncActive;
    }

    public void setSyncActive(Boolean syncActive) {
        this.syncActive = syncActive;
    }

    public Boolean getSyncWarehouseActive() {
        return syncWarehouseActive;
    }

    public void setSyncWarehouseActive(Boolean syncWarehouseActive) {
        this.syncWarehouseActive = syncWarehouseActive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SyncSettings that = (SyncSettings) o;
        return Objects.equals(syncActive, that.syncActive) && Objects.equals(syncWarehouseActive, that.syncWarehouseActive) && type == that.type && Objects.equals(url, that.url) && Objects.equals(id, that.id) && Objects.equals(key, that.key) && Objects.equals(timeZone, that.timeZone) && Objects.equals(date, that.date) && Objects.equals(statusIds, that.statusIds) && Objects.equals(transportation, that.transportation) && Objects.equals(warehouse, that.warehouse) && Objects.equals(counterparty, that.counterparty) && Objects.equals(part, that.part) && Objects.equals(label, that.label) && Objects.equals(abilities, that.abilities) && Objects.equals(syncWarehouse, that.syncWarehouse) && Objects.equals(warehouseAbilities, that.warehouseAbilities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(syncActive, syncWarehouseActive, type, url, id, key, timeZone, date, statusIds, transportation, warehouse, counterparty, part, label, abilities, syncWarehouse, warehouseAbilities);
    }

    @Override
    public String toString() {
        return "SyncSettings{" +
                "syncActive=" + syncActive +
                ", syncWarehouseActive=" + syncWarehouseActive +
                ", type=" + type +
                ", url='" + url + '\'' +
                ", id='" + id + '\'' +
                ", key='" + key + '\'' +
                ", timeZone='" + timeZone + '\'' +
                ", date=" + date +
                ", statusIds='" + statusIds + '\'' +
                ", transportation=" + transportation +
                ", warehouse=" + warehouse +
                ", counterparty=" + counterparty +
                ", part=" + part +
                ", label='" + label + '\'' +
                ", abilities=" + abilities +
                ", syncWarehouse=" + syncWarehouse +
                ", warehouseAbilities=" + warehouseAbilities +
                '}';
    }
}

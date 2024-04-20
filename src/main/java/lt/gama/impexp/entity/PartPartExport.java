package lt.gama.impexp.entity;

import lt.gama.model.type.GamaBigMoney;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.base.BaseDocPart;
import lt.gama.model.type.cf.CFValue;
import lt.gama.model.type.doc.DocWarehouse;
import lt.gama.model.type.enums.PartType;
import lt.gama.model.type.part.DocPartInvoice;
import lt.gama.model.type.part.PartSN;

import java.math.BigDecimal;
import java.util.List;

/**
 * gama-online
 * Created by valdas on 2016-04-03.
 */
public class PartPartExport {

    private Long id;

    private String name;

    private String sku;

    private String barcode;

    private String unit;

    private PartType type;

    private PartSN sn;

    private GamaBigMoney price;

    private BigDecimal quantity;

    /**
     * Is everything is done with part, i.e. calculated balance, cost, etc.
     */
    private boolean finished;

    private GamaMoney total;

    private GamaMoney baseTotal;

    /**
     * Allow forward sell, i.e. to have negative remainder.
     */
    private boolean forwardSell;

    /**
     * if forward sell is allowed this is remainder without cost.
     * Must be resolved in the future.
     */
    private BigDecimal remainder;

    /**
     * Total cost in base currency.
     * p.s. information can be filled later if part can be allowed to sell without knowing cost at the moment of sell.
     * In this case the remainder will be negative and must be compensated later.
     */
    private GamaMoney costTotal;

    /**
     * Cost source info list, i.e. list of purchase/production docs info: id, document date and number, quantity and cost.
     * Information filled in the time of sale, i.e. at finishing invoice document.
     * The information here can be used for the reconstruction of inventory records (purchase, production or others)
     * at finishing returning invoice document (i.e. invoice with negative quantities)
     * which mus be linked through {@link DocPartInvoice#getDiscountDoc() cReturn docReturn} to this document.
     */
    private List<PartCostSourceExport> costInfo;

    private DocWarehouse warehouse;

    /**
     * Part Custom fields
     */
    private List<CFValue> cf;

    /**
     * Returning doc info
     */
    private DocExport docReturn;


    @SuppressWarnings("unused")
    protected PartPartExport() {}

    public PartPartExport(BaseDocPart src) {
        if (src == null) return;
        id = src.getId();
        name = src.getName();
        sku = src.getSku();
        barcode = src.getBarcode();
        unit = src.getUnit();
        type = src.getType();
        sn = src.getSn();
    }

    // generated

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public PartType getType() {
        return type;
    }

    public void setType(PartType type) {
        this.type = type;
    }

    public PartSN getSn() {
        return sn;
    }

    public void setSn(PartSN sn) {
        this.sn = sn;
    }

    public GamaBigMoney getPrice() {
        return price;
    }

    public void setPrice(GamaBigMoney price) {
        this.price = price;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public GamaMoney getTotal() {
        return total;
    }

    public void setTotal(GamaMoney total) {
        this.total = total;
    }

    public GamaMoney getBaseTotal() {
        return baseTotal;
    }

    public void setBaseTotal(GamaMoney baseTotal) {
        this.baseTotal = baseTotal;
    }

    public boolean isForwardSell() {
        return forwardSell;
    }

    public void setForwardSell(boolean forwardSell) {
        this.forwardSell = forwardSell;
    }

    public BigDecimal getRemainder() {
        return remainder;
    }

    public void setRemainder(BigDecimal remainder) {
        this.remainder = remainder;
    }

    public GamaMoney getCostTotal() {
        return costTotal;
    }

    public void setCostTotal(GamaMoney costTotal) {
        this.costTotal = costTotal;
    }

    public List<PartCostSourceExport> getCostInfo() {
        return costInfo;
    }

    public void setCostInfo(List<PartCostSourceExport> costInfo) {
        this.costInfo = costInfo;
    }

    public DocWarehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(DocWarehouse warehouse) {
        this.warehouse = warehouse;
    }

    public List<CFValue> getCf() {
        return cf;
    }

    public void setCf(List<CFValue> cf) {
        this.cf = cf;
    }

    public DocExport getDocReturn() {
        return docReturn;
    }

    public void setDocReturn(DocExport docReturn) {
        this.docReturn = docReturn;
    }

    @Override
    public String toString() {
        return "PartPartExport{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", sku='" + sku + '\'' +
                ", barcode='" + barcode + '\'' +
                ", unit='" + unit + '\'' +
                ", type=" + type +
                ", sn=" + sn +
                ", price=" + price +
                ", quantity=" + quantity +
                ", finished=" + finished +
                ", total=" + total +
                ", baseTotal=" + baseTotal +
                ", forwardSell=" + forwardSell +
                ", remainder=" + remainder +
                ", costTotal=" + costTotal +
                ", costInfo=" + costInfo +
                ", warehouse=" + warehouse +
                ", cf=" + cf +
                ", docReturn=" + docReturn +
                '}';
    }
}

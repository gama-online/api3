package lt.gama.model.dto.entities;

import lt.gama.model.dto.base.BasePartPartApiDto;
import lt.gama.model.type.GamaMoney;

import java.io.Serial;

public class PartPurchaseApiDto extends BasePartPartApiDto {

    @Serial
    private static final long serialVersionUID = -1L;

    private Long recordId;

    /**
     * If 'service' price must be included in cost calculations
     */
    private Boolean inCost;

    /**
     * 3'rd parties expenses amount in the cost
     */
    private GamaMoney expense;

    /**
     *  taxTotal = discountedTotal * vatRate / 100
     */
    private GamaMoney taxTotal;

    private String vendorCode;


    // customized getters/setters

    public boolean isInCost() {
        return inCost != null && inCost;
    }

    // generated
    // except getInCost()


    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public void setInCost(Boolean inCost) {
        this.inCost = inCost;
    }

    public GamaMoney getExpense() {
        return expense;
    }

    public void setExpense(GamaMoney expense) {
        this.expense = expense;
    }

    public GamaMoney getTaxTotal() {
        return taxTotal;
    }

    public void setTaxTotal(GamaMoney taxTotal) {
        this.taxTotal = taxTotal;
    }

    public String getVendorCode() {
        return vendorCode;
    }

    public void setVendorCode(String vendorCode) {
        this.vendorCode = vendorCode;
    }

    @Override
    public String toString() {
        return "PartPurchaseApiDto{" +
                "recordId=" + recordId +
                ", inCost=" + inCost +
                ", expense=" + expense +
                ", taxTotal=" + taxTotal +
                ", vendorCode='" + vendorCode + '\'' +
                "} " + super.toString();
    }
}

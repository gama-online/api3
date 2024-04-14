package lt.gama.model.dto.entities;

import lt.gama.model.dto.base.BasePartPartApiDto;
import lt.gama.model.type.GamaMoney;

import java.math.BigDecimal;

public class PartInventoryApiDto extends BasePartPartApiDto {

    private Long recordId;

    /**
     * if change = true then
     *  1) quantityRemainder = quantityInitial + quantity
     *  2) costTotal is set only if quantity > 0, i.e. parts are added
     * <p>
     * if change = false then
     *  1) quantityTotal is set
     *  2) quantity = quantityInitial - quantityRemainder
     */
    private Boolean change;

    private BigDecimal quantityInitial;

    private GamaMoney costInitial;

    private BigDecimal quantityRemainder;

    private GamaMoney costRemainder;


    // customized getters/setters

    public boolean isChange() {
        return change != null && change;
    }

    // generated
    // except getChange()

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public void setChange(Boolean change) {
        this.change = change;
    }

    public BigDecimal getQuantityInitial() {
        return quantityInitial;
    }

    public void setQuantityInitial(BigDecimal quantityInitial) {
        this.quantityInitial = quantityInitial;
    }

    public GamaMoney getCostInitial() {
        return costInitial;
    }

    public void setCostInitial(GamaMoney costInitial) {
        this.costInitial = costInitial;
    }

    public BigDecimal getQuantityRemainder() {
        return quantityRemainder;
    }

    public void setQuantityRemainder(BigDecimal quantityRemainder) {
        this.quantityRemainder = quantityRemainder;
    }

    public GamaMoney getCostRemainder() {
        return costRemainder;
    }

    public void setCostRemainder(GamaMoney costRemainder) {
        this.costRemainder = costRemainder;
    }

    @Override
    public String toString() {
        return "PartInventoryApiDto{" +
                "recordId=" + recordId +
                ", change=" + change +
                ", quantityInitial=" + quantityInitial +
                ", costInitial=" + costInitial +
                ", quantityRemainder=" + quantityRemainder +
                ", costRemainder=" + costRemainder +
                "} " + super.toString();
    }
}

package lt.gama.model.type.inventory.taxfree;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

public class Good implements Serializable {
    @Serial
    private static final long serialVersionUID = -1L;

    private int sequenceNo;
    private String description;
    private BigDecimal quantity;
    private String unitOfMeasureCode;
    private String unitOfMeasureOther;
    private BigDecimal taxableAmount;
    private BigDecimal vatRate;
    private BigDecimal vatAmount;
    private BigDecimal totalAmount;

    private BigDecimal acceptedQuantity;
    private BigDecimal acceptedVatAmount;

    public Good() {
    }

    public Good(int sequenceNo, String description, BigDecimal quantity,
                String unitOfMeasureCode, String unitOfMeasureOther,
                BigDecimal taxableAmount, BigDecimal vatRate, BigDecimal vatAmount, BigDecimal totalAmount) {
        this.sequenceNo = sequenceNo;
        this.description = description;
        this.quantity = quantity;
        this.unitOfMeasureCode = unitOfMeasureCode;
        this.unitOfMeasureOther = unitOfMeasureOther;
        this.taxableAmount = taxableAmount;
        this.vatRate = vatRate;
        this.vatAmount = vatAmount;
        this.totalAmount = totalAmount;
    }

    // generated

    public int getSequenceNo() {
        return sequenceNo;
    }

    public void setSequenceNo(int sequenceNo) {
        this.sequenceNo = sequenceNo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getUnitOfMeasureCode() {
        return unitOfMeasureCode;
    }

    public void setUnitOfMeasureCode(String unitOfMeasureCode) {
        this.unitOfMeasureCode = unitOfMeasureCode;
    }

    public String getUnitOfMeasureOther() {
        return unitOfMeasureOther;
    }

    public void setUnitOfMeasureOther(String unitOfMeasureOther) {
        this.unitOfMeasureOther = unitOfMeasureOther;
    }

    public BigDecimal getTaxableAmount() {
        return taxableAmount;
    }

    public void setTaxableAmount(BigDecimal taxableAmount) {
        this.taxableAmount = taxableAmount;
    }

    public BigDecimal getVatRate() {
        return vatRate;
    }

    public void setVatRate(BigDecimal vatRate) {
        this.vatRate = vatRate;
    }

    public BigDecimal getVatAmount() {
        return vatAmount;
    }

    public void setVatAmount(BigDecimal vatAmount) {
        this.vatAmount = vatAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getAcceptedQuantity() {
        return acceptedQuantity;
    }

    public void setAcceptedQuantity(BigDecimal acceptedQuantity) {
        this.acceptedQuantity = acceptedQuantity;
    }

    public BigDecimal getAcceptedVatAmount() {
        return acceptedVatAmount;
    }

    public void setAcceptedVatAmount(BigDecimal acceptedVatAmount) {
        this.acceptedVatAmount = acceptedVatAmount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Good good = (Good) o;
        return sequenceNo == good.sequenceNo && Objects.equals(description, good.description) && Objects.equals(quantity, good.quantity) && Objects.equals(unitOfMeasureCode, good.unitOfMeasureCode) && Objects.equals(unitOfMeasureOther, good.unitOfMeasureOther) && Objects.equals(taxableAmount, good.taxableAmount) && Objects.equals(vatRate, good.vatRate) && Objects.equals(vatAmount, good.vatAmount) && Objects.equals(totalAmount, good.totalAmount) && Objects.equals(acceptedQuantity, good.acceptedQuantity) && Objects.equals(acceptedVatAmount, good.acceptedVatAmount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sequenceNo, description, quantity, unitOfMeasureCode, unitOfMeasureOther, taxableAmount, vatRate, vatAmount, totalAmount, acceptedQuantity, acceptedVatAmount);
    }

    @Override
    public String toString() {
        return "Good{" +
                "sequenceNo=" + sequenceNo +
                ", description='" + description + '\'' +
                ", quantity=" + quantity +
                ", unitOfMeasureCode='" + unitOfMeasureCode + '\'' +
                ", unitOfMeasureOther='" + unitOfMeasureOther + '\'' +
                ", taxableAmount=" + taxableAmount +
                ", vatRate=" + vatRate +
                ", vatAmount=" + vatAmount +
                ", totalAmount=" + totalAmount +
                ", acceptedQuantity=" + acceptedQuantity +
                ", acceptedVatAmount=" + acceptedVatAmount +
                '}';
    }
}

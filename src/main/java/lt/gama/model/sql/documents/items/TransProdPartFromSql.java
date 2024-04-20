package lt.gama.model.sql.documents.items;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lt.gama.model.type.ibase.IBaseDocPartOutRemainder;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue(TransProdPartFromSql.DISCRIMINATOR_VALUE_FROM)
public class TransProdPartFromSql extends TransProdPartSql implements IBaseDocPartOutRemainder {

    public static final String DISCRIMINATOR_VALUE_FROM = "F";

    /**
     * Remainder to complete production process
     */
    private BigDecimal remainder;

    /**
     * Is part moved into warehouse for reservation
     */
    private Boolean reserved;

    /**
     * Reserved quantity, i.e. moved into reservation warehouse.
     * The rest, i.e. quantity - reservedQuantity should be ordered
     */
    private BigDecimal reservedQuantity;


    public void reset() {
        reserved = false;
    }

    // customized getters/setters

    public boolean isReserved() {
        return reserved != null && reserved;
    }

    // generated

    @Override
    public BigDecimal getRemainder() {
        return remainder;
    }

    @Override
    public void setRemainder(BigDecimal remainder) {
        this.remainder = remainder;
    }

    public void setReserved(Boolean reserved) {
        this.reserved = reserved;
    }

    public BigDecimal getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(BigDecimal reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }

    @Override
    public String toString() {
        return "TransProdPartFromSql{" +
                "remainder=" + remainder +
                ", reserved=" + reserved +
                ", reservedQuantity=" + reservedQuantity +
                "} " + super.toString();
    }
}

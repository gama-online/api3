package lt.gama.model.sql.documents.items;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue(TransProdPartToSql.DISCRIMINATOR_VALUE_TO)
public class TransProdPartToSql extends TransProdPartSql {

    public static final String DISCRIMINATOR_VALUE_TO = "T";

    private BigDecimal costPercent;


    public void reset() {
        setFinished(false);
    }

    // generated

    public BigDecimal getCostPercent() {
        return costPercent;
    }

    public void setCostPercent(BigDecimal costPercent) {
        this.costPercent = costPercent;
    }

    @Override
    public String toString() {
        return "TransProdPartToSql{" +
                "costPercent=" + costPercent +
                "} " + super.toString();
    }
}

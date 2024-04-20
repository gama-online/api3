package lt.gama.model.sql.entities;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue(RecipePartToSql.DISCRIMINATOR_VALUE_TO)
public class RecipePartToSql extends RecipePartSql {

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
        return "RecipePartToSql{" +
                "costPercent=" + costPercent +
                "} " + super.toString();
    }
}

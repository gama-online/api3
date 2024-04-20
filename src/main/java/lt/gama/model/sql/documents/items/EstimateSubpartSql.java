package lt.gama.model.sql.documents.items;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lt.gama.model.i.IParentLinkUuid;

import java.util.UUID;

@Entity
@DiscriminatorValue(EstimateSubpartSql.DISCRIMINATOR_VALUE_SUB_PART)
public class EstimateSubpartSql extends EstimateBasePartSql implements IParentLinkUuid {

    public static final String DISCRIMINATOR_VALUE_SUB_PART = "S";

    private UUID parentLinkUuid;

    // generated

    @Override
    public UUID getParentLinkUuid() {
        return parentLinkUuid;
    }

    @Override
    public void setParentLinkUuid(UUID parentLinkUuid) {
        this.parentLinkUuid = parentLinkUuid;
    }

    @Override
    public String toString() {
        return "EstimateSubPartSql{" +
                "parentLinkUuid=" + parentLinkUuid +
                "} " + super.toString();
    }
}

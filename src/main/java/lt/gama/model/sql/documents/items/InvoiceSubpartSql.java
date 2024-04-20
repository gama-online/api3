package lt.gama.model.sql.documents.items;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lt.gama.model.i.IParentLinkUuid;

import java.util.UUID;

@Entity
@DiscriminatorValue(InvoiceSubpartSql.DISCRIMINATOR_VALUE_SUB_PART)
public class InvoiceSubpartSql extends InvoiceBasePartSql implements IParentLinkUuid {

    public static final String DISCRIMINATOR_VALUE_SUB_PART = "S";

    private UUID parentLinkUuid;

    // generated

    public UUID getParentLinkUuid() {
        return parentLinkUuid;
    }

    public void setParentLinkUuid(UUID parentLinkUuid) {
        this.parentLinkUuid = parentLinkUuid;
    }

    @Override
    public String toString() {
        return "InvoiceSubPartSql{" +
                "parentLinkUuid=" + parentLinkUuid +
                "} " + super.toString();
    }
}

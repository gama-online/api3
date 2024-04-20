package lt.gama.model.sql.system;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lt.gama.model.i.IId;
import lt.gama.model.sql.base.BaseEntitySql;
import lt.gama.model.type.GamaMoney;


@Entity
@Table(name = "system_settings")
public class SystemSettingsSql extends BaseEntitySql implements IId<Long> {

    public static final long ID = 1L;

    @Id
    private Long id = ID;

    /**
     * Default active account price
     */
    @Embedded
    private GamaMoney accountPrice;

    /**
     * Company id - the owner of the system
     */
    private Long ownerCompanyId;

    /**
     * Subscription service id - service id in the owner company used to generate invoice
     */
    private Long subscriptionServiceId;

    /**
     * Subscription warehouse id - warehouse id in the owner company used to generate invoice
     */
    private Long subscriptionWarehouseId;

    // generated

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public GamaMoney getAccountPrice() {
        return accountPrice;
    }

    public void setAccountPrice(GamaMoney accountPrice) {
        this.accountPrice = accountPrice;
    }

    public Long getOwnerCompanyId() {
        return ownerCompanyId;
    }

    public void setOwnerCompanyId(Long ownerCompanyId) {
        this.ownerCompanyId = ownerCompanyId;
    }

    public Long getSubscriptionServiceId() {
        return subscriptionServiceId;
    }

    public void setSubscriptionServiceId(Long subscriptionServiceId) {
        this.subscriptionServiceId = subscriptionServiceId;
    }

    public Long getSubscriptionWarehouseId() {
        return subscriptionWarehouseId;
    }

    public void setSubscriptionWarehouseId(Long subscriptionWarehouseId) {
        this.subscriptionWarehouseId = subscriptionWarehouseId;
    }

    @Override
    public String toString() {
        return "SystemSettingsSql{" +
                "id=" + id +
                ", accountPrice=" + accountPrice +
                ", ownerCompanyId=" + ownerCompanyId +
                ", subscriptionServiceId=" + subscriptionServiceId +
                ", subscriptionWarehouseId=" + subscriptionWarehouseId +
                "} " + super.toString();
    }
}

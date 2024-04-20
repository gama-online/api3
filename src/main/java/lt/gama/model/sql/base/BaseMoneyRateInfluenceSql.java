package lt.gama.model.sql.base;

import jakarta.persistence.MappedSuperclass;
import lt.gama.helpers.BooleanUtils;

import java.util.Set;


@MappedSuperclass
public abstract class BaseMoneyRateInfluenceSql extends BaseDocumentSql {

    public abstract Set<? extends BaseMoneyBalanceSql> getAccounts();

    public boolean hasItemFinished() {
        if (getAccounts() == null) return false;
        return getAccounts().stream().anyMatch(x -> BooleanUtils.isTrue(x.getFinished()));
    }

    public boolean hasItemUnfinished() {
        if (getAccounts() == null) return false;
        return getAccounts().stream().anyMatch(x -> BooleanUtils.isNotTrue(x.getFinished()));
    }

    public void setItemsFinished(Boolean finished) {
        if (getAccounts() == null) return;
        getAccounts().forEach(x -> x.setFinished(finished));
    }

    @Override
    public boolean isFullyFinished() {
        return super.isFullyFinished() && !hasItemUnfinished();
    }

    @Override
    public boolean setFullyFinished() {
        boolean changed = super.setFullyFinished() || !hasItemUnfinished();
        setItemsFinished(true);
        return changed;
    }
}

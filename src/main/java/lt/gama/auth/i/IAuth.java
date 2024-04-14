package lt.gama.auth.i;

import lt.gama.helpers.CollectionsHelper;
import lt.gama.helpers.StringHelper;
import lt.gama.model.type.auth.CompanySettings;

import java.time.Instant;
import java.util.Set;

public interface IAuth extends IPermission {

    default boolean hasValue() {
        return StringHelper.hasValue(getId()) && getCompanyId() != null && CollectionsHelper.hasValue(getPermissions());
    }

    String getId();

    String getName();

    Long getEmployeeId();

    Long getCompanyId();

    String getUuid();

    Boolean getRefresh();

    Boolean getAdmin();

    Boolean getImpersonated() ;

    Instant getExpiresAt();

    CompanySettings getSettings();

    default boolean isMigrating() { return false; }

    void setId(String id);

    void setName(String name);

    void setEmployeeId(Long employeeId);

    void setCompanyId(Long companyId);

    void setUuid(String uuid);

    void setRefresh(Boolean refresh);

    void setAdmin(Boolean admin);

    void setImpersonated(Boolean impersonated);

    void setPermissions(Set<String> permissions);

    void setExpiresAt(Instant validDate);

    void setSettings(CompanySettings settings);

    default String getLanguage() {
        return getSettings() != null ? getSettings().getLanguage() : null;
    }

    default void setMigrating(boolean migrated) {}

    default void clear() {
        setId(null);
        setName(null);
        setEmployeeId(null);
        setCompanyId(null);
        setUuid(null);
        setRefresh(null);
        setAdmin(null);
        setImpersonated(null);
        setPermissions(null);
        setExpiresAt(null);
        setSettings(null);
        setMigrating(false);
    }

    default void cloneFrom(IAuth auth) {
        if (this != auth) clear();
        if (auth != null) {
            setId(auth.getId());
            setName(auth.getName());
            setEmployeeId(auth.getEmployeeId());
            setCompanyId(auth.getCompanyId());
            setUuid(auth.getUuid());
            setRefresh(auth.getRefresh());
            setAdmin(auth.getAdmin());
            setImpersonated(auth.getImpersonated());
            setPermissions(auth.getPermissions());
            setExpiresAt(auth.getExpiresAt());
            setSettings(auth.getSettings());
            setMigrating(auth.isMigrating());
        }
    }
}

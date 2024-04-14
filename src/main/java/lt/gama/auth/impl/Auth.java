package lt.gama.auth.impl;

import lt.gama.auth.i.IAuth;
import lt.gama.model.type.auth.CompanySettings;

import java.time.Instant;
import java.util.Set;

public class Auth implements IAuth {

    /**
     * employee login name or programme id
     */
    private String id;

    /**
     * Employee/Programme name
     */
    private String name;

    private Long employeeId;

    /**
     * Auth data uuid
     */
    private String uuid;

    /**
     * is refresh token?
     */
    Boolean refresh;

    private Boolean admin;

    private Boolean impersonated;

    private Set<String> permissions;

    /**
     * Trial period expires at
     */
    private Instant expiresAt;

    private Long companyId;

    private CompanySettings settings;

    private boolean migrating;

    // generated

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Long getEmployeeId() {
        return employeeId;
    }

    @Override
    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public Boolean getRefresh() {
        return refresh;
    }

    @Override
    public void setRefresh(Boolean refresh) {
        this.refresh = refresh;
    }

    @Override
    public Boolean getAdmin() {
        return admin;
    }

    @Override
    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    @Override
    public Boolean getImpersonated() {
        return impersonated;
    }

    @Override
    public void setImpersonated(Boolean impersonated) {
        this.impersonated = impersonated;
    }

    @Override
    public Set<String> getPermissions() {
        return permissions;
    }

    @Override
    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    @Override
    public Instant getExpiresAt() {
        return expiresAt;
    }

    @Override
    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    @Override
    public Long getCompanyId() {
        return companyId;
    }

    @Override
    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    @Override
    public CompanySettings getSettings() {
        return settings;
    }

    @Override
    public void setSettings(CompanySettings settings) {
        this.settings = settings;
    }

    @Override
    public boolean isMigrating() {
        return migrating;
    }

    @Override
    public void setMigrating(boolean migrating) {
        this.migrating = migrating;
    }

    @Override
    public String toString() {
        return "Auth{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", employeeId=" + employeeId +
                ", uuid='" + uuid + '\'' +
                ", refresh=" + refresh +
                ", admin=" + admin +
                ", impersonated=" + impersonated +
                ", permissions=" + permissions +
                ", expiresAt=" + expiresAt +
                ", companyId=" + companyId +
                ", settings=" + settings +
                ", migrating=" + migrating +
                '}';
    }
}

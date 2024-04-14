package lt.gama.model.dto.entities;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lt.gama.model.type.auth.AccountInfo;
import lt.gama.model.dto.base.BaseEntityDto;
import lt.gama.model.i.IId;
import lt.gama.service.json.ser.LocalDateTimeTZSerializer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;

public class AccountDto extends BaseEntityDto implements IId<String> {

    private String id;

    private ArrayList<AccountInfo> companies;

    private Integer companyIndex;

    private Boolean admin;

    /**
     * reset password token
     */
    private String resetToken;

    private LocalDateTime resetTokenDate;

    /**
     * Refresh token for getting new access token
     */
    private String refreshToken;

    @JsonSerialize(using = LocalDateTimeTZSerializer.class)
    private LocalDateTime refreshTokenDate;

    @JsonSerialize(using = LocalDateTimeTZSerializer.class)
    private LocalDateTime lastLogin;

    /**
     * if set then all employees/connections in all companies of this account will be paid by payer company
     */
    private CompanyDto payer;

    public AccountDto() {
    }

    public AccountDto(String id, ArrayList<AccountInfo> companies, LocalDateTime lastLogin, CompanyDto payer) {
        this.id = id;
        this.companies = companies;
        this.lastLogin = lastLogin;
        this.payer = payer;
    }

    /**
     * toString without payer
     */
    @Override
    public String toString() {
        return "AccountDto{" +
                "id='" + id + '\'' +
                ", companies=" + companies +
                ", companyIndex=" + companyIndex +
                ", admin=" + admin +
                ", resetToken='" + resetToken + '\'' +
                ", resetTokenDate=" + resetTokenDate +
                ", refreshToken='" + refreshToken + '\'' +
                ", refreshTokenDate=" + refreshTokenDate +
                ", lastLogin=" + lastLogin +
                "} " + super.toString();
    }

    // generated

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<AccountInfo> getCompanies() {
        return companies;
    }

    public void setCompanies(ArrayList<AccountInfo> companies) {
        this.companies = companies;
    }

    public Integer getCompanyIndex() {
        return companyIndex;
    }

    public void setCompanyIndex(Integer companyIndex) {
        this.companyIndex = companyIndex;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public LocalDateTime getResetTokenDate() {
        return resetTokenDate;
    }

    public void setResetTokenDate(LocalDateTime resetTokenDate) {
        this.resetTokenDate = resetTokenDate;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public LocalDateTime getRefreshTokenDate() {
        return refreshTokenDate;
    }

    public void setRefreshTokenDate(LocalDateTime refreshTokenDate) {
        this.refreshTokenDate = refreshTokenDate;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public CompanyDto getPayer() {
        return payer;
    }

    public void setPayer(CompanyDto payer) {
        this.payer = payer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AccountDto that = (AccountDto) o;
        return Objects.equals(id, that.id) && Objects.equals(companies, that.companies) && Objects.equals(companyIndex, that.companyIndex) && Objects.equals(admin, that.admin) && Objects.equals(resetToken, that.resetToken) && Objects.equals(resetTokenDate, that.resetTokenDate) && Objects.equals(refreshToken, that.refreshToken) && Objects.equals(refreshTokenDate, that.refreshTokenDate) && Objects.equals(lastLogin, that.lastLogin) && Objects.equals(payer, that.payer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, companies, companyIndex, admin, resetToken, resetTokenDate, refreshToken, refreshTokenDate, lastLogin, payer);
    }
}

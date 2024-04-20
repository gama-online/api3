package lt.gama.model.sql.system;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.*;
import lt.gama.model.i.IId;
import lt.gama.model.sql.base.BaseEntitySql;
import lt.gama.model.type.auth.AccountInfo;
import lt.gama.service.json.ser.LocalDateTimeTZSerializer;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Entity
@Table(name = "accounts")
@NamedEntityGraph(name = AccountSql.GRAPH_ALL, attributeNodes = @NamedAttributeNode("payer"))
public class AccountSql extends BaseEntitySql implements IId<String> {

    public static final String GRAPH_ALL = "graph.AccountSql.all";

    private static final long serialVersionUID = -1L;

    @Id
    private String id;

    private String password;

    private String salt;

    @JdbcTypeCode(SqlTypes.JSON)
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
    @ManyToOne(fetch = FetchType.LAZY)
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "payer_id")
    CompanySql payer;

    public AccountSql() {
    }

    public AccountSql(String id) {
        this.id = id;
    }

    /**
     * toString except payer
     */
    @Override
    public String toString() {
        return "AccountSql{" +
                "id='" + id + '\'' +
                ", password='" + password + '\'' +
                ", salt='" + salt + '\'' +
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
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

    public CompanySql getPayer() {
        return payer;
    }

    public void setPayer(CompanySql payer) {
        this.payer = payer;
    }
}

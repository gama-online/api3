package lt.gama.model.type.doc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.Hidden;
import lt.gama.helpers.StringHelper;
import lt.gama.model.i.IBankAccount;
import lt.gama.model.type.base.BaseDocEntity;
import lt.gama.model.type.enums.DBType;

import java.io.Serial;
import java.util.Objects;

/**
 * Gama
 * Created by valdas on 15-03-14.
 */
public class DocBankAccount extends BaseDocEntity implements IBankAccount {

    @Serial
    private static final long serialVersionUID = -1L;

    private String account;

    private DocBank bank;

    public DocBankAccount() {}

    public DocBankAccount(Long id, DBType db, String account, DocBank bank) {
        setId(id);
        setDb(db);
        this.account = account;
        this.bank = bank;
    }

    public DocBankAccount(String account) {
        this.account = account;
    }

    public DocBankAccount(IBankAccount account) {
        if (account == null) return;
        setId(account.getId());
        this.account = account.getAccount();
        this.bank = account.getBank();
        setDb(account.getDb());
    }

    @Hidden
    @JsonIgnore
    public String getAccountCompressed() {
        return account == null ? null : StringHelper.normalizeBankAccount(account);
    }

    // generated

    @Override
    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    @Override
    public DocBank getBank() {
        return bank;
    }

    public void setBank(DocBank bank) {
        this.bank = bank;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DocBankAccount that = (DocBankAccount) o;
        return Objects.equals(account, that.account) && Objects.equals(bank, that.bank);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), account, bank);
    }

    @Override
    public String toString() {
        return "DocBankAccount{" +
                "account='" + account + '\'' +
                ", bank=" + bank +
                "} " + super.toString();
    }
}

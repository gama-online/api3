package lt.gama.model.type.doc;

import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.base.BaseMoneyBalance;
import lt.gama.model.type.enums.DBType;

/**
 * Gama
 * Created by valdas on 15-05-11.
 */
public class DocBankAccountBalance extends BaseMoneyBalance {

    private DocBankAccount account;

    public DocBankAccountBalance() {
    }

    public DocBankAccountBalance(DocBankAccount account, GamaMoney sum) {
        this.account = account;
        this.setSum(sum);
    }

    public DocBankAccountBalance(DocBankAccount account, GamaMoney sum, GamaMoney baseSum, GamaMoney baseFixSum) {
        super(sum, baseSum, baseFixSum);
        this.account = account;
    }

    public DocBankAccount getBankAccount() {
        return account;
    }

    public void setBankAccount(DocBankAccount bankAccount) {
        this.account = bankAccount;
    }

    @Override
    public Long getAccountId() {
        return account != null ? account.getId() : null;
    }

    @Override
    public String getAccountName() {
        return account != null ? account.getAccount() : null;
    }

    @Override
    public DBType getAccountDb() {
        return account != null ? account.getDb() : null;
    }

    // generated

    @Override
    public DocBankAccount getAccount() {
        return account;
    }

    public void setAccount(DocBankAccount account) {
        this.account = account;
    }
}

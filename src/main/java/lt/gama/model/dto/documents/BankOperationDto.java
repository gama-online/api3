package lt.gama.model.dto.documents;

import lt.gama.model.dto.base.BaseMoneyDocumentDto;
import lt.gama.model.dto.entities.BankAccountDto;

import java.io.Serial;

public class BankOperationDto extends BaseMoneyDocumentDto {

    @Serial
    private static final long serialVersionUID = -1L;


    /**
     * Our bank account
     */
    private BankAccountDto bankAccount;

    /**
     * Counterparty or Employee account number or our second account if operation is between two our accounts
     */
    private BankAccountDto bankAccount2;

    private Boolean cashOperation;

    private Boolean paymentCode;


    @Deprecated
    private BankAccountDto getAccount() {
        return getBankAccount();
    }

    @Deprecated
    private BankAccountDto getAccount2() {
        return getBankAccount2();
    }

    // for imports compatibility with old versions

    public void setAccount(BankAccountDto bankAccount) {
        setBankAccount(bankAccount);
    }

    // generated

    @Override
    public BankAccountDto getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccountDto bankAccount) {
        this.bankAccount = bankAccount;
    }

    @Override
    public BankAccountDto getBankAccount2() {
        return bankAccount2;
    }

    public void setBankAccount2(BankAccountDto bankAccount2) {
        this.bankAccount2 = bankAccount2;
    }

    public Boolean getCashOperation() {
        return cashOperation;
    }

    public void setCashOperation(Boolean cashOperation) {
        this.cashOperation = cashOperation;
    }

    public Boolean getPaymentCode() {
        return paymentCode;
    }

    public void setPaymentCode(Boolean paymentCode) {
        this.paymentCode = paymentCode;
    }

    @Override
    public String toString() {
        return "BankOperationDto{" +
                "bankAccount=" + bankAccount +
                ", bankAccount2=" + bankAccount2 +
                ", cashOperation=" + cashOperation +
                ", paymentCode=" + paymentCode +
                "} " + super.toString();
    }
}

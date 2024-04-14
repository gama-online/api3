package lt.gama.model.i;

import lt.gama.model.type.Exchange;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.enums.AccountType;

import java.time.LocalDate;

/**
 * Gama
 * Created by valdas on 15-05-11.
 */
public interface IMoneyDocument extends ICompany, IId<Long> {

    LocalDate getDate();

    boolean isFinishedMoneyType(AccountType accountType);

    void setFinishedMoneyType(AccountType accountType, boolean finished);

    Exchange getExchange();

    void setExchange(Exchange exchange);

    GamaMoney getAmount();

    void setAmount(GamaMoney amount);

    GamaMoney getBaseAmount();

    void setBaseAmount(GamaMoney baseAmount);

    IDocEmployee getEmployee();

    ICounterparty getCounterparty();

    ICash getCash();

    IBankAccount getBankAccount();

    IBankAccount getBankAccount2();
}

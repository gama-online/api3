package lt.gama.model.sql.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lt.gama.helpers.StringHelper;
import lt.gama.model.dto.entities.BankAccountDto;
import lt.gama.model.i.IBankAccount;
import lt.gama.model.i.IFront;
import lt.gama.model.i.IName;
import lt.gama.model.sql.base.BaseMoneyAccountSql;
import lt.gama.model.type.auth.BankCard;
import lt.gama.model.type.doc.DocBank;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "bank_accounts")
public class BankAccountSql extends BaseMoneyAccountSql<BankAccountDto> implements IName, IBankAccount, IFront<BankAccountDto> {

	private String account;

	@Embedded
	private DocBank bank;

	/**
	 * Print bank account on invoice
	 */
	private Boolean invoice;

    /**
     * Linked bank cards
     * Stored only first 6 and last 4 digits !!!! - 1234 56** **** 1234
     */
	@JdbcTypeCode(SqlTypes.JSON)
	private List<BankCard> cards;

	@JsonIgnore
	public String getAccountCompressed() {
		return account == null ? null : StringHelper.deleteSpaces(account);
	}

	@JsonIgnore
	public String getName() {
		return getAccount();
	}

	@Override
	public void setName(String name) {
		setAccount(name);
	}

	public boolean isInvoice() {
		return invoice != null && invoice;
	}

    @Override
    public BankAccountDto doc() {
        return new BankAccountDto(this);
    }

    @Override
    public BankAccountDto front() {
        BankAccountDto front = new BankAccountDto();
        front.setId(getId());
        front.setName(getName());
        front.setUsedCurrencies(getUsedCurrencies());
        front.setMoneyAccount(getMoneyAccount());
		return front;
	}

	// generated
	// except getInvoice()


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

	public void setInvoice(Boolean invoice) {
		this.invoice = invoice;
	}

	public List<BankCard> getCards() {
		return cards;
	}

	public void setCards(List<BankCard> cards) {
		this.cards = cards;
	}
}

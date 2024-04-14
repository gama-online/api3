package lt.gama.model.dto.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lt.gama.helpers.StringHelper;
import lt.gama.model.type.auth.BankCard;
import lt.gama.model.type.doc.DocBank;
import lt.gama.model.dto.base.BaseMoneyAccountDocumentDto;
import lt.gama.model.i.IBankAccount;
import lt.gama.model.i.IFront;
import lt.gama.model.i.IName;
import lt.gama.model.type.enums.DBType;

import java.io.Serial;
import java.util.List;

public class BankAccountDto extends BaseMoneyAccountDocumentDto<BankAccountDto> implements IName, IBankAccount, IFront<BankAccountDto> {

	@Serial
	private static final long serialVersionUID = -1L;

	private String account;

	private DocBank bank;

	/**
	 * Print bank account on invoice
	 */
	private Boolean invoice;

    /**
     * Linked bank cards
     * Stored only first 6 and last 4 digits !!!! - 1234 56** **** 1234
     */
	private List<BankCard> cards;

	public BankAccountDto() {
	}

	public BankAccountDto(Long id) {
		setId(id);
	}

	public BankAccountDto(Long id, DBType db) {
		setId(id);
		setDb(db);
	}

	public BankAccountDto(IBankAccount bankAccount) {
		if (bankAccount == null) return;
		setId(bankAccount.getId());
		setDb(bankAccount.getDb());
		this.account = bankAccount.getAccount();
		this.bank = bankAccount.getBank();
	}

	public BankAccountDto(Long id, DBType db, String account, DocBank bank) {
		setId(id);
		setDb(db);
		this.account = account;
		this.bank = bank;
	}

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

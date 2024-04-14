package lt.gama.model.dto.entities;

import lt.gama.model.dto.base.BaseMoneyAccountDocumentDto;
import lt.gama.model.i.ICash;
import lt.gama.model.i.IFront;
import lt.gama.model.i.IName;
import lt.gama.model.type.enums.DBType;

import java.io.Serial;

public class CashDto extends BaseMoneyAccountDocumentDto<CashDto> implements IName, ICash, IFront<CashDto> {

	@Serial
	private static final long serialVersionUID = -1L;

	private String name;

	private String cashier;

	public CashDto() {
	}

	public CashDto(Long id, DBType db) {
		setId(id);
		setDb(db);
	}

	public CashDto(ICash cash) {
		if (cash == null) return;
		setId(cash.getId());
		setDb(cash.getDb());
		this.name = cash.getName();
		this.cashier = cash.getCashier();
	}

	@Override
    public CashDto doc() {
        return new CashDto(this);
    }

	@Override
	public CashDto front() {
		CashDto front = new CashDto();
        front.setId(getId());
		front.setName(getName());
		front.setCashier(getCashier());
		front.setUsedCurrencies(getUsedCurrencies());
		front.setMoneyAccount(getMoneyAccount());
		return front;
	}

	// generated

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getCashier() {
		return cashier;
	}

	public void setCashier(String cashier) {
		this.cashier = cashier;
	}

	@Override
	public String toString() {
		return "CashDto{" +
				"name='" + name + '\'' +
				", cashier='" + cashier + '\'' +
				"} " + super.toString();
	}
}

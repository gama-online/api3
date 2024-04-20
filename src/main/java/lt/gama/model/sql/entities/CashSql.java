package lt.gama.model.sql.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lt.gama.model.dto.entities.CashDto;
import lt.gama.model.i.ICash;
import lt.gama.model.i.IFront;
import lt.gama.model.i.IName;
import lt.gama.model.sql.base.BaseMoneyAccountSql;


@Entity
@Table(name = "cash")
public class CashSql extends BaseMoneyAccountSql<CashDto> implements IName, ICash, IFront<CashDto> {

	private String name;

	private String cashier;

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
		return "CashSql{" +
				"name='" + name + '\'' +
				", cashier='" + cashier + '\'' +
				"} " + super.toString();
	}
}

package lt.gama.model.i;

import lt.gama.model.type.NameContact;
import lt.gama.model.type.doc.DocBankAccount;
import lt.gama.model.type.enums.DBType;
import lt.gama.model.type.enums.DebtType;
import lt.gama.model.type.enums.TaxpayerType;
import lt.gama.model.type.gl.GLOperationAccount;

import java.util.List;
import java.util.Map;

public interface ICounterparty extends IId<Long>, IName, ILocations {

    String getName();

	String getShortName();

	String getComCode();

	String getVatCode();

	List<NameContact> getContacts();

	List<DocBankAccount> getBanks();

	GLOperationAccount getAccount(DebtType type);

	Map<String, GLOperationAccount> getAccounts();

	Integer getCreditTerm();

	GLOperationAccount getNoDebtAccount();

	Boolean getNoDebt();

	TaxpayerType getTaxpayerType();

	DBType getDb();
}

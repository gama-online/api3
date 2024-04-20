package lt.gama.impexp.map;

import lt.gama.helpers.CSVRecordUtils;
import lt.gama.impexp.MapBase;
import lt.gama.model.sql.entities.CounterpartySql;
import lt.gama.model.type.doc.DocBankAccount;
import lt.gama.model.type.enums.DebtType;
import lt.gama.model.type.gl.GLOperationAccount;
import org.apache.commons.csv.CSVRecord;

import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;

public class MapCounterparty extends MapBase<CounterpartySql> {

    @Serial
    private static final long serialVersionUID = -1L;

	@Override
	public Class<CounterpartySql> getEntityClass() {
		return CounterpartySql.class;
	}

	@Override
	public CounterpartySql importCSV(CSVRecord record) {
		CounterpartySql entity = new CounterpartySql();
		entity.setName(CSVRecordUtils.getString(record, "name", "(no-name)"));
		entity.setShortName(CSVRecordUtils.getString(record, "shortName"));
		entity.setComCode(CSVRecordUtils.getString(record, "comCode"));
		entity.setVatCode(CSVRecordUtils.getString(record, "vatCode"));
		entity.setBanks(new ArrayList<>());
		entity.getBanks().add(new DocBankAccount(CSVRecordUtils.getString(record, "bankAccount")));

        DebtType counterpartyType = DebtType.from(CSVRecordUtils.getString(record, "accountType"));
        if (counterpartyType != null) {
            String number = CSVRecordUtils.getString(record, "accountNumber");
            String name = CSVRecordUtils.getString(record, "accountName");
            if (number != null) {
                entity.setAccounts(new HashMap<>());
                entity.getAccounts().put(counterpartyType.toString(),
                        new GLOperationAccount(number, name != null ? name : ("Account " + number)));
            }
        }
        return entity;
    }
}

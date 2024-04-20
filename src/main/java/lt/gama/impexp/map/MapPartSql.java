package lt.gama.impexp.map;

import lt.gama.helpers.CSVRecordUtils;
import lt.gama.impexp.MapBase;
import lt.gama.model.sql.entities.PartSql;
import org.apache.commons.csv.CSVRecord;

public class MapPartSql extends MapBase<PartSql> {

	private static final long serialVersionUID = -1L;

	@Override
	public Class<PartSql> getEntityClass() {
		return PartSql.class;
	}

	@Override
	public PartSql importCSV(CSVRecord record) {
		PartSql entity = new PartSql();
		entity.setName(CSVRecordUtils.getString(record, "name", "(no-name)"));
		entity.setSku(CSVRecordUtils.getString(record, "sku"));
		entity.setBarcode(CSVRecordUtils.getString(record, "barcode"));
		entity.setUnit(CSVRecordUtils.getString(record, "unit"));
		entity.setPrice(CSVRecordUtils.getBigMoney(record, "price"));
		return entity;
	}
}

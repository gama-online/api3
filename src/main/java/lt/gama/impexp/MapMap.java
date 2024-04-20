package lt.gama.impexp;

import lt.gama.impexp.map.*;
import lt.gama.model.i.ICompany;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import static java.util.Map.entry;

public final class MapMap {

	private static final Logger log = LoggerFactory.getLogger(MapMap.class);


	/**
	 * Add here all entities maps
	 */
	static private final Map<String, Class<?>> mapTypes = Map.ofEntries(
			entry(EntityType.PART.toString() + EntityType.SQL, MapPartSql.class),
			entry(EntityType.COUNTERPARTY.toString(), MapCounterparty.class),
			entry(EntityType.GL_ACCOUNT.toString(), MapGLAccount.class),
			entry(EntityType.BANK.toString(), MapBankAccount.class),
			entry(EntityType.CASH.toString(), MapCash.class),
			entry(EntityType.EMPLOYEE.toString(), MapEmployee.class),
            entry(EntityType.EMPLOYEE_CARD.toString(), MapEmployeeCard.class),
			entry(EntityType.WAREHOUSE.toString(), MapWarehouseSql.class),
			entry(EntityType.DOCUMENT.toString(), MapDocument.class),
			entry(EntityType.ASSET.toString(), MapAsset.class),
			entry(EntityType.WORK_SCHEDULE.toString(), MapWorkSchedule.class),
            entry(EntityType.POSITION.toString(), MapPosition.class));

	static private Map<String, MapBase<? extends ICompany>> mapMaps = null;


	@SuppressWarnings("unchecked")
	static public MapBase<? extends ICompany> getMap(String entityType) {
		if (mapMaps == null) mapMaps = new HashMap<>();

		MapBase<? extends ICompany> map = mapMaps.get(entityType);
		if (map == null) {
			Class<?> classType = mapTypes.get(entityType);
			if (classType != null) {
				try {
					map = (MapBase<? extends ICompany>) classType.getDeclaredConstructor().newInstance();
					mapMaps.put(entityType, map);

				} catch (NoSuchMethodException | SecurityException |
						 InstantiationException | IllegalAccessException |
						 IllegalArgumentException | InvocationTargetException e) {
					log.error(e.toString());
				}
			}
		}
		return map;
	}
}

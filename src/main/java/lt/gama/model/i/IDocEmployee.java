package lt.gama.model.i;

import lt.gama.model.type.Location;
import lt.gama.model.type.cf.CFValue;
import lt.gama.model.type.enums.DBType;
import lt.gama.model.type.l10n.LangEmployee;

import java.util.List;
import java.util.Map;

public interface IDocEmployee extends IId<Long> {

    Long getId();

    void setId(Long id);

    String getName();

    String getOffice();

    String getDepartment();

    String getEmployeeId();

    Location getAddress();

    List<CFValue> getCf();

    Map<String, LangEmployee> getTranslation();

    DBType getDb();
}

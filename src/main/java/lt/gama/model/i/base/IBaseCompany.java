package lt.gama.model.i.base;

import lt.gama.model.i.ICompany;
import lt.gama.model.i.IDb;
import lt.gama.model.i.IExportId;
import lt.gama.model.i.IId;

import java.util.Set;

public interface IBaseCompany extends IBaseEntity, ICompany, IId<Long>, IExportId, IDb {

    Set<String> getLabels();

    void setLabels(Set<String> labels);
}

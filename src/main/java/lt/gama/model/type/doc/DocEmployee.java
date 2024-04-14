package lt.gama.model.type.doc;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;
import lt.gama.model.i.IDocEmployee;
import lt.gama.model.i.IEmployee;
import lt.gama.model.i.IName;
import lt.gama.model.i.ITranslations;
import lt.gama.model.type.Location;
import lt.gama.model.type.base.BaseDocEntity;
import lt.gama.model.type.cf.CFValue;
import lt.gama.model.type.enums.DBType;
import lt.gama.model.type.l10n.LangEmployee;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Valdas
 * Created by valdas on 15-03-13.
 */
@Embeddable
public class DocEmployee extends BaseDocEntity implements IDocEmployee, IName, Serializable, ITranslations<LangEmployee> {

    @Serial
    private static final long serialVersionUID = -1L;

    private String name;

    @Transient
    private String office;

    @Transient
    private String department;

    /**
     * Employee id in accounting
     */
    @Transient
    private String employeeId;

    @Transient
    private Location address;

    /**
     * Custom fields values
     */
    @Hidden
    @Transient
    private List<CFValue> cf;

    /**
     * Translations
     */
    @Hidden
    @Transient
    private Map<String, LangEmployee> translation;


    public DocEmployee() {
    }

    public DocEmployee(Long id) {
        super(id);
    }

    public DocEmployee(Long id, DBType db, String name) {
        super(id);
        setDb(db);
        this.name = name;
    }

    public DocEmployee(IDocEmployee employee) {
        if (employee == null) return;
        setId(employee.getId());
        this.name = employee.getName();
        this.office = employee.getOffice();
        this.department = employee.getDepartment();
        this.employeeId = employee.getEmployeeId();
        this.address = employee.getAddress();
        this.cf = employee.getCf();
        this.translation = employee.getTranslation();
        setDb(employee.getDb());
    }

    public DocEmployee(IEmployee employee) {
        if (employee == null) return;
        setId(employee.getId());
        this.name = employee.getName();
        this.office = employee.getOffice();
        this.department = employee.getDepartment();
        this.employeeId = employee.getEmployeeId();
        this.address = employee.getAddress();
        this.cf = employee.getCf();
        this.translation = employee.getTranslation();
        setDb(employee.getDb());
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
    public String getOffice() {
        return office;
    }

    public void setOffice(String office) {
        this.office = office;
    }

    @Override
    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    @Override
    public Location getAddress() {
        return address;
    }

    public void setAddress(Location address) {
        this.address = address;
    }

    @Override
    public List<CFValue> getCf() {
        return cf;
    }

    public void setCf(List<CFValue> cf) {
        this.cf = cf;
    }

    @Override
    public Map<String, LangEmployee> getTranslation() {
        return translation;
    }

    @Override
    public void setTranslation(Map<String, LangEmployee> translation) {
        this.translation = translation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DocEmployee that = (DocEmployee) o;
        return Objects.equals(name, that.name) && Objects.equals(office, that.office) && Objects.equals(department, that.department) && Objects.equals(employeeId, that.employeeId) && Objects.equals(address, that.address) && Objects.equals(cf, that.cf) && Objects.equals(translation, that.translation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, office, department, employeeId, address, cf, translation);
    }

    @Override
    public String toString() {
        return "DocEmployee{" +
                "name='" + name + '\'' +
                ", office='" + office + '\'' +
                ", department='" + department + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", address=" + address +
                ", cf=" + cf +
                ", translation=" + translation +
                "} " + super.toString();
    }
}

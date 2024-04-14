package lt.gama.model.dto.entities;

import com.google.common.collect.ComparisonChain;
import lt.gama.model.type.Contact;
import lt.gama.model.type.Location;
import lt.gama.model.type.auth.EmployeeRole;
import lt.gama.model.type.cf.CFValue;
import lt.gama.model.type.doc.DocBankAccount;
import lt.gama.model.type.doc.DocEmployee;
import lt.gama.model.dto.base.BaseMoneyAccountDocumentDto;
import lt.gama.model.i.IEmployee;
import lt.gama.model.i.IFront;
import lt.gama.model.i.IName;
import lt.gama.model.i.ITranslations;
import lt.gama.model.type.enums.DBType;
import lt.gama.model.type.enums.EmployeeType;
import lt.gama.model.type.l10n.LangEmployee;

import java.io.Serial;
import java.util.*;

/**
 * Employee
 */
public class EmployeeDto extends BaseMoneyAccountDocumentDto<EmployeeDto> implements Comparable<EmployeeDto>,
        IName, IEmployee, ITranslations<LangEmployee>, IFront<EmployeeDto> {

    @Serial
    private static final long serialVersionUID = -1L;

    private String name;

    private Set<Contact> contacts;

    private Set<EmployeeRole> roles;

    /**
     * is employee is active user, .i.e. can login and will be calculated in billing process
     */
    private Boolean active;

    /**
     * account email - can't be changed after account is activated.
     * If want to change - need to suspend account, change email and activate account again.
     */
    private String email;

    /**
     * Bank.s accounts
     */
    private List<DocBankAccount> banks;

    private Location address;

    /**
     * Employee id in accounting
     */
    private String employeeId;

    private String office;

    /**
     * Employee type, only "EmployeeType.INTERNAL" is visible in employees list and can be selected in documents.
     */
    private EmployeeType type;

    private String department;

    /**
     * Custom fields values
     */
    private List<CFValue> cf;

    /**
     * Translations
     */
    private Map<String, LangEmployee> translation;


    public EmployeeDto(long id, DBType db) {
        setId(id);
        setDb(db);
    }

    public EmployeeDto() {
    }

    public EmployeeDto(String name) {
        this.name = name;
    }

    public EmployeeDto(Long id, DBType dbType, String name) {
        setId(id);
        this.setDb(dbType);
        this.name = name;
    }


    public EmployeeDto(IEmployee employee) {
        if (employee == null) return;
        setId(employee.getId());
        setDb(employee.getDb());
        this.name = employee.getName();
        this.office = employee.getOffice();
        this.employeeId = employee.getEmployeeId();
        this.type = employee.getType();
        this.department = employee.getDepartment();
        this.address = employee.getAddress();
        this.cf = employee.getCf();
    }

    public EmployeeDto(DocEmployee employee) {
        if (employee == null) return;
        setId(employee.getId());
        setDb(employee.getDb());
        this.name = employee.getName();
        this.office = employee.getOffice();
        this.employeeId = employee.getEmployeeId();
        this.department = employee.getDepartment();
        this.address = employee.getAddress();
        this.cf = employee.getCf();
    }

    /**
     * Get union of permissions from all roles
     * @return set of permissions from all employee roles
     */
    public Set<String> getUnionPermissions() {
        Set<String> union = new HashSet<>();
        if (getRoles() != null) {
            for (EmployeeRole role : getRoles()) {
                union.addAll(role.getPermissions());
            }
        }
        return union;
    }

    public boolean isActive() {
        return active != null && active;
    }

    public EmployeeType getType() {
        return type == null ? EmployeeType.INTERNAL : type;
    }

    @Override
    public EmployeeDto doc() {
        return new EmployeeDto(this);
    }

    @Override
    public EmployeeDto front() {
        EmployeeDto front = new EmployeeDto();
        front.setId(getId());
        front.setName(getName());
        front.setEmployeeId(getEmployeeId());
        front.setDepartment(getDepartment());
        front.setOffice(getOffice());
        front.setUsedCurrencies(getUsedCurrencies());
        front.setMoneyAccount(getMoneyAccount());
        return front;
    }

    @Override
    public int compareTo(EmployeeDto o) {
        return ComparisonChain.start()
                .compare(this.name, o.name)
                .compare(this.getId(), o.getId())
                .result();
    }

    // generated
    // except getActive()

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public Set<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(Set<Contact> contacts) {
        this.contacts = contacts;
    }

    public Set<EmployeeRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<EmployeeRole> roles) {
        this.roles = roles;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<DocBankAccount> getBanks() {
        return banks;
    }

    public void setBanks(List<DocBankAccount> banks) {
        this.banks = banks;
    }

    @Override
    public Location getAddress() {
        return address;
    }

    public void setAddress(Location address) {
        this.address = address;
    }

    @Override
    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    @Override
    public String getOffice() {
        return office;
    }

    public void setOffice(String office) {
        this.office = office;
    }

    public void setType(EmployeeType type) {
        this.type = type;
    }

    @Override
    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
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
        EmployeeDto that = (EmployeeDto) o;
        return Objects.equals(name, that.name) && Objects.equals(contacts, that.contacts) && Objects.equals(roles, that.roles) && Objects.equals(active, that.active) && Objects.equals(email, that.email) && Objects.equals(banks, that.banks) && Objects.equals(address, that.address) && Objects.equals(employeeId, that.employeeId) && Objects.equals(office, that.office) && type == that.type && Objects.equals(department, that.department) && Objects.equals(cf, that.cf) && Objects.equals(translation, that.translation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, contacts, roles, active, email, banks, address, employeeId, office, type, department, cf, translation);
    }

    @Override
    public String toString() {
        return "EmployeeDto{" +
                "name='" + name + '\'' +
                ", contacts=" + contacts +
                ", roles=" + roles +
                ", active=" + active +
                ", email='" + email + '\'' +
                ", banks=" + banks +
                ", address=" + address +
                ", employeeId='" + employeeId + '\'' +
                ", office='" + office + '\'' +
                ", type=" + type +
                ", department='" + department + '\'' +
                ", cf=" + cf +
                ", translation=" + translation +
                "} " + super.toString();
    }
}

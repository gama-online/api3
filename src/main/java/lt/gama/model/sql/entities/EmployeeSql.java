package lt.gama.model.sql.entities;

import com.google.common.collect.ComparisonChain;
import jakarta.persistence.*;
import lt.gama.helpers.StringHelper;
import lt.gama.model.dto.entities.EmployeeDto;
import lt.gama.model.i.IEmployee;
import lt.gama.model.i.IFront;
import lt.gama.model.i.IName;
import lt.gama.model.i.ITranslations;
import lt.gama.model.sql.base.BaseMoneyAccountSql;
import lt.gama.model.type.Contact;
import lt.gama.model.type.Location;
import lt.gama.model.type.cf.CFValue;
import lt.gama.model.type.doc.DocBankAccount;
import lt.gama.model.type.enums.EmployeeType;
import lt.gama.model.type.l10n.LangEmployee;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Employee
 */
@Entity
@Table(name = "employee")
@NamedEntityGraph(name = EmployeeSql.GRAPH_ALL, attributeNodes = @NamedAttributeNode(EmployeeSql_.ROLES))
public class EmployeeSql extends BaseMoneyAccountSql<EmployeeDto> implements Comparable<EmployeeSql>, IName, IEmployee, ITranslations<LangEmployee>, IFront<EmployeeDto> {

	public static final String GRAPH_ALL = "graph.EmployeeSql.all";

    private String name;

	@JdbcTypeCode(SqlTypes.JSON)
	private Set<Contact> contacts;

	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.LAZY)
	@JoinTable(name = "employee_roles",
			joinColumns = @JoinColumn(name = "employee_id"),
			inverseJoinColumns = @JoinColumn(name = "roles_id"))
	private Set<RoleSql> roles;

	/**
	 * is employee is active user, .i.e. can log in and will be calculated in billing process
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
	@JdbcTypeCode(SqlTypes.JSON)
	private List<DocBankAccount> banks;

	@Embedded
	private Location address;

    /**
     * Employee id in accounting
     */
	private String employeeId;

	private String office;

	/**
	 * Employee type
	 * <br>
	 * Note: only {@link EmployeeType#INTERNAL} is visible in employees list and can be selected in documents.
	 */
	private EmployeeType type;

	private String department;

    /**
     * Custom fields values
     */
	@JdbcTypeCode(SqlTypes.JSON)
	private List<CFValue> cf;

	/**
	 * Translations
	 */
	@JdbcTypeCode(SqlTypes.JSON)
	private Map<String, LangEmployee> translation;


	public EmployeeSql() {
	}

	public EmployeeSql(String name) {
		this.name = name;
	}

	/**
	 * Get union of permissions from all roles
	 * @return set of permissions from all employee roles
	 */
	public Set<String> getUnionPermissions() {
		Set<String> union = new HashSet<>();
		if (getRoles() != null) {
			for (RoleSql role : getRoles()) {
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
	public int compareTo(EmployeeSql o) {
        return ComparisonChain.start()
                .compare(this.name, o.name)
                .compare(this.getId(), o.getId())
                .result();
	}

	@SuppressWarnings("unused")
	@PrePersist
	@PreUpdate
	private void onSave() {
		if (type == null) type = EmployeeType.INTERNAL;
        if (active == null || StringHelper.isEmpty(email)) active = false;
    }


	/**
	 * toString except roles
	 */
	@Override
	public String toString() {
		return "EmployeeSql{" +
				"name='" + name + '\'' +
				", contacts=" + contacts +
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

	public Set<RoleSql> getRoles() {
		return roles;
	}

	public void setRoles(Set<RoleSql> roles) {
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
}

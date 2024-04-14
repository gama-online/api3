package lt.gama.model.type.auth;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

public class EmployeeRole implements Serializable {

	@Serial
    private static final long serialVersionUID = -1L;

	/**
	 * Role id
	 */
	private long id;

	/**
	 * Role name
	 */
	private String name;

	private Set<String> permissions;

	public EmployeeRole() {
	}

	public EmployeeRole(long id, String name, Set<String> permissions) {
		this.id = id;
		this.name = name;
		this.permissions = permissions;
	}

	// generated

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Set<String> getPermissions() {
		return permissions;
	}

	public void setPermissions(Set<String> permissions) {
		this.permissions = permissions;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		EmployeeRole that = (EmployeeRole) o;
		return id == that.id && Objects.equals(name, that.name) && Objects.equals(permissions, that.permissions);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, permissions);
	}

	@Override
	public String toString() {
		return "EmployeeRole{" +
				"id=" + id +
				", name='" + name + '\'' +
				", permissions=" + permissions +
				'}';
	}
}

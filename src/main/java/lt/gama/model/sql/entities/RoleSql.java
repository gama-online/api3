package lt.gama.model.sql.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lt.gama.model.sql.base.BaseCompanySql;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Set;

@Entity
@Table(name = "roles")
public class RoleSql extends BaseCompanySql {

	private String name;

	private String description;

	@JdbcTypeCode(SqlTypes.JSON)
	private Set<String> permissions;


	public RoleSql() {
	}

	public RoleSql(String name) {
		this.name = name;
	}

	// generated

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<String> getPermissions() {
		return permissions;
	}

	public void setPermissions(Set<String> permissions) {
		this.permissions = permissions;
	}

	@Override
	public String toString() {
		return "RoleSql{" +
				"name='" + name + '\'' +
				", description='" + description + '\'' +
				", permissions=" + permissions +
				"} " + super.toString();
	}
}

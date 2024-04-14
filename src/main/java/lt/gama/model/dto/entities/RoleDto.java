package lt.gama.model.dto.entities;

import lt.gama.model.dto.base.BaseCompanyDto;

import java.io.Serial;
import java.util.Objects;
import java.util.Set;

public class RoleDto extends BaseCompanyDto {

	@Serial
    private static final long serialVersionUID = -1L;

	private String name;

	private String description;

	private Set<String> permissions;


	public RoleDto() {
	}

	public RoleDto(String name) {
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
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		RoleDto roleDto = (RoleDto) o;
		return Objects.equals(name, roleDto.name) && Objects.equals(description, roleDto.description) && Objects.equals(permissions, roleDto.permissions);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), name, description, permissions);
	}

	@Override
	public String toString() {
		return "RoleDto{" +
				"name='" + name + '\'' +
				", description='" + description + '\'' +
				", permissions=" + permissions +
				"} " + super.toString();
	}
}

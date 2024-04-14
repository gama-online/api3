package lt.gama.model.dto.base;

import io.swagger.v3.oas.annotations.Hidden;
import lt.gama.model.i.ICompany;
import lt.gama.model.i.IExportId;
import lt.gama.model.i.IId;
import lt.gama.model.type.enums.DBType;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

public abstract class BaseCompanyDto extends BaseEntityDto implements IId<Long>, ICompany, IExportId, Serializable {

	private Long id;

	private Long foreignId;

	@Hidden
	private long companyId;

	/**
	 * Used for import/export only
	 */
	private String exportId;

	private Set<String> labels;

	public BaseCompanyDto() {
	}

	public BaseCompanyDto(long companyId, long id, DBType db) {
		this.companyId = companyId;
		this.id = id;
		setDb(db);
	}

	public void reset() {}

	// generated

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public Long getForeignId() {
		return foreignId;
	}

	public void setForeignId(Long foreignId) {
		this.foreignId = foreignId;
	}

	@Override
	public long getCompanyId() {
		return companyId;
	}

	@Override
	public void setCompanyId(long companyId) {
		this.companyId = companyId;
	}

	@Override
	public String getExportId() {
		return exportId;
	}

	@Override
	public void setExportId(String exportId) {
		this.exportId = exportId;
	}

	public Set<String> getLabels() {
		return labels;
	}

	public void setLabels(Set<String> labels) {
		this.labels = labels;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		BaseCompanyDto that = (BaseCompanyDto) o;
		return companyId == that.companyId && Objects.equals(id, that.id) && Objects.equals(foreignId, that.foreignId) && Objects.equals(exportId, that.exportId) && Objects.equals(labels, that.labels);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), id, foreignId, companyId, exportId, labels);
	}

	@Override
	public String toString() {
		return "BaseCompanyDto{" +
				"id=" + id +
				", foreignId=" + foreignId +
				", companyId=" + companyId +
				", exportId='" + exportId + '\'' +
				", labels=" + labels +
				"} " + super.toString();
	}
}

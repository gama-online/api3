package lt.gama.model.dto.documents;

import lt.gama.model.dto.base.BaseDocumentDto;
import lt.gama.model.dto.documents.items.EmployeeBalanceDto;

import java.io.Serial;
import java.util.List;
import java.util.Objects;

public class EmployeeOpeningBalanceDto extends BaseDocumentDto {

    @Serial
    private static final long serialVersionUID = -1L;

    private List<EmployeeBalanceDto> employees;

    // generated

    public List<EmployeeBalanceDto> getEmployees() {
        return employees;
    }

    public void setEmployees(List<EmployeeBalanceDto> employees) {
        this.employees = employees;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        EmployeeOpeningBalanceDto that = (EmployeeOpeningBalanceDto) o;
        return Objects.equals(employees, that.employees);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), employees);
    }

    @Override
    public String toString() {
        return "EmployeeOpeningBalanceDto{" +
                "employees=" + employees +
                "} " + super.toString();
    }
}

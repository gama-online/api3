package lt.gama.model.sql.entities;

import jakarta.persistence.*;
import lt.gama.helpers.CollectionsHelper;
import lt.gama.model.i.IEmployeeCard;
import lt.gama.model.sql.base.BaseCompanySql;
import lt.gama.model.type.doc.DocPosition;
import lt.gama.model.type.enums.SexType;
import lt.gama.model.type.salary.EmployeeTaxSettings;
import lt.gama.model.type.salary.SalaryPerMonth;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;

/**
 * gama-online
 * <p>
 * one-to-one relationship (the same id) with related Employee entity
 */
@Entity
@Table(name = "employee_cards")
@NamedEntityGraph(name = EmployeeCardSql.GRAPH_ALL, attributeNodes = @NamedAttributeNode(EmployeeCardSql_.EMPLOYEE))
public class EmployeeCardSql extends BaseCompanySql implements IEmployeeCard {

    public static final String GRAPH_ALL = "graph.EmployeeCardSql.all";

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    @MapsId
    private EmployeeSql employee;

    /**
     * Social Security Number
     */
    private String ssn;

    /**
     * National identification number
     */
    private String nin;

    /**
     * List of employee taxes by dates
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private List<EmployeeTaxSettings> taxes;

    private LocalDate hired;

    private String hireNote;

    private LocalDate fired;

    private String fireNote;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<DocPosition> positions;

    private SexType sex;

    /**
     * Salary history, i.e. salary records before start of accounting
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private List<SalaryPerMonth> salaryHistory;


    @SuppressWarnings("unused")
    @PrePersist
    @PreUpdate
    private void onSave() {
        // Put main position to the top.
        // If no main position - mark the first as main.
        if (this.positions != null && this.positions.size() > 0 && !this.positions.get(0).isMain()) {
            DocPosition mainPosition = null;
            Iterator<DocPosition> it = this.positions.iterator();
            while (it.hasNext()) {
                DocPosition position = it.next();
                if (position.isMain()) {
                    if (mainPosition == null) {
                        mainPosition = position;
                        it.remove();
                    } else {
                        position.setMain(false);
                    }
                }
            }
            if (mainPosition == null) {
                this.positions.get(0).setMain(true);
            } else {
                this.positions.add(0, mainPosition);
            }
        }
        // do not save some tax properties
        if (CollectionsHelper.hasValue(getTaxes())) {
            getTaxes().forEach(x -> x.setEmployeeSSTaxRateTotal(null));
        }
    }

    /**
     * toString except employee
     */
    @Override
    public String toString() {
        return "EmployeeCardSql{" +
                "ssn='" + ssn + '\'' +
                ", nin='" + nin + '\'' +
                ", taxes=" + taxes +
                ", hired=" + hired +
                ", hireNote='" + hireNote + '\'' +
                ", fired=" + fired +
                ", fireNote='" + fireNote + '\'' +
                ", positions=" + positions +
                ", sex=" + sex +
                ", salaryHistory=" + salaryHistory +
                "} " + super.toString();
    }

    // generated

    public EmployeeSql getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeSql employee) {
        this.employee = employee;
    }

    @Override
    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    @Override
    public String getNin() {
        return nin;
    }

    public void setNin(String nin) {
        this.nin = nin;
    }

    @Override
    public List<EmployeeTaxSettings> getTaxes() {
        return taxes;
    }

    public void setTaxes(List<EmployeeTaxSettings> taxes) {
        this.taxes = taxes;
    }

    @Override
    public LocalDate getHired() {
        return hired;
    }

    public void setHired(LocalDate hired) {
        this.hired = hired;
    }

    public String getHireNote() {
        return hireNote;
    }

    public void setHireNote(String hireNote) {
        this.hireNote = hireNote;
    }

    @Override
    public LocalDate getFired() {
        return fired;
    }

    public void setFired(LocalDate fired) {
        this.fired = fired;
    }

    public String getFireNote() {
        return fireNote;
    }

    public void setFireNote(String fireNote) {
        this.fireNote = fireNote;
    }

    @Override
    public List<DocPosition> getPositions() {
        return positions;
    }

    public void setPositions(List<DocPosition> positions) {
        this.positions = positions;
    }

    @Override
    public SexType getSex() {
        return sex;
    }

    public void setSex(SexType sex) {
        this.sex = sex;
    }

    public List<SalaryPerMonth> getSalaryHistory() {
        return salaryHistory;
    }

    public void setSalaryHistory(List<SalaryPerMonth> salaryHistory) {
        this.salaryHistory = salaryHistory;
    }
}

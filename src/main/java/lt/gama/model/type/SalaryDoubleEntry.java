package lt.gama.model.type;

import lt.gama.model.sql.documents.DoubleEntrySql;
import lt.gama.model.sql.documents.SalarySql;
import lt.gama.model.sql.documents.items.EmployeeChargeSql;

public class SalaryDoubleEntry extends DocumentDoubleEntry<SalarySql>{

    private EmployeeChargeSql employeeCharge;


    @SuppressWarnings("unused")
    protected SalaryDoubleEntry() {}

    public SalaryDoubleEntry(SalarySql document, DoubleEntrySql doubleEntry, EmployeeChargeSql employeeCharge) {
        super(document, doubleEntry);
        this.employeeCharge = employeeCharge;
    }

    public EmployeeChargeSql getEmployeeCharge() {
        return employeeCharge;
    }
}

package lt.gama.api.request;

import java.time.LocalDate;

public class WorkHoursRequest extends IdRequest {

    @SuppressWarnings("unused")
    protected WorkHoursRequest() {}

    public WorkHoursRequest(int year, int month, long employeeId) {
        super(employeeId, LocalDate.of(year, month, 1).toString());
    }

    public long getEmployeeId() {
        return getId();
    }

    public int getYear() {
        return LocalDate.parse(getParentName()).getYear();
    }

    public int getMonth() {
        return LocalDate.parse(getParentName()).getMonthValue();
    }
}

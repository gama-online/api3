package lt.gama.api.request;

/**
 * gama-online
 * Created by valdas on 2017-05-29.
 */
public class GenerateEmployeeChargeRequest extends IdRequest {

    private SalaryType salaryType;


    @SuppressWarnings("unused")
    protected GenerateEmployeeChargeRequest() {}

    public GenerateEmployeeChargeRequest(long id, Long parentId, SalaryType salaryType) {
        super(id, parentId);
        this.salaryType = salaryType;
    }

    // generated

    public SalaryType getSalaryType() {
        return salaryType;
    }

    public void setSalaryType(SalaryType salaryType) {
        this.salaryType = salaryType;
    }

    @Override
    public String toString() {
        return "GenerateEmployeeChargeRequest{" +
                "salaryType=" + salaryType +
                "} " + super.toString();
    }
}

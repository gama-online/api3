package lt.gama.api.request;

/**
 * gama-online
 * Created by valdas on 2017-01-26.
 */
public class RefreshSalaryRequest {

    private SalaryType salaryType;

    /**
     * if 'fresh' then delete all existing records and regenerate all, else generate only records for missed employees
     */
    private Boolean fresh;

    /**
     * Salary id
     */
    private Long id;


    @SuppressWarnings("unused")
    protected RefreshSalaryRequest() {}

    public RefreshSalaryRequest(Long id, SalaryType salaryType, Boolean fresh) {
        this.id = id;
        this.salaryType = salaryType;
        this.fresh = fresh;
    }

    // generated

    public SalaryType getSalaryType() {
        return salaryType;
    }

    public void setSalaryType(SalaryType salaryType) {
        this.salaryType = salaryType;
    }

    public Boolean getFresh() {
        return fresh;
    }

    public void setFresh(Boolean fresh) {
        this.fresh = fresh;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "RefreshSalaryRequest{" +
                "salaryType=" + salaryType +
                ", fresh=" + fresh +
                ", id=" + id +
                '}';
    }
}

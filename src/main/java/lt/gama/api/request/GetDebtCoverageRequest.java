package lt.gama.api.request;

/**
 * gama-online
 * Created by valdas on 2018-05-11.
 */
public class GetDebtCoverageRequest extends IdRequest {

    private GetDebtCoverageRequestParentObj parentObj;


    public GetDebtCoverageRequest() {}

    // generated

    public GetDebtCoverageRequestParentObj getParentObj() {
        return parentObj;
    }

    public void setParentObj(GetDebtCoverageRequestParentObj parentObj) {
        this.parentObj = parentObj;
    }

    @Override
    public String toString() {
        return "GetDebtCoverageRequest{" +
                "parentObj=" + parentObj +
                "} " + super.toString();
    }
}

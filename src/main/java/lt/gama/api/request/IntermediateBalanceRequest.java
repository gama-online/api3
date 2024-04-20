package lt.gama.api.request;

public class IntermediateBalanceRequest {

    private int yearToClose;


    public IntermediateBalanceRequest() {
    }

    public IntermediateBalanceRequest(int yearToClose) {
        this.yearToClose = yearToClose;
    }

    // generated

    public int getYearToClose() {
        return yearToClose;
    }

    public void setYearToClose(int yearToClose) {
        this.yearToClose = yearToClose;
    }

    @Override
    public String toString() {
        return "IntermediateBalanceRequest{" +
                "yearToClose=" + yearToClose +
                '}';
    }
}



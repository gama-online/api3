package lt.gama.api.response;

import lt.gama.model.type.GamaMoney;

/**
 * gama-online
 * Created by valdas on 2017-06-07.
 */
public class EmployeeVacationResponse {

    private int days;

    private GamaMoney amount;

    private GamaMoney dayAvg;

    // generated

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public GamaMoney getAmount() {
        return amount;
    }

    public void setAmount(GamaMoney amount) {
        this.amount = amount;
    }

    public GamaMoney getDayAvg() {
        return dayAvg;
    }

    public void setDayAvg(GamaMoney dayAvg) {
        this.dayAvg = dayAvg;
    }

    @Override
    public String toString() {
        return "EmployeeVacationResponse{" +
                "days=" + days +
                ", amount=" + amount +
                ", dayAvg=" + dayAvg +
                '}';
    }
}

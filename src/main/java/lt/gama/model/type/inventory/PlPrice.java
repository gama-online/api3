package lt.gama.model.type.inventory;

import lt.gama.model.i.IPeriod;
import lt.gama.model.type.GamaBigMoney;

import java.time.LocalDate;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 2018-01-29.
 */
public class PlPrice implements IPeriod {

    private LocalDate dateFrom;

    private LocalDate dateTo;

    private GamaBigMoney price;

    public PlPrice() {
    }

    public PlPrice(LocalDate dateFrom, LocalDate dateTo, GamaBigMoney price) {
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.price = price;
    }

    public PlPrice copy() {
        return new PlPrice(dateFrom, dateTo, price);
    }

    // generated


    @Override
    public LocalDate getDateFrom() {
        return dateFrom;
    }

    @Override
    public void setDateFrom(LocalDate dateFrom) {
        this.dateFrom = dateFrom;
    }

    @Override
    public LocalDate getDateTo() {
        return dateTo;
    }

    @Override
    public void setDateTo(LocalDate dateTo) {
        this.dateTo = dateTo;
    }

    public GamaBigMoney getPrice() {
        return price;
    }

    public void setPrice(GamaBigMoney price) {
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlPrice plPrice = (PlPrice) o;
        return Objects.equals(dateFrom, plPrice.dateFrom) && Objects.equals(dateTo, plPrice.dateTo) && Objects.equals(price, plPrice.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dateFrom, dateTo, price);
    }

    @Override
    public String toString() {
        return "PlPrice{" +
                "dateFrom=" + dateFrom +
                ", dateTo=" + dateTo +
                ", price=" + price +
                '}';
    }
}

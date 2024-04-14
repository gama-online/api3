package lt.gama.model.type.inventory;

import lt.gama.helpers.GamaMoneyUtils;
import lt.gama.helpers.NumberUtils;
import lt.gama.model.i.IPeriod;
import lt.gama.model.type.GamaBigMoney;

import java.time.LocalDate;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 2018-02-04.
 */
public class PlPriceDiscount implements IPeriod {

    private LocalDate dateFrom;

    private LocalDate dateTo;

    private GamaBigMoney price;

    private Double discount;

    public PlPriceDiscount() {
    }

    public PlPriceDiscount(LocalDate dateFrom, LocalDate dateTo, GamaBigMoney price, Double discount) {
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.price = price;
        this.discount = discount;
    }

    public PlPriceDiscount copy() {
        return new PlPriceDiscount(dateFrom, dateTo, price, discount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlPriceDiscount that = (PlPriceDiscount) o;
        return Objects.equals(dateFrom, that.dateFrom) &&
                Objects.equals(dateTo, that.dateTo) &&
                GamaMoneyUtils.isEqual(price, that.price) &&
                NumberUtils.isEq(discount, that.discount, 1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dateFrom, dateTo,
                GamaMoneyUtils.isZero(price) ? null : price,
                NumberUtils.isZero(discount, 1) ? null : discount);
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

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    @Override
    public String toString() {
        return "PlPriceDiscount{" +
                "dateFrom=" + dateFrom +
                ", dateTo=" + dateTo +
                ", price=" + price +
                ", discount=" + discount +
                '}';
    }
}

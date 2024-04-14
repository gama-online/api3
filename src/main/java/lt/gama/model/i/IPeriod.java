package lt.gama.model.i;

import java.time.LocalDate;

/**
 * gama-online
 * Created by valdas on 2018-02-08.
 */
public interface IPeriod {

    LocalDate getDateFrom();

    void setDateFrom(LocalDate dateFrom);

    LocalDate getDateTo();

    void setDateTo(LocalDate dateTo);
}

package lt.gama.model.i;

import java.time.LocalDate;

/**
 * Gama
 * Created by valdas on 15-05-22.
 */
public interface INumberDocument  {

    LocalDate getDate();

    void setDate(LocalDate date);

    String getNumber();

    void setNumber(String number);

    String getNote();


}

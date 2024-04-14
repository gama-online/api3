package lt.gama.model.type.ibase;


import java.math.BigDecimal;

public interface IBaseDocPartOutRemainder {

    /**
     * if forward sell is allowed this is remainder without cost. Must be resolved in future.
     */
    BigDecimal getRemainder();

    void setRemainder(BigDecimal remainder);
}

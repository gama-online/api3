package lt.gama.model.type.ibase;

import lt.gama.model.sql.entities.PartSql;
import lt.gama.model.sql.entities.WarehouseSql;
import lt.gama.model.type.GamaMoney;

import java.math.BigDecimal;

public interface IBaseDocPartSql {

    BigDecimal getQuantity();
    void setQuantity(BigDecimal quantity);

    GamaMoney getTotal();
    void setTotal(GamaMoney total);

    GamaMoney getBaseTotal();
    void setBaseTotal(GamaMoney baseTotal);

    WarehouseSql getWarehouse();
    void setWarehouse(WarehouseSql warehouse);

    String getTag();
    void setTag(String tag);

    /**
     *  Not enough quantity to finish operation - used in frontend only
     */
    BigDecimal getNotEnough();
    void setNotEnough(BigDecimal notEnough);

    PartSql getPart();
    void setPart(PartSql part);
}

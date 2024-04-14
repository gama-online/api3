package lt.gama.model.i;

import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.inventory.VATCodeTotal;

import java.time.LocalDate;
import java.util.List;

/**
 * gama-online
 * Created by valdas on 2016-11-11.
 */
public interface IISaf extends INumberDocument {

    ICounterparty getCounterparty();

    List<VATCodeTotal> getVatCodeTotals();

    String getIsafInvoiceType();

    boolean isIsafSpecialTaxation();

    LocalDate getRegDate();

    GamaMoney getBaseSubtotal();

    GamaMoney getBaseTaxTotal();
}

package lt.gama.model.type.ibase;

import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.part.DocPartInvoice;
import lt.gama.model.type.part.PartCostSource;

import java.util.List;

public interface IBaseDocPartCost {

    /**
     * Total cost in base currency.
     * p.s. information can be filled later if part can be allowed to sell without knowing cost at the moment of sell.
     * In this case the remainder will be negative and must be compensated later.
     */
    GamaMoney getCostTotal();
    void setCostTotal(GamaMoney costTotal);

    /**
     * Cost source info list, i.e. list of purchase/production docs info: id, document date and number, quantity and cost.
     * Information filled in the time of sale, i.e. at finishing invoice document.
     * The information here can be used for the reconstruction of inventory records (purchase, production or others)
     * at finishing returning invoice document (i.e. invoice with negative quantities)
     * which mus be linked through {@link DocPartInvoice#getDocReturn docReturn} to this document.
     */
    List<PartCostSource> getCostInfo();
    void setCostInfo(List<PartCostSource> costInfo);

}

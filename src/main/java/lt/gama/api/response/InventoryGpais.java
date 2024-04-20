package lt.gama.api.response;

import lt.gama.helpers.BigDecimalUtils;
import lt.gama.helpers.CollectionsHelper;
import lt.gama.model.type.Packaging;
import lt.gama.model.type.base.BaseDocPart;

import java.math.BigDecimal;

public record InventoryGpais(
        BaseDocPart part,
        String sellerCountry,

        PackInfo ob,
        PackInfo imp,
        PackInfo purchase,
        PackInfo exp,
        PackInfo wholesale,
        PackInfo retail,
        PackInfo production,
        PackInfo inventory,
        PackInfo remainder
) {

    public InventoryGpais(BaseDocPart part,
                          String sellerCountry,
                          BigDecimal ob,
                          BigDecimal imp,
                          BigDecimal purchase,
                          BigDecimal exp,
                          BigDecimal wholesale,
                          BigDecimal retail,
                          BigDecimal production,
                          BigDecimal inventory,
                          BigDecimal remainder) {
        this(part, sellerCountry,
                new PackInfo(ob, CollectionsHelper.streamOf(part.getPackaging()).map(p -> new Packaging(p.type(), p.code(), BigDecimalUtils.multiply(p.weight(), ob))).toList()),
                new PackInfo(imp, CollectionsHelper.streamOf(part.getPackaging()).map(p -> new Packaging(p.type(), p.code(), BigDecimalUtils.multiply(p.weight(), imp))).toList()),
                new PackInfo(purchase, CollectionsHelper.streamOf(part.getPackaging()).map(p -> new Packaging(p.type(), p.code(), BigDecimalUtils.multiply(p.weight(), purchase))).toList()),
                new PackInfo(exp, CollectionsHelper.streamOf(part.getPackaging()).map(p -> new Packaging(p.type(), p.code(), BigDecimalUtils.multiply(p.weight(), exp))).toList()),
                new PackInfo(wholesale, CollectionsHelper.streamOf(part.getPackaging()).map(p -> new Packaging(p.type(), p.code(), BigDecimalUtils.multiply(p.weight(), wholesale))).toList()),
                new PackInfo(retail, CollectionsHelper.streamOf(part.getPackaging()).map(p -> new Packaging(p.type(), p.code(), BigDecimalUtils.multiply(p.weight(), retail))).toList()),
                new PackInfo(production, CollectionsHelper.streamOf(part.getPackaging()).map(p -> new Packaging(p.type(), p.code(), BigDecimalUtils.multiply(p.weight(), production))).toList()),
                new PackInfo(inventory, CollectionsHelper.streamOf(part.getPackaging()).map(p -> new Packaging(p.type(), p.code(), BigDecimalUtils.multiply(p.weight(), inventory))).toList()),
                new PackInfo(remainder, CollectionsHelper.streamOf(part.getPackaging()).map(p -> new Packaging(p.type(), p.code(), BigDecimalUtils.multiply(p.weight(), remainder))).toList()));
    }
}

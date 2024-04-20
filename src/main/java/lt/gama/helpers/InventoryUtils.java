package lt.gama.helpers;

import lt.gama.model.i.*;
import lt.gama.model.sql.documents.items.EstimateBasePartSql;
import lt.gama.model.sql.documents.items.InvoiceBasePartSql;
import lt.gama.model.sql.entities.WarehouseSql;
import lt.gama.model.type.ibase.IBaseDocPartSql;
import lt.gama.model.type.l10n.LangPart;
import lt.gama.model.type.part.DocPartBalance;
import lt.gama.model.type.part.DocPartHistory;
import lt.gama.model.type.part.DocPartInvoice;
import org.hibernate.internal.util.collections.CollectionHelper;

import java.util.*;

/**
 * gama-online
 * Created by valdas on 2016-01-19.
 */
public final class InventoryUtils {

    public static <E extends IInvoicePartPrice, F extends IInvoicePartPrice>
    boolean isInvoicePartsCurrencyAndSumAreSame(List<E> parts1, List<F> parts2) {
        if (CollectionsHelper.isEmpty(parts1) && CollectionsHelper.isEmpty(parts2)) return true;
        if (CollectionsHelper.isEmpty(parts1) || CollectionsHelper.isEmpty(parts2) || parts1.size() != parts2.size()) return false;

        int len = parts1.size();
        for (int i = 0; i < len; i++) {
            var p1 = parts1.get(i);
            var p2 = parts2.get(i);

            if (!Objects.equals(BooleanUtils.isTrue(p1.isFixTotal()), BooleanUtils.isTrue(p2.isFixTotal())) ||
                    !Objects.equals(p1.getQuantity(), p2.getQuantity()) ||
                    !GamaMoneyUtils.isSameCurrencyAndEqual(p1.getPrice(), p2.getPrice()) ||
                    !Objects.equals(p1.getDiscount(), p2.getDiscount()) ||
                    !Objects.equals(p1.getDiscountDoc(), p2.getDiscountDoc()) ||
                    !GamaMoneyUtils.isSameCurrencyAndEqual(p1.getDiscountedPrice(), p2.getDiscountedPrice()) ||
                    !GamaMoneyUtils.isSameCurrencyAndEqual(p1.getDiscountedTotal(), p2.getDiscountedTotal()) ||
                    !GamaMoneyUtils.isSameCurrencyAndEqual(p1.getDiscountDocTotal(), p2.getDiscountDocTotal()))
                return false;
        }
        return true;
    }

    private static <E extends IPart & ITranslations<LangPart> & IPartSN & IUuid>
    void clone(E src, E dst, boolean cloneUuid) {
        if (src == null || dst == null) return;
        dst.setId(src.getId());
        dst.setType(src.getType());
        dst.setName(src.getName());
        dst.setSku(src.getSku());
        dst.setBarcode(src.getBarcode());
        dst.setUnit(src.getUnit());
        dst.setSn(src.getSn());
        if (src.getCf() != null) {
            dst.setCf(new ArrayList<>());
            dst.getCf().addAll(src.getCf());
        }
        if (src.getTranslation() != null) {
            dst.setTranslation(new HashMap<>());
            dst.getTranslation().putAll(src.getTranslation());
        }
        dst.setAccountAsset(src.getAccountAsset());
        dst.setGlExpense(src.getGlExpense());
        dst.setGlIncome(src.getGlIncome());

        dst.setUuid(cloneUuid ? src.getUuid() : UUID.randomUUID());
    }

    public static <E extends IPart & ITranslations<LangPart> & IPartSN & IUuid> DocPartHistory partHistory(E part) {
        DocPartHistory clone = new DocPartHistory();
        clone(part, clone, true);
        return clone;
    }

    public static <E extends IPart & ITranslations<LangPart> & IPartSN & IUuid> DocPartBalance partBalance(E part) {
        DocPartBalance clone = new DocPartBalance();
        clone(part, clone, false);
        return clone;
    }

    public static void setPartsUuid(List<? extends IUuid> parts) {
        if (CollectionHelper.isEmpty(parts)) return;

        for (IUuid part : parts) {
            if (part.getUuid() == null) part.setUuid(UUID.randomUUID());
            if (part instanceof DocPartInvoice) setPartsUuid(((DocPartInvoice) part).getParts());
        }
    }

    public static void setPartsWarehouseSQL(WarehouseSql warehouse, String tag, List<? extends IBaseDocPartSql> parts) {
        if (warehouse == null || CollectionHelper.isEmpty(parts)) return;

        for (IBaseDocPartSql part : parts) {
            if (part.getWarehouse() == null) {
                part.setWarehouse(warehouse);
                part.setTag(tag);
            }
        }
    }

    public static void clearFinished(List<? extends IFinished> parts) {
        if (CollectionHelper.isEmpty(parts)) return;

        for (IFinished part : parts) {
            part.setFinished(false);
            if (part instanceof DocPartInvoice) clearFinished(((DocPartInvoice) part).getParts());
        }
    }

    //TODO *** fix after migration *** optimize more.
    public static void assignSortOrder(List<? extends ISortOrder> parts) {
        if (CollectionsHelper.isEmpty(parts)) return;

        List<Part> sOParts;

        if (parts.get(0) instanceof InvoiceBasePartSql || parts.get(0) instanceof EstimateBasePartSql) {
            sOParts = CollectionsHelper.streamOf(parts)
                    .filter(p -> p instanceof ILinkUuid)
                    .map(ILinkUuid.class::cast)
                    .map(p -> new Part(p.getLinkUuid(), (ISortOrder) p))
                    .toList();

            Map<UUID, List<ISortOrder>> subpartsMap = new LinkedHashMap<>();
            CollectionsHelper.streamOf(parts)
                    .filter(p -> p instanceof IParentLinkUuid)
                    .map(IParentLinkUuid.class::cast)
                    .forEach(p -> {
                        if (!subpartsMap.containsKey(p.getParentLinkUuid())) {
                            List<ISortOrder> sp = new ArrayList<>();
                            sp.add((ISortOrder) p);
                            subpartsMap.put(p.getParentLinkUuid(), sp);
                        } else {
                            subpartsMap.get(p.getParentLinkUuid()).add((ISortOrder) p);
                        }
                    });
            sOParts.forEach(p -> p.setSubparts(subpartsMap.get(p.getLinkUuid())));
        } else {
            sOParts = CollectionsHelper.streamOf(parts).map(Part::new).toList();
        }

        double partOrder = 0;
        for (int i = 0; i < sOParts.size(); i++) {
            ISortOrder part = sOParts.get(i).getPart();
            ISortOrder nextPart = i != sOParts.size() - 1 ? sOParts.get(i + 1).getPart() : null;
            if (part instanceof InvoiceBasePartSql || part instanceof EstimateBasePartSql) {
                partOrder = setOrder(part, nextPart, partOrder);
                if (CollectionsHelper.hasValue(sOParts.get(i).getSubparts())) {
                    double subpartOrder = 0;
                    List<ISortOrder> subparts = sOParts.get(i).getSubparts();
                    for (int j = 0; j < subparts.size(); j++) {
                        subpartOrder = setOrder(subparts.get(j), j != subparts.size() - 1 ? subparts.get(j + 1) : null, subpartOrder);
                    }
                }
            } else {
                partOrder = setOrder(part, nextPart, partOrder);
            }
        }
    }

    private static class Part {
        UUID linkUuid;

        ISortOrder part;

        List<ISortOrder> subparts;

        public Part(ISortOrder part) {
            this.part = part;
        }

        public Part(UUID linkUuid, ISortOrder part) {
            this (part);
            this.linkUuid = linkUuid;
        }

        public UUID getLinkUuid() {
            return linkUuid;
        }

        public ISortOrder getPart() {
            return part;
        }

        public List<ISortOrder> getSubparts() {
            return subparts;
        }

        public void setSubparts(List<ISortOrder> subparts) {
            this.subparts = subparts;
        }
    }

    private static double setOrder(ISortOrder part, ISortOrder nextPart, double lastOrder) {
        if (part.getSortOrder() == null || part.getSortOrder() <= lastOrder) {
            part.setSortOrder(lastOrder + 64);
        } else {
            double partOrder = part.getSortOrder();
            Double nextPartOrder = nextPart != null ? nextPart.getSortOrder() : null;
            if (nextPartOrder != null && nextPartOrder <= partOrder && nextPartOrder > lastOrder) {
                part.setSortOrder((nextPartOrder - lastOrder) / 2 + lastOrder);
            }
        }
        return part.getSortOrder();
    }

    public static <E extends IId<Long>> void clearPartId(List<E> parts) {
        CollectionsHelper.streamOf(parts).forEach(p -> p.setId(null));
    }
}

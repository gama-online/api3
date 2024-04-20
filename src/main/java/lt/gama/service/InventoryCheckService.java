package lt.gama.service;

import com.google.common.collect.Lists;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.*;
import lt.gama.model.i.*;
import lt.gama.model.i.base.IBaseDocument;
import lt.gama.model.sql.base.BaseDocumentSql;
import lt.gama.model.sql.documents.InvoiceSql;
import lt.gama.model.sql.documents.items.*;
import lt.gama.model.sql.entities.InventoryNowSql;
import lt.gama.model.type.GamaBigMoney;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.enums.PartType;
import lt.gama.model.type.ibase.IBaseDocPartCost;
import lt.gama.model.type.ibase.IBaseDocPartSql;
import lt.gama.model.type.inventory.InventoryQ;
import lt.gama.model.type.inventory.WarehouseTagged;
import lt.gama.model.type.part.DocPart;
import lt.gama.model.type.part.PartCostSource;
import lt.gama.model.type.part.PartSN;
import lt.gama.service.ex.rt.GamaException;
import lt.gama.service.ex.rt.GamaNotEnoughQuantityException;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

@Service
public class InventoryCheckService {

    private static final Logger log = LoggerFactory.getLogger(InventoryCheckService.class);

    private final DBServiceSQL dbServiceSQL;
    private final Auth auth;

    public InventoryCheckService(DBServiceSQL dbServiceSQL, Auth auth) {
        this.dbServiceSQL = dbServiceSQL;
        this.auth = auth;
    }

    public <E extends IBaseDocPartSql & IDocPart & IPartSN & IFinished & IPartMessage>
    void checkInventorySQL(IBaseDocument document, List<E> parts, WarehouseTagged docWarehouse) {
        if (document == null || parts == null) return;

        Map<RemainderKey, List<InventoryQ>> remainders = new HashMap<>();

        List<String> messages = new ArrayList<>();

        Map<UUID, Long> docsReturn = new HashMap<>();
        CollectionsHelper.streamOf(parts).forEach(p -> {
            if (p instanceof InvoicePartSql e) docsReturn.put(e.getLinkUuid(), e.getDocReturn() != null ? e.getDocReturn().getId() : null);
        });

        for (E part : parts) {
            if (BigDecimalUtils.isZero(part.getQuantity())) continue;

            if (part instanceof InvoiceBasePartSql) {
                if (BigDecimalUtils.isPositive(part.getQuantity())) {
                    checkPartInWarehouseSQL(remainders, document, part, docWarehouse, messages);
                } else {
                    Long docReturnId = part instanceof InvoicePartSql
                            ? docsReturn.get(((InvoicePartSql) part).getLinkUuid())
                            : docsReturn.get(((InvoiceSubpartSql) part).getParentLinkUuid());

                    checkPartInvoiceReturnSQL(remainders, document, part, docReturnId, docWarehouse, messages);
                }
            } else if (part instanceof InventoryPartSql) {
                checkPartInventorySQL(remainders, document, (InventoryPartSql) part, docWarehouse, messages);
            } else {
                checkPartInWarehouseSQL(remainders, document, part, docWarehouse, messages);
            }
        }

        if (!messages.isEmpty()) throw new GamaNotEnoughQuantityException(messages);
    }

    public <E extends IBaseDocPartCost & IBaseDocPartSql & IDocPart & IPartSN & IFinished & IPartMessage>
    void checkInventoryRecallSQL(BaseDocumentSql document, List<E> parts, WarehouseTagged docWarehouse) {
        if (document == null || parts == null) return;

        Map<RemainderKey, List<InventoryQ>> remainders = new HashMap<>();

        Map<UUID, Long> docsReturn = new HashMap<>();
        CollectionsHelper.streamOf(parts).forEach(p -> {
            if (p instanceof InvoicePartSql e) docsReturn.put(e.getLinkUuid(), Validators.isValid(e.getDocReturn()) ? e.getDocReturn().getId() : null);
        });

        for (E part : parts) {
            if (BigDecimalUtils.isZero(part.getQuantity())) continue;

            if (part instanceof InvoiceBasePartSql) {
                if (BigDecimalUtils.isPositive(part.getQuantity())) {
                    checkPartInvoiceRecallSQL(document, part);
                } else {
                    Long docReturnId = part instanceof InvoicePartSql
                            ? docsReturn.get(((InvoicePartSql) part).getLinkUuid())
                            : docsReturn.get(((InvoiceSubpartSql) part).getParentLinkUuid());

                    checkPartInvoiceReturnRecallSQL(remainders, document, part, docReturnId, docWarehouse);
                }
            } else if (part instanceof InventoryPartSql) {
                checkPartInventoryRecallSQL(remainders, document, (InventoryPartSql) part, docWarehouse);

            } else if (part instanceof PurchasePartSql) {
                if (BigDecimalUtils.isPositive(part.getQuantity())) {
                    checkPartRecallSQL(remainders, document, part, docWarehouse);
                }

            } else {
                checkPartRecallSQL(remainders, document, part, docWarehouse);
            }
        }
    }

    public <E extends IBaseDocPartSql & IDocPart & IPartSN & IFinished & IPartMessage>
    void checkPartInWarehouseSQL(Map<RemainderKey, List<InventoryQ>> remainders, IBaseDocument document, E part,
                                 WarehouseTagged docWarehouse, List<String> messages) {
        part.setNotEnough(null);

        if (BooleanUtils.isTrue(part.getFinished()) || part.getType() == PartType.SERVICE) return;
        if (!(part instanceof InvoicePartSql) && BooleanUtils.isTrue(part.isForwardSell())) return;

        boolean isReturning = false;
        Long returningDocId = 0L;
        boolean forwardSell = part.isForwardSell();

        if (part instanceof InvoicePartSql partInvoice) {
            if (BigDecimalUtils.isNegativeOrZero(partInvoice.getQuantity())) {
                // do nothing if return and no origin document
                if (!Validators.isValid(partInvoice.getDocReturn()) || CollectionsHelper.isEmpty(partInvoice.getCostInfo())) return;
                returningDocId = partInvoice.getDocReturn().getId();
                isReturning = true;
            }

        } else if (part instanceof PurchasePartSql partPurchase) {
            if (BigDecimalUtils.isPositiveOrZero(partPurchase.getQuantity())) return;
            // do nothing if return and no origin document
            if (partPurchase.getDocReturn() == null || partPurchase.getDocReturn().getId() == null) return;
            returningDocId = partPurchase.getDocReturn().getId();
            isReturning = true;
        }

        WarehouseTagged warehouse = Validators.isValid(part.getWarehouse()) ? new WarehouseTagged(part.getWarehouse(), part.getTag()) : docWarehouse;
        final String tag = BooleanUtils.isTrue(warehouse.getWithTag()) ? warehouse.getTag() : null;

        var key = new RemainderKey()
                .setPartId(part.getPartId())
                .setWarehouseId(warehouse.getId());
        if (part.getType() == PartType.PRODUCT_SN) key.setPartSN(part.getSn());
        if (isReturning) key.setDocId(returningDocId);

        List<InventoryQ> inventories;
        if (remainders.containsKey(key)) {
            inventories = remainders.get(key);
        } else {
            List<InventoryNowSql> inventoriesNow = dbServiceSQL.getInventoriesNow(warehouse, part);
            if (CollectionsHelper.isEmpty(inventoriesNow)) {
                if (forwardSell) return;  // no remainders - it's OK if forward-sell
                String msg = MessageFormat.format(TranslationService.getInstance().translate(TranslationService.INVENTORY.NotEnoughQuantity, auth.getLanguage()),
                        document.getNumber(), document.getDate(), part.toMessage(), warehouse.getName(), part.getQuantity());
                if (messages == null) {
                    log.info(this.getClass().getSimpleName() + "::checkPartInWarehouse" +
                            " GamaNotEnoughQuantityException: no inventoriesNow" +
                            "\n document=" + document.getClass().getSimpleName() + "{id=" + document.getId() + ", date=" + document.getDate() + ", number=" + document.getNumber() + "}" +
                            "\n part=" + part +
                            "\n warehouse=" + warehouse);
                    throw new GamaNotEnoughQuantityException(msg);
                } else {
                    messages.add(msg);
                    part.setNotEnough(part.getQuantity());
                    return;
                }
            }

            inventories = new ArrayList<>();
            for (InventoryNowSql inventoryNow : inventoriesNow) {
                if (!Objects.equals(inventoryNow.getTag(), tag)) continue;
                if (inventoryNow.getDoc().getDate().isAfter(document.getDate())) {
                    if (forwardSell) {
                        log.info(this.getClass().getSimpleName() + "::checkPartInWarehouse" +
                                " GamaNotEnoughQuantityException: quantity is not zero" +
                                "\n document=" + document.getClass().getSimpleName() + "{id=" + document.getId() + ", date=" + document.getDate() + ", number=" + document.getNumber() + "}" +
                                "\n part=" + part +
                                "\n warehouse=" + warehouse);
                        throw new GamaNotEnoughQuantityException(MessageFormat.format(
                                TranslationService.getInstance().translate(TranslationService.INVENTORY.NoForwardSellQuantity, auth.getLanguage()),
                                document.getNumber(), document.getDate(), part.toMessage(), warehouse.getName()));
                    }
                    continue;
                }
                if (BigDecimalUtils.isZero(inventoryNow.getQuantity())) continue;
                if (isReturning && (inventoryNow.getDoc() == null || !Objects.equals(returningDocId, inventoryNow.getDoc().getId()))) continue;
                inventories.add(new InventoryQ(inventoryNow.getQuantity(), inventoryNow.getCostTotal()));
            }
            remainders.put(key, inventories);
        }
        if (forwardSell) return;  // we can stop here if no errors

        BigDecimal quantity = BigDecimalUtils.abs(part.getQuantity());

        Iterator<InventoryQ> iterator = inventories.iterator();
        while (iterator.hasNext()) {
            InventoryQ inventoryQ = iterator.next();

            // do not proceed if negative quantity
            if (BigDecimalUtils.isNegative(inventoryQ.getQuantity())) break;

            // if quantity < inventory
            if (BigDecimalUtils.isLessThan(quantity, inventoryQ.getQuantity())) {
                inventoryQ.setQuantity(BigDecimalUtils.subtract(inventoryQ.getQuantity(), quantity));
                quantity = null;
                break;
            }
            // if quantity >= inventory
            quantity = BigDecimalUtils.subtract(quantity, inventoryQ.getQuantity());
            iterator.remove();

            if (BigDecimalUtils.isNegativeOrZero(quantity)) break;
        }
        if (BigDecimalUtils.isNegative(quantity)) {
            String msg = MessageFormat.format("{0} {1}: Error in quantity calculations of {2} in {3} - negative result {4}",
                    document.getNumber(), document.getDate(), part.toMessage(), warehouse.getName(), quantity);
            throw new GamaException(msg);
        }
        if (!BigDecimalUtils.isZero(quantity)) {
            String msg = MessageFormat.format(TranslationService.getInstance().translate(TranslationService.INVENTORY.NotEnoughQuantity, auth.getLanguage()),
                    document.getNumber(), document.getDate(), part.toMessage(), warehouse.getName(), quantity);
            if (messages == null) {
                log.info(this.getClass().getSimpleName() + "::checkPartInWarehouse" +
                        " GamaNotEnoughQuantityException: quantity is not zero" +
                        "\n document=" + document.getClass().getSimpleName() + "{id=" + document.getId() + ", date=" + document.getDate() + ", number=" + document.getNumber() + "}" +
                        "\n part=" + part +
                        "\n warehouse=" + warehouse +
                        "\n quantity=" + quantity);
                throw new GamaNotEnoughQuantityException(msg);
            } else {
                messages.add(msg);
                part.setNotEnough(quantity);
            }
        }
    }

    public void checkPartInventorySQL(Map<RemainderKey, List<InventoryQ>> remainders, IBaseDocument document,
                                     InventoryPartSql part, WarehouseTagged docWarehouse, List<String> messages) {

        part.setNotEnough(null);

        if (BooleanUtils.isTrue(part.getFinished()) || part.getType() == PartType.SERVICE) return;

        WarehouseTagged warehouse = Validators.isValid(part.getWarehouse())
                ? new WarehouseTagged(part.getWarehouse().getId(), part.getWarehouse().getName()) : docWarehouse;

        /*
         * if change = true then
         *  1) quantityChange is set
         *  2) quantityRemainder = quantityInitial + quantity
         *  3) costTotal is set only if quantityChange > 0, i.e. parts are added
         *
         * if change = false then
         *  1) quantityTotal is set
         *  2) quantity = quantityInitial - quantityRemainder
         */

        if (!part.isChange() || BigDecimalUtils.isPositiveOrZero(part.getQuantity())) return;

        var key = new RemainderKey()
                .setPartId(part.getPartId())
                .setWarehouseId(warehouse.getId());
        if (part.getType() == PartType.PRODUCT_SN) key.setPartSN(part.getSn());

        List<InventoryQ> inventories;
        if (remainders.containsKey(key)) {
            inventories = remainders.get(key);
        } else {
            List<InventoryNowSql> inventoriesNow = dbServiceSQL.getInventoriesNow(warehouse, part);
            if (CollectionsHelper.isEmpty(inventoriesNow)) {
                String msg = MessageFormat.format(TranslationService.getInstance().translate(TranslationService.INVENTORY.NotEnoughQuantity, auth.getLanguage()),
                        document.getNumber(), document.getDate(), part.toMessage(), warehouse.getName(), BigDecimalUtils.negated(part.getQuantity()));
                if (messages == null) {
                    log.info(this.getClass().getSimpleName() + "::checkPartInventory" +
                            " GamaNotEnoughQuantityException: no inventoriesNow" +
                            "\n document=" + document.getClass().getSimpleName() + "{id=" + document.getId() + ", date=" + document.getDate() + ", number=" + document.getNumber() + "}" +
                            "\n part=" + part +
                            "\n warehouse=" + warehouse);
                    throw new GamaNotEnoughQuantityException(msg);
                } else {
                    messages.add(msg);
                    part.setNotEnough(BigDecimalUtils.negated(part.getQuantity()));
                    return;
                }
            }

            inventories = new ArrayList<>();
            for (InventoryNowSql inventoryNow : inventoriesNow) {
                if (inventoryNow.getDoc().getDate().isAfter(document.getDate())) continue;
                if (BigDecimalUtils.isZero(inventoryNow.getQuantity())) continue;
                inventories.add(new InventoryQ(inventoryNow.getQuantity(), inventoryNow.getCostTotal()));
            }
            remainders.put(key, inventories);
        }

        BigDecimal quantity = BigDecimalUtils.negated(part.getQuantity());

        Iterator<InventoryQ> iterator = inventories.iterator();
        while (iterator.hasNext()) {
            InventoryQ inventoryQ = iterator.next();

            // do not proceed if negative quantity
            if (BigDecimalUtils.isNegative(inventoryQ.getQuantity())) break;

            // if quantity < inventory
            if (BigDecimalUtils.isLessThan(quantity, inventoryQ.getQuantity())) {
                inventoryQ.setQuantity(BigDecimalUtils.subtract(inventoryQ.getQuantity(), quantity));
                quantity = null;
                break;
            }
            // if quantity >= inventory
            quantity = BigDecimalUtils.subtract(quantity, inventoryQ.getQuantity());
            iterator.remove();

            if (BigDecimalUtils.isNegativeOrZero(quantity)) break;
        }
        if (BigDecimalUtils.isNegative(quantity)) {
            throw new GamaException(
                    MessageFormat.format("{0} {1}: Error in quantity calculations of {2} in {3} - negative result {4}",
                            document.getNumber(), document.getDate(), part.toMessage(), warehouse.getName(), quantity));
        }
        if (!BigDecimalUtils.isZero(quantity)) {
            String msg = MessageFormat.format(TranslationService.getInstance().translate(TranslationService.INVENTORY.NotEnoughQuantity, auth.getLanguage()),
                    document.getNumber(), document.getDate(), part.toMessage(), warehouse.getName(), quantity);
            if (messages == null) {
                log.info(this.getClass().getSimpleName() + "::checkPartInventory" +
                        " GamaNotEnoughQuantityException: quantity is not zero" +
                        "\n document=" + document.getClass().getSimpleName() + "{id=" + document.getId() + ", date=" + document.getDate() + ", number=" + document.getNumber() + "}" +
                        "\n part=" + part +
                        "\n warehouse=" + warehouse +
                        "\n quantity=" + quantity);
                throw new GamaNotEnoughQuantityException(msg);
            } else {
                messages.add(msg);
                part.setNotEnough(quantity);
            }
        }
    }

    public void checkPartInventoryRecallSQL(Map<RemainderKey, List<InventoryQ>> remainders,
                                           BaseDocumentSql document, InventoryPartSql part, WarehouseTagged docWarehouse) {
        if (BooleanUtils.isNotTrue(part.getFinished()) || part.getType() == PartType.SERVICE) return;

        WarehouseTagged warehouse = Validators.isValid(part.getWarehouse())
                ? new WarehouseTagged(part.getWarehouse().getId(), part.getWarehouse().getName()) : docWarehouse;

        /*
         * if change = true then
         *  1) quantityChange is set
         *  2) quantityRemainder = quantityInitial + quantity
         *  3) costTotal is set only if quantityChange > 0, i.e. parts are added
         *
         * if change = false then
         *  1) quantityTotal is set
         *  2) quantity = quantityInitial - quantityRemainder
         */

        if (part.isChange()) {
            if (BigDecimalUtils.isNegativeOrZero(part.getQuantity())) return;

            var key = new RemainderKey()
                    .setPartId(part.getPartId())
                    .setWarehouseId(warehouse.getId());
            if (part.getType() == PartType.PRODUCT_SN) key.setPartSN(part.getSn());

            List<InventoryQ> inventories;
            if (remainders.containsKey(key)) {
                inventories = remainders.get(key);
            } else {
                InventoryNowSql inventoryNow = dbServiceSQL.getInventoryNow(warehouse, part, document.getId());
                if (inventoryNow == null ||
                        inventoryNow.getDoc().getDate().isAfter(document.getDate()) ||
                        BigDecimalUtils.isZero(inventoryNow.getQuantity())) {
                    log.info(this.getClass().getSimpleName() + "::checkPartInventoryRecall" +
                            " GamaNotEnoughQuantityException: inventoryNow is null or ..." +
                            "\n document=" + document.getClass().getSimpleName() + "{id=" + document.getId() + ", date=" + document.getDate() + ", number=" + document.getNumber() + "}" +
                            "\n part=" + part +
                            "\n warehouse=" + warehouse);
                    throw new GamaNotEnoughQuantityException(
                            MessageFormat.format(TranslationService.getInstance().translate(TranslationService.INVENTORY.NotEnoughQuantity, auth.getLanguage()),
                                    document.getNumber(), document.getDate(), part.toMessage(), warehouse.getName(), part.getQuantity()));
                }
                //inventories = new ArrayList<>(inventoryNow.getRemainder());
                inventories = new ArrayList<>(Collections.singletonList(new InventoryQ(inventoryNow.getQuantity(), inventoryNow.getCostTotal())));
                remainders.put(key, inventories);
            }

            BigDecimal quantity = BigDecimalUtils.abs(part.getQuantity());

            Iterator<InventoryQ> iterator = inventories.iterator();
            while (iterator.hasNext()) {
                InventoryQ inventoryQ = iterator.next();

                // do not proceed if negative quantity
                if (BigDecimalUtils.isNegative(inventoryQ.getQuantity())) break;

                // if quantity < inventory
                if (BigDecimalUtils.isLessThan(quantity, inventoryQ.getQuantity())) {
                    inventoryQ.setQuantity(BigDecimalUtils.subtract(inventoryQ.getQuantity(), quantity));
                    quantity = null;
                    break;
                }
                // if quantity >= inventory
                quantity = BigDecimalUtils.subtract(quantity, inventoryQ.getQuantity());
                iterator.remove();

                if (BigDecimalUtils.isNegativeOrZero(quantity)) break;
            }
            if (BigDecimalUtils.isNegative(quantity)) {
                log.info(this.getClass().getSimpleName() + "::checkPartInventoryRecall" +
                        " GamaNotEnoughQuantityException: quantity is negative" +
                        "\n document=" + document.getClass().getSimpleName() + "{id=" + document.getId() + ", date=" + document.getDate() + ", number=" + document.getNumber() + "}" +
                        "\n part=" + part +
                        "\n warehouse=" + warehouse +
                        "\n quantity=" + quantity);
                throw new GamaNotEnoughQuantityException(
                        MessageFormat.format("{0} {1}: Not enough quantity of {2} in {3} - negative result {4}",
                                document.getNumber(), document.getDate(), part.toMessage(), warehouse.getName(), quantity));
            }
            if (!BigDecimalUtils.isZero(quantity)) {
                log.info(this.getClass().getSimpleName() + "::checkPartInventoryRecall" +
                        " GamaNotEnoughQuantityException: quantity is not zero" +
                        "\n document=" + document.getClass().getSimpleName() + "{id=" + document.getId() + ", date=" + document.getDate() + ", number=" + document.getNumber() + "}" +
                        "\n part=" + part +
                        "\n warehouse=" + warehouse +
                        "\n quantity=" + quantity);
                throw new GamaNotEnoughQuantityException(
                        MessageFormat.format(TranslationService.getInstance().translate(TranslationService.INVENTORY.NotEnoughQuantity, auth.getLanguage()),
                                document.getNumber(), document.getDate(), part.toMessage(), warehouse.getName(), quantity));
            }
        }
    }

    public <E extends IBaseDocPartSql & IDocPart & IPartSN & IFinished & IPartMessage>
    void checkPartInvoiceReturnSQL(Map<RemainderKey, List<InventoryQ>> remainders, IBaseDocument document, E part, Long returningDocId,
                                  WarehouseTagged docWarehouse, List<String> messages) {
        part.setNotEnough(null);

        // do nothing if:
        if (BooleanUtils.isTrue(part.getFinished()) ||
                BigDecimalUtils.isPositiveOrZero(part.getQuantity()) || // no returning
                returningDocId == null || // no returning document info
                part.getType() == PartType.SERVICE) return;

        WarehouseTagged warehouse = Validators.isValid(part.getWarehouse()) ? new WarehouseTagged(part.getWarehouse(), part.getTag()) : docWarehouse;

        var key = new RemainderKey()
                .setPartId(part.getPartId())
                .setWarehouseId(warehouse.getId());
        if (part.getType() == PartType.PRODUCT_SN) key.setPartSN(part.getSn());
        key.setDocId(returningDocId);

        List<InventoryQ> inventories;
        if (remainders.containsKey(key)) {
            inventories = remainders.get(key);
        } else {
            // retrieve quantities from all document
            InvoiceSql invoice = dbServiceSQL.getById(InvoiceSql.class, returningDocId);
            if (invoice == null) {
                log.info(this.getClass().getSimpleName() + "::checkPartInvoiceReturn" +
                        " GamaNotEnoughQuantityException: invoice is null" +
                        "\n document=" + document.getClass().getSimpleName() + "{id=" + document.getId() + ", date=" + document.getDate() + ", number=" + document.getNumber() + "}" +
                        "\n part=" + part +
                        "\n warehouse=" + warehouse);
                throw new GamaNotEnoughQuantityException(
                        MessageFormat.format(TranslationService.getInstance().translate(TranslationService.INVENTORY.NoOriginInvoice, auth.getLanguage()),
                                document.getNumber(), document.getDate(), part.toMessage(), warehouse.getName()));
            }
            inventories = new ArrayList<>();

            for (var invoicePart : invoice.getParts()) {
                if (!invoicePart.getPartId().equals(part.getPartId()))  continue;
                if (BigDecimalUtils.isNegativeOrZero(invoicePart.getQuantity())) continue;
                if (CollectionsHelper.isEmpty(invoicePart.getCostInfo())) continue;

                for (PartCostSource costSource : invoicePart.getCostInfo()) {
                    if (BigDecimalUtils.isGreaterThanOrEqual(costSource.getRetQuantity(), costSource.getQuantity())) continue;

                    inventories.add(new InventoryQ(
                            BigDecimalUtils.subtract(costSource.getQuantity(), costSource.getRetQuantity()),
                            GamaMoneyUtils.subtract(costSource.getCostTotal(), costSource.getRetCostTotal())));
                }

            }

            remainders.put(key, inventories);
        }

        BigDecimal quantity = BigDecimalUtils.abs(part.getQuantity());

        Iterator<InventoryQ> iterator = inventories.iterator();
        while (iterator.hasNext()) {
            InventoryQ inventoryQ = iterator.next();

            // do not proceed if negative quantity
            if (BigDecimalUtils.isNegative(inventoryQ.getQuantity())) break;

            // if quantity < inventory
            if (BigDecimalUtils.isLessThan(quantity, inventoryQ.getQuantity())) {
                inventoryQ.setQuantity(BigDecimalUtils.subtract(inventoryQ.getQuantity(), quantity));
                quantity = null;
                break;
            }
            // if quantity >= inventory
            quantity = BigDecimalUtils.subtract(quantity, inventoryQ.getQuantity());
            iterator.remove();

            if (BigDecimalUtils.isNegativeOrZero(quantity)) break;
        }
        if (BigDecimalUtils.isNegative(quantity)) {
            log.info(this.getClass().getSimpleName() + "::checkPartInvoiceReturn" +
                    " GamaNotEnoughQuantityException: quantity is negative" +
                    "\n document=" + document.getClass().getSimpleName() + "{id=" + document.getId() + ", date=" + document.getDate() + ", number=" + document.getNumber() + "}" +
                    "\n part=" + part +
                    "\n warehouse=" + warehouse +
                    "\n quantity=" + quantity);
            throw new GamaNotEnoughQuantityException(
                    MessageFormat.format("{0} {1}: Not enough quantity of {2} in {3} - negative result {4}",
                            document.getNumber(), document.getDate(), part.toMessage(), warehouse.getName(), quantity));
        }
        if (!BigDecimalUtils.isZero(quantity)) {
            String msg = MessageFormat.format(TranslationService.getInstance().translate(TranslationService.INVENTORY.NotEnoughQuantity, auth.getLanguage()),
                    document.getNumber(), document.getDate(), part.toMessage(), warehouse.getName(), quantity);
            if (messages == null) {
                log.info(this.getClass().getSimpleName() + "::checkPartInvoiceReturn" +
                        " GamaNotEnoughQuantityException: quantity is not zero" +
                        "\n document=" + document.getClass().getSimpleName() + "{id=" + document.getId() + ", date=" + document.getDate() + ", number=" + document.getNumber() + "}" +
                        "\n part=" + part +
                        "\n warehouse=" + warehouse +
                        "\n quantity=" + quantity);
                throw new GamaNotEnoughQuantityException(msg);
            } else {
                messages.add(msg);
                part.setNotEnough(quantity);
            }
        }
    }

    public <E extends IBaseDocPartSql & IDocPart & IPartSN & IFinished & IPartMessage>
    void checkPartInvoiceReturnRecallSQL(Map<RemainderKey, List<InventoryQ>> remainders, BaseDocumentSql document, E part, Long returningDocId,
                                        WarehouseTagged docWarehouse) {
        if (BooleanUtils.isNotTrue(part.getFinished()) || part.getType() == PartType.SERVICE) return;
        if (!(document instanceof InvoiceSql)) return;
        if (returningDocId == null) return;

        InvoiceSql originInvoice = dbServiceSQL.getById(InvoiceSql.class, returningDocId);
        if (CollectionsHelper.isEmpty(originInvoice.getParts()))
            throw new GamaException(
                    MessageFormat.format(TranslationService.getInstance().translate(TranslationService.INVENTORY.NoPartsInOriginInvoice, auth.getLanguage()),
                            document.getNumber(), document.getDate(), part.toMessage(),
                            originInvoice.getDate(), originInvoice.getNumber()));

        WarehouseTagged warehouse = Validators.isValid(part.getWarehouse())
                ? new WarehouseTagged(part.getWarehouse().getId(), part.getWarehouse().getName()) : docWarehouse;

        var key = new RemainderKey()
                .setPartId(part.getPartId())
                .setWarehouseId(warehouse.getId());
        if (part.getType() == PartType.PRODUCT_SN) key.setPartSN(part.getSn());

        boolean done = false;
        GamaMoney costTotal = null;
        BigDecimal quantity = BigDecimalUtils.abs(part.getQuantity());

        for (var partInvoice : originInvoice.getParts()) {
            if (!Objects.equals(partInvoice.getPartId(), part.getPartId())) continue;
            if (!PartSN.equals(partInvoice.getSn(), part.getSn())) continue;
            if (!GamaMoneyUtils.isEqual(partInvoice.getPrice(), part.getPrice())) continue;
            if (partInvoice.getCostInfo() == null) continue;

            List<InventoryQ> costInfo = new ArrayList<>();
            for (PartCostSource partCostSource : Lists.reverse(partInvoice.getCostInfo())) {
                if (!BigDecimalUtils.isPositive(partCostSource.getRetQuantity())) continue;
                costInfo.add(new InventoryQ(partCostSource.getRetQuantity(), partCostSource.getRetCostTotal(),
                        partCostSource.getDoc().getId(), partCostSource.getDoc().getDb()));
            }

            for (InventoryQ partCostSource : costInfo) {
                if (!BigDecimalUtils.isPositive(partCostSource.getQuantity())) continue;

                BigDecimal qty;
                GamaMoney cost = null;

                if (BigDecimalUtils.isLessThan(quantity, partCostSource.getQuantity())) {

                    if (GamaMoneyUtils.isNonZero(partCostSource.getCostTotal())) {

                        GamaBigMoney unitPrice = partCostSource.getCostTotal().toBigMoney().withScale(8)
                                .dividedBy(partCostSource.getQuantity());
                        cost = GamaMoneyUtils.toMoney(unitPrice.multipliedBy(BigDecimalUtils.doubleValue(quantity)));
                        if (GamaMoneyUtils.isGreaterThan(cost, partCostSource.getCostTotal()))
                            cost = partCostSource.getCostTotal();

                        partCostSource.setCostTotal(GamaMoneyUtils.subtract(partCostSource.getCostTotal(), cost));
                    }
                    partCostSource.setQuantity(BigDecimalUtils.subtract(partCostSource.getQuantity(), quantity));

                    qty = quantity;
                    quantity = null;

                } else {
                    cost = partCostSource.getCostTotal();
                    qty = partCostSource.getQuantity();

                    partCostSource.setQuantity(null);
                    partCostSource.setCostTotal(null);

                    quantity = BigDecimalUtils.subtract(quantity, qty);
                }

                // check inventory
                key.setDocId(partCostSource.getDocumentId());
                List<InventoryQ> inventories;
                if (remainders.containsKey(key)) {
                    inventories = remainders.get(key);
                } else {
                    InventoryNowSql inventoryNow = dbServiceSQL.getInventoryNow(warehouse, part, partCostSource.getDocumentId());

                    if (inventoryNow == null || BigDecimalUtils.isZero(inventoryNow.getQuantity())) {
                        log.info(this.getClass().getSimpleName() + "::checkPartInvoiceReturnRecall" +
                                " GamaNotEnoughQuantityException: inventoryNow is null or no quantity" +
                                "\n document=" + document.getClass().getSimpleName() + "{id=" + document.getId() + ", date=" + document.getDate() + ", number=" + document.getNumber() + "}" +
                                "\n part=" + part +
                                "\n warehouse=" + warehouse +
                                "\n quantity=" + quantity);
                        throw new GamaNotEnoughQuantityException(
                                MessageFormat.format(TranslationService.getInstance().translate(TranslationService.INVENTORY.NotEnoughQuantity, auth.getLanguage()),
                                        document.getNumber(), document.getDate(), part.toMessage(), warehouse.getName(), part.getQuantity()));
                    }

                    inventories = new ArrayList<>(List.of(new InventoryQ(inventoryNow.getQuantity(), inventoryNow.getCostTotal())));
                    remainders.put(key, inventories);
                }

                costTotal = GamaMoneyUtils.add(costTotal, cost);

                Iterator<InventoryQ> iterator = inventories.iterator();
                // cost, qty
                while (iterator.hasNext()) {
                    InventoryQ inventoryQ = iterator.next();

                    if (BigDecimalUtils.isLessThanOrEqual(qty, inventoryQ.getQuantity())) {
                        inventoryQ.setQuantity(BigDecimalUtils.subtract(inventoryQ.getQuantity(), qty));
                        qty = null;
                    } else {
                        qty = BigDecimalUtils.subtract(qty, inventoryQ.getQuantity());
                        inventoryQ.setQuantity(null);
                    }
                    if (GamaMoneyUtils.isLessThanOrEqual(cost, inventoryQ.getCostTotal())) {
                        inventoryQ.setCostTotal(GamaMoneyUtils.subtract(inventoryQ.getCostTotal(), cost));
                        cost = null;
                    } else {
                        cost = GamaMoneyUtils.subtract(cost, inventoryQ.getCostTotal());
                        inventoryQ.setCostTotal(null);
                    }

                    if (BigDecimalUtils.isZero(inventoryQ.getQuantity()) && GamaMoneyUtils.isZero(inventoryQ.getCostTotal())) {
                        iterator.remove();
                    }

                    if (qty == null && cost == null) break;
                }

                if (qty != null || cost != null) {
                    log.info(this.getClass().getSimpleName() + "::checkPartInvoiceReturnRecall" +
                            " GamaNotEnoughQuantityException: qty != null or cost != null" +
                            "\n qty=" + qty + ", cost=" + cost +
                            "\n document=" + document.getClass().getSimpleName() + "{id=" + document.getId() + ", date=" + document.getDate() + ", number=" + document.getNumber() + "}" +
                            "\n part=" + part +
                            "\n warehouse=" + warehouse +
                            "\n quantity=" + quantity);
                    throw new GamaNotEnoughQuantityException(
                            MessageFormat.format(TranslationService.getInstance().translate(TranslationService.INVENTORY.NotEnoughQuantity, auth.getLanguage()),
                                    document.getNumber(), document.getDate(), part.toMessage(), warehouse.getName(), quantity));
                }

                done = BigDecimalUtils.isZero(quantity);
            }
            if (done) break;
        }
        if (!done) {
            log.info(this.getClass().getSimpleName() + "::checkPartInvoiceReturnRecall" +
                    " GamaNotEnoughQuantityException: !done" +
                    "\n document=" + document.getClass().getSimpleName() + "{id=" + document.getId() + ", date=" + document.getDate() + ", number=" + document.getNumber() + "}" +
                    "\n part=" + part +
                    "\n warehouse=" + warehouse +
                    "\n quantity=" + quantity);
            throw new GamaNotEnoughQuantityException(
                    MessageFormat.format(TranslationService.getInstance().translate(TranslationService.INVENTORY.NotEnoughQuantity, auth.getLanguage()),
                            document.getNumber(), document.getDate(), part.toMessage(), warehouse.getName(), quantity));
        }
    }
    
    public <E extends IBaseDocPartCost & IBaseDocPartSql & IDocPart & IPartSN& IFinished & IPartMessage>
    void checkPartInvoiceRecallSQL(BaseDocumentSql document, E part) {
        if (BooleanUtils.isNotTrue(part.getFinished()) || part.getType() == PartType.SERVICE) return;
        if (!(document instanceof InvoiceSql)) return;

        for (var partInvoice : ((InvoiceSql) document).getParts()) {
            if (!Objects.equals(partInvoice.getPartId(), part.getPartId())) continue;
            if (!PartSN.equals(partInvoice.getSn(), part.getSn())) continue;
            if (!GamaMoneyUtils.isEqual(partInvoice.getPrice(), part.getPrice())) continue;
            if (partInvoice.getCostInfo() == null) continue;

            for (PartCostSource partCostSource : partInvoice.getCostInfo()) {

                if (!BigDecimalUtils.isZero(partCostSource.getRetQuantity())) {
                    log.info(this.getClass().getSimpleName() + "::checkPartInvoiceRecall" +
                            " GamaNotEnoughQuantityException: partCostSource.getRetQuantity() is not zero" +
                            "\n partCostSource.getRetQuantity()=" + partCostSource.getRetQuantity() +
                            "\n document=" + document.getClass().getSimpleName() + "{id=" + document.getId() + ", date=" + document.getDate() + ", number=" + document.getNumber() + "}" +
                            "\n part=" + part);
                    throw new GamaNotEnoughQuantityException(
                            MessageFormat.format(
                                    TranslationService.getInstance().translate(TranslationService.INVENTORY.NotEnoughQuantityInvoiceRecall, auth.getLanguage()),
                                    document.getNumber(), document.getDate(), part.toMessage()));
                }
            }
        }
    }

    public <E extends IBaseDocPartSql & IDocPart & IPartSN & IFinished & IPartMessage>
    void checkPartRecallSQL(Map<RemainderKey, List<InventoryQ>> remainders, BaseDocumentSql document, E part, WarehouseTagged docWarehouse) {

        if (BooleanUtils.isNotTrue(part.getFinished()) || part.getType() == PartType.SERVICE) return;
        if (part.isForwardSell() && document instanceof InvoiceSql) return;

        WarehouseTagged warehouse = Validators.isValid(part.getWarehouse()) ? new WarehouseTagged(part.getWarehouse(), part.getTag()) : docWarehouse;

        var key = new RemainderKey()
                .setPartId(part.getPartId())
                .setWarehouseId(warehouse.getId());
        if (part.getType() == PartType.PRODUCT_SN) key.setPartSN(part.getSn());

        List<InventoryQ> inventories;
        if (remainders.containsKey(key)) {
            inventories = remainders.get(key);
        } else {
            InventoryNowSql inventoryNow = dbServiceSQL.getInventoryNow(warehouse, part, document.getId());

            if (inventoryNow == null ||
                    inventoryNow.getDoc().getDate().isAfter(document.getDate()) ||
                    BigDecimalUtils.isZero(inventoryNow.getQuantity())) {
                log.info(this.getClass().getSimpleName() + "::checkPartRecall" +
                        " GamaNotEnoughQuantityException: inventoryNow is null or ..." +
                        "\n document=" + document.getClass().getSimpleName() + "{id=" + document.getId() + ", date=" + document.getDate() + ", number=" + document.getNumber() + "}" +
                        "\n part=" + part +
                        "\n warehouse=" + warehouse);
                throw new GamaNotEnoughQuantityException(
                        MessageFormat.format(TranslationService.getInstance().translate(TranslationService.INVENTORY.NotEnoughQuantity, auth.getLanguage()),
                                document.getNumber(), document.getDate(), part.toMessage(), warehouse.getName(), part.getQuantity()));
            }
            inventories = new ArrayList<>(List.of(new InventoryQ(inventoryNow.getQuantity(), inventoryNow.getCostTotal())));
            remainders.put(key, inventories);
        }

        BigDecimal quantity = BigDecimalUtils.abs(part.getQuantity());

        Iterator<InventoryQ> iterator = inventories.iterator();
        while (iterator.hasNext()) {
            InventoryQ inventoryQ = iterator.next();

            // do not proceed if negative quantity
            if (BigDecimalUtils.isNegative(inventoryQ.getQuantity())) break;

            // if quantity < inventory
            if (BigDecimalUtils.isLessThan(quantity, inventoryQ.getQuantity())) {
                inventoryQ.setQuantity(BigDecimalUtils.subtract(inventoryQ.getQuantity(), quantity));
                quantity = null;
                break;
            }
            // if quantity >= inventory
            quantity = BigDecimalUtils.subtract(quantity, inventoryQ.getQuantity());
            iterator.remove();

            if (BigDecimalUtils.isNegativeOrZero(quantity)) break;
        }
        if (BigDecimalUtils.isNegative(quantity)) {
            log.info(this.getClass().getSimpleName() + "::checkPartRecall" +
                    " GamaNotEnoughQuantityException: quantity is negative" +
                    "\n document=" + document.getClass().getSimpleName() + "{id=" + document.getId() + ", date=" + document.getDate() + ", number=" + document.getNumber() + "}" +
                    "\n part=" + part +
                    "\n warehouse=" + warehouse +
                    "\n quantity=" + quantity);
            throw new GamaNotEnoughQuantityException(
                    MessageFormat.format("{0} {1}: Not enough quantity of {2} in {3} - negative result {4}",
                            document.getNumber(), document.getDate(), part.toMessage(), warehouse.getName(), quantity));
        }
        if (!BigDecimalUtils.isZero(quantity)) {
            log.info(this.getClass().getSimpleName() + "::checkPartRecall" +
                    " GamaNotEnoughQuantityException: quantity is not zero" +
                    "\n document=" + document.getClass().getSimpleName() + "{id=" + document.getId() + ", date=" + document.getDate() + ", number=" + document.getNumber() + "}" +
                    "\n part=" + part +
                    "\n warehouse=" + warehouse +
                    "\n quantity=" + quantity);
            throw new GamaNotEnoughQuantityException(
                    MessageFormat.format(TranslationService.getInstance().translate(TranslationService.INVENTORY.NotEnoughQuantity, auth.getLanguage()),
                            document.getNumber(), document.getDate(), part.toMessage(), warehouse.getName(), quantity));
        }
    }

    public <E extends IUuid & IQuantity> void checkPartUuids(List<E> parts, boolean notNull) {
        Set<UUID> uuids = new HashSet<>();
        CollectionsHelper.streamOf(parts)
                .flatMap(p -> Stream.concat(
                        Stream.of(p),
                        CollectionsHelper.streamOf(p instanceof IUuidParts<?> pi ? pi.getParts() : List.of())))
                .forEach(p -> {
                    UUID uuid = p.getUuid();
                    if (notNull && BigDecimalUtils.isNegative(p.getQuantity()) && uuid == null) {
                        throw new GamaException(TranslationService.getInstance().translate(TranslationService.INVENTORY.NoUuid));
                    }
                    if (BigDecimalUtils.isPositiveOrZero(p.getQuantity()) && (uuid == null || uuids.contains(p.getUuid()))) {
                        uuid = UUID.randomUUID();
                        p.setUuid(uuid);
                    }
                    uuids.add(uuid);
                });
    }

    public <E extends ILinkUuidParts<? extends IParentLinkUuid>> void checkPartLinkUuids(List<E> parts) {
        Set<UUID> uuids = new HashSet<>();
        CollectionsHelper.streamOf(parts).forEach(p-> {
            UUID uuid = p.getLinkUuid();
            if (uuid == null || uuids.contains(p.getLinkUuid())) {
                uuid = UUID.randomUUID();
                p.setLinkUuid(uuid);
            }
            uuids.add(uuid);
            CollectionsHelper.streamOf(p.getParts())
                    .filter(s -> !Objects.equals(s.getParentLinkUuid(), p.getLinkUuid()))
                    .forEach(s -> s.setParentLinkUuid(p.getLinkUuid()));
        });
    }

    public <E extends IDocPart> void checkPartLinkUuidsEntity(List<E> parts, Function<UUID, E> linkUuidFunc) {
        UUID fixPartLinkUuid = UUID.randomUUID();
        Set<UUID> uuids = new HashSet<>();
        CollectionsHelper.streamOf(parts)
                .filter(p -> p instanceof ILinkUuid)
                .map(ILinkUuid.class::cast)
                .forEach(p -> {
                    UUID uuid = p.getLinkUuid();
                    if (uuid == null || uuids.contains(p.getLinkUuid())) {
                        uuid = UUID.randomUUID();
                        p.setLinkUuid(uuid);
                    }
                    uuids.add(uuid);
                });
        var fixSubparts = new MutableBoolean();
        CollectionsHelper.streamOf(parts)
                .filter(p -> p instanceof IParentLinkUuid)
                .map(IParentLinkUuid.class::cast)
                .filter(s -> !uuids.contains(s.getParentLinkUuid()))
                .forEach(s -> {
                    s.setParentLinkUuid(fixPartLinkUuid);
                    fixSubparts.setTrue();
                });
        if (fixSubparts.isTrue()) {
            E fixPart = linkUuidFunc.apply(fixPartLinkUuid);
            fixPart.setDocPart(new DocPart());
            fixPart.setName("*** FIX ***");
            parts.add(fixPart);
        }
    }
}

package lt.gama.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import lt.gama.Constants;
import lt.gama.api.request.InventoryBalanceRequest;
import lt.gama.api.request.MailRequestContact;
import lt.gama.api.request.PageRequest;
import lt.gama.api.request.PageRequestCondition;
import lt.gama.api.response.InventoryBalanceResponse;
import lt.gama.api.response.InventoryGpais;
import lt.gama.api.response.PageResponse;
import lt.gama.auth.i.IAuth;
import lt.gama.auth.i.IPermission;
import lt.gama.auth.impl.Auth;
import lt.gama.freemarker.GamaMoneyFormatter;
import lt.gama.helpers.*;
import lt.gama.model.dto.documents.InvoiceDto;
import lt.gama.model.dto.entities.InventoryHistoryDto;
import lt.gama.model.dto.entities.InventoryNowDto;
import lt.gama.model.dto.entities.PartDto;
import lt.gama.model.dto.entities.PartPartDto;
import lt.gama.model.i.*;
import lt.gama.model.i.base.IBaseCompany;
import lt.gama.model.mappers.InventoryHistorySqlMapper;
import lt.gama.model.mappers.InventoryNowSqlMapper;
import lt.gama.model.mappers.InvoiceSqlMapper;
import lt.gama.model.mappers.PartSqlMapper;
import lt.gama.model.sql.base.BaseDocumentSql;
import lt.gama.model.sql.base.EntitySql;
import lt.gama.model.sql.documents.*;
import lt.gama.model.sql.documents.items.*;
import lt.gama.model.sql.entities.*;
import lt.gama.model.sql.entities.id.ImportId;
import lt.gama.model.sql.system.CompanySql;
import lt.gama.model.sql.system.CountryVatRateSql;
import lt.gama.model.type.GamaBigMoney;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.Packaging;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.model.type.auth.CounterDesc;
import lt.gama.model.type.auth.VATRatesDate;
import lt.gama.model.type.balance.InventoryTypeBalance;
import lt.gama.model.type.doc.Doc;
import lt.gama.model.type.doc.DocCounterparty;
import lt.gama.model.type.doc.DocWarehouse;
import lt.gama.model.type.doc.Doc_;
import lt.gama.model.type.enums.*;
import lt.gama.model.type.gl.GLDC;
import lt.gama.model.type.ibase.IBaseDocPartCost;
import lt.gama.model.type.ibase.IBaseDocPartOutRemainder;
import lt.gama.model.type.ibase.IBaseDocPartSql;
import lt.gama.model.type.inventory.VATCodeTotal;
import lt.gama.model.type.inventory.WarehouseTagged;
import lt.gama.model.type.part.DocPart;
import lt.gama.model.type.part.DocPartBalance;
import lt.gama.model.type.part.PartCostSource;
import lt.gama.model.type.part.PartSN;
import lt.gama.report.RepInventoryBalance;
import lt.gama.report.RepInventoryDetail;
import lt.gama.service.ex.rt.GamaException;
import lt.gama.service.ex.rt.GamaNotEnoughQuantityException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static lt.gama.ConstWorkers.DOCS_PRINT_FOLDER;
import static lt.gama.ConstWorkers.GCS_BUFFER_SIZE;

/**
 * Gama
 * Created by valdas on 15-04-09.
 *
 */
@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final MailService mailService;
    private final TemplateService templateService;
    private final Auth auth;
    private final DBServiceSQL dbServiceSQL;
    private final CounterService counterService;
    private final StorageService storageService;
    private final PartSqlMapper partSqlMapper;
    private final InventoryNowSqlMapper inventoryNowSqlMapper;
    private final InventoryHistorySqlMapper inventoryHistorySqlMapper;
    private final InvoiceSqlMapper invoiceSqlMapper;
    private final ObjectMapper objectMapper;

    InventoryService(MailService mailService,
                     TemplateService templateService,
                     Auth auth,
                     DBServiceSQL dbServiceSQL,
                     CounterService counterService,
                     StorageService storageService,
                     PartSqlMapper partSqlMapper,
                     InventoryNowSqlMapper inventoryNowSqlMapper,
                     InventoryHistorySqlMapper inventoryHistorySqlMapper,
                     InvoiceSqlMapper invoiceSqlMapper, ObjectMapper objectMapper) {
        this.mailService = mailService;
        this.templateService = templateService;
        this.auth = auth;
        this.dbServiceSQL = dbServiceSQL;
        this.counterService = counterService;
        this.storageService = storageService;
        this.partSqlMapper = partSqlMapper;
        this.inventoryNowSqlMapper = inventoryNowSqlMapper;
        this.inventoryHistorySqlMapper = inventoryHistorySqlMapper;
        this.invoiceSqlMapper = invoiceSqlMapper;
        this.objectMapper = objectMapper;
    }

    public PageResponse<PartDto, Void> listPart(PageRequest request) {
        String country = Validators.checkNotNull(
                Validators.checkNotNull(auth.getSettings(), "No company settings").getCountry(),
                "No country in settings");

        PageResponse<PartDto, Void> response = dbServiceSQL.queryPage(request, PartSql.class, null, partSqlMapper,
                () -> allQueryPart(request),
                () -> countQueryPart(request),
                (resp) -> dataQueryPart(request, resp));

        List<PartDto> items = response.getItems();

        if (PageRequestUtils.getFieldValue(request.getConditions(), "?detail") != null) {
            if (CollectionsHelper.hasValue(items)) {
                List<Long> ids = new ArrayList<>();
                for (PartDto part : items) {
                    if (CollectionsHelper.hasValue(part.getRemainders())) {
                        ids.add(part.getId());
                    }
                }
                if (CollectionsHelper.hasValue(ids)) {
                    List<InventoryNowSql> rems = entityManager
                            .createQuery(
                                    "SELECT i FROM " + InventoryNowSql.class.getName() + " i" +
                                            " LEFT JOIN FETCH i." + InventoryNowSql_.WAREHOUSE + " w" +
                                            " WHERE i." + InventoryNowSql_.COMPANY_ID + " = :companyId" +
                                            " AND i." + InventoryNowSql_.PART + "." + PartSql_.ID + " IN :ids" +
                                            " ORDER BY i." + InventoryNowSql_.PART + "." + PartSql_.ID +
                                            ", i." + InventoryNowSql_.DOC + ".date" +
                                            ", i." + InventoryNowSql_.DOC + ".ordinal" +
                                            ", i." + InventoryNowSql_.DOC + ".series" +
                                            ", i." + InventoryNowSql_.DOC + ".number" +
                                            ", i." + InventoryNowSql_.DOC + ".id",
                                    InventoryNowSql.class)
                            .setParameter("companyId", auth.getCompanyId())
                            .setParameter("ids", ids)
                            .getResultList();

                    if (CollectionsHelper.hasValue(rems)) {
                        Map<Long, List<InventoryNowDto>> mapByPart = rems.stream()
                                .map(inventoryNowSqlMapper::toDto)
                                .collect(Collectors.groupingBy(InventoryNowDto::getPartId));
                        for (PartDto part : items) {
                            part.setRemaindersNow(mapByPart.get(part.getId()));
                        }
                    }
                }
            }
        }

        updatePartsVAT(items, country);

        return response;
    }

    private void makeQueryPart(PageRequest request, StringJoiner sj, Map<String, Object> params) {
        if (StringHelper.hasValue(request.getFilter())) {
            sj.add("LEFT JOIN manufacturer m ON m.id = p.manufacturer_id");
            sj.add("LEFT JOIN jsonb_array_elements_text(jsonb_path_query_array(translation, '$.*.name')) nameTr ON true");
        }
        sj.add("WHERE p.company_id = :companyId");
        params.put("companyId", auth.getCompanyId());
        sj.add("AND (p.archive IS null OR p.archive = false)");
        sj.add("AND (p.hidden IS null OR p.hidden = false)");
        if (StringHelper.hasValue(request.getFilter())) {
            sj.add("AND (");
            Collection<String> tokens = StringHelper.splitTokens(StringUtils.stripAccents(request.getFilter()).toLowerCase().trim());
            if (CollectionsHelper.hasValue(tokens)) {
                sj.add("(");
                Iterator<String> iterator = tokens.iterator();
                int filterSentence = 0;
                while (iterator.hasNext()) {
                    filterSentence++;
                    String s = iterator.next();
                    sj.add("(");
                    sj.add("unaccent(trim(p.name)) ~* ('(^|\\s)' || :" + filterSentence + ")");
                    sj.add("OR trim(regexp_replace(unaccent(p.name), '[^[:alnum:]]+', ' ', 'g')) ~* ('(^|\\s)' || :" + filterSentence + ")");
                    sj.add("OR unaccent(trim(account_asset_name)) ~* ('(^|\\s)' || :" + filterSentence + ")");
                    sj.add("OR trim(regexp_replace(unaccent(account_asset_name), '[^[:alnum:]]+', ' ', 'g')) ~* ('(^|\\s)' || :" + filterSentence + ")");
                    sj.add("OR unaccent(trim(gl_income_debit_name)) ~* ('(^|\\s)' || :" + filterSentence + ")");
                    sj.add("OR trim(regexp_replace(unaccent(gl_income_debit_name), '[^[:alnum:]]+', ' ', 'g')) ~* ('(^|\\s)' || :" + filterSentence + ")");
                    sj.add("OR unaccent(trim(gl_income_credit_name)) ~* ('(^|\\s)' || :" + filterSentence + ")");
                    sj.add("OR trim(regexp_replace(unaccent(gl_income_credit_name), '[^[:alnum:]]+', ' ', 'g')) ~* ('(^|\\s)' || :" + filterSentence + ")");
                    sj.add("OR unaccent(trim(gl_expense_debit_name)) ~* ('(^|\\s)' || :" + filterSentence + ")");
                    sj.add("OR trim(regexp_replace(unaccent(gl_expense_debit_name), '[^[:alnum:]]+', ' ', 'g')) ~* ('(^|\\s)' || :" + filterSentence + ")");
                    sj.add("OR unaccent(trim(gl_expense_credit_name)) ~* ('(^|\\s)' || :" + filterSentence + ")");
                    sj.add("OR trim(regexp_replace(unaccent(gl_expense_credit_name), '[^[:alnum:]]+', ' ', 'g')) ~* ('(^|\\s)' || :" + filterSentence + ")");
                    sj.add("OR unaccent(trim(gl_expense_credit_name)) ~* ('(^|\\s)' || :" + filterSentence + ")");
                    sj.add("OR trim(regexp_replace(unaccent(gl_expense_credit_name), '[^[:alnum:]]+', ' ', 'g')) ~* ('(^|\\s)' || :" + filterSentence + ")");
                    sj.add("OR unaccent(trim(nameTr)) ~* ('(^|\\s)' || :" + filterSentence + ")");
                    sj.add("OR trim(regexp_replace(unaccent(nameTr), '[^[:alnum:]]+', ' ', 'g')) ~* ('(^|\\s)' || :" + filterSentence + ")");
                    sj.add("OR unaccent(trim(m.name)) ~* ('(^|\\s)' || :" + filterSentence + ")");
                    sj.add("OR trim(regexp_replace(unaccent(m.name), '[^[:alnum:]]+', ' ', 'g')) ~* ('(^|\\s)' || :" + filterSentence + ")");
                    sj.add(")");
                    if (iterator.hasNext()) sj.add("AND");
                    params.put(String.valueOf(filterSentence), s);
                }
                sj.add(")");
                sj.add("OR");
            }
            String filterWord = StringUtils.stripAccents(request.getFilter().charAt(0) == '\"'
                    ? request.getFilter().substring(1)
                    : request.getFilter()).toLowerCase().trim() + "%";
            if (StringHelper.hasValue(filterWord)) {
                sj.add("unaccent(trim(sku)) ILIKE :filterWord");
                sj.add("OR unaccent(trim(account_asset_number)) ILIKE :filterWord");
                sj.add("OR unaccent(trim(gl_income_debit_number)) ILIKE :filterWord");
                sj.add("OR unaccent(trim(gl_income_credit_number)) ILIKE :filterWord");
                sj.add("OR unaccent(trim(gl_expense_debit_number)) ILIKE :filterWord");
                sj.add("OR unaccent(trim(gl_expense_credit_number)) ILIKE :filterWord");
                sj.add("OR");
                params.put("filterWord", filterWord);
            }
            sj.add("unaccent(barcode) ILIKE :filterBarcode");
            sj.add("OR trim(regexp_replace(unaccent(barcode), '(\\s+)', '', 'g')) ILIKE :filterBarcode");
            params.put("filterBarcode", StringUtils.stripAccents(request.getFilter()).toLowerCase().trim());
            sj.add(")");
        }

        if (StringHelper.hasValue(request.getLabel())) {
            sj.add("AND (jsonb_path_exists(labels, '$[*] ? (@ == $label)', jsonb_build_object('label', :label)))");
            params.put("label", request.getLabel());
        }

        if (request.getConditions() != null) {
            for (PageRequestCondition condition : request.getConditions()) {
                if (CustomSearchType.PART_TYPE.getField().equals(condition.getField()) && condition.getValue() instanceof String) {
                    sj.add("AND type = :type");
                    params.put("type", condition.getValue());

                }
                if (CustomSearchType.REMAINDER.getField().equals(condition.getField()) && condition.getValue() instanceof Boolean) {
                    sj.add("AND (jsonb_path_exists(remainder, '$.* ? (@.quantity != 0)'))");
                }

            }
        }
    }

    private String partOrder(String field) {
        CompanySettings settings = auth.getSettings();
        if (StringHelper.isEmpty(field)) {
            PartSortOrderType sortOrderType = settings.getSales() != null ? settings.getSales().getDefaultPartSortOrder() : null;
            return sortOrderType == PartSortOrderType.SKU
                    ? "ORDER BY lower(unaccent(p.sku)), lower(unaccent(p.name)), p.id"
                    : "ORDER BY lower(unaccent(p.name)), p.id";
        }
        String order = "";
        if (field.charAt(0) == '-') {
            order = " DESC ";
            field = field.substring(1);
        }
        return switch (field.toLowerCase()) {
            case "sku" -> "ORDER BY lower(unaccent(p.sku)) " + order + ", lower(unaccent(p.name)) "  + order + ", p.id " + order;
            default -> "ORDER BY lower(unaccent(p.name)) " + order + ", p.id " + order;
        };
    }

    private Query allQueryPart(PageRequest request) {
        Map<String, Object> params = new HashMap<>();
        StringJoiner sj = new StringJoiner(" ");
        sj.add("SELECT p.* FROM parts p");
        makeQueryPart(request, sj, params);

        sj.add(partOrder(request.getOrder()));

        Query query = entityManager.createNativeQuery(sj.toString(), PartSql.class);
        params.forEach(query::setParameter);

        return query;
    }

    private Integer countQueryPart(PageRequest request) {
        Map<String, Object> params = new HashMap<>();
        StringJoiner sj = new StringJoiner(" ");
        sj.add("SELECT COUNT(DISTINCT p.id) FROM parts p");
        makeQueryPart(request, sj, params);

        Query query = entityManager.createNativeQuery(sj.toString());
        params.forEach(query::setParameter);

        return ((BigInteger) query.getSingleResult()).intValue();
    }

    private Query idsPageQueryPart(PageRequest request) {
        Map<String, Object> params = new HashMap<>();
        StringJoiner sj = new StringJoiner(" ");
        sj.add("SELECT DISTINCT p.id, lower(unaccent(p.name)) as name, lower(unaccent(p.sku)) as sku FROM parts p");
        makeQueryPart(request, sj, params);
        sj.add(partOrder(request.getOrder()));

        var query = entityManager.createNativeQuery(sj.toString(), Tuple.class);
        params.forEach(query::setParameter);
        return query;
    }

    private Query dataQueryPart(PageRequest request, PageResponse<PartDto, ?> response) {
        int cursor = request.getCursor();

        @SuppressWarnings("unchecked")
        List<Tuple> listId = idsPageQueryPart(request)
                .setFirstResult(cursor)
                .setMaxResults(request.getPageSize() + 1)
                .getResultList();

        response.setResponseValues(request, cursor, listId.size());

        return listId.isEmpty() ? null :
                entityManager.createQuery(
                                "SELECT p FROM " + PartSql.class.getName() + " p" +
                                        " WHERE id IN :ids" +
                                        ' ' + partOrder(request.getOrder()),
                                PartSql.class)
                        .setParameter("ids",
                                listId.stream()
                                        .limit(request.getPageSize())
                                        .map(x -> x.get("id", Number.class).longValue())
                                        .collect(Collectors.toList()));
    }

    public PartDto savePart(PartDto request, IPermission permission) {
        Validators.checkNotNull(request.getType(), "No type");

        final Long id = request.getId();
        final long companyId = auth.getCompanyId();
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        final boolean isCompanyAdmin = permission.checkPermission(Permission.ADMIN);
        final boolean isAccountant = isCompanyAdmin || permission.checkPermission(Permission.GL);
        final boolean isSettingsAdmin = isCompanyAdmin || permission.checkPermission(Permission.SETTINGS);

        if (!companySettings.isDisableGL() && companySettings.getGl() != null) {
            if (request.getType() == PartType.SERVICE) {
                if (!Validators.isPartialValid(request.getGlIncome())) {
                    request.setGlIncome(new GLDC(companySettings.getGl().getServiceIncome()));
                }
                if (!Validators.isPartialValid(request.getGlExpense())) {
                    request.setGlExpense(new GLDC(companySettings.getGl().getServiceExpense()));
                }
            } else {
                if (!Validators.isValid(request.getAccountAsset())) {
                    request.setAccountAsset(companySettings.getGl().getProductAsset());
                }
                if (!Validators.isPartialValid(request.getGlIncome())) {
                    request.setGlIncome(new GLDC(companySettings.getGl().getProductIncome()));
                }
                if (!Validators.isPartialValid(request.getGlExpense())) {
                    request.setGlExpense(new GLDC(companySettings.getGl().getProductExpense()));
                }
            }
        }

        return partSqlMapper.toDto(dbServiceSQL.executeAndReturnInTransaction(em -> {
            PartSql entity = id == null ? new PartSql() :
                    dbServiceSQL.getAndCheck(PartSql.class, request.getId());
            entity.setCompanyId(companyId);
            entity.setName(request.getName());
            entity.setDescription(request.getDescription());
            entity.setSku(request.getSku());
            //entity.setGroup(request.getGroup());
            entity.setManufacturer(Validators.isValid(request.getManufacturer())
                    ? em.getReference(ManufacturerSql.class, request.getManufacturer().getId())
                    : null);
            entity.setUnit(request.getUnit());
            entity.setPrice(request.getPrice());

            if (isAccountant) {
                entity.setAccountAsset(request.getAccountAsset());
                entity.setGlExpense(request.getGlExpense());
                entity.setGlIncome(request.getGlIncome());

                entity.setTaxable(request.isTaxable());
                entity.setVatRateCode(request.getVat() != null ? request.getVat().getCode() : null);
            }

            entity.setType(request.getType());
            entity.setCf(request.getCf());
            entity.setArchive(request.getArchive());
            entity.setForwardSell(request.isForwardSell());

            entity.getParts().clear();

            for (PartPartDto partPart : request.getParts()) {
                PartPartSql partPartSql;
                if (partPart.getRecordId() != null) {
                    partPartSql = em.getReference(PartPartSql.class, partPart.getRecordId());
                } else {
                    partPartSql = new PartPartSql();
                    partPartSql.setParent(entity);
                    partPartSql.setPart(em.getReference(PartSql.class, partPart.getId()));
                }
                partPartSql.setCompanyId(companyId);
                partPartSql.setQuantity(partPart.getQuantity());

                entity.getParts().add(partPartSql);
            }

            InventoryUtils.assignSortOrder(entity.getParts());

            entity.setLabels(request.getLabels());

            // Packing info
            entity.setBrutto(request.getBrutto());
            entity.setNetto(request.getNetto());
            entity.setUnitsWeight(request.getUnitsWeight());
            entity.setPackUnits(request.getPackUnits());
            entity.setLength(request.getLength());
            entity.setWidth(request.getWidth());
            entity.setHeight(request.getHeight());
            entity.setUnitsLength(request.getUnitsLength());
            entity.setPackaging(request.getPackaging());

            entity.setVendor(Validators.isValid(request.getVendor())
                    ? em.getReference(CounterpartySql.class, request.getVendor().getId())
                    : null);
            entity.setVendorCode(request.getVendorCode());
            entity.setTranslation(request.getTranslation());

            if (StringHelper.isEmpty(request.getBarcode()) && BooleanUtils.isTrue(request.getAutoBarcode())) {
                CounterDesc desc = companySettings.getCounter() != null ?
                        companySettings.getCounter().get(CompanySettings.CounterBarcode) : null;
                if (desc != null) {
                    entity.setBarcode(counterService.next(desc).getNumber());
                }

            } else {
                entity.setBarcode(request.getBarcode());
            }

            if (isSettingsAdmin && !StringHelper.isEquals(request.getExportId(), entity.getExportId())) {
                // if new value not empty check if new value not used
                if (StringHelper.hasValue(request.getExportId())) {
                    ImportSql imp = dbServiceSQL.getById(ImportSql.class, new ImportId(companyId, PartSql.class, request.getExportId()));
                    if (imp != null)
                        throw new GamaException("Ex.id in use already");
                }

                // if old value not empty delete it
                if (StringHelper.hasValue(entity.getExportId())) {
                    ImportSql imp = dbServiceSQL.getById(ImportSql.class, new ImportId(companyId, CounterpartySql.class, entity.getExportId()));
                    if (imp != null) entityManager.remove(imp);
                }

                // create new record with new value
                ImportSql imp = new ImportSql(companyId, CounterpartySql.class, request.getExportId(), id, DBType.POSTGRESQL);
                dbServiceSQL.saveEntity(imp);

                entity.setExportId(request.getExportId());
            }
            return dbServiceSQL.saveEntityInCompany(entity);
        }));
    }

    public PartDto getPartRemainder(InventoryBalanceRequest request) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        String country = Validators.checkNotNull(companySettings.getCountry(), "No country in settings");

        PartDto part = partSqlMapper.toDto(dbServiceSQL.getAndCheck(PartSql.class, request.getPartId()));
        updatePartVAT(part, country);

        List<InventoryNowSql> rems = entityManager
                .createQuery(
                        "SELECT i FROM " + InventoryNowSql.class.getName() + " i" +
                                " LEFT JOIN FETCH i." + InventoryNowSql_.WAREHOUSE + " w" +
                                " WHERE i." + InventoryNowSql_.COMPANY_ID + " = :companyId" +
                                " AND i." + InventoryNowSql_.PART + "." + PartSql_.ID + " = :partId" +
                                " AND i." + InventoryNowSql_.WAREHOUSE + ".id = :warehouseId" +
                                " ORDER BY i." + InventoryNowSql_.DOC + ".date" +
                                ", i." + InventoryNowSql_.DOC + ".ordinal" +
                                ", i." + InventoryNowSql_.DOC + ".series" +
                                ", i." + InventoryNowSql_.DOC + ".number" +
                                ", i." + InventoryNowSql_.DOC + ".id",
                        InventoryNowSql.class)
                .setParameter("companyId", auth.getCompanyId())
                .setParameter("partId", request.getPartId())
                .setParameter("warehouseId", request.getWarehouseId())
                .getResultList();

        part.setRemaindersNow(rems.stream().map(inventoryNowSqlMapper::toDto).collect(Collectors.toList()));

        return part;
    }

    private void saveOrDeleteInventoryNow(IInventoryNow inventoryNow) {
        if (inventoryNow instanceof InventoryNowSql) {
            if (BigDecimalUtils.isZero(inventoryNow.getQuantity()) && GamaMoneyUtils.isZero(inventoryNow.getCostTotal())) {
                entityManager.remove(inventoryNow);
            } else {
                dbServiceSQL.saveEntityInCompany((InventoryNowSql) inventoryNow);
            }
        } else {
            throw new GamaException("Wrong entity " + inventoryNow.getClass().getSimpleName());
        }
    }

    public PurchaseSql finishPurchaseSQL(PurchaseSql document) {
        if (document.isFinishedParts()) return document;

        Validators.checkNotNull(document.getExchange(), "No currency exchange info in {0}", document);

        if (CollectionsHelper.hasValue(document.getParts())) {
            Validators.checkValid(document.getWarehouse(), "warehouse", document.toString(), auth.getLanguage());

            for (PurchasePartSql part : document.getParts()) {
                if (BooleanUtils.isTrue(part.getFinished())) continue;

                part.setCostInfo(null);

                WarehouseTagged warehouse = new WarehouseTagged(document.getWarehouse(), document.getTag());

                if (BigDecimalUtils.isZero(part.getQuantity())) {
                    part.setFinished(true);

                } else if (BigDecimalUtils.isPositive(part.getQuantity())) {
                    addInventorySQL(InventoryType.PURCHASE, document, warehouse, part);

                } else {
                    returnPurchaseInventorySQL(document, warehouse, part);
                }
            }
        }
        document.setFinished(true);
        document.setFinishedParts(true);

        // if everything is ok - look for 'forward-sell' parts to finish
        document = finishPurchaseForwardSellSQL(document);

        return document;
    }

    public PurchaseSql recallPurchaseSQL(PurchaseSql document) {
        if (findFinished(document.getParts()) == null) return document;

        if (CollectionsHelper.hasValue(document.getParts())) {
            Validators.checkValid(document.getWarehouse(), "warehouse", document.toString(), auth.getLanguage());

            document.getParts().forEach(part -> {
                if (BooleanUtils.isNotTrue(part.getFinished())) return;

                WarehouseTagged warehouse = getValidWarehouseSQL(part, document,
                        new WarehouseTagged(document.getWarehouse(), document.getTag()));

                if (BigDecimalUtils.isZero(part.getQuantity())) {
                    part.setFinished(false);

                } else if (BigDecimalUtils.isPositive(part.getQuantity())) {
                    addInventoryRecallSQL(InventoryType.PURCHASE, document, warehouse, part);

                } else {
                    returnPurchaseRecallSQL(document, warehouse, part);
                }
            });
        }
        document.setFinishedParts(null);

        return document;
    }

    static class PartCostSourceWithPartSQL<E extends IBaseDocPartCost & IDocPart> {
        PartCostSource costSource;
        E part;

        PartCostSourceWithPartSQL(PartCostSource costSource, E part) {
            this.costSource = costSource;
            this.part = part;
        }
    }

    private <E extends IBaseDocPartCost & IDocPart> PartCostSourceWithPartSQL<E> findUnfinishedForwardSellSQL(List<E> parts) {
        if (parts != null) {
            for (E part : parts) {
                if (!part.isForwardSell()) continue;
                if (CollectionsHelper.isEmpty(part.getCostInfo())) continue;
                for (final PartCostSource costSource : part.getCostInfo()) {
                    if (costSource.isForwardSell() && !costSource.isFsUpdated()) {
                        return new PartCostSourceWithPartSQL<>(costSource, part);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Set Invoice forward-sell data
     * @param entity Purchase or Transportation/Production document
     * @param parts Purchase or Transportation/Production parts
     * @param companyId company id
     * @param <E> parts class
     * @return true if all parts processed, false - if not
     */
    private <E extends IBaseDocPartCost & IBaseDocPartSql & IDocPart & IPartSN & IUuid>
    boolean finishForwardSellSQL(InventoryType inventoryType, BaseDocumentSql entity, List<E> parts, long companyId) {
        if (!entity.hasFs()) return true;

        PartCostSourceWithPartSQL<E> partCostSource = findUnfinishedForwardSellSQL(parts);
        if (partCostSource == null) {
            return true;
        }

        PartCostSource costSource = partCostSource.costSource;
        E part = partCostSource.part;

        Doc doc = Validators.checkNotNull(costSource.getDoc(), "No forward-sell document");
        BaseDocumentSql baseDocument = Validators.checkNotNull(dbServiceSQL.getById(BaseDocumentSql.class, doc.getId()),
                "No forward-sell document %s", doc.getId());
        Validators.checkArgument(baseDocument.getCompanyId() == companyId, "Invalid forward-sell document %s", doc.getId());

        boolean updated = false;
        if (!(baseDocument instanceof InvoiceSql invoice)) {
            throw new GamaException("Invalid forward-sell document type " + baseDocument.getDocumentType());
        }

        Validators.checkArgument(CollectionsHelper.hasValue(invoice.getParts()), "No parts in invoice %", doc.getId());

        BigDecimal quantity = BigDecimalUtils.negated(costSource.getQuantity());
        GamaBigMoney unitPrice = BigDecimalUtils.isZero(quantity) || GamaMoneyUtils.isZero(costSource.getCostTotal()) ? null :
                costSource.getCostTotal().toBigMoney().withScale(8).dividedBy(costSource.getQuantity());
        GamaMoney costTotal = GamaMoneyUtils.negated(costSource.getCostTotal());

        while (!costSource.isFsUpdated()) {
            InvoiceBasePartSql partInvoice = CollectionsHelper.streamOf(invoice.getParts())
                    .filter(p -> Objects.equals(p.getPartId(), part.getPartId()) && p.isForwardSell() && BigDecimalUtils.isPositive(p.getRemainder()))
                    .findFirst().orElse(null);
            if (partInvoice == null) {
                throw new GamaException("No partInvoice");
            }
            if (!BigDecimalUtils.isPositive(partInvoice.getRemainder())) {
                throw new GamaException("PartInvoice remainder is negative");
            }

            if (BigDecimalUtils.isLessThanOrEqual(quantity, partInvoice.getRemainder())) {
                partInvoice.setRemainder(BigDecimalUtils.subtract(partInvoice.getRemainder(), quantity));
                costSource.setFsUpdated(true);

                updatePartCostSource(partInvoice, Doc.of(entity), entity.getCounterparty(), quantity, costTotal,
                        true, inventoryType);
                partInvoice.setCostTotal(GamaMoneyUtils.add(partInvoice.getCostTotal(), costTotal));

            } else {
                GamaMoney cost = unitPrice == null ? null : GamaMoneyUtils.toMoney(unitPrice.multipliedBy(partInvoice.getRemainder()));
                if (GamaMoneyUtils.isGreaterThan(cost, costTotal)) cost = costTotal;


                updatePartCostSource(partInvoice, Doc.of(entity), entity.getCounterparty(), partInvoice.getRemainder(), cost,
                        true, inventoryType);
                partInvoice.setCostTotal(GamaMoneyUtils.add(partInvoice.getCostTotal(), cost));

                costTotal = GamaMoneyUtils.subtract(costTotal, cost);
                quantity = BigDecimalUtils.subtract(quantity, partInvoice.getRemainder());

                partInvoice.setRemainder(null);
            }
            updated = true;
        }

        // write to history
        long warehouseId = Validators.isValid(part.getWarehouse()) ? part.getWarehouse().getId() : ((InvoiceSql) baseDocument).getWarehouse().getId();

        CounterpartySql docCounterpartyRef = Validators.isValid(baseDocument.getCounterparty())
                ? entityManager.getReference(CounterpartySql.class, baseDocument.getCounterparty().getId()) : null;

        InventoryHistorySql history = new InventoryHistorySql(companyId,
                entityManager.getReference(PartSql.class, part.getPartId()),
                entityManager.getReference(WarehouseSql.class, warehouseId),
                part.getSn(), part.getUuid(), inventoryType,
                Doc.of(entity), docCounterpartyRef,
                Doc.of(baseDocument), docCounterpartyRef,
                null, costSource.getCostTotal(),
                null, null);
        dbServiceSQL.saveEntityInCompany(history);

        if (updated) dbServiceSQL.saveEntityInCompany(entity);
        return false;
    }

    private PurchaseSql finishPurchaseForwardSellSQL(PurchaseSql document) {
        if (document == null) return null;

        final long companyId = auth.getCompanyId();

        while (document.hasFs()) {
            if (finishForwardSellSQL(InventoryType.PURCHASE, document, document.getParts(), companyId)) {
                document.setFs(null);
            }
        }
        return document;
    }

    private TransProdSql finishTransProdForwardSellSQL(TransProdSql document) {
        if (document == null) return null;

        final long companyId = auth.getCompanyId();

        final boolean transportation = CollectionsHelper.isEmpty(document.getPartsTo());
        final InventoryType inventoryType = transportation ? InventoryType.TRANSPORT : InventoryType.PRODUCTION;

        while (document.hasFs()) {
            if (finishForwardSellSQL(inventoryType, document, document.getPartsFrom(), companyId)) {
                if (finishForwardSellSQL(inventoryType, document, document.getPartsTo(), companyId)) {
                    document.setFs(null);
                }
            }
        }
        return document;
    }

    public InvoiceSql finishInvoiceSQL(InvoiceSql document) {
        if (document.isFinishedParts()) return document;

        Validators.checkNotNull(document.getExchange(), "No currency exchange info in {0}", document);

        if (CollectionsHelper.hasValue(document.getParts())) {
            Validators.checkValid(document.getWarehouse(), "warehouse", document.toString(), auth.getLanguage());

            Map<UUID, Doc> docsReturn = new HashMap<>();
            document.getParts().forEach(p -> {
                if (p instanceof InvoicePartSql e) docsReturn.put(e.getLinkUuid(), e.getDocReturn());
            });

            document.getParts().forEach(part -> {
                if (BooleanUtils.isTrue(part.getFinished())) return;

                Doc docReturn = part instanceof InvoicePartSql
                        ? docsReturn.get(((InvoicePartSql) part).getLinkUuid())
                        : docsReturn.get(((InvoiceSubpartSql) part).getParentLinkUuid());

                if (BigDecimalUtils.isZero(part.getQuantity())) {
                    part.setFinished(true);
                } else {
                    WarehouseTagged warehouse = new WarehouseTagged(document.getWarehouse(), document.getTag());

                    if (BigDecimalUtils.isNegative(part.getQuantity())) {
                        returnInvoiceSQL(document, warehouse, docReturn, part);
                    } else {
                        removeInventorySQL(InventoryType.INVOICE, document, warehouse, part);
                    }
                }
            });
        }
        document.setFinished(true);
        document.setFinishedParts(true);

        return document;
    }

    public InvoiceSql recallInvoiceSQL(InvoiceSql document) {
        if (findFinished(document.getParts()) == null) return document;

        if (CollectionsHelper.hasValue(document.getParts())) {
            Validators.checkValid(document.getWarehouse(), "warehouse", document.toString(), auth.getLanguage());

            Map<UUID, Doc> docsReturn = new HashMap<>();
            document.getParts().forEach(p -> {
                if (p instanceof InvoicePartSql e && Validators.isValid(e.getDocReturn())) docsReturn.put(e.getLinkUuid(), e.getDocReturn());
            });

            document.getParts().forEach(part -> {
                if (BooleanUtils.isNotTrue(part.getFinished())) return;

                Doc docReturn = part instanceof InvoicePartSql
                        ? docsReturn.get(((InvoicePartSql) part).getLinkUuid())
                        : docsReturn.get(((InvoiceSubpartSql) part).getParentLinkUuid());

                if (BigDecimalUtils.isZero(part.getQuantity())) {
                    part.setFinished(null);
                    part.setRemainder(null);
                    part.setCostTotal(null);

                } else {
                    WarehouseTagged warehouse = new WarehouseTagged(document.getWarehouse(), document.getTag());

                    if (BigDecimalUtils.isNegative(part.getQuantity())) {
                        returnInvoiceRecallSQL(document, warehouse, docReturn, part);
                    } else {
                        removeInventoryRecallSQL(document, warehouse, part);
                    }
                }
            });
        }
        document.setFinishedParts(null);

        return document;
    }

    public void emailInvoice(long documentId, List<MailRequestContact> recipients, String language, String country) {
        InvoiceDto invoice = invoiceSqlMapper.toDto(dbServiceSQL.getAndCheck(InvoiceSql.class, documentId, InvoiceSql.GRAPH_ALL));
        CompanySql company = Validators.checkNotNull(dbServiceSQL.getById(CompanySql.class, auth.getCompanyId()), "No company");
        if (StringHelper.isEmpty(language)) language = company.getSettings().getLanguage();
        if (StringHelper.isEmpty(country)) country = company.getSettings().getCountry();
        if ("lt".equals(language)) {
            country = "LT";
        } else if ("en".equals(language) &&
                !"US".equals(country) && !"GB".equals(country) && !"CA".equals(country) && !"AU".equals(country)) {
            country = "GB";
        }
        Locale locale = Locale.of(language, country);
        String subject = String.format("%s %s, %s", company.getSettings().docNamesByLanguage(language).get(EntityUtils.normalizeEntityName(InvoiceSql.class.getSimpleName())),
                invoice.getNumber(), invoice.getDate().toString());
        String body = MessageFormat.format(TranslationService.getInstance().translate(TranslationService.INVENTORY.InvoiceEmailTemplate, language),
                invoice.getNumber(), invoice.getDate(),
                GamaMoneyFormatter.getInstance(locale, company.getSettings().getDecimalPrice()).format(invoice.getTotal()),
                company.getBusinessName());

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        templateService.generateDocument(invoice, buffer, null, false, language, country);

        String sender = company.getEmail() != null && !company.getEmail().trim().isEmpty() ? company.getEmail() : Constants.DEFAULT_SENDER_EMAIL;
        mailService.sendMails(sender, company.getBusinessName(), recipients, subject, null, body,
                buffer.toByteArray(),
                String.format("%s %s %s.pdf", company.getSettings().docNamesByLanguage(language).get(EntityUtils.normalizeEntityName(InvoiceSql.class.getSimpleName())),
                        invoice.getNumber(), invoice.getDate().toString()),
                company.getCcEmail());
    }

    public void emailInvoiceTaxFree(long id, DBType db) {
        InvoiceDto invoice = invoiceSqlMapper.toDto(dbServiceSQL.getAndCheck(InvoiceSql.class, id, InvoiceSql.GRAPH_ALL));
        CompanySql company = Validators.checkNotNull(dbServiceSQL.getById(CompanySql.class, auth.getCompanyId()), "No company");
        final CompanySettings companySettings = Validators.checkNotNull(company.getSettings(), "No company settings");
        Validators.checkArgument("LT".equals(companySettings.getCountry()) && BooleanUtils.isTrue(companySettings.getEnableTaxFree()), "Tax Free not enabled");
        String subject = "Tax Free, id=" + invoice.getTaxFree().getDocHeader().getDocId() + ", " + invoice.getDate();
        String body = MessageFormat.format(
                "Sveiki,<br><br>Siunčiame Jums TaxFree deklaraciją, id: {0}, data {1}. " +
                        "<br><br>pagarbiai<br>{2}" +
                        "<br><br><hr>" +
                        "Hello<br><br>We are sending you TaxFree declaration, id: {0}, date {1}." +
                        "<br><br>respectfully<br>{2}",
                invoice.getTaxFree().getDocHeader().getDocId(),
                invoice.getDate(),
                company.getBusinessName());

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        final String filePath = storageService.getFilePath(invoice.getCompanyId(), DOCS_PRINT_FOLDER, templateService.filename(invoice.getUuid(), "tf", false, null));
        boolean exists = storageService.gcsFileExists(filePath);
        if (exists) {
            try (ReadableByteChannel channel = storageService.gcsFileReadChannel(filePath)) {
                ByteBuffer buff = ByteBuffer.allocate(GCS_BUFFER_SIZE / 2);
                while (channel.read(buff) > 0) {
                    os.write(buff.array(), 0, buff.position());
                    buff.clear();
                }
            } catch (IOException e) {
                throw new GamaException(e.getMessage(), e);
            }
        } else {
            templateService.generateDocument(invoice, os, "tf", false, null, null);
        }
        String sender = company.getEmail() != null && !company.getEmail().trim().isEmpty() ? company.getEmail() : Constants.DEFAULT_SENDER_EMAIL;
        mailService.sendMail(sender, company.getBusinessName(),
                invoice.getTaxFree().getCustomer().getEmail(),
                invoice.getTaxFree().getCustomer().getFirstName() + " " + invoice.getTaxFree().getCustomer().getLastName(),
                subject, null, body,
                os.toByteArray(),
                String.format("Tax Free %s, %s",
                        invoice.getTaxFree().getDocHeader().getDocId(), invoice.getDate().toString()),
                company.getCcEmail());
    }

    public InventoryOpeningBalanceSql finishOpeningBalanceSQL(InventoryOpeningBalanceSql document) {
        if (document.isFinishedParts()) return document;

        if (CollectionsHelper.hasValue(document.getParts())) {
            Validators.checkValid(document.getWarehouse(), "warehouse", document.toString(), auth.getLanguage());

            document.getParts().forEach(part -> {
                if (BooleanUtils.isTrue(part.getFinished())) return;

                if (part.getType() == PartType.SERVICE || BigDecimalUtils.isZero(part.getQuantity())) {
                    part.setFinished(true);
                } else {
                    if (BigDecimalUtils.isNegative(part.getQuantity())) {
                        throw new GamaException("Negative quantity");
                    }
                    addInventorySQL(InventoryType.OPENING_BALANCE, document, new WarehouseTagged(document.getWarehouse(), document.getTag()), part);
                }
            });
        }
        document.setFinished(true);
        document.setFinishedParts(true);

        return document;
    }

    public TransProdSql finishTransProdSQL(TransProdSql document) {
        if (BooleanUtils.isTrue(document.getFinishedPartsFrom()) && BooleanUtils.isTrue(document.getFinishedPartsTo())) return document;

        Validators.checkValid(document.getWarehouseFrom(), "warehouse from/resources", document.toString(), auth.getLanguage());
        Validators.checkValid(document.getWarehouseTo(), "warehouse to/production", document.toString(), auth.getLanguage());

        final boolean transportationOnly = (CollectionsHelper.isEmpty(document.getPartsTo()));

        if (CollectionsHelper.hasValue(document.getPartsFrom())) {
            for (TransProdPartFromSql part : document.getPartsFrom()) {
                if (BooleanUtils.isTrue(part.getFinished())) continue;

                if (BigDecimalUtils.isZero(part.getQuantity())) {
                    part.setFinished(true);
                } else {
                    if (BigDecimalUtils.isNegative(part.getQuantity())) {
                        throw new GamaException("Negative quantities not supported yet");
                    }
                    if (transportationOnly) {
                        removeInventorySQL(InventoryType.TRANSPORT, document, new WarehouseTagged(document.getWarehouseFrom(), document.getTagFrom()), part);
                        addInventorySQL(InventoryType.TRANSPORT, document, new WarehouseTagged(document.getWarehouseTo(), document.getTagTo()), part);
                    } else {
                        removeInventorySQL(InventoryType.PRODUCTION, document, new WarehouseTagged(document.getWarehouseFrom(), document.getTagFrom()), part);
                    }
                }
            }
        }
        document.setFinished(true);
        document.setFinishedPartsFrom(true);
        if (transportationOnly) document.setFinishedPartsTo(true);

        if (BooleanUtils.isNotTrue(document.getFinishedPartsTo())) {
            Validators.checkArgument(CollectionsHelper.hasValue(document.getPartsTo()),
                    TranslationService.getInstance().translate(TranslationService.INVENTORY.NoProductionParts));

            BigDecimal percent = null;
            for (TransProdPartToSql part : document.getPartsTo()) {
                percent = BigDecimalUtils.add(percent, part.getCostPercent());
            }
            Validators.checkArgument(BigDecimalUtils.isEqual(percent, BigDecimal.valueOf(100)),
                    TranslationService.getInstance().translate(TranslationService.INVENTORY.NoCostPercent100));

            GamaMoney calCostTotal = null;
            for (TransProdPartFromSql part : document.getPartsFrom()) {
                calCostTotal = GamaMoneyUtils.add(calCostTotal, part.getCostTotal());
            }
            final GamaMoney costTotal = calCostTotal;

            for (TransProdPartToSql part : document.getPartsTo()) {
                if (BooleanUtils.isTrue(part.getFinished())) continue;

                if (BigDecimalUtils.isZero(part.getQuantity())) {
                    part.setFinished(true);
                } else {
                    GamaMoney finishedCost = null;
                    int unfinishedCount = 0;
                    for (TransProdPartFromSql finishedPart : document.getPartsFrom()) {
                        if (BooleanUtils.isTrue(finishedPart.getFinished())) {
                            finishedCost = GamaMoneyUtils.add(finishedCost, finishedPart.getCostTotal());
                        } else {
                            unfinishedCount++;
                        }
                    }
                    GamaMoney cost;
                    if (BigDecimalUtils.isZero(part.getCostPercent()) || GamaMoneyUtils.isZero(costTotal))
                        cost = null;
                    else if (unfinishedCount == 1)
                        cost = GamaMoneyUtils.subtract(costTotal, finishedCost);
                    else
                        cost = costTotal.multipliedBy(part.getCostPercent().doubleValue() / 100.0);
                    part.setCostTotal(cost);
                    addInventorySQL(InventoryType.PRODUCTION, document, new WarehouseTagged(document.getWarehouseTo(), document.getTagTo()), part);
                }
            }
            document.setFinished(true);
            document.setFinishedPartsTo(true);
        }

        // if everything is ok - look for 'forward-sell' parts to finish
        document = finishTransProdForwardSellSQL(document);

        return document;
    }

    public TransProdSql recallTransProdSQL(TransProdSql document) {
        if (BooleanUtils.isNotTrue(document.getFinishedPartsFrom()) && BooleanUtils.isNotTrue(document.getFinishedPartsTo())) return document;

        Validators.checkValid(document.getWarehouseFrom(), "warehouse from/resources", document.toString(), auth.getLanguage());
        Validators.checkValid(document.getWarehouseTo(), "warehouse to/production", document.toString(), auth.getLanguage());

        final boolean transportation = (CollectionsHelper.isEmpty(document.getPartsTo()));

        // start recall from 'PartsTo'
        if (!transportation) {
            document.getPartsTo().forEach(part -> {
                if (BooleanUtils.isNotTrue(part.getFinished())) return;

                if (BigDecimalUtils.isZero(part.getQuantity())) {
                    part.setFinished(false);
                } else if (BigDecimalUtils.isNegative(part.getQuantity())) {
                    throw new GamaException("Negative quantities not supported yet");
                } else {
                    addInventoryRecallSQL(InventoryType.PRODUCTION, document, new WarehouseTagged(document.getWarehouseTo(), document.getTagTo()), part);
                }
            });
            document.setFinishedPartsTo(false);
        }

        if (CollectionsHelper.hasValue(document.getPartsFrom())) {

            document.getPartsFrom().forEach(part -> {
                if (BooleanUtils.isNotTrue(part.getFinished())) return;

                if (BigDecimalUtils.isZero(part.getQuantity())) {
                    part.setFinished(false);
                } else {
                    if (BigDecimalUtils.isNegative(part.getQuantity())) {
                        throw new GamaException("Negative quantities not supported yet");
                    }
                    if (transportation) {
                        GamaMoney costTotal = part.getCostTotal();
                        removeInventoryRecallSQL(document, new WarehouseTagged(document.getWarehouseFrom(), document.getTagFrom()), part);
                        part.setCostTotal(costTotal);     // restore costTotal
                        addInventoryRecallSQL(InventoryType.TRANSPORT, document, new WarehouseTagged(document.getWarehouseTo(), document.getTagTo()), part);
                    } else {
                        removeInventoryRecallSQL(document, new WarehouseTagged(document.getWarehouseFrom(), document.getTagFrom()), part);
                    }
                }
            });
        }
        document.setFinishedPartsFrom(false);
        return document;
    }

    /**
     * Make parts reservation, i.e. move them from 'warehouseFrom' to 'warehouseReserved'.
     * If 'warehouseReserved' has not empty tag use it, if not - assign tag from document number if presented,
     * or from random generated UUID.
     * @param document source document
     * @return document
     */
    public TransProdSql reserveTransProdSQL(TransProdSql document) {
        if (BooleanUtils.isTrue(document.getFinished())) return document;
        if (BooleanUtils.isTrue(document.getReservedParts())) {
            if (BooleanUtils.isNotTrue(document.getReserved())) {
                document.setReserved(true);
            }
            return document;
        }

        if (CollectionsHelper.hasValue(document.getPartsFrom())) {
            Validators.checkValid(document.getWarehouseFrom(), "warehouse from/resources", document.toString(), auth.getLanguage());
            Validators.checkValid(document.getWarehouseReserved(), "warehouse for reservations", document.toString(), auth.getLanguage());
            Validators.checkArgument(BooleanUtils.isTrue(document.getWarehouseReserved().getWithTag()),
                    "warehouse for reservations has not marked as with tag - {0}",
                    document.getWarehouseReserved().toString());

            document.getPartsFrom().forEach(part -> {
                if (part.isReserved()) return;

                if (StringHelper.isEmpty(document.getTagReserved())) {
                    document.setTagReserved(StringHelper.hasValue(document.getNumber()) ? document.getNumber() : UUID.randomUUID().toString());
                }

                if (BigDecimalUtils.isZero(part.getQuantity())) {
                    part.setReserved(true);
                } else {
                    if (BigDecimalUtils.isNegative(part.getQuantity())) {
                        throw new GamaException("Negative quantities not supported yet");
                    }
                    reserveRemoveInventorySQL(document, new WarehouseTagged(document.getWarehouseFrom(), document.getTagFrom()), part);
                    reserveAddInventorySQL(document, new WarehouseTagged(document.getWarehouseReserved(), document.getTagReserved()), part);
                }
            });
        }
        document.setReserved(true);
        document.setReservedParts(true);

        return document;
    }

    public TransProdSql recallReserveTransProdSQL(TransProdSql document) {
        if (BooleanUtils.isNotTrue(document.getReserved())) return document;
        if (CollectionsHelper.isEmpty(document.getPartsFrom())) return document;

        Validators.checkValid(document.getWarehouseFrom(), "warehouse from/resources", document.toString(), auth.getLanguage());
        Validators.checkValid(document.getWarehouseReserved(), "warehouse for reservations", document.toString(), auth.getLanguage());
        Validators.checkArgument(BooleanUtils.isTrue(document.getWarehouseReserved().getWithTag()),
                "warehouse for reservations has not marked as with tag - {0}",
                document.getWarehouseReserved().toString());
        Validators.notEmpty(document.getTagReserved(), "there is no tag for reservations in " + document);

        if (CollectionsHelper.hasValue(document.getPartsFrom())) {
            document.getPartsFrom().forEach(part -> {
                if (!part.isReserved()) return;

                if (BigDecimalUtils.isZero(part.getReservedQuantity())) {
                    part.setReserved(false);
                } else {
                    if (BigDecimalUtils.isNegative(part.getReservedQuantity())) {
                        throw new GamaException("Negative quantities not supported yet");
                    }
                    reserveRemoveInventoryRecallSQL(document, new WarehouseTagged(document.getWarehouseFrom(), document.getTagFrom()), part);
                    reserveAddInventoryRecallSQL(document, new WarehouseTagged(document.getWarehouseReserved(), document.getTagReserved()), part);
                }

            });
        }
        document.setReservedParts(null);
        document.setReserved(null);

        return document;
    }

    public TransProdSql genOrdersFromTransProdSQL(TransProdSql document) {
        if (BooleanUtils.isNotTrue(document.getReserved())) return document;
        if (CollectionsHelper.isEmpty(document.getPartsFrom())) return document;

        Validators.checkValid(document.getWarehouseReserved(), "warehouse for reservations", document.toString(), auth.getLanguage());

        CompanySettings companySettings = Validators.checkNotNull(dbServiceSQL.getCompanySettings(document.getCompanyId()),
                MessageFormat.format(TranslationService.getInstance().translate(TranslationService.DB.NoCompanySettings), document.getCompanyId()));

        CountryVatRateSql countryVatRate = dbServiceSQL.getById(CountryVatRateSql.class, companySettings.getCountry());
        VATRatesDate vatRatesDate = countryVatRate.getRatesMap(document.getDate());

        Map<Long, OrderSql> ordersMap = new HashMap<>();
        List<Doc> docs = new ArrayList<>();

        for (TransProdPartFromSql part : document.getPartsFrom()) {
            if (BigDecimalUtils.isZero(part.getQuantity())) continue;

            if (BigDecimalUtils.isNegative(part.getQuantity())) {
                throw new GamaException("Negative quantities not supported yet");
            }

            BigDecimal orderQuantity = BigDecimalUtils.subtract(part.getQuantity(), part.getReservedQuantity());
            if (BigDecimalUtils.isNegativeOrZero(orderQuantity)) continue;

            // search for order document by part vendor id
            makeOrderSQL(docs, ordersMap, document.getCompanyId(), vatRatesDate, part.getPartId(), orderQuantity, document.getDate(),
                    new WarehouseTagged(document.getWarehouseReserved(), document.getTagReserved()), document.getNumber());
        }

        document.setDocs(docs);

        return document;
    }

    private void makeOrderSQL(List<Doc> docs, Map<Long, OrderSql> orderMap, long companyId, VATRatesDate vatRatesDate,
                              long partId, BigDecimal orderQuantity, LocalDate docDate, WarehouseTagged warehouse, String numberPrefix) {

        PartSql p = dbServiceSQL.getAndCheck(PartSql.class, partId, PartSql.GRAPH_VENDOR);

        boolean isNewOrder = false;
        long vendorId = p.getVendor() == null ? 0 : p.getVendor().getId() == null ? 0 : p.getVendor().getId();
        OrderSql order = orderMap.get(vendorId);
        if (order == null) {
            isNewOrder = true;
            order = new OrderSql();
            order.setCompanyId(companyId);
            order.setCounterparty(Validators.isValid(p.getVendor())
                    ? entityManager.getReference(CounterpartySql.class, p.getVendor().getId()) : null);
            order.setWarehouse(entityManager.getReference(WarehouseSql.class, warehouse.getId()));
            order.setTag(warehouse.getTag());
            order.setDate(docDate);
            order.setNumber(numberPrefix + "/" + String.format("%02d", orderMap.size() + 1));
            order.setParts(new ArrayList<>());
            order.setUuid(UUID.randomUUID());
            orderMap.put(vendorId, order);
        }
        OrderPartSql orderPartSql = new OrderPartSql();
        orderPartSql.setParent(order);
        orderPartSql.setPart(entityManager.getReference(PartSql.class, p.getId()));
        orderPartSql.setDocPart(new DocPart());
        orderPartSql.getDocPart().setId(p.getId());
        orderPartSql.setBarcode(p.getBarcode());
        orderPartSql.setSku(p.getSku());
        orderPartSql.setName(p.getName());
        orderPartSql.setUnit(p.getUnit());
        orderPartSql.setType(p.getType());
        orderPartSql.setTaxable(p.isTaxable());
        orderPartSql.setVendorCode(p.getVendorCode());

        orderPartSql.setVatRateCode(p.getVatRateCode());

        if (StringHelper.hasValue(orderPartSql.getVatRateCode())) {
            orderPartSql.setVat(vatRatesDate.getRatesMap().get(orderPartSql.getVatRateCode()));
        }

        orderPartSql.setQuantity(orderQuantity);
        order.getParts().add(orderPartSql);

        order = dbServiceSQL.saveEntityInCompany(order);
        if (isNewOrder) docs.add(Doc.of(order));
    }

    public InventorySql finishInventorySQL(InventorySql document) {
        if (BooleanUtils.isTrue(document.getFinishedParts())) return document;

        if (CollectionsHelper.hasValue(document.getParts())) {
            Validators.checkValid(document.getWarehouse(), "warehouse", document.toString(), auth.getLanguage());

            document.getParts().forEach(part -> {
                if (BooleanUtils.isTrue(part.getFinished())) return;

                changeInventorySQL(document, part);
            });
        }
        document.setFinished(true);
        document.setFinishedParts(true);

        return document;
    }

    public InventorySql recallInventorySQL(InventorySql document) {
        if (findFinished(document.getParts()) == null) return document;

        Validators.checkValid(document.getWarehouse(), "warehouse", document.toString(), auth.getLanguage());

        if (CollectionsHelper.hasValue(document.getParts())) {
            document.getParts().forEach(part -> {
                if (BooleanUtils.isNotTrue(part.getFinished())) return;
                recallChangeInventorySQL(document, part);
            });
        }
        document.setFinishedParts(null);

        return document;
    }

    private <E extends IFinished> E findFinished(List<E> parts) {
        if (parts != null) {
            for (E part : parts) {
                if (BooleanUtils.isTrue(part.getFinished())) return part;
            }
        }
        return null;
    }

    /**
     * update part warehouses and remainders
     */
    private void updatePartRemainder(long partId, PartType partType, DocWarehouse warehouse, BigDecimal quantity, GamaMoney cost) {
        if (partType != PartType.SERVICE) dbServiceSQL.getAndCheck(PartSql.class, partId).updateRemainder(warehouse, quantity, cost);
    }

    /**
     * remember origin doc, counterparty, quantity and cost
     */
    private void updatePartCostSource(IBaseDocPartCost part, Doc doc, ICounterparty counterparty, BigDecimal quantity,
                                      GamaMoney cost, boolean forwardSell, InventoryType inventoryType) {
        if (part.getCostInfo() == null) part.setCostInfo(new ArrayList<>());
        PartCostSource partCostSource = new PartCostSource();
        partCostSource.setDoc(doc);
        partCostSource.setInventoryType(inventoryType);
        partCostSource.setCounterparty(Validators.isValid(counterparty) ? new DocCounterparty(counterparty.getId()) : null);
        partCostSource.setQuantity(quantity);
        partCostSource.setCostTotal(cost);
        if (forwardSell) partCostSource.setForwardSell(true);
        part.getCostInfo().add(partCostSource);
    }

    private WarehouseTagged getValidWarehouseSQL(IBaseDocPartSql part, BaseDocumentSql document, WarehouseTagged docWarehouse) {
        WarehouseTagged warehouse = Validators.isValid(part.getWarehouse())
                ? new WarehouseTagged(part.getWarehouse(), part.getTag())
                : docWarehouse;
        Validators.checkValid(warehouse, "warehouse", document.toString(), auth.getLanguage());
        return warehouse;
    }

    /**
     * Add part into warehouse.
     * If reservation = false - add part.quantity
     * If reservation = true - add part.reserved
     * @param inventoryType inventory type
     * @param document current document
     * @param docWarehouse warehouse
     * @param part part to remove
     */
    private <E extends IBaseDocPartCost & IBaseDocPartSql & IDocPart & IPartSN & IUuid & IFinished & IPartMessage>
    void addInventorySQL(InventoryType inventoryType, BaseDocumentSql document, WarehouseTagged docWarehouse, E part) {
        WarehouseTagged warehouse = getValidWarehouseSQL(part, document, docWarehouse);

        addInventoryQuantitySQL(inventoryType, document, part, warehouse, document.getCompanyId(), part.getQuantity());

        part.setFinished(true);
    }

    /**
     * Add part into warehouse like reserved
     * If reservation = false - add part.quantity
     * If reservation = true - add part.reserved
     * @param document current document
     * @param docWarehouse warehouse
     * @param part part to remove
     */
    private void reserveAddInventorySQL(BaseDocumentSql document, WarehouseTagged docWarehouse, TransProdPartFromSql part) {
        WarehouseTagged warehouse = getValidWarehouseSQL(part, document, docWarehouse);

        addInventoryQuantitySQL(InventoryType.TRANSPORT, document, part, warehouse, document.getCompanyId(), part.getReservedQuantity());

        part.setReserved(true);
    }

    private <E extends IBaseDocPartCost & IDocPart & IPartSN & IUuid & IPartMessage>
    void addInventoryQuantitySQL(InventoryType inventoryType, BaseDocumentSql document, E part,
                                 WarehouseTagged warehouse, long companyId, BigDecimal partQuantity) {

        CounterpartySql docCounterpartyRef = Validators.isValid(document.getCounterparty())
                ? entityManager.getReference(CounterpartySql.class, document.getCounterparty().getId()) : null;
        PartSql partRef = entityManager.getReference(PartSql.class, part.getPartId());
        WarehouseSql warehouseRef = entityManager.getReference(WarehouseSql.class, warehouse.getId());

        if (part.getType() != PartType.SERVICE) {
            // check if where are negative quantities first, i.e. forward-sell
            BigDecimal quantity = partQuantity;
            GamaMoney costTotal = part.getCostTotal();

            GamaBigMoney unitPrice = GamaMoneyUtils.isZero(costTotal) || BigDecimalUtils.isZero(quantity) ? null :
                    costTotal.toBigMoney().withScale(8).dividedBy(quantity);

            List<InventoryNowSql> inventories = dbServiceSQL.getInventoriesNow(warehouse, part, InventoryNowSql.GRAPH_COUNTERPARTY);

            if (CollectionsHelper.hasValue(inventories)) {
                for (InventoryNowSql inventoryNow : inventories) {
                    // if inventory with not the same tag - skip it
                    if (!Objects.equals(warehouse.getTag(), inventoryNow.getTag())) continue;

                    GamaMoney costTotalUsed = costTotal;

                    if (BigDecimalUtils.isNegative(inventoryNow.getQuantity())) {
                        if (BigDecimalUtils.isEqual(quantity, inventoryNow.getQuantity().negate())) {
                            // quantity == inventoryQ
                            // remember origin doc, counterparty, quantity and cost
                            updatePartCostSource(part, inventoryNow.getDoc(), inventoryNow.getCounterparty(),
                                    inventoryNow.getQuantity(), GamaMoneyUtils.negated(costTotalUsed), true, inventoryType);
                            part.setForwardSell(true);
                            document.setFs(true);

                            // mark to delete
                            inventoryNow.setQuantity(null);
                            inventoryNow.setCostTotal(null);

                            quantity = null;

                        } else if (BigDecimalUtils.isLessThan(quantity, inventoryNow.getQuantity().negate())) {
                            // quantity < inventoryQ
                            // remember origin doc, counterparty, quantity and cost
                            updatePartCostSource(part, inventoryNow.getDoc(), inventoryNow.getCounterparty(),
                                    BigDecimalUtils.negated(quantity), GamaMoneyUtils.negated(costTotalUsed), true, inventoryType);
                            part.setForwardSell(true);
                            document.setFs(true);

                            inventoryNow.setQuantity(BigDecimalUtils.add(inventoryNow.getQuantity(), quantity));
                            quantity = null;

                        } else {
                            // quantity > inventoryQ
                            BigDecimal quantityUsed = inventoryNow.getQuantity().negate();
                            if (GamaMoneyUtils.isNonZero(unitPrice) && !BigDecimalUtils.isZero(quantityUsed)) {
                                costTotalUsed = GamaMoneyUtils.toMoney(unitPrice.multipliedBy(quantityUsed));
                                if (GamaMoneyUtils.isGreaterThan(costTotalUsed, costTotal)) costTotalUsed = costTotal;
                            } else {
                                costTotalUsed = null;
                            }
                            quantity = BigDecimalUtils.add(quantity, inventoryNow.getQuantity());
                            costTotal = GamaMoneyUtils.subtract(costTotal, costTotalUsed);

                            // remember origin doc, counterparty, quantity and cost
                            updatePartCostSource(part, inventoryNow.getDoc(), inventoryNow.getCounterparty(),
                                    inventoryNow.getQuantity(), GamaMoneyUtils.negated(costTotalUsed), true, inventoryType);
                            part.setForwardSell(true);
                            document.setFs(true);

                            // mark to delete
                            inventoryNow.setQuantity(null);
                            inventoryNow.setCostTotal(null);
                        }

                        if (GamaMoneyUtils.isNonZero(costTotalUsed)) {
                            // update part warehouses and remainders
                            updatePartRemainder(part.getPartId(), part.getType(), new DocWarehouse(warehouse), null, costTotalUsed.negated());
                        }
                    }

                    saveOrDeleteInventoryNow(inventoryNow);
                    if (BigDecimalUtils.isNegativeOrZero(quantity)) break;
                }
            }
            if (BigDecimalUtils.isNegative(quantity)) {
                throw new GamaNotEnoughQuantityException(
                        MessageFormat.format("{0} {1}: Not enough quantity of {2} in {3} - negative result {4}",
                                document.getNumber(), document.getDate(), part.toMessage(), warehouse.getName(), quantity));
            }
            if (BigDecimalUtils.isPositive(quantity)) {
                inventories = inventories.stream()
                        .filter(i -> Objects.equals(i.getDoc().getId(), document.getId()))
                        .toList();
                if (inventories.size() > 1)
                    throw new GamaException("To many InventoryNow records");

                InventoryNowSql inventoryNow = inventories.size() == 1 ? inventories.get(0) : null;
                if (inventoryNow == null) {
                    inventoryNow = new InventoryNowSql(companyId, partRef, part.getSn(), warehouseRef,
                            Doc.of(document), docCounterpartyRef, warehouse.getTag());
                }
                inventoryNow.setQuantity(BigDecimalUtils.add(inventoryNow.getQuantity(), quantity));
                inventoryNow.setCostTotal(GamaMoneyUtils.add(inventoryNow.getCostTotal(), costTotal));
                saveOrDeleteInventoryNow(inventoryNow);
            }
        }

        // write to history
        InventoryHistorySql history = new InventoryHistorySql(companyId, partRef, warehouseRef,
                part.getSn(), part.getUuid(), inventoryType,
                Doc.of(document), docCounterpartyRef,
                Doc.of(document), docCounterpartyRef,
                partQuantity, part.getCostTotal(),
                null, null);
        dbServiceSQL.saveEntityInCompany(history);

        // update part warehouses and remainders
        updatePartRemainder(part.getPartId(), part.getType(), new DocWarehouse(warehouse), partQuantity, part.getCostTotal());
    }

    private <E extends IBaseDocPartSql & IDocPart & IPartSN & IPartMessage>
    void addInventoryRecallInventoryNowSQL(BaseDocumentSql document, WarehouseTagged warehouse, E part,
                                           BigDecimal quantity, GamaMoney costTotal) {
        InventoryNowSql inventoryNow = dbServiceSQL.getInventoryNow(warehouse, part, document.getId());

        if (inventoryNow == null) {
            throw new GamaNotEnoughQuantityException(
                    MessageFormat.format(TranslationService.getInstance().translate(TranslationService.INVENTORY.NotEnoughQuantity, auth.getLanguage()),
                            document.getNumber(), document.getDate(), part.toMessage(), warehouse.getName(), part.getQuantity()));

        }

        if (BigDecimalUtils.isEqual(inventoryNow.getQuantity(), quantity)) {
            inventoryNow.setQuantity(null);
            quantity = null;
        } else if (BigDecimalUtils.isLessThan(inventoryNow.getQuantity(), quantity)) {
            quantity = BigDecimalUtils.subtract(quantity, inventoryNow.getQuantity());
            inventoryNow.setQuantity(null);
        } else {
            inventoryNow.setQuantity(BigDecimalUtils.subtract(inventoryNow.getQuantity(), quantity));
            quantity = null;
        }

        if (GamaMoneyUtils.isEqual(inventoryNow.getCostTotal(), costTotal)) {
            inventoryNow.setCostTotal(null);
            costTotal = null;
        } else if (GamaMoneyUtils.isLessThan(inventoryNow.getCostTotal(), costTotal)) {
            costTotal = GamaMoneyUtils.subtract(costTotal, inventoryNow.getCostTotal());
            inventoryNow.setCostTotal(null);
        } else {
            inventoryNow.setCostTotal(GamaMoneyUtils.subtract(inventoryNow.getCostTotal(), costTotal));
            costTotal = null;
        }

        if (!BigDecimalUtils.isZero(quantity) || GamaMoneyUtils.isNonZero(costTotal)) {
            throw new GamaNotEnoughQuantityException(
                    MessageFormat.format(TranslationService.getInstance().translate(TranslationService.INVENTORY.NotEnoughQuantity, auth.getLanguage()),
                            document.getNumber(), document.getDate(), part.toMessage(), warehouse.getName(), quantity));
        }

        saveOrDeleteInventoryNow(inventoryNow);
    }

    private <E extends IBaseDocPartCost & IBaseDocPartSql & IDocPart & IPartSN  & IFinished & IPartMessage>
    void addInventoryRecallSQL(InventoryType inventoryType, BaseDocumentSql document, WarehouseTagged docWarehouse, E part) {
        WarehouseTagged warehouse = getValidWarehouseSQL(part, document, docWarehouse);

        addInventoryRecallQuantitySQL(inventoryType, document, part, warehouse, part.getQuantity());

        part.setFinished(false);
    }

    private void reserveAddInventoryRecallSQL(BaseDocumentSql document, WarehouseTagged docWarehouse, TransProdPartFromSql part) {
        WarehouseTagged warehouse = getValidWarehouseSQL(part, document, docWarehouse);

        addInventoryRecallQuantitySQL(InventoryType.TRANSPORT, document, part, warehouse, part.getReservedQuantity());

        part.setReserved(false);
    }

    private <E extends IBaseDocPartCost & IBaseDocPartSql & IDocPart & IPartSN & IPartMessage>
    void addInventoryRecallQuantitySQL(InventoryType inventoryType, BaseDocumentSql document, E part,
                                       WarehouseTagged warehouse, BigDecimal partQuantity) {
        if (part.getType() != PartType.SERVICE) {
            if (part.getCostInfo() != null) {
                if (inventoryType == InventoryType.PURCHASE) {
                    addInventoryRecallInventoryNowSQL(document, warehouse, part, partQuantity, part.getCostTotal());

                } else {
                    for (PartCostSource costSource : part.getCostInfo()) {
                        addInventoryRecallInventoryNowSQL(document, warehouse, part, costSource.getQuantity(), costSource.getCostTotal());
                    }
                }
            } else {
                addInventoryRecallInventoryNowSQL(document, warehouse, part, partQuantity, part.getCostTotal());
            }
        }

        // clear history
        clearHistory(part.getPartId(), warehouse.getId(), document.getId());

        // update part warehouses and remainders
        updatePartRemainder(part.getPartId(), part.getType(), new DocWarehouse(warehouse), BigDecimalUtils.negated(partQuantity), GamaMoneyUtils.negated(part.getCostTotal()));
    }

    /**
     * Remove inventory from warehouse and calculate cost of it. Write cost to Part's costInfo list.
     * If part is not 'forwardSell' and if not enough quantity at the time of document in warehouse the exception will be thrown.
     * TODO If part is 'forwardSell' - try to look into the future (!) or mark as negative quantity in InventoryNow.
     * @param inventoryType inventory type
     * @param document current document
     * @param docWarehouse warehouse
     * @param part part to remove
     */
    private <E extends IBaseDocPartOutRemainder & IBaseDocPartCost & IBaseDocPartSql & IDocPart & IPartSN & IUuid & IFinished & IPartMessage>
    void removeInventorySQL(InventoryType inventoryType, BaseDocumentSql document, WarehouseTagged docWarehouse, E part) {
        WarehouseTagged warehouse = getValidWarehouseSQL(part, document, docWarehouse);

        // reset cost and costInfo
        part.setCostTotal(null);
        part.setCostInfo(null);

        if (part.getType() == PartType.SERVICE) {
            removeInventoryServiceSQL(inventoryType, document, part, warehouse);
        } else {
            List<InventoryNowSql> inventories = dbServiceSQL.getInventoriesNow(warehouse, part, InventoryNowSql.GRAPH_COUNTERPARTY);

            if (CollectionsHelper.isEmpty(inventories) && !part.isForwardSell()) {
                throw new GamaNotEnoughQuantityException(
                        MessageFormat.format(TranslationService.getInstance().translate(TranslationService.INVENTORY.NotEnoughQuantity),
                                document.getNumber(), document.getDate(), part.toMessage(), warehouse.getName(), part.getQuantity()));
            }
            removeInventoryProductSQL(inventoryType, document, part, warehouse, inventories);
        }

        part.setFinished(true);
    }

    /**
     * Remove inventory from warehouse and calculate cost of it. Write cost to Part's costInfo list.
     * If part is not 'forwardSell' and if not enough quantity at the time of document in warehouse:
     *  1) if reservation = false - the exception will be thrown.
     *  2) if reservation = true -  simple return, because the order will be generated or part will be added to existing order
     * @param document current document
     * @param docWarehouse warehouse
     * @param part part to remove
     */
    private void reserveRemoveInventorySQL(BaseDocumentSql document, WarehouseTagged docWarehouse, TransProdPartFromSql part) {
        WarehouseTagged warehouse = getValidWarehouseSQL(part, document, docWarehouse);

        // reset cost and costInfo
        part.setCostTotal(null);
        part.setCostInfo(null);

        if (part.getType() == PartType.SERVICE) {
            removeInventoryServiceSQL(InventoryType.TRANSPORT, document, part, warehouse);

        } else {
            List<InventoryNowSql> inventories = dbServiceSQL.getInventoriesNow(warehouse, part, InventoryNowSql.GRAPH_COUNTERPARTY);
            if (CollectionsHelper.isEmpty(inventories) && !part.isForwardSell()) return;
            removeInventoryProductReservationSQL(document, part, warehouse, inventories);
        }

        part.setReserved(true);
    }

    static class RemoveInventoryProductTotals {
        BigDecimal quantity;
        GamaMoney costTotal;
        GamaMoney total;
        GamaMoney baseTotal;

        public RemoveInventoryProductTotals(BigDecimal quantity, GamaMoney costTotal, GamaMoney total, GamaMoney baseTotal) {
            this.quantity = quantity;
            this.costTotal = costTotal;
            this.total = total;
            this.baseTotal = baseTotal;
        }
    }

    private <E extends IBaseDocPartOutRemainder & IBaseDocPartCost & IBaseDocPartSql & IDocPart & IPartSN & IUuid & IPartMessage>
    void removeInventoryProductSQL(InventoryType inventoryType, BaseDocumentSql document, E part,
                                   WarehouseTagged warehouse, List<InventoryNowSql> inventories) {
        RemoveInventoryProductTotals totals = calcRemoveInventoryProductTotalsSQL(inventoryType, document, part, warehouse, inventories);

        if (BigDecimalUtils.isPositive(totals.quantity)) {
            if (!part.isForwardSell()) {
                throw new GamaNotEnoughQuantityException(
                        MessageFormat.format(TranslationService.getInstance().translate(TranslationService.INVENTORY.NotEnoughQuantity, auth.getLanguage()),
                                document.getNumber(), document.getDate(), part.toMessage(), warehouse.getName(), totals.quantity));
            }
            part.setRemainder(BigDecimalUtils.add(part.getRemainder(), totals.quantity));

            // write InventoryNow with negative quantity and no cost
            inventories = inventories.stream()
                    .filter(i -> Objects.equals(i.getDoc().getId(), document.getId()))
                    .collect(Collectors.toList());
            if (inventories.size() > 1)
                throw new GamaException("To many InventoryNow records");

            CounterpartySql docCounterpartyRef = Validators.isValid(document.getCounterparty())
                    ? entityManager.getReference(CounterpartySql.class, document.getCounterparty().getId()) : null;
            PartSql partRef = entityManager.getReference(PartSql.class, part.getPartId());
            WarehouseSql warehouseRef = entityManager.getReference(WarehouseSql.class, warehouse.getId());

            InventoryNowSql inventoryNow = inventories.size() == 1 ? inventories.get(0) : null;
            if (inventoryNow == null) {
                inventoryNow = new InventoryNowSql(auth.getCompanyId(), partRef, part.getSn(), warehouseRef,
                        Doc.of(document), docCounterpartyRef, warehouse.getTag());
            }
            inventoryNow.setQuantity(BigDecimalUtils.subtract(inventoryNow.getQuantity(), totals.quantity));
            saveOrDeleteInventoryNow(inventoryNow);

            // write to history
            InventoryHistorySql history = new InventoryHistorySql(auth.getCompanyId(), partRef, warehouseRef,
                    part.getSn(), part.getUuid(), inventoryType,
                    Doc.of(document), docCounterpartyRef,
                    Doc.of(document), docCounterpartyRef,
                    totals.quantity.negate(), null,
                    totals.total, totals.baseTotal);
            dbServiceSQL.saveEntityInCompany(history);
        }

        // update part warehouses and remainders
        updatePartRemainder(part.getPartId(), part.getType(), new DocWarehouse(warehouse), BigDecimalUtils.negated(part.getQuantity()), totals.costTotal);
    }

    private void removeInventoryProductReservationSQL(BaseDocumentSql document, TransProdPartFromSql part,
                                                      WarehouseTagged warehouse, List<InventoryNowSql> inventories) {
        RemoveInventoryProductTotals totals = calcRemoveInventoryProductTotalsSQL(InventoryType.TRANSPORT, document, part, warehouse, inventories);

        part.setReservedQuantity(part.getQuantity());
        if (BigDecimalUtils.isPositive(totals.quantity)) {
            part.setReservedQuantity(BigDecimalUtils.subtract(part.getQuantity(), totals.quantity));
        }

        // update part warehouses and remainders
        updatePartRemainder(part.getPartId(), part.getType(), new DocWarehouse(warehouse), BigDecimalUtils.negated(part.getReservedQuantity()), totals.costTotal);
    }

    private <E extends IBaseDocPartCost & IBaseDocPartSql & IDocPart & IPartSN & IUuid & IPartMessage>
    RemoveInventoryProductTotals calcRemoveInventoryProductTotalsSQL(InventoryType inventoryType, BaseDocumentSql document,
                                                                     E part, WarehouseTagged warehouse,
                                                                     List<InventoryNowSql> inventories) {
        RemoveInventoryProductTotals totals = new RemoveInventoryProductTotals(part.getQuantity(), null, part.getTotal(), part.getBaseTotal());
        if (CollectionsHelper.hasValue(inventories)) {
            for (InventoryNowSql inventoryNow : inventories) {
                // if inventory with not the same tag - skip it
                if (!Objects.equals(warehouse.getTag(), inventoryNow.getTag())) continue;

                if (BigDecimalUtils.isZero(inventoryNow.getQuantity())) {
                    entityManager.remove(inventoryNow);
                    continue;
                }
                if (inventoryNow.getDoc().getDate().isAfter(document.getDate())) continue;

                PartSql partRef = entityManager.getReference(PartSql.class, part.getPartId());
                WarehouseSql warehouseRef = entityManager.getReference(WarehouseSql.class, warehouse.getId());
                CounterpartySql invCounterpartyRef = Validators.isValid(inventoryNow.getCounterparty())
                        ? entityManager.getReference(CounterpartySql.class, inventoryNow.getCounterparty().getId()) : null;
                CounterpartySql docCounterpartyRef = Validators.isValid(document.getCounterparty())
                        ? entityManager.getReference(CounterpartySql.class, document.getCounterparty().getId()) : null;

                if (BigDecimalUtils.isPositive(inventoryNow.getQuantity())) {
                    // do not proceed if negative quantity
                    if (BigDecimalUtils.isLessThan(totals.quantity, inventoryNow.getQuantity())) {
                        GamaMoney cost = null;
                        if (GamaMoneyUtils.isNonZero(inventoryNow.getCostTotal())) {
                            GamaBigMoney unitPrice = inventoryNow.getCostTotal().toBigMoney().withScale(8)
                                    .dividedBy(inventoryNow.getQuantity());
                            cost = GamaMoneyUtils.toMoney(unitPrice.multipliedBy(totals.quantity));
                            if (GamaMoneyUtils.isGreaterThan(cost, inventoryNow.getCostTotal()))
                                cost = inventoryNow.getCostTotal();

                            part.setCostTotal(GamaMoneyUtils.add(part.getCostTotal(), cost));
                            inventoryNow.setCostTotal(GamaMoneyUtils.subtract(inventoryNow.getCostTotal(), cost));
                        }
                        inventoryNow.setQuantity(BigDecimalUtils.subtract(inventoryNow.getQuantity(), totals.quantity));

                        // remember origin doc, counterparty, quantity and cost
                        updatePartCostSource(part, inventoryNow.getDoc(), inventoryNow.getCounterparty(),
                                totals.quantity, cost, false, inventoryType);

                        // write to history
                        InventoryHistorySql history = new InventoryHistorySql(auth.getCompanyId(), partRef, warehouseRef,
                                part.getSn(), part.getUuid(), inventoryType,
                                inventoryNow.getDoc(), invCounterpartyRef,
                                Doc.of(document), docCounterpartyRef,
                                BigDecimalUtils.negated(totals.quantity), GamaMoneyUtils.negated(cost),
                                totals.total, totals.baseTotal);
                        dbServiceSQL.saveEntityInCompany(history);

                        totals.costTotal = GamaMoneyUtils.add(totals.costTotal, GamaMoneyUtils.negated(cost));
                        totals.quantity = null;

                    } else {

                        // prepare part info for PartHistory
                        double k = inventoryNow.getQuantity().doubleValue() / totals.quantity.doubleValue();

                        GamaMoney historyTotal = GamaMoneyUtils.multipliedBy(totals.total, k);
                        GamaMoney historyBaseTotal = GamaMoneyUtils.multipliedBy(totals.baseTotal, k);

                        totals.total = GamaMoneyUtils.subtract(totals.total, historyTotal);
                        totals.baseTotal = GamaMoneyUtils.subtract(totals.baseTotal, historyBaseTotal);

                        // update cost info
                        part.setCostTotal(GamaMoneyUtils.add(part.getCostTotal(), inventoryNow.getCostTotal()));
                        totals.quantity = BigDecimalUtils.subtract(totals.quantity, inventoryNow.getQuantity());

                        // remember cost and quantity info in the part
                        updatePartCostSource(part, inventoryNow.getDoc(), inventoryNow.getCounterparty(),
                                inventoryNow.getQuantity(), inventoryNow.getCostTotal(), false, inventoryType);

                        // write to history
                        InventoryHistorySql history = new InventoryHistorySql(auth.getCompanyId(), partRef, warehouseRef,
                                part.getSn(), part.getUuid(), inventoryType,
                                inventoryNow.getDoc(), invCounterpartyRef,
                                Doc.of(document), docCounterpartyRef,
                                BigDecimalUtils.negated(inventoryNow.getQuantity()), GamaMoneyUtils.negated(inventoryNow.getCostTotal()),
                                historyTotal, historyBaseTotal);
                        dbServiceSQL.saveEntityInCompany(history);

                        totals.costTotal = GamaMoneyUtils.add(totals.costTotal, GamaMoneyUtils.negated(inventoryNow.getCostTotal()));

                        inventoryNow.setQuantity(null);
                    }
                }

                if (BigDecimalUtils.isZero(inventoryNow.getQuantity())) {
                    entityManager.remove(inventoryNow);
                } else {
                    dbServiceSQL.saveEntityInCompany(inventoryNow);
                }

                if (BigDecimalUtils.isNegativeOrZero(totals.quantity)) break;
            }
        }
        if (BigDecimalUtils.isNegative(totals.quantity)) {
            throw new GamaNotEnoughQuantityException(
                    MessageFormat.format(TranslationService.getInstance().translate(TranslationService.INVENTORY.NotEnoughQuantity, auth.getLanguage()),
                            document.getNumber(), document.getDate(), part.toMessage(), warehouse.getName(), totals.quantity));
        }
        return totals;
    }

    private <E extends IBaseDocPartCost & IBaseDocPartSql & IDocPart & IPartSN & IUuid>
    void removeInventoryServiceSQL(InventoryType inventoryType, BaseDocumentSql document, E part, WarehouseTagged warehouse) {
        // write to history
        CounterpartySql docCounterpartyRef = Validators.isValid(document.getCounterparty())
                ? entityManager.getReference(CounterpartySql.class, document.getCounterparty().getId()) : null;

        InventoryHistorySql history = new InventoryHistorySql(auth.getCompanyId(),
                entityManager.getReference(PartSql.class, part.getPartId()),
                entityManager.getReference(WarehouseSql.class, warehouse.getId()),
                part.getSn(), part.getUuid(), inventoryType,
                Doc.of(document), docCounterpartyRef,
                Doc.of(document), docCounterpartyRef,
                BigDecimalUtils.negated(part.getQuantity()), GamaMoneyUtils.negated(part.getCostTotal()),
                part.getTotal(), part.getBaseTotal());
        dbServiceSQL.saveEntityInCompany(history);
    }

    private <E extends IBaseDocPartOutRemainder & IBaseDocPartCost & IBaseDocPartSql & IDocPart & IPartSN & IFinished>
    void removeInventoryRecallSQL(BaseDocumentSql document, WarehouseTagged docWarehouse, E part) {
        WarehouseTagged warehouse = getValidWarehouseSQL(part, document, docWarehouse);

        removeInventoryRecallQuantitySQL(document, part, warehouse, part.getQuantity());

        part.setFinished(null);
        part.setCostTotal(null);
        part.setRemainder(null);

    }

    private <E extends IBaseDocPartCost & IBaseDocPartSql & IDocPart & IPartSN & IFinished>
    void removeInventoryRecallSQL(BaseDocumentSql document, WarehouseTagged docWarehouse, E part) {
        WarehouseTagged warehouse = getValidWarehouseSQL(part, document, docWarehouse);

        removeInventoryRecallQuantitySQL(document, part, warehouse, part.getQuantity());

        part.setFinished(null);
        part.setCostTotal(null);
    }

    private void reserveRemoveInventoryRecallSQL(BaseDocumentSql document, WarehouseTagged docWarehouse, TransProdPartFromSql part) {
        WarehouseTagged warehouse = getValidWarehouseSQL(part, document, docWarehouse);

        removeInventoryRecallQuantitySQL(document, part, warehouse, part.getReservedQuantity());

        part.setReserved(false);
    }

    private <E extends IBaseDocPartCost & IDocPart & IPartSN>
    void removeInventoryRecallQuantitySQL(BaseDocumentSql document, E part,
                                          WarehouseTagged warehouse, BigDecimal partQuantity) {
        if (part.getType() != PartType.SERVICE) {
            if (part.getCostInfo() != null) {
                for (PartCostSource costSource : part.getCostInfo()) {
                    // restore InventoryNow - source
                    InventoryNowSql inventoryNow = dbServiceSQL.getInventoryNow(warehouse, part, costSource.getDoc().getId());

                    if (inventoryNow == null) {
                        inventoryNow = new InventoryNowSql(auth.getCompanyId(),
                                entityManager.getReference(PartSql.class, part.getPartId()), part.getSn(),
                                entityManager.getReference(WarehouseSql.class, warehouse.getId()),
                                costSource.getDoc(),
                                Validators.isValid(costSource.getCounterparty())
                                        ? entityManager.getReference(CounterpartySql.class, costSource.getCounterparty().getId())
                                        : null,
                                warehouse.getTag());
                    }
                    inventoryNow.setQuantity(BigDecimalUtils.add(inventoryNow.getQuantity(), costSource.getQuantity()));
                    inventoryNow.setCostTotal(GamaMoneyUtils.add(inventoryNow.getCostTotal(), costSource.getCostTotal()));
                    saveOrDeleteInventoryNow(inventoryNow);
                }
            }

            if (BooleanUtils.isTrue(part.isForwardSell())) {
                int deleted = entityManager.createQuery(
                                "DELETE FROM " + InventoryNowSql.class.getName() + " n" +
                                        " WHERE " + InventoryNowSql_.COMPANY_ID + " = :companyId" +
                                        " AND " + InventoryNowSql_.PART + "." + PartSql_.ID + " = :partId" +
                                        " AND " + InventoryNowSql_.WAREHOUSE + "." + WarehouseSql_.ID + " = :warehouseId" +
                                        " AND " + InventoryNowSql_.DOC + ".id = :docId")
                        .setParameter("companyId", auth.getCompanyId())
                        .setParameter("partId", part.getPartId())
                        .setParameter("warehouseId", warehouse.getId())
                        .setParameter("docId", document.getId())
                        .executeUpdate();

                log.info("deleted " + InventoryNowSql.class.getSimpleName() + ": " + deleted);
            }

            // update part warehouses and remainders
            updatePartRemainder(part.getPartId(), part.getType(), new DocWarehouse(warehouse), BigDecimalUtils.abs(partQuantity),
                    GamaMoneyUtils.abs(part.getCostTotal()));
        }

        // clear history
        clearHistory(part.getPartId(), warehouse.getId(), document.getId());
    }

    private void returnPurchaseInventorySQL(PurchaseSql document, WarehouseTagged docWarehouse, PurchasePartSql part) {

        Validators.checkNotNull(part.getDocReturn(), "No Return Document in {0}", document.toString());
        Validators.checkNotNull(part.getDocReturn().getDate(), "No Return Document date in {0}", document.toString());
        Validators.checkNotNull(part.getDocReturn().getNumber(), "No Return Document Number in {0}", document.toString());

        WarehouseTagged warehouse = getValidWarehouseSQL(part, document, docWarehouse);

        final long companyId = auth.getCompanyId();

        CounterpartySql docCounterpartyRef = Validators.isValid(document.getCounterparty())
                ? entityManager.getReference(CounterpartySql.class, document.getCounterparty().getId()) : null;
        PartSql partRef = entityManager.getReference(PartSql.class, part.getPartId());
        WarehouseSql warehouseRef = entityManager.getReference(WarehouseSql.class, warehouse.getId());

        if (part.getType() == PartType.SERVICE) {
            // write to history
            InventoryHistorySql history = new InventoryHistorySql(companyId, partRef, warehouseRef,
                    part.getSn(), part.getUuid(), InventoryType.PURCHASE,
                    Doc.of(document), docCounterpartyRef,
                    Doc.of(document), docCounterpartyRef,
                    part.getQuantity(), part.getCostTotal(),
                    null, null);
            dbServiceSQL.saveEntityInCompany(history);

        } else {
            List<InventoryNowSql> inventories = dbServiceSQL.getInventoriesNow(warehouse, part, InventoryNowSql.GRAPH_COUNTERPARTY);

            if (CollectionsHelper.isEmpty(inventories)) {
                throw new GamaNotEnoughQuantityException(
                        MessageFormat.format(TranslationService.getInstance().translate(TranslationService.INVENTORY.NotEnoughQuantity),
                                document.getNumber(), document.getDate(), part.toMessage(), warehouse.getName(), part.getQuantity()));
            }

            BigDecimal quantity = BigDecimalUtils.abs(part.getQuantity());
            GamaMoney returnCostTotal = null;

            for (InventoryNowSql inventoryNow : inventories) {
                // if zero - delete and skip
                if (BigDecimalUtils.isZero(inventoryNow.getQuantity()) && GamaMoneyUtils.isZero(inventoryNow.getCostTotal())) {
                    entityManager.remove(inventoryNow);

                    continue;
                }

                // if not the source document - skip
                if (!part.getDocReturn().getDate().isEqual(inventoryNow.getDoc().getDate()) ||
                        !part.getDocReturn().getNumber().equals(inventoryNow.getDoc().getNumber())) {
                    continue;
                }

                BigDecimal returnQuantity = null;
                GamaMoney returnCost = null;

                if (BigDecimalUtils.isPositive(quantity)) {
                    if (BigDecimalUtils.isLessThan(quantity, inventoryNow.getQuantity())) {
                        // if enough quantity
                        returnQuantity = quantity;
                        returnCost = GamaMoneyUtils.multipliedBy(inventoryNow.getCostTotal(),
                                quantity.doubleValue() / inventoryNow.getQuantity().doubleValue());

                        returnCostTotal = GamaMoneyUtils.add(returnCostTotal, returnCost);

                        inventoryNow.setQuantity(BigDecimalUtils.subtract(inventoryNow.getQuantity(), returnQuantity));
                        inventoryNow.setCostTotal(GamaMoneyUtils.subtract(inventoryNow.getCostTotal(), returnCost));

                        quantity = null;
                    } else {
                        // not enough quantity - use all
                        returnQuantity = inventoryNow.getQuantity();
                        returnCost = inventoryNow.getCostTotal();

                        returnCostTotal = GamaMoneyUtils.add(returnCostTotal, returnCost);

                        quantity = BigDecimalUtils.subtract(quantity, inventoryNow.getQuantity());

                        inventoryNow.setQuantity(null);
                        inventoryNow.setCostTotal(null);
                    }
                }
//                if (JodaMoneyUtils.isPositive(costTotal)) {
//                    if (JodaMoneyUtils.isLessThan(costTotal, inventoryNow.getCostTotal())) {
//                        returnCost = costTotal;
//                        inventoryNow.setCostTotal(JodaMoneyUtils.subtract(inventoryNow.getCostTotal(), costTotal));
//                        costTotal = null;
//                    } else {
//                        returnCost = inventoryNow.getCostTotal();
//                        costTotal = JodaMoneyUtils.subtract(costTotal, inventoryNow.getCostTotal());
//                        inventoryNow.setCostTotal(null);
//                    }
//                }

                // write to history
                if (!BigDecimalUtils.isZero(returnQuantity) || GamaMoneyUtils.isNonZero(returnCost)) {
                    InventoryHistorySql history = new InventoryHistorySql(companyId, partRef, warehouseRef,
                            part.getSn(), part.getUuid(), InventoryType.PURCHASE,
                            inventoryNow.getDoc(),
                            Validators.isValid(inventoryNow.getCounterparty())
                                    ? entityManager.getReference(CounterpartySql.class, inventoryNow.getCounterparty().getId()) : null,
                            Doc.of(document), docCounterpartyRef,
                            BigDecimalUtils.negated(returnQuantity), GamaMoneyUtils.negated(returnCost),
                            null, null);
                    dbServiceSQL.saveEntityInCompany(history);
                }

                saveOrDeleteInventoryNow(inventoryNow);
                if (BigDecimalUtils.isNegativeOrZero(quantity)) break;
            }

            if (!BigDecimalUtils.isZero(quantity)) {
                throw new GamaNotEnoughQuantityException(
                        MessageFormat.format(TranslationService.getInstance().translate(TranslationService.INVENTORY.NotEnoughQuantity),
                                document.getNumber(), document.getDate(), part.toMessage(), warehouse.getName(), quantity));
            }

//            if (JodaMoneyUtils.isNonZero(costTotal)) {
//                throw new NotEnoughQuantityRuntimeException(
//                        MessageFormat.format(TranslationService.getInstance().translate(TranslationService.INVENTORY.NotEnoughCost),
//                                document.getNumber(), document.getDate(), part.toMessage(), warehouse.getName(), costTotal),
//                        "returnPurchaseInventory", 300);
//            }

            // update document part
            part.setCostTotal(GamaMoneyUtils.negated(returnCostTotal));

            // update part warehouses and remainders
            updatePartRemainder(part.getPartId(), part.getType(), new DocWarehouse(warehouse), part.getQuantity(), part.getCostTotal());
        }
        part.setFinished(true);
    }

    private void returnPurchaseRecallSQL(PurchaseSql document, WarehouseTagged docWarehouse, PurchasePartSql part) {

        Validators.checkNotNull(part.getDocReturn(), "No Return Document data in {0}", document.toString());
        Validators.checkNotNull(part.getDocReturn().getDate(), "No Return Document date in {0}", document.toString());
        Validators.checkNotNull(part.getDocReturn().getNumber(), "No Return Document Number in {0}", document.toString());

        WarehouseTagged warehouse = getValidWarehouseSQL(part, document, docWarehouse);

        final long companyId = auth.getCompanyId();

        if (part.getType() != PartType.SERVICE) {
            InventoryNowSql inventoryNow = dbServiceSQL.getInventoryNow(warehouse, part, part.getDocReturn().getId());

            if (inventoryNow == null) {
                inventoryNow = new InventoryNowSql(companyId,
                        entityManager.getReference(PartSql.class, part.getPartId()), part.getSn(),
                        entityManager.getReference(WarehouseSql.class, warehouse.getId()),
                        part.getDocReturn(),
                        Validators.isValid(document.getCounterparty())
                                ? entityManager.getReference(CounterpartySql.class, document.getCounterparty().getId()) : null);
            }
            inventoryNow.setQuantity(BigDecimalUtils.subtract(inventoryNow.getQuantity(), part.getQuantity()));
            inventoryNow.setCostTotal(GamaMoneyUtils.subtract(inventoryNow.getCostTotal(), part.getCostTotal()));
            saveOrDeleteInventoryNow(inventoryNow);

            // update part warehouses and remainders
            updatePartRemainder(part.getPartId(), part.getType(), new DocWarehouse(warehouse), BigDecimalUtils.negated(part.getQuantity()),
                    GamaMoneyUtils.negated(part.getCostTotal()));
        }
        part.setFinished(false);

        // clear history
        clearHistory(part.getPartId(), warehouse.getId(), document.getId());
    }

    private <E extends IBaseDocPartCost & IBaseDocPartSql & IDocPart & IPartSN & IUuid & IFinished & IPartMessage>
    void returnInvoiceSQL(InvoiceSql document, WarehouseTagged docWarehouse, Doc docReturn, E part) {

        Validators.checkNotNull(docReturn, "No Return Document data in {0}", document.toString());
        Validators.checkNotNull(docReturn.getDate(), "No Return Document date in {0}", document.toString());
        Validators.checkNotNull(docReturn.getNumber(), "No Return Document Number in {0}", document.toString());

        WarehouseTagged warehouse = getValidWarehouseSQL(part, document, docWarehouse);

        final long companyId = auth.getCompanyId();

        CounterpartySql docCounterpartyRef = Validators.isValid(document.getCounterparty())
                ? entityManager.getReference(CounterpartySql.class, document.getCounterparty().getId()) : null;
        PartSql partRef = entityManager.getReference(PartSql.class, part.getPartId());
        WarehouseSql warehouseRef = entityManager.getReference(WarehouseSql.class, warehouse.getId());

        if (part.getType() == PartType.SERVICE) {
            // write to history
            InventoryHistorySql history = new InventoryHistorySql(companyId, partRef, warehouseRef,
                    part.getSn(), part.getUuid(), InventoryType.INVOICE,
                    docReturn, docCounterpartyRef,
                    Doc.of(document), docCounterpartyRef,
                    BigDecimalUtils.negated(part.getQuantity()), GamaMoneyUtils.negated(part.getBaseTotal()),
                    null, null);
            dbServiceSQL.saveEntityInCompany(history);
        } else {
            BigDecimal quantity = BigDecimalUtils.abs(part.getQuantity());

            // if it's return document - check origin invoice document parts costInfo
            boolean done = false;
            InvoiceSql invoice = dbServiceSQL.getAndCheck(InvoiceSql.class, docReturn.getId());

            Validators.checkArgument(Objects.equals(invoice.getCounterparty().getId(), document.getCounterparty().getId()), "Wrong counterparty");
            Validators.checkNotNull(invoice.getParts(), "No parts in invoice");
            Validators.checkArgument(Objects.equals(invoice.getExchange().getCurrency(), document.getExchange().getCurrency()),
                    "Documents currencies differ: " + invoice.getExchange().getCurrency() + "/" + document.getExchange().getCurrency());

            InventoryNowSql inventoryNow = null;
            GamaMoney costTotal = null;

            for (var partInvoice : invoice.getParts()) {

                if (!Objects.equals(partInvoice.getPartId(), part.getPartId())) continue;
                if (!PartSN.equals(partInvoice.getSn(), part.getSn())) continue;
                if (part.getUuid() != null && partInvoice.getUuid() != null && !Objects.equals(partInvoice.getUuid(), part.getUuid()))
                    continue;
                // if (!JodaMoneyUtils.isEqual(partInvoice.getPrice(), part.getPrice())) continue;
                if (partInvoice.getCostInfo() == null) continue;

                part.setCostTotal(null);
                for (PartCostSource partCostSource : Lists.reverse(partInvoice.getCostInfo())) {
                    BigDecimal balanceQuantity = BigDecimalUtils.subtract(partCostSource.getQuantity(), partCostSource.getRetQuantity());
                    if (BigDecimalUtils.isZero(balanceQuantity)) continue;

                    if (BigDecimalUtils.isPositive(quantity)) {

                        BigDecimal qty;
                        GamaMoney cost = null;

                        if (BigDecimalUtils.isLessThan(quantity, balanceQuantity)) {
                            if (GamaMoneyUtils.isNonZero(partCostSource.getCostTotal())) {
                                GamaBigMoney unitPrice = partCostSource.getCostTotal().toBigMoney().withScale(8)
                                        .dividedBy(partCostSource.getQuantity());
                                cost = GamaMoneyUtils.toMoney(unitPrice.multipliedBy(quantity));
                                GamaMoney balanceCost = GamaMoneyUtils.subtract(partCostSource.getCostTotal(), partCostSource.getRetCostTotal());
                                if (GamaMoneyUtils.isGreaterThan(cost, balanceCost)) cost = balanceCost;
                                partCostSource.setRetCostTotal(GamaMoneyUtils.add(partCostSource.getRetCostTotal(), cost));
                            }
                            partCostSource.setRetQuantity(BigDecimalUtils.add(partCostSource.getRetQuantity(), quantity));

                            qty = quantity;
                            quantity = null;
                        } else {
                            cost = GamaMoneyUtils.subtract(partCostSource.getCostTotal(), partCostSource.getRetCostTotal());

                            partCostSource.setRetQuantity(partCostSource.getQuantity());
                            partCostSource.setRetCostTotal(partCostSource.getCostTotal());

                            qty = balanceQuantity;
                            quantity = BigDecimalUtils.subtract(quantity, balanceQuantity);
                        }
                        // update inventory
                        if (inventoryNow == null || !Objects.equals(inventoryNow.getDoc().getId(), partCostSource.getDoc().getId())) {
                            inventoryNow = dbServiceSQL.getInventoryNow(warehouse, part, partCostSource.getDoc().getId());
                        }

                        if (inventoryNow == null) {
                            inventoryNow = new InventoryNowSql(companyId, partRef, part.getSn(), warehouseRef,
                                    partCostSource.getDoc(),
                                    Validators.isValid(partCostSource.getCounterparty())
                                            ? entityManager.getReference(CounterpartySql.class, partCostSource.getCounterparty().getId()) : null);
                        }
                        inventoryNow.setQuantity(BigDecimalUtils.add(inventoryNow.getQuantity(), qty));
                        inventoryNow.setCostTotal(GamaMoneyUtils.add(inventoryNow.getCostTotal(), cost));
                        saveOrDeleteInventoryNow(inventoryNow);

                        part.setCostTotal(GamaMoneyUtils.add(part.getCostTotal(), GamaMoneyUtils.negated(cost)));

                        // write to history
                        InventoryHistorySql history = new InventoryHistorySql(companyId, partRef, warehouseRef,
                                part.getSn(), part.getUuid(), InventoryType.INVOICE,
                                docReturn, docCounterpartyRef,
                                Doc.of(document), docCounterpartyRef,
                                qty, cost,
                                null, null);
                        dbServiceSQL.saveEntityInCompany(history);

                        costTotal = GamaMoneyUtils.add(costTotal, cost);
                    }
                    done = BigDecimalUtils.isZero(quantity);
                }

                if (done) break;
            }
            if (!done) {
                throw new GamaNotEnoughQuantityException(
                        MessageFormat.format(TranslationService.getInstance().translate(TranslationService.INVENTORY.NotEnoughQuantity, auth.getLanguage()),
                                document.getNumber(), document.getDate(), part.toMessage(), warehouse.getName(), quantity));
            }

            // update part warehouses and remainders
            updatePartRemainder(part.getPartId(), part.getType(), new DocWarehouse(warehouse), BigDecimalUtils.negated(part.getQuantity()), costTotal);

        }
        part.setFinished(true);
    }

    private <E extends IBaseDocPartCost & IBaseDocPartSql & IDocPart & IPartSN & IFinished & IUuid & IPartMessage>
    void returnInvoiceRecallSQL(InvoiceSql document, WarehouseTagged docWarehouse, Doc docReturn, E part) {

        Validators.checkNotNull(docReturn, "No return document data");
        Validators.checkNotNull(docReturn.getDate(), "No Return Document date");
        Validators.checkNotNull(docReturn.getNumber(), "No Return Document Number");

        WarehouseTagged warehouse = getValidWarehouseSQL(part, document, docWarehouse);

        final long companyId = auth.getCompanyId();

        if (part.getType() != PartType.SERVICE) {

            BigDecimal quantity = BigDecimalUtils.abs(part.getQuantity());

            if (docReturn.getId() == null) {
                // if no return document - simple remove with cost (using costTotal !!!)
                InventoryNowSql inventoryNow = dbServiceSQL.getInventoryNow(warehouse, part, document.getId());

                if (inventoryNow == null) {
                    inventoryNow = new InventoryNowSql(companyId,
                            entityManager.getReference(PartSql.class, part.getPartId()), part.getSn(),
                            entityManager.getReference(WarehouseSql.class, warehouse.getId()),
                            Doc.of(document),
                            Validators.isValid(document.getCounterparty())
                                    ? entityManager.getReference(CounterpartySql.class, document.getCounterparty().getId()) : null);
                }
                inventoryNow.setQuantity(BigDecimalUtils.add(inventoryNow.getQuantity(), part.getQuantity()));
                inventoryNow.setCostTotal(GamaMoneyUtils.add(inventoryNow.getCostTotal(), part.getCostTotal()));
                saveOrDeleteInventoryNow(inventoryNow);

                // update part warehouses and remainders
                updatePartRemainder(part.getPartId(), part.getType(), new DocWarehouse(warehouse), part.getQuantity(), part.getCostTotal());

            } else {
                // have returning document - check invoice documents in parts costInfo
                boolean done = false;
                InvoiceSql invoice = dbServiceSQL.getAndCheck(InvoiceSql.class, docReturn.getId());

                Validators.checkArgument(Objects.equals(invoice.getCounterparty().getId(), document.getCounterparty().getId()), "Wrong counterparty");
                Validators.checkNotNull(invoice.getParts(), "No parts in invoice");
                Validators.checkArgument(Objects.equals(invoice.getExchange().getCurrency(), document.getExchange().getCurrency()),
                        "Documents currencies differ: " + invoice.getExchange().getCurrency() + "/" + document.getExchange().getCurrency());

                GamaMoney costTotal = null;

                InventoryNowSql inventoryNow = null;

                for (var partInvoice : invoice.getParts()) {

                    if (!Objects.equals(partInvoice.getPartId(), part.getPartId())) continue;
                    if (!PartSN.equals(partInvoice.getSn(), part.getSn())) continue;
                    if (!GamaMoneyUtils.isEqual(partInvoice.getPrice(), part.getPrice())) continue;
                    if (partInvoice.getCostInfo() == null) continue;

                    for (PartCostSource partCostSource : Lists.reverse(partInvoice.getCostInfo())) {
                        if (!BigDecimalUtils.isPositive(partCostSource.getRetQuantity())) continue;

                        BigDecimal qty;
                        GamaMoney cost = null;

                        if (BigDecimalUtils.isLessThan(quantity, partCostSource.getRetQuantity())) {

                            if (GamaMoneyUtils.isNonZero(partCostSource.getRetCostTotal())) {

                                GamaBigMoney unitPrice = partCostSource.getRetCostTotal().toBigMoney().withScale(8)
                                        .dividedBy(partCostSource.getRetQuantity());
                                cost = GamaMoneyUtils.toMoney(unitPrice.multipliedBy(quantity == null ? BigDecimal.ZERO : quantity));
                                if (GamaMoneyUtils.isGreaterThan(cost, partCostSource.getRetCostTotal()))
                                    cost = partCostSource.getRetCostTotal();

                                partCostSource.setRetCostTotal(GamaMoneyUtils.subtract(partCostSource.getRetCostTotal(), cost));
                            }
                            partCostSource.setRetQuantity(BigDecimalUtils.subtract(partCostSource.getRetQuantity(), quantity));

                            qty = quantity;
                            quantity = null;

                        } else {
                            cost = partCostSource.getRetCostTotal();
                            qty = partCostSource.getRetQuantity();

                            partCostSource.setRetQuantity(null);
                            partCostSource.setRetCostTotal(null);

                            quantity = BigDecimalUtils.subtract(quantity, qty);
                        }

                        // update inventory
                        if (inventoryNow == null || !Objects.equals(inventoryNow.getDoc().getId(), partCostSource.getDoc().getId())) {
                            inventoryNow = dbServiceSQL.getInventoryNow(warehouse, part, partCostSource.getDoc().getId());
                        }
                        if (inventoryNow == null || BigDecimalUtils.isZero(inventoryNow.getQuantity())) {
                            throw new GamaNotEnoughQuantityException(
                                    MessageFormat.format(TranslationService.getInstance().translate(TranslationService.INVENTORY.NotEnoughQuantity, auth.getLanguage()),
                                            document.getNumber(), document.getDate(), part.toMessage(), warehouse.getName(), quantity));
                        }

                        costTotal = GamaMoneyUtils.add(costTotal, cost);

                        if (BigDecimalUtils.isLessThanOrEqual(qty, inventoryNow.getQuantity())) {
                            inventoryNow.setQuantity(BigDecimalUtils.subtract(inventoryNow.getQuantity(), qty));
                            qty = null;
                        } else {
                            qty = BigDecimalUtils.subtract(qty, inventoryNow.getQuantity());
                            inventoryNow.setQuantity(null);
                        }
                        if (GamaMoneyUtils.isLessThanOrEqual(cost, inventoryNow.getCostTotal())) {
                            inventoryNow.setCostTotal(GamaMoneyUtils.subtract(inventoryNow.getCostTotal(), cost));
                            cost = null;
                        } else {
                            cost = GamaMoneyUtils.subtract(cost, inventoryNow.getCostTotal());
                            inventoryNow.setCostTotal(null);
                        }

                        if (qty != null || cost != null) {
                            throw new GamaNotEnoughQuantityException(
                                    MessageFormat.format(TranslationService.getInstance().translate(TranslationService.INVENTORY.NotEnoughQuantity, auth.getLanguage()),
                                            document.getNumber(), document.getDate(), part.toMessage(), warehouse.getName(), quantity));
                        }

                        saveOrDeleteInventoryNow(inventoryNow);
                        done = BigDecimalUtils.isZero(quantity);

                    } // for (PartCostSource partCostSource : Lists.reverse(partInvoice.getCostInfo())) {

                    if (done) break;

                } // for (PartPart partInvoice : InvoicePartsIterator.of(invoice)) { ...

                if (!done) {
                    throw new GamaNotEnoughQuantityException(
                            MessageFormat.format(TranslationService.getInstance().translate(TranslationService.INVENTORY.NotEnoughQuantity, auth.getLanguage()),
                                    document.getNumber(), document.getDate(), part.toMessage(), warehouse.getName(), quantity));
                }

                // update part warehouses and remainders
                updatePartRemainder(part.getPartId(), part.getType(), new DocWarehouse(warehouse), part.getQuantity(), GamaMoneyUtils.negated(costTotal));
            }
        }

        part.setFinished(false);

        // clear history
        clearHistory(part.getPartId(), warehouse.getId(), document.getId());
    }

    private void changeInventorySQL(InventorySql document, InventoryPartSql part) {

        WarehouseTagged warehouse = getValidWarehouseSQL(part, document, new WarehouseTagged(document.getWarehouse(), document.getTag()));

        final long companyId = auth.getCompanyId();

        if (part.getType() == PartType.PRODUCT_SN) {
            Validators.checkArgument(part.getSn() != null && StringHelper.hasValue(part.getSn().getSn()),
                    "No S/N in " + part.toMessage());
        }

        List<? extends InventoryNowSql> inventories = dbServiceSQL.getInventoriesNow(warehouse, part, InventoryNowSql.GRAPH_COUNTERPARTY);

        BigDecimal quantityInitial = null;
        GamaMoney costInitial = null;
        if (CollectionsHelper.hasValue(inventories)) {
            for (InventoryNowSql inventoryNow : inventories) {
                quantityInitial = BigDecimalUtils.add(quantityInitial, inventoryNow.getQuantity());
                costInitial = GamaMoneyUtils.add(costInitial, inventoryNow.getCostTotal());
            }
        }
        part.setQuantityInitial(quantityInitial);
        part.setCostInitial(costInitial);
        part.setCostInfo(null);

        if (part.isChange()) {
            part.setQuantityRemainder(BigDecimalUtils.add(part.getQuantityInitial(), part.getQuantity()));
        } else {
            part.setQuantity(BigDecimalUtils.subtract(part.getQuantityRemainder(), part.getQuantityInitial()));
        }

        if (BigDecimalUtils.isZero(part.getQuantity())) {
            part.setCostTotal(null);
            part.setCostRemainder(part.getCostInitial());
            part.setFinished(true);
            return;
        }

        PartSql partRef = entityManager.getReference(PartSql.class, part.getPartId());
        WarehouseSql warehouseRef = entityManager.getReference(WarehouseSql.class, warehouse.getId());
        CounterpartySql docCounterpartyRef = Validators.isValid(document.getCounterparty())
                ? entityManager.getReference(CounterpartySql.class, document.getCounterparty().getId()) : null;

        if (BigDecimalUtils.isPositive(part.getQuantity())) {

            inventories = CollectionsHelper.streamOf(inventories)
                    .filter(i -> Objects.equals(i.getDoc().getId(), document.getId()))
                    .collect(Collectors.toList());
            if (inventories.size() > 1)
                throw new GamaException("To many InventoryNow records");

            InventoryNowSql inventoryNow = inventories.size() == 1 ? inventories.get(0) : null;
            if (inventoryNow == null) {
                inventoryNow = new InventoryNowSql(companyId, partRef, part.getSn(), warehouseRef,
                        Doc.of(document), docCounterpartyRef);
            }
            inventoryNow.setQuantity(BigDecimalUtils.add(inventoryNow.getQuantity(), part.getQuantity()));
            inventoryNow.setCostTotal(GamaMoneyUtils.add(inventoryNow.getCostTotal(), part.getCostTotal()));
            saveOrDeleteInventoryNow(inventoryNow);

            // write to history
            InventoryHistorySql history = new InventoryHistorySql(companyId, partRef, warehouseRef,
                    part.getSn(), part.getUuid(), InventoryType.INVENTORY,
                    Doc.of(document), docCounterpartyRef,
                    Doc.of(document), docCounterpartyRef,
                    part.getQuantity(), part.getCostTotal(),
                    null, null);
            dbServiceSQL.saveEntityInCompany(history);

            // update part warehouses and remainders
            updatePartRemainder(part.getPartId(), part.getType(), new DocWarehouse(warehouse), part.getQuantity(), part.getCostTotal());
        } else {

            if (CollectionsHelper.isEmpty(inventories)) {
                throw new GamaNotEnoughQuantityException(
                        MessageFormat.format(TranslationService.getInstance().translate(TranslationService.INVENTORY.NotEnoughQuantity),
                                document.getNumber(), document.getDate(), part.toMessage(), warehouse.getName(), part.getQuantity()));
            }

            part.setCostTotal(null);
            BigDecimal quantity = part.getQuantity().negate();
            for (InventoryNowSql inventoryNow : inventories) {
                if (BigDecimalUtils.isZero(inventoryNow.getQuantity())) {
                    entityManager.remove(inventoryNow);
                    continue;
                }

                if (inventoryNow.getDoc().getDate().isAfter(document.getDate())) continue;

                CounterpartySql invCounterpartyRef = Validators.isValid(inventoryNow.getCounterparty())
                        ? entityManager.getReference(CounterpartySql.class, inventoryNow.getCounterparty().getId()) : null;

                if (BigDecimalUtils.isLessThan(quantity, inventoryNow.getQuantity())) {
                    GamaMoney cost = null;
                    if (GamaMoneyUtils.isNonZero(inventoryNow.getCostTotal())) {
                        GamaBigMoney unitPrice = inventoryNow.getCostTotal().toBigMoney().withScale(8)
                                .dividedBy(inventoryNow.getQuantity());
                        cost = GamaMoneyUtils.toMoney(unitPrice.multipliedBy(quantity));
                        if (GamaMoneyUtils.isGreaterThan(cost, inventoryNow.getCostTotal()))
                            cost = inventoryNow.getCostTotal();

                        part.setCostTotal(GamaMoneyUtils.add(part.getCostTotal(), cost));
                        inventoryNow.setCostTotal(GamaMoneyUtils.subtract(inventoryNow.getCostTotal(), cost));
                    }
                    inventoryNow.setQuantity(BigDecimalUtils.subtract(inventoryNow.getQuantity(), quantity));
                    saveOrDeleteInventoryNow(inventoryNow);

                    // remember origin doc, counterparty, quantity and cost
                    updatePartCostSource(part, inventoryNow.getDoc(), inventoryNow.getCounterparty(),
                            quantity, cost, false, InventoryType.INVENTORY);

                    // write to history
                    InventoryHistorySql history = new InventoryHistorySql(companyId, partRef, warehouseRef,
                            part.getSn(), part.getUuid(), InventoryType.INVENTORY,
                            inventoryNow.getDoc(), invCounterpartyRef,
                            Doc.of(document), docCounterpartyRef,
                            BigDecimalUtils.negated(quantity), GamaMoneyUtils.negated(cost),
                            null, null);
                    dbServiceSQL.saveEntityInCompany(history);

                    quantity = null;

                } else {
                    part.setCostTotal(GamaMoneyUtils.add(part.getCostTotal(), inventoryNow.getCostTotal()));
                    quantity = BigDecimalUtils.subtract(quantity, inventoryNow.getQuantity());

                    // remember cost and quantity
                    updatePartCostSource(part, inventoryNow.getDoc(), inventoryNow.getCounterparty(),
                            inventoryNow.getQuantity(), inventoryNow.getCostTotal(), false, InventoryType.INVENTORY);

                    // write to history
                    InventoryHistorySql history = new InventoryHistorySql(companyId, partRef, warehouseRef,
                            part.getSn(), part.getUuid(), InventoryType.INVENTORY,
                            inventoryNow.getDoc(), invCounterpartyRef,
                            Doc.of(document), docCounterpartyRef,
                            BigDecimalUtils.negated(inventoryNow.getQuantity()), GamaMoneyUtils.negated(inventoryNow.getCostTotal()),
                            null, null);
                    dbServiceSQL.saveEntityInCompany(history);

                    // set to delete
                    inventoryNow.setQuantity(null);
                    inventoryNow.setCostTotal(null);
                }

                saveOrDeleteInventoryNow(inventoryNow);
                if (BigDecimalUtils.isNegativeOrZero(quantity)) break;
            }
            if (!BigDecimalUtils.isZero(quantity)) {
                throw new GamaNotEnoughQuantityException(
                        MessageFormat.format(TranslationService.getInstance().translate(TranslationService.INVENTORY.NotEnoughQuantity),
                                document.getNumber(), document.getDate(), part.toMessage(), warehouse.getName(), quantity));
            }

            part.setCostTotal(GamaMoneyUtils.negated(part.getCostTotal())); // cost negated !!!

            // update part warehouses and remainders
            updatePartRemainder(part.getPartId(), part.getType(), new DocWarehouse(warehouse), part.getQuantity(), part.getCostTotal());
        }
        part.setCostRemainder(GamaMoneyUtils.add(part.getCostInitial(), part.getCostTotal()));
        part.setFinished(true);
    }

    private void recallChangeInventorySQL(InventorySql document, InventoryPartSql part) {
        BigDecimal quantity = part.getQuantity();
        if (BigDecimalUtils.isZero(quantity)) {
            part.setFinished(false);
            return;
        }

        WarehouseTagged warehouse = getValidWarehouseSQL(part, document, null);

        if (BigDecimalUtils.isPositive(part.getQuantity())) {
            // remove
            addInventoryRecallSQL(InventoryType.INVENTORY, document, warehouse, part);

        } else {
            // restore - add
            removeInventoryRecallSQL(document, warehouse, part);
        }
    }


    public PageResponse<RepInventoryBalance, Void> reportBalance(PageRequest request) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");

        LocalDate startAccounting = companySettings.getStartAccounting();
        LocalDate dateFrom = DateUtils.max(startAccounting, request.getDateFrom());
        LocalDate dateTo = request.getDateTo();
        Validators.checkPeriod(dateFrom, dateTo, auth.getLanguage());

        PageResponse<RepInventoryBalance, Void> response = new PageResponse<>();
        List<RepInventoryBalance> report = new ArrayList<>();

        int cursor = request.getCursor() != null ? request.getCursor() : 0;
        if (request.isBackward() && cursor >= request.getPageSize()) cursor = cursor - request.getPageSize();

        Long warehouseId = (Long) PageRequestUtils.getFieldValue(request.getConditions(), CustomSearchType.WAREHOUSE);

        @SuppressWarnings("unchecked")
        List<String> types = (List<String>) PageRequestUtils.getFieldValue(request.getConditions(), CustomSearchType.PART_TYPE);

        try {
            var q = entityManager.createNativeQuery(StringHelper.loadSQLFromFile("inventory", "inventory_balance.sql"), Tuple.class);
            q.setParameter("companyId", auth.getCompanyId());
            q.setParameter("startAccounting", startAccounting.minusDays(1));
            q.setParameter("dateFrom", dateFrom);
            q.setParameter("dateTo", dateTo.plusDays(1));
            q.setParameter("types", CollectionsHelper.hasValue(types) ? types : "");
            q.setParameter("warehouseId", warehouseId != null ? warehouseId : 0L);
            q.setParameter("partId", 0L);
            q.setParameter("filter", StringHelper.hasValue(request.getFilter()) ? '%' + StringUtils.stripAccents(request.getFilter().trim()) + '%' : "");
            q.setMaxResults(request.getPageSize() + 1);
            q.setFirstResult(cursor);

            @SuppressWarnings("unchecked")
            List<Tuple> results = q.getResultList();

            if (results.size() > request.getPageSize()) {
                response.setMore(true);
                results.remove(results.size() - 1);
            }

            String c = auth.getSettings().getCurrency().getCode();
            if (!results.isEmpty()) {
                Tuple o = results.get(0);
                response.setTotal(o.get("total", BigInteger.class).intValue());
            }

            for (Tuple o : results) {
                DocPartBalance part = new DocPartBalance();
                part.setId(o.get("part_id", BigInteger.class).longValue());
                part.setName(o.get("name", String.class));
                part.setSku(o.get("sku", String.class));
                part.setForwardSell(o.get("forward_sell", Boolean.class));
                var packaging = o.get("packaging", String.class);
                if (packaging != null)
                    part.setPackaging(objectMapper.readValue(packaging, new TypeReference<>() {
                    }));
                part.setDb(DBType.POSTGRESQL);
                part.setType(PartType.from(o.get("part_type", String.class)));
                part.setForeignId(NumberUtils.toLong(o.get("foreign_id", BigInteger.class)));

                Map<InventoryType, InventoryTypeBalance> balanceMap = new HashMap<>();

                var purchaseQuantity = o.get("purchase_quantity", BigDecimal.class);
                var purchaseCost = o.get("purchase_cost", BigDecimal.class);
                if (BigDecimalUtils.isNonZero(purchaseCost) || BigDecimalUtils.isNonZero(purchaseQuantity)) {
                    balanceMap.put(InventoryType.PURCHASE,
                            new InventoryTypeBalance(InventoryType.PURCHASE, purchaseQuantity, GamaMoney.ofNullable(c, purchaseCost)));
                }

                var invoiceQuantity = o.get("invoice_quantity", BigDecimal.class);
                var invoiceCost = o.get("invoice_cost", BigDecimal.class);
                if (BigDecimalUtils.isNonZero(invoiceCost) || BigDecimalUtils.isNonZero(invoiceQuantity)) {
                    balanceMap.put(InventoryType.INVOICE,
                            new InventoryTypeBalance(InventoryType.INVOICE, invoiceQuantity, GamaMoney.ofNullable(c, invoiceCost)));
                }

                var transportQuantity = o.get("transport_quantity", BigDecimal.class);
                var transportCost = o.get("transport_cost", BigDecimal.class);
                if (BigDecimalUtils.isNonZero(transportCost) || BigDecimalUtils.isNonZero(transportQuantity)) {
                    balanceMap.put(InventoryType.TRANSPORT,
                            new InventoryTypeBalance(InventoryType.TRANSPORT, transportQuantity, GamaMoney.ofNullable(c, transportCost)));
                }

                var productionQuantity = o.get("production_quantity", BigDecimal.class);
                var productionCost = o.get("production_cost", BigDecimal.class);
                if (BigDecimalUtils.isNonZero(productionCost) || BigDecimalUtils.isNonZero(productionQuantity)) {
                    balanceMap.put(InventoryType.PRODUCTION,
                            new InventoryTypeBalance(InventoryType.PRODUCTION, productionQuantity, GamaMoney.ofNullable(c, productionCost)));
                }

                var inventoryQuantity = o.get("inventory_quantity", BigDecimal.class);
                var inventoryCost = o.get("inventory_cost", BigDecimal.class);
                if (BigDecimalUtils.isNonZero(inventoryCost) || BigDecimalUtils.isNonZero(inventoryQuantity)) {
                    balanceMap.put(InventoryType.INVENTORY,
                            new InventoryTypeBalance(InventoryType.INVENTORY, inventoryQuantity, GamaMoney.ofNullable(c, inventoryCost)));
                }

                RepInventoryBalance repInventoryBalance = new RepInventoryBalance();
                repInventoryBalance.setPart(part);
                repInventoryBalance.setQuantity(o.get("ob_quantity", BigDecimal.class));
                repInventoryBalance.setCost(GamaMoney.ofNullable(c, o.get("ob_cost", BigDecimal.class)));
                if (CollectionsHelper.hasValue(balanceMap)) repInventoryBalance.setBalanceMap(balanceMap);

                report.add(repInventoryBalance);
            }
        } catch (Exception e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }

        if (request.isBackward() && cursor == 0) response.setMore(false);
        if (!request.isBackward()) cursor = cursor + request.getPageSize();

        response.setCursor(cursor);
        response.setItems(report);

        if (!costVisible() && CollectionsHelper.hasValue(response.getItems())) {
            response.getItems().forEach(e -> {
                e.setCost(null);
                if (CollectionsHelper.hasValue(e.getBalanceMap())) {
                    e.getBalanceMap().values().forEach(v -> v.setCost(null));
                }
            });
        }

        return response;
    }

    private boolean costVisible() {
        return auth.checkPermission(Permission.ADMIN) ||
                auth.checkPermission(Permission.PART_S) ||
                auth.checkPermission(Permission.PURCHASE_R) ||
                auth.checkPermission(Permission.PURCHASE_M) ||
                auth.checkPermission(Permission.GL);
    }

    public InventoryBalanceResponse getBalance(long partId, long warehouseId, PartSN sn) {
        StringJoiner sj = new StringJoiner(" ");
        sj.add("SELECT SUM(i.quantity) AS quantity, SUM(i.costTotal.amount) AS amount, i.costTotal.currency AS currency");
        sj.add("FROM " + InventoryNowSql.class.getName() + " i");
        sj.add("WHERE companyId = :companyId");
        sj.add("AND part.id = :partId");
        sj.add("AND warehouse.id = :warehouseId");
        sj.add((sn != null && StringHelper.hasValue(sn.getSn())) ? "AND sn.sn = :sn" : "AND sn IS NULL");
        sj.add("GROUP BY currency");

        TypedQuery<Tuple> q = entityManager.createQuery(sj.toString(), Tuple.class);
        q.setParameter("companyId", auth.getCompanyId());
        q.setParameter("partId", partId);
        q.setParameter("warehouseId", warehouseId);
        if (sn != null && StringHelper.hasValue(sn.getSn())) q.setParameter("sn", sn.getSn());

        List<Tuple> r = q.getResultList();

        boolean costVisible = costVisible();

        InventoryBalanceResponse response = new InventoryBalanceResponse();
        response.setPartId(partId);
        response.setWarehouseId(warehouseId);
        response.setSn(sn);
        if (!r.isEmpty()) {
            response.setQuantity(r.get(0).get("quantity", BigDecimal.class));
            if (costVisible) response.setCost(GamaMoney.ofNullable(r.get(0).get("currency", String.class),
                    r.get(0).get("amount", BigDecimal.class)));
        }
        return response;
    }

    private Predicate whereReportParts(long partId, Long warehouseId, CriteriaBuilder cb, Root<?> root) {
        Predicate where = cb.equal(root.get(InventoryHistorySql_.PART).get(PartSql_.ID), partId);
        if (warehouseId != null) where = cb.and(where, cb.equal(root.get(InventoryHistorySql_.WAREHOUSE).get(WarehouseSql_.ID), warehouseId));

        // ignore InventoryOpeningBalance records in period
        where = cb.and(where, cb.notEqual(root.get(InventoryHistorySql_.INVENTORY_TYPE), InventoryType.OPENING_BALANCE));

        return where;
    }

    private List<Order> orderParts(String orderBy, CriteriaBuilder cb, Root<?> root) {
        if ("-mainIndex".equals(orderBy) || "-date".equals(orderBy)) {
            return List.of(
                    cb.desc(root.get(InventoryHistorySql_.DOC).get(Doc_.DATE)),
                    cb.desc(root.get(InventoryHistorySql_.DOC).get(Doc_.SERIES)),
                    cb.desc(root.get(InventoryHistorySql_.DOC).get(Doc_.ORDINAL)),
                    cb.desc(root.get(InventoryHistorySql_.DOC).get(Doc_.NUMBER)),
                    cb.desc(root.get(InventoryHistorySql_.DOC).get(Doc_.ID)),
                    cb.desc(root.get(InventoryHistorySql_.ID)));
        }
        return List.of(
                cb.asc(root.get(InventoryHistorySql_.DOC).get(Doc_.DATE)),
                cb.asc(root.get(InventoryHistorySql_.DOC).get(Doc_.SERIES)),
                cb.asc(root.get(InventoryHistorySql_.DOC).get(Doc_.ORDINAL)),
                cb.asc(root.get(InventoryHistorySql_.DOC).get(Doc_.NUMBER)),
                cb.asc(root.get(InventoryHistorySql_.DOC).get(Doc_.ID)),
                cb.asc(root.get(InventoryHistorySql_.ID)));
    }

    private List<Selection<?>> selectIdsParts(CriteriaBuilder cb, Root<?> root) {
        return List.of(
                root.get(InventoryHistorySql_.DOC).get(Doc_.DATE),
                root.get(InventoryHistorySql_.DOC).get(Doc_.SERIES),
                root.get(InventoryHistorySql_.DOC).get(Doc_.ORDINAL),
                root.get(InventoryHistorySql_.DOC).get(Doc_.NUMBER),
                root.get(InventoryHistorySql_.DOC).get(Doc_.ID),
                root.get(InventoryHistorySql_.ID).alias("id"));
    }

    public PageResponse<InventoryHistoryDto, RepInventoryDetail> reportDetail(PageRequest request) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");

        LocalDate startAccounting = companySettings.getStartAccounting();
        LocalDate dateFrom = DateUtils.max(startAccounting, request.getDateFrom());
        LocalDate dateTo = request.getDateTo();
        Validators.checkPeriod(dateFrom, dateTo, auth.getLanguage());

        long partId = (Long) Validators.checkNotNull(PageRequestUtils.getFieldValue(request.getConditions(),
                CustomSearchType.ORIGIN_ID.getField()), "No Part id");

        Long warehouseId = (Long) PageRequestUtils.getFieldValue(request.getConditions(),
                CustomSearchType.WAREHOUSE);

        PageResponse<InventoryHistoryDto, RepInventoryDetail> response = dbServiceSQL.queryPage(request, InventoryHistorySql.class,
                null, inventoryHistorySqlMapper,
                (cb, root) -> whereReportParts(partId, warehouseId, cb, root),
                (cb, root) -> orderParts(request.getOrder(), cb, root),
                this::selectIdsParts);

        boolean costVisible = costVisible();

        if (request.isRefresh()) {

            RepInventoryDetail detail = new RepInventoryDetail();
            detail.setDateFrom(request.getDateFrom());
            detail.setDateTo(request.getDateTo());

            try {
                var q = entityManager.createNativeQuery(StringHelper.loadSQLFromFile("inventory", "inventory_balance.sql"), Tuple.class);
                q.setParameter("companyId", auth.getCompanyId());
                q.setParameter("startAccounting", startAccounting.minusDays(1));
                q.setParameter("dateFrom", dateFrom);
                q.setParameter("dateTo", dateTo.plusDays(1));
                q.setParameter("types", "");
                q.setParameter("warehouseId", warehouseId != null ? warehouseId : 0L);
                q.setParameter("partId", partId);
                q.setParameter("filter", "");

                @SuppressWarnings("unchecked")
                List<Tuple> results = q.getResultList();

                String c = auth.getSettings().getCurrency().getCode();

                if (results.size() == 1) {
                    Tuple r = results.get(0);

                    DocPartBalance p = new DocPartBalance();
                    p.setId(r.get("part_id", BigInteger.class).longValue());
                    p.setName(r.get("name", String.class));
                    p.setSku(r.get("sku", String.class));
                    p.setForwardSell(r.get("forward_sell", Boolean.class));
                    p.setDb(DBType.POSTGRESQL);
                    detail.setPart(p);

                    detail.setQuantityFrom(r.get("ob_quantity", BigDecimal.class));
                    detail.setQuantityTo(r.get("total_quantity", BigDecimal.class));
                    if (costVisible) detail.setCostFrom(GamaMoney.ofNullable(c, r.get("ob_cost", BigDecimal.class)));
                    if (costVisible) detail.setCostTo(GamaMoney.ofNullable(c, r.get("total_cost", BigDecimal.class)));

                } else if (results.isEmpty()) {
                    PartSql part = dbServiceSQL.getAndCheck(PartSql.class, partId);
                    DocPartBalance p = new DocPartBalance();
                    p.setId(part.getId());
                    p.setName(part.getName());
                    p.setSku(part.getSku());
                    p.setForwardSell(part.getForwardSell());
                    p.setDb(DBType.POSTGRESQL);
                    detail.setPart(p);
                }
            } catch (Exception e) {
                log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
            }

            response.setAttachment(detail);
        }
        if (!costVisible && CollectionsHelper.hasValue(response.getItems())) {
            response.getItems().forEach(e -> e.setCostTotal(null));
        }
        return response;
    }

    public PageResponse<InventoryGpais, Void> reportGpais(PageRequest request) {
        // dateFrom, dateTo and filter by product name, code and package type
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");

        LocalDate startAccounting = companySettings.getStartAccounting();
        LocalDate dateFrom = DateUtils.max(startAccounting, request.getDateFrom());
        LocalDate dateTo = request.getDateTo();
        Validators.checkPeriod(dateFrom, dateTo, auth.getLanguage());

        PageResponse<InventoryGpais, Void> response = new PageResponse<>();
        List<InventoryGpais> report = new ArrayList<>();

        int cursor = request.getCursor() != null ? request.getCursor() : 0;
        if (request.isBackward() && cursor >= request.getPageSize()) cursor = cursor - request.getPageSize();

        try {
            var q = entityManager.createNativeQuery(StringHelper.loadSQLFromFile("gpais", "report.sql"), Tuple.class);
            q.setParameter("companyId", auth.getCompanyId());
            q.setParameter("startAccounting", startAccounting.minusDays(1));
            q.setParameter("dateFrom", dateFrom);
            q.setParameter("dateTo", dateTo.plusDays(1));
            q.setParameter("filter", StringHelper.hasValue(request.getFilter()) ? '%' + StringUtils.stripAccents(request.getFilter().trim()) + '%' : "");
            q.setMaxResults(request.getPageSize() + 1);
            q.setFirstResult(cursor);

            @SuppressWarnings("unchecked")
            List<Tuple> results = q.getResultList();

            if (results.size() > request.getPageSize()) {
                response.setMore(true);
                results.remove(results.size() - 1);
            }

            String c = auth.getSettings().getCurrency().getCode();
            if (!results.isEmpty()) {
                Tuple o = results.get(0);
                response.setTotal(o.get("total", BigInteger.class).intValue());
            }

            for (Tuple o : results) {
                DocPartBalance part = new DocPartBalance();
                part.setId(o.get("part_id", BigInteger.class).longValue());
                part.setName(o.get("name", String.class));
                part.setSku(o.get("sku", String.class));
                part.setPackaging(objectMapper.readValue(o.get("packaging", String.class), new TypeReference<>() {}));
                part.setDb(DBType.POSTGRESQL);

                var rep = new InventoryGpais(part, o.get("origin_country", String.class),
                        o.get("ob_quantity", BigDecimal.class),
                        o.get("import_quantity", BigDecimal.class),
                        o.get("purchase_quantity", BigDecimal.class),
                        o.get("export_quantity", BigDecimal.class),
                        o.get("wholesale_quantity", BigDecimal.class),
                        o.get("retail_quantity", BigDecimal.class),
                        o.get("production_quantity", BigDecimal.class),
                        o.get("inventory_quantity", BigDecimal.class),
                        o.get("remainder_quantity", BigDecimal.class));

                report.add(rep);
            }

        } catch (Exception e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }

        if (request.isBackward() && cursor == 0) response.setMore(false);
        if (!request.isBackward()) cursor = cursor + request.getPageSize();

        response.setCursor(cursor);
        response.setItems(report);

        return response;
    }

    public String fixTransProdDocumentStatus(final long documentId) {
        return dbServiceSQL.executeAndReturnInTransaction(em -> {
            TransProdSql document = dbServiceSQL.getById(TransProdSql.class, documentId);
            if (document == null) return MessageFormat.format(
                    TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDocumentWithId), auth.getLanguage(), documentId);

            boolean updated = false;
            if (document.getPartsFrom() == null) return "Document " + documentId + " has no parts from";
            boolean partsFinished = document.getPartsFrom().stream().allMatch(x -> BooleanUtils.isTrue(x.getFinished()));
            if (partsFinished != BooleanUtils.isTrue(document.getFinishedPartsFrom())) {
                document.setFinishedPartsFrom(partsFinished);
                updated = true;
            }
            if (document.getPartsTo() != null) {
                partsFinished = document.getPartsTo().stream().allMatch(x -> BooleanUtils.isTrue(x.getFinished()));
                if (partsFinished != BooleanUtils.isTrue(document.getFinishedPartsTo())) {
                    document.setFinishedPartsTo(partsFinished);
                    updated = true;
                }
            }
            if (updated) {
                return "Fixed";
            }
            return "Everything OK";
        });
    }

    public void clearHistory(long partId, long warehouseId, long docId) {
        dbServiceSQL.executeInTransaction(entityManager -> {
            int deleted = entityManager.createQuery(
                            "DELETE FROM " + InventoryHistorySql.class.getName() + " h" +
                                    " WHERE " + InventoryHistorySql_.COMPANY_ID + " = :companyId" +
                                    " AND " + InventoryHistorySql_.PART + "." + PartSql_.ID + " = :partId" +
                                    " AND " + InventoryHistorySql_.WAREHOUSE + "." + WarehouseSql_.ID + " = :warehouseId" +
                                    " AND " + InventoryHistorySql_.DOC + ".id = :docId")
                    .setParameter("companyId", auth.getCompanyId())
                    .setParameter("partId", partId)
                    .setParameter("warehouseId", warehouseId)
                    .setParameter("docId", docId)
                    .executeUpdate();

            log.info("deleted " + MoneyHistorySql.class.getSimpleName() + ": " + deleted);
        });
    }

    public List<InventoryHistorySql> createInventoryHistoryFromDoc(long partId, long warehouseId, long docId, UUID partUUID) {
        return dbServiceSQL.executeAndReturnInTransaction(em -> {
            BaseDocumentSql document = dbServiceSQL.getById(BaseDocumentSql.class, docId);
            if (document == null) throw new GamaException("No document with id " + docId);
            if (document instanceof InvoiceSql invoice) {
                var docPart = invoice.getParts().stream().filter(part -> part.getPartId() == partId && Objects.equals(part.getUuid(), partUUID)).findFirst().orElse(null);
                if (docPart == null) {
                    throw new GamaException("Part " + partId + " (" + partUUID + ") not found in document " + docId);
                }
                if (BigDecimalUtils.isZero(docPart.getQuantity())) {
                    throw new GamaException("Part " + partId + " (" + partUUID + ") quantity=0");
                }
                if (BigDecimalUtils.isNegative(docPart.getQuantity())) {
                    throw new GamaException("Part " + partId + " (" + partUUID + ") quantity=" + docPart.getQuantity() + " i.e. negative - not supported");
                }
                // fix parts types
                invoice = fixDocumentPartTypes(invoice);

                // write to history
                Validators.checkArgument(CollectionsHelper.hasValue(docPart.getCostInfo()), "No costInfo");
                WarehouseSql warehouse = Validators.checkValid(dbServiceSQL.getById(WarehouseSql.class, warehouseId), "No warehouse=" + warehouseId);
                long companyId = auth.getCompanyId();
                List<InventoryHistorySql> historyList = new ArrayList<>();
                GamaMoney totalBalance = docPart.getTotal();
                GamaMoney baseTotalBalance = docPart.getBaseTotal();
                GamaBigMoney total1 = totalBalance.toBigMoney().dividedBy(docPart.getQuantity());
                GamaBigMoney baseTotal1 = baseTotalBalance.toBigMoney().dividedBy(docPart.getQuantity());

                for (var i = 0; i < docPart.getCostInfo().size(); i++) {
                    var costInfo = docPart.getCostInfo().get(i);
                    GamaMoney total;
                    GamaMoney baseTotal;
                    if (i == docPart.getCostInfo().size() - 1) {
                        total = totalBalance;
                        baseTotal = baseTotalBalance;
                    } else {
                        total = total1.multipliedBy(costInfo.getQuantity()).toMoney();
                        baseTotal = baseTotal1.multipliedBy(costInfo.getQuantity()).toMoney();

                        totalBalance = totalBalance.minus(total);
                        baseTotalBalance = baseTotalBalance.minus(baseTotal);
                    }
                    InventoryHistorySql history = new InventoryHistorySql(companyId,
                            entityManager.getReference(PartSql.class, docPart.getPartId()),
                            warehouse, docPart.getSn(), docPart.getUuid(), InventoryType.INVOICE,
                            costInfo.getDoc(), Validators.isValid(costInfo.getCounterparty())
                            ? entityManager.getReference(CounterpartySql.class, costInfo.getCounterparty().getId()) : null,
                            Doc.of(invoice), invoice.getCounterparty(),
                            BigDecimalUtils.negated(costInfo.getQuantity()), GamaMoneyUtils.negated(costInfo.getCostTotal()),
                            total, baseTotal);
                    historyList.add(dbServiceSQL.saveEntityInCompany(history));
                }
                return historyList;
            } else if (document instanceof PurchaseSql purchase) {
                var docPart = purchase.getParts().stream().filter(part -> part.getPartId() == partId && Objects.equals(part.getUuid(), partUUID)).findFirst().orElse(null);
                if (docPart == null) {
                    throw new GamaException("Part " + partId + " (" + partUUID + ") not found in document " + docId);
                }
                if (BigDecimalUtils.isZero(docPart.getQuantity())) {
                    throw new GamaException("Part " + partId + " (" + partUUID + ") quantity=0");
                }
                if (BigDecimalUtils.isNegative(docPart.getQuantity())) {
                    throw new GamaException("Part " + partId + " (" + partUUID + ") quantity=" + docPart.getQuantity() + " i.e. negative - not supported");
                }
                // fix parts types
                purchase = fixDocumentPartTypes(purchase);

                // write to history
                WarehouseSql warehouse = Validators.checkValid(dbServiceSQL.getById(WarehouseSql.class, warehouseId), "No warehouse=" + warehouseId);
                long companyId = auth.getCompanyId();

                InventoryHistorySql history = new InventoryHistorySql(companyId,
                        entityManager.getReference(PartSql.class, docPart.getPartId()),
                        warehouse, docPart.getSn(), docPart.getUuid(), InventoryType.PURCHASE,
                        Doc.of(purchase), purchase.getCounterparty(),
                        Doc.of(purchase), purchase.getCounterparty(),
                        docPart.getQuantity(), docPart.getCostTotal(),
                        null, null);

                return List.of(dbServiceSQL.saveEntityInCompany(history));
            } else if (document instanceof TransProdSql transProd) {
                if (!transProd.isFullyFinished()) {
                    throw new GamaException("Document " + document.getClass().getSimpleName() + " id=" + docId + " is not finished");
                }

                var inventoryType = CollectionsHelper.hasValue(transProd.getPartsTo()) ? InventoryType.PRODUCTION : InventoryType.TRANSPORT;
                boolean sameWarehouse = transProd.getWarehouseFrom().getId().equals(transProd.getWarehouseTo().getId());
                boolean warehouseFrom = transProd.getWarehouseFrom().getId() == warehouseId;

                var docPart = (inventoryType == InventoryType.TRANSPORT || warehouseFrom ? transProd.getPartsFrom().stream() : CollectionsHelper.streamOf(transProd.getPartsTo()))
                        .filter(part -> part.getPartId() == partId && Objects.equals(part.getUuid(), partUUID))
                        .findFirst().orElse(null);

                if (inventoryType == InventoryType.TRANSPORT) {
                    docPart = transProd.getPartsFrom().stream()
                            .filter(part -> part.getPartId() == partId && Objects.equals(part.getUuid(), partUUID))
                            .findFirst().orElse(null);
                } else {
                    if (sameWarehouse) {
                        docPart = transProd.getPartsFrom().stream()
                                .filter(part -> part.getPartId() == partId && Objects.equals(part.getUuid(), partUUID))
                                .findFirst().orElse(null);
                        if (docPart == null) {
                            docPart = transProd.getPartsTo().stream()
                                    .filter(part -> part.getPartId() == partId && Objects.equals(part.getUuid(), partUUID))
                                    .findFirst().orElse(null);
                            if (docPart != null) {
                                warehouseFrom = false;
                            }
                        }
                    } else {
                        docPart = (warehouseFrom ? transProd.getPartsFrom().stream() : CollectionsHelper.streamOf(transProd.getPartsTo()))
                                .filter(part -> part.getPartId() == partId && Objects.equals(part.getUuid(), partUUID))
                                .findFirst().orElse(null);
                    }
                }

                if (docPart == null) {
                    throw new GamaException("Part " + partId + " (" + partUUID + ") not found in document " + docId);
                }
                if (BigDecimalUtils.isZero(docPart.getQuantity())) {
                    throw new GamaException("Part " + partId + " (" + partUUID + ") quantity=0");
                }
                if (BigDecimalUtils.isNegative(docPart.getQuantity())) {
                    throw new GamaException("Part " + partId + " (" + partUUID + ") quantity=" + docPart.getQuantity() + " i.e. negative - not supported");
                }

                // write to history
                long companyId = auth.getCompanyId();
                WarehouseSql warehouse = Validators.checkNotNull(dbServiceSQL.getById(WarehouseSql.class, warehouseId), "No warehouse=" + warehouseId);

                // write to history
                List<InventoryHistorySql> historyList = new ArrayList<>();
                if (warehouseFrom) {
                    for (var i = 0; i < docPart.getCostInfo().size(); i++) {
                        var costInfo = docPart.getCostInfo().get(i);
                        InventoryHistorySql history = new InventoryHistorySql(companyId,
                                entityManager.getReference(PartSql.class, docPart.getPartId()),
                                warehouse, docPart.getSn(), docPart.getUuid(), inventoryType,
                                costInfo.getDoc(), Validators.isValid(costInfo.getCounterparty())
                                ? entityManager.getReference(CounterpartySql.class, costInfo.getCounterparty().getId()) : null,
                                Doc.of(transProd), null,
                                BigDecimalUtils.negated(costInfo.getQuantity()), GamaMoneyUtils.negated(costInfo.getCostTotal()),
                                null, null);
                        historyList.add(dbServiceSQL.saveEntityInCompany(history));
                    }
                } else {
                    var history = new InventoryHistorySql(companyId,
                            entityManager.getReference(PartSql.class, docPart.getPartId()),
                            warehouse, docPart.getSn(), docPart.getUuid(), inventoryType,
                            Doc.of(transProd), null,
                            Doc.of(transProd), null,
                            docPart.getQuantity(), docPart.getCostTotal(),
                            null, null);
                    historyList.add(dbServiceSQL.saveEntityInCompany(history));
                }
                return historyList;
            } else {
                throw new GamaException("Wrong document type - Invoice, Purchase or TransProd are supported only");
            }
        });
    }

    public Map<String, Integer> deleteInventory(long partId) {
        return dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            PartSql part = dbServiceSQL.getById(PartSql.class, partId);
            if (part == null) throw new GamaException("No part with id=" + partId);
            if (part.getCompanyId() != auth.getCompanyId())
                throw new GamaException("Wrong part company id, expected " + auth.getCompanyId() + " but was " + part.getCompanyId());

            Map<String, Integer> result = new HashMap<>();

            // InventoryNow
            {
                int deleted = entityManager.createQuery(
                                "DELETE FROM " + InventoryNowSql.class.getName() + " n" +
                                        " WHERE " + InventoryNowSql_.COMPANY_ID + " = :companyId" +
                                        " AND " + InventoryNowSql_.PART + "." + PartSql_.ID + " = :partId")
                        .setParameter("companyId", auth.getCompanyId())
                        .setParameter("partId", part.getId())
                        .executeUpdate();

                result.put("InventoryNow", deleted);
            }

            // InventoryHistory
            {
                int deleted = entityManager.createQuery(
                                "DELETE FROM " + InventoryHistorySql.class.getName() + " h" +
                                        " WHERE " + InventoryHistorySql_.COMPANY_ID + " = :companyId" +
                                        " AND " + InventoryHistorySql_.PART + "." + PartSql_.ID + " = :partId")
                        .setParameter("companyId", auth.getCompanyId())
                        .setParameter("partId", part.getId())
                        .executeUpdate();

                result.put("InventoryHistory", deleted);
            }

            //  Import
            {
                if (StringHelper.hasValue(part.getExportId())) {
                    ImportId key = new ImportId(auth.getCompanyId(), PartSql.class, part.getExportId());
                    ImportSql imp = dbServiceSQL.getById(ImportSql.class, key);
                    if (imp != null) {
                        result.put("Import", 1);
                        entityManager.remove(imp);
                    }
                } else {
                    result.put("Import", 0);
                }
            }

            // Part
            dbServiceSQL.removeById(PartSql.class, part.getId());
            result.put("Part", 1);

            return result;
        });
    }

    public void updatePartsVAT(List<PartDto> parts, String country) {
        if (CollectionsHelper.hasValue(parts)) {
            VATRatesDate vatRatesDate = dbServiceSQL.getVATRateDate(country, DateUtils.date());
            if (vatRatesDate == null || CollectionsHelper.isEmpty(vatRatesDate.getRates())) return;
            for (PartDto part : parts) {
                if (StringHelper.hasValue(part.getVatRateCode())) {
                    part.setVat(vatRatesDate.getRatesMap().get(part.getVatRateCode()));
                }
            }
        }
    }

    public void updatePartVAT(PartDto part, String country) {
        if (part != null) {
            VATRatesDate vatRatesDate = dbServiceSQL.getVATRateDate(country, DateUtils.date());
            if (vatRatesDate == null || CollectionsHelper.isEmpty(vatRatesDate.getRates())) return;
            if (StringHelper.hasValue(part.getVatRateCode())) {
                part.setVat(vatRatesDate.getRatesMap().get(part.getVatRateCode()));
            }
        }
    }

    public void calculateVatCodeTotalsSQL(InvoiceSql document) {
        if (document.getVatCodeTotals() == null) {
            document.setVatCodeTotals(new ArrayList<>());
        } else {
            for (VATCodeTotal vatCodeTotal : document.getVatCodeTotals()) {
                vatCodeTotal.setAmount(null);
                vatCodeTotal.setTax(null);
            }
        }

        document.getParts().stream()
                .filter(p -> p instanceof InvoicePartSql)
                .map(InvoicePartSql.class::cast)
                .forEach(part -> {
                    if (part.isTaxable()) {
                        double vatRate = part.getVat() != null ? part.getVat().getRate()
                                : part.getVatRate() != null ? part.getVatRate() : 0;
                        boolean found = false;
                        for (VATCodeTotal vatCodeTotal : document.getVatCodeTotals()) {
                            if (vatCodeTotal.getRate() != null && NumberUtils.isEq(vatCodeTotal.getRate(), vatRate, 2)) {
                                vatCodeTotal.setAmount(GamaMoneyUtils.add(vatCodeTotal.getAmount(), part.getBaseTotal()));
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            VATCodeTotal vatCodeTotal = new VATCodeTotal();
                            vatCodeTotal.setRate(vatRate);
                            vatCodeTotal.setAmount(part.getBaseTotal());
                            document.getVatCodeTotals().add(vatCodeTotal);
                        }
                    }
                });

        document.getVatCodeTotals().forEach(vatCodeTotal -> {
            if (!NumberUtils.isZero(vatCodeTotal.getRate(), 2)) {
                vatCodeTotal.setTax(GamaMoneyUtils.multipliedBy(vatCodeTotal.getAmount(), vatCodeTotal.getRate() / 100.0));
            }
        });
    }

    private <P extends IBaseDocPartSql & IDocPart & IId<Long>>
    void fixPartType(P part, Map<Long, PartType> partTypeMap) {
        if (Validators.isValid(part.getDocPart()) && part.getType() == null) {
            var partType = partTypeMap.get(part.getPartId());
            if (partType == null) {
                var p = dbServiceSQL.getById(PartSql.class, part.getPartId());
                if (p != null && p.getType() != null) {
                    partTypeMap.put(part.getPartId(), partType = p.getType());
                    part.setType(partType);
                }
            } else {
                part.setType(partType);
            }
        }
    }

    public <P extends IBaseDocPartSql & IDocPart & IId<Long>, E extends IId<Long> & EntitySql & IBaseCompany & IDocPartsSql<P>>
    E fixDocumentPartTypes(E document) {
        if (document == null || CollectionsHelper.isEmpty(document.getParts())) return document;
        long warehouseId = document instanceof InvoiceSql invoice ? invoice.getWarehouse().getId()
                : document instanceof PurchaseSql purchase ? purchase.getWarehouse().getId() : 0;
        Validators.checkArgument(warehouseId > 0, "No warehouse in document=" + document);
        Map<Long, PartType> partTypeMap = new HashMap<>();
        for (var part : document.getParts()) {
            fixPartType(part, partTypeMap);
        }
        return document;
    }

    public Document gpaisProducts(LocalDate dateFrom) {
        try {
            var gpaisSettings = Validators.checkNotNull(auth.getSettings().getGpais(), "No GPAIS settings");
            Validators.checkArgument(StringHelper.hasValue(gpaisSettings.registrationId()), "No GPAIS registration ID");
            Validators.checkArgument(StringHelper.hasValue(gpaisSettings.subjectCode()), "No GPAIS subject code");
            Validators.checkNotNull(gpaisSettings.registrationDate(), "No GPAIS registration date");

            Document document = XMLUtils.document();
            Element root = document.createElement("produktuSarasas");
            root.setAttribute("xmlns", "urn:x-gpais:vvs:produktai");
            root.setAttribute("xmlns:gpais", "urn:x-gpais:bendra");
            root.setAttribute("xmlns:kls", "urn:x-gpais:kls");
            document.appendChild(root);
            XMLUtils.createElement(document, root, "subjektas").setAttribute("kodas", gpaisSettings.subjectCode());
            var products = XMLUtils.createElement(document, root, "produktai");
            var registrationId = gpaisSettings.registrationId();
            var registrationDate = gpaisSettings.registrationDate();
            Stream<Tuple> stream;
            if (dateFrom == null) {
                //noinspection unchecked
                stream = entityManager.createNativeQuery("""
                                WITH R(part_id, first_date) AS (
                                	SELECT DISTINCT ON(ip.part_id) ip.part_id, d.date
                                	FROM invoice_parts ip
                                	JOIN documents d ON D.id = ip.parent_id
                                	WHERE d.company_id = :companyId AND d.finished = true
                                	ORDER BY ip.part_id, d.date
                                )
                                SELECT p.id AS id, p.sku AS sku, p.name AS name,
                                    CAST(R.first_date AS text) AS first_date,
                                    CAST(packaging AS text) packaging
                                FROM parts p
                                LEFT JOIN R ON R.part_id = p.id
                                WHERE p.company_id = :companyId
                                AND (p.archive IS NULL OR p.archive = false)
                                AND p.packaging IS NOT NULL
                                AND p.sku IS NOT NULL
                                """, Tuple.class)
                        .setParameter("companyId", auth.getCompanyId())
                        .getResultStream();
            } else {
                //noinspection unchecked
                stream = entityManager.createNativeQuery("""
                                WITH R(part_id, first_date) AS (
                                	SELECT DISTINCT ON(ip.part_id) ip.part_id, d.date
                                	FROM invoice_parts ip
                                	JOIN documents d ON D.id = ip.parent_id
                                	WHERE d.company_id = :companyId AND d.finished = true
                                	ORDER BY ip.part_id, d.date
                                )
                                SELECT p.id AS id, p.sku AS sku, p.name AS name,
                                    CAST(R.first_date AS text) AS first_date,
                                    CAST(packaging AS text) packaging
                                FROM parts p
                                LEFT JOIN R ON R.part_id = p.id
                                WHERE p.company_id = :companyId
                                AND p.updated_on >= :dateFrom
                                AND (p.archive IS NULL OR p.archive = false)
                                AND p.packaging IS NOT NULL
                                AND p.sku IS NOT NULL
                                """, Tuple.class)
                        .setParameter("companyId", auth.getCompanyId())
                        .setParameter("dateFrom", dateFrom)
                        .getResultStream();
            }
            stream.forEach(part -> {
                var dateTxt = part.get("first_date", String.class);
                var firstDate = dateTxt == null ? null : LocalDate.parse(dateTxt);
                var date = firstDate == null || registrationDate.isAfter(firstDate) ? registrationDate : firstDate;
                var prekinisVienetas = XMLUtils.createElement(document, products, "prekinisVienetas");
                XMLUtils.createElement(document, prekinisVienetas, "registracijosId", registrationId);
                XMLUtils.createElement(document, prekinisVienetas, "kodas", part.get("sku", String.class));
                XMLUtils.createElement(document, prekinisVienetas, "pavadinimas", part.get("name", String.class), 100);
                XMLUtils.createElement(document, prekinisVienetas, "pradetaTiektiNuo", date.toString());
                var packagingElement = XMLUtils.createElement(document, prekinisVienetas, "pakuotes");
                List<Packaging> packaging;
                try {
                    packaging = objectMapper.readValue(part.get("packaging", String.class), new TypeReference<>() {});
                } catch (JsonProcessingException e) {
                    throw new GamaException(e);
                }
                CollectionsHelper.streamOf(packaging)
                        .filter(pack -> pack != null && pack.type() != null && pack.code() != null &&
                                BigDecimalUtils.isPositive(pack.weight()))
                        .forEach(pack -> {
                            var p = XMLUtils.createElement(document, packagingElement, "pvPakuote");
                            XMLUtils.createElement(document, p, "pavadinimas", "Pakuotė " + part.get("id", BigInteger.class) + ' ' + pack.code());
                            XMLUtils.createElement(document, p, "kategorija", gpaisXMLCategory(pack.type()));
                            XMLUtils.createElement(document, p, "rusis", gpaisTypesMap().get(pack.code()));
                            XMLUtils.createElement(document, p, "kiekisPakuoteje", "1");
                            XMLUtils.createElement(document, p, "perdirbama", "true");
                            XMLUtils.createElement(document, p, "vienkartine", "true");
                            XMLUtils.createElement(document, p, "svoris", weightInTons(pack.weight()).toString());
                            XMLUtils.createElement(document, p, "pradetaTiektiNuo", date.toString());
                        });
            });
            return document;

        } catch (ParserConfigurationException e) {
            throw new GamaException(e);
        }
    }

    public Document gpaisRegistry(LocalDate dateFrom, LocalDate dateTo) {
        try {
            LocalDate startAccounting = auth.getSettings().getStartAccounting();
            LocalDate dateFromFixed = DateUtils.max(startAccounting, dateFrom);
            Validators.checkPeriod(dateFromFixed, dateTo, auth.getLanguage());

            var gpaisSettings = Validators.checkNotNull(auth.getSettings().getGpais(), "No GPAIS settings");
            Validators.checkArgument(StringHelper.hasValue(gpaisSettings.registrationId()), "No GPAIS registration ID");
            Validators.checkArgument(StringHelper.hasValue(gpaisSettings.subjectCode()), "No GPAIS subject code");
            Validators.checkNotNull(gpaisSettings.registrationDate(), "No GPAIS registration date");

            Document document = XMLUtils.document();
            Element root = document.createElement("irasuSarasas");
            root.setAttribute("xmlns", "urn:x-gpais:vvs:zurnalas");
            root.setAttribute("xmlns:gpais", "urn:x-gpais:bendra");
            root.setAttribute("xmlns:kls", "urn:x-gpais:kls");
            document.appendChild(root);
            XMLUtils.createElement(document, root, "subjektas").setAttribute("kodas", gpaisSettings.subjectCode());
            var records = XMLUtils.createElement(document, root, "irasai");
            var registracijosId = gpaisSettings.registrationId();

            var q = entityManager.createNativeQuery(StringHelper.loadSQLFromFile("gpais", "registry.sql"), Tuple.class);
            q.setParameter("companyId", auth.getCompanyId());
            q.setParameter("startAccounting", startAccounting.minusDays(1));
            q.setParameter("dateFrom", dateFromFixed);
            q.setParameter("dateTo", dateTo.plusDays(1));

            var index = new MutableInt();
            //noinspection unchecked
            ((Stream<Tuple>) q.getResultStream()).forEach(part -> {
                var sku = part.get("sku", String.class);

                var quantity = part.get("export_quantity", BigDecimal.class);
                if (BigDecimalUtils.isNegative(quantity)) {
                    record(document, records, index.incrementAndGet(), registracijosId, sku, dateFromFixed, CL118_EXPORT, quantity.negate());
                }

                quantity = part.get("wholesale_quantity", BigDecimal.class);
                if (BigDecimalUtils.isNegative(quantity)) {
                    record(document, records, index.incrementAndGet(), registracijosId, sku, dateFromFixed, CL118_WHOLESALE, quantity.negate());
                }

                quantity = part.get("retail_quantity", BigDecimal.class);
                if (BigDecimalUtils.isNegative(quantity)) {
                    record(document, records, index.incrementAndGet(), registracijosId, sku, dateFromFixed, CL118_RETAIL, quantity.negate());
                }
            });
            return document;

        } catch (ParserConfigurationException e) {
            throw new GamaException(e);
        }
    }

    private void record(Document document, Element records, int id, String registrationId, String sku, LocalDate date, String cl118, BigDecimal quantity) {
        var record = XMLUtils.createElement(document, records, "irasas");
        record.setAttribute("id", String.valueOf(id));
        XMLUtils.createElement(document, record, "registracijosId", registrationId);
        XMLUtils.createElement(document, record, "produktoKodas", sku);
        XMLUtils.createElement(document, record, "tiekimoRinkaiData", date.toString());
        XMLUtils.createElement(document, record, "gavimoBudas", CL140_IMPORT);
        XMLUtils.createElement(document, record, "veiklosBudas", cl118);
        XMLUtils.createElement(document, record, "kiekis", quantity.toString());
    }

    private BigDecimal weightInTons(BigDecimal weight) {
        var w = weight.divide(BigDecimal.valueOf(1000), Math.min(weight.scale() + 3, 6), RoundingMode.HALF_UP);
        return BigDecimalUtils.isZero(w) ? BigDecimal.valueOf(1, 6) : w;
    }

    private static final String CL140_IMPORT = "CL140:1:2017-02-22";
    // private static final String CL140_PRODUCTION = "CL140:2:2017-02-22";
    // private static final String CL140_RETURN = "CL140:3:2017-02-22";

    private static final String CL118_WHOLESALE = "CL118:DP:2016-12-07";  // if local sell and TaxpayerType.LEGAL
    private static final String CL118_RETAIL = "CL118:MP:2016-12-07";     // if local sell and TaxpayerType.PHYSICAL or FARMER
    private static final String CL118_EXPORT = "CL118:EV:2016-12-07";     // if export from the country
    // private static final String CL118_SELF = "CL118:SS:2016-12-07";

    private String gpaisXMLCategory(PackagingType type) {
        return switch (type) {
            case PRIMARY -> "CL138:1:2014-01-01";
            case SECONDARY -> "CL138:2:2014-01-01";
            case TERTIARY -> "CL138:3:2014-01-01";
        };
    }

    private static Document _gpaisTypes;
    private static Document gpaisTypesDoc() {
        var localRef = _gpaisTypes;
        if (localRef == null) {
            synchronized (InventoryService.class) {
                localRef = _gpaisTypes;
                if (localRef == null) {
                    var fileName = "gpais" + File.separator + "gpais-klasifikatoriai.xsd";
                    try (var is = StringHelper.class.getClassLoader().getResourceAsStream(fileName)) {
                        Document document = XMLUtils.document(is);
                        document.getDocumentElement().normalize();
                        _gpaisTypes = localRef = document;
                    } catch (Exception e) {
                        throw new GamaException(e.getMessage(), e);
                    }
                }
            }
        }
        return localRef;
    }

    private static Map<String, String> _gpaisTypesMap;
    private static Map<String, String> gpaisTypesMap() {
        var localRef = _gpaisTypesMap;
        if (localRef == null) {
            synchronized (InventoryService.class) {
                localRef = _gpaisTypesMap;
                if (localRef == null) {
                    try {
                        var doc = gpaisTypesDoc();
                        var map = new HashMap<String, String>();
                        NodeList simpleTypeList = Validators.checkNotNull(doc.getElementsByTagName("simpleType"), "XML error - no simpleType");
                        for (int i = 0; i < simpleTypeList.getLength(); i++) {
                            Element element = (Element) simpleTypeList.item(i);
                            if ("CL130".equals(element.getAttribute("name"))) {
                                NodeList enumerations = element.getElementsByTagName("enumeration");
                                for (int k = 0; k < enumerations.getLength(); k++) {
                                    Element enu = (Element) enumerations.item(k);
                                    var xmlCode = enu.getAttribute("value");
                                    var code = xmlCode.split(":")[1];
                                    if (code.length() == 3) {
                                        map.put(code, xmlCode);
                                    }
                                }
                                break;
                            }
                        }
                        _gpaisTypesMap = localRef = map;
                    } catch (Exception e) {
                        throw new GamaException(e.getMessage(), e);
                    }
                }
            }
        }
        return localRef;
    }
}

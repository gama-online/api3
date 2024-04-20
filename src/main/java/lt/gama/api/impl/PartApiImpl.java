package lt.gama.api.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.IdRequest;
import lt.gama.api.request.InventoryBalanceRequest;
import lt.gama.api.request.PageRequest;
import lt.gama.api.response.PageResponse;
import lt.gama.api.service.PartApi;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.InventoryUtils;
import lt.gama.helpers.StringHelper;
import lt.gama.helpers.Validators;
import lt.gama.model.dto.entities.ManufacturerDto;
import lt.gama.model.dto.entities.PartDto;
import lt.gama.model.dto.entities.RecipeDto;
import lt.gama.model.mappers.ManufacturerSqlMapper;
import lt.gama.model.mappers.PartSqlMapper;
import lt.gama.model.mappers.RecipeSqlMapper;
import lt.gama.model.sql.entities.ManufacturerSql;
import lt.gama.model.sql.entities.PartSql;
import lt.gama.model.sql.entities.RecipeSql;
import lt.gama.model.sql.system.CountryVatRateSql;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.service.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@RestController
public class PartApiImpl implements PartApi {

    private static final Logger log = LoggerFactory.getLogger(PartApi.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final InventoryService inventoryService;
    private final Auth auth;
    private final DBServiceSQL dbServiceSQL;
    private final ManufacturerSqlMapper manufacturerSqlMapper;
    private final PartSqlMapper partSqlMapper;
    private final RecipeSqlMapper recipeSqlMapper;
    private final InventoryCheckService inventoryCheckService;
    private final TradeService tradeService;
    private final APIResultService apiResultService;

    public PartApiImpl(InventoryService inventoryService, Auth auth, DBServiceSQL dbServiceSQL, ManufacturerSqlMapper manufacturerSqlMapper, PartSqlMapper partSqlMapper, RecipeSqlMapper recipeSqlMapper, InventoryCheckService inventoryCheckService, TradeService tradeService, APIResultService apiResultService) {
        this.inventoryService = inventoryService;
        this.auth = auth;
        this.dbServiceSQL = dbServiceSQL;
        this.manufacturerSqlMapper = manufacturerSqlMapper;
        this.partSqlMapper = partSqlMapper;
        this.recipeSqlMapper = recipeSqlMapper;
        this.inventoryCheckService = inventoryCheckService;
        this.tradeService = tradeService;
        this.apiResultService = apiResultService;
    }


    @Override
    public APIResult<PageResponse<PartDto, Void>> listPart(PageRequest request) throws GamaApiException {
        return apiResultService.result(() -> inventoryService.listPart(request));
    }

    @Override
    public APIResult<PartDto> savePart(final PartDto request) throws GamaApiException {
        return apiResultService.result(() -> inventoryService.savePart(request, ()-> auth.getPermissions()));
    }

    @Override
    public APIResult<PartDto> getPart(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> partSqlMapper.toDto(dbServiceSQL.getById(PartSql.class, request.getId())));
    }

    @Override
    public APIResult<Void> deletePart(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> dbServiceSQL.deleteById(PartSql.class, request.getId()));
    }

    @Override
    public APIResult<PartDto> undeletePart(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> partSqlMapper.toDto(dbServiceSQL.undeleteById(PartSql.class, request.getId())));
    }

    @Override
    public APIResult<PartDto> getPartRemainder(InventoryBalanceRequest request) throws GamaApiException {
        return apiResultService.result(() -> inventoryService.getPartRemainder(request));
    }

    @Override
    public APIResult<PageResponse<RecipeDto, Void>> listRecipe(PageRequest request) throws GamaApiException {
        return apiResultService.result(() -> dbServiceSQL.queryPage(request, RecipeSql.class, RecipeSql.GRAPH_ALL, recipeSqlMapper,
                () -> allQueryRecipe(request),
                () -> countQueryRecipe(request),
                (resp) -> dataQueryRecipe(request, resp)));
    }

    private Query allQueryRecipe(PageRequest ignoredRequest) {
        Query query = entityManager.createQuery(
                "SELECT r FROM " + RecipeSql.class.getName() + " r" +
                        " WHERE companyId = :companyId" +
                        " AND (r.archive IS null OR r.archive = false)" +
                        " AND (r.hidden IS null OR r.hidden = false)" +
                        " ORDER BY r.name, r.id");
        query.setParameter("companyId", auth.getCompanyId());
        return query;
    }

    private Integer countQueryRecipe(PageRequest request) {
        Map<String, Object> params = new HashMap<>();
        StringJoiner sj = new StringJoiner(" ");
        sj.add("SELECT COUNT(DISTINCT r.id) FROM recipe r");
        makeQueryRecipe(request, sj, params);

        Query query = entityManager.createNativeQuery(sj.toString());
        params.forEach(query::setParameter);

        return ((BigInteger) query.getSingleResult()).intValue();
    }

    private void makeQueryRecipe(PageRequest request, StringJoiner sj, Map<String, Object> params) {
        sj.add("WHERE r.company_id = :companyId");
        params.put("companyId", auth.getCompanyId());
        sj.add("AND (r.archive IS null OR r.archive = false)");
        sj.add("AND (r.hidden IS null OR r.hidden = false)");
        if (StringHelper.hasValue(request.getFilter())) {
            sj.add("AND (");
            sj.add("trim(unaccent(r.name)) ILIKE :filter");
            sj.add("OR trim(regexp_replace(unaccent(r.name), '[^[:alnum:]]+', ' ', 'g')) ILIKE :filter");
            sj.add(")");
            params.put("filter", "%" + StringUtils.stripAccents(request.getFilter()).toLowerCase() + "%");
        }
    }

    private Query idsPageQueryRecipe(PageRequest request) {
        Map<String, Object> params = new HashMap<>();
        StringJoiner sj = new StringJoiner(" ");
        sj.add("SELECT DISTINCT r.id AS id, r.name FROM recipe r");
        makeQueryRecipe(request, sj, params);
        sj.add("ORDER BY r.name, r.id");

        Query query = entityManager.createNativeQuery(sj.toString(), Tuple.class);
        params.forEach(query::setParameter);
        return query;
    }

    private Query dataQueryRecipe(PageRequest request, PageResponse<RecipeDto, ?> response) {
        int cursor = request.getCursor();

        @SuppressWarnings("unchecked")
        List<Tuple> listId = idsPageQueryRecipe(request)
                .setFirstResult(cursor)
                .setMaxResults(request.getPageSize() + 1)
                .getResultList();

        response.setResponseValues(request, cursor, listId.size());

        return listId.isEmpty() ? null :
                entityManager.createQuery(
                                "SELECT r FROM " + RecipeSql.class.getName() + " r" +
                                        " WHERE r.id IN :ids" +
                                        " ORDER BY r.name, r.id",
                                RecipeSql.class)
                        .setParameter("ids",
                                listId.stream()
                                        .limit(request.getPageSize())
                                        .map(x -> x.get("id", Number.class).longValue())
                                        .collect(Collectors.toList()));
    }

    @Override
    public APIResult<RecipeDto> saveRecipe(RecipeDto request) throws GamaApiException {
        return apiResultService.result(() -> {
            RecipeSql entity = recipeSqlMapper.toEntity(request);
            inventoryCheckService.checkPartUuids(entity.getPartsFrom(), false);
            inventoryCheckService.checkPartUuids(entity.getPartsTo(), false);
            InventoryUtils.assignSortOrder(entity.getPartsFrom());
            InventoryUtils.assignSortOrder(entity.getPartsTo());

            if (entity.getId() == null) {
                InventoryUtils.clearPartId(entity.getPartsFrom());
                InventoryUtils.clearPartId(entity.getPartsTo());
            }
            return recipeSqlMapper.toDto(dbServiceSQL.saveEntityInCompany(entity));
        });
    }

    @Override
    public APIResult<RecipeDto> getRecipe(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> recipeSqlMapper.toDto(dbServiceSQL.getById(RecipeSql.class, request.getId())));
    }

    @Override
    public APIResult<Void> deleteRecipe(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> dbServiceSQL.deleteById(RecipeSql.class, request.getId()));
    }

    /*
     * VAT rates
     */

    @Override
    public APIResult<CountryVatRateSql> getVatRate() throws GamaApiException {
        return apiResultService.result(() -> {
            CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
            return dbServiceSQL.getById(CountryVatRateSql.class, companySettings.getCountry());
        });
    }

    /*
     * Manufacturer
     */

    @Override
    public APIResult<PageResponse<ManufacturerDto, Void>> listManufacturer(PageRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                dbServiceSQL.queryPage(request, ManufacturerSql.class, null,
                        manufacturerSqlMapper,
                        () -> allQueryManufacturer(request),
                        () -> countQueryManufacturer(request),
                        resp -> dataQueryManufacturer(request, resp)));
    }

    private Query allQueryManufacturer(PageRequest request) {
        Map<String, Object> params = new HashMap<>();
        StringJoiner sj = new StringJoiner(" ");
        sj.add("SELECT a.* FROM manufacturer a");
        makeQueryManufacturer(request, sj, params);
        sj.add("ORDER BY a.name, a.id");
        Query query = entityManager.createNativeQuery(sj.toString(), ManufacturerSql.class);
        params.forEach(query::setParameter);

        return query;
    }

    private Integer countQueryManufacturer(PageRequest request) {
        Map<String, Object> params = new HashMap<>();
        StringJoiner sj = new StringJoiner(" ");
        sj.add("SELECT COUNT(DISTINCT a.id) FROM manufacturer a");
        makeQueryManufacturer(request, sj, params);

        Query query = entityManager.createNativeQuery(sj.toString());
        params.forEach(query::setParameter);

        return ((BigInteger) query.getSingleResult()).intValue();
    }

    private void makeQueryManufacturer(PageRequest request, StringJoiner sj, Map<String, Object> params) {
        sj.add("WHERE company_id = :companyId");
        params.put("companyId", auth.getCompanyId());
        sj.add("AND (a.archive IS null OR a.archive = false)");
        sj.add("AND (a.hidden IS null OR a.hidden = false)");
        if (StringHelper.hasValue(request.getFilter())) {
            sj.add("AND (");
            sj.add("trim(unaccent(a.name)) ILIKE :filter");
            sj.add("OR trim(regexp_replace(unaccent(a.name), '[^[:alnum:]]+', ' ', 'g')) ILIKE :filter");
            sj.add("OR trim(unaccent(a.description)) ILIKE :filter");
            sj.add("OR trim(regexp_replace(unaccent(a.description), '[^[:alnum:]]+', ' ', 'g')) ILIKE :filter");
            sj.add(")");
            params.put("filter", "%" + StringUtils.stripAccents(request.getFilter().trim()).toLowerCase() + "%");
        }
    }

    private Query idsPageQueryManufacturer(PageRequest request) {
        Map<String, Object> params = new HashMap<>();
        StringJoiner sj = new StringJoiner(" ");
        sj.add("SELECT DISTINCT a.id AS id, a.name FROM manufacturer a");
        makeQueryManufacturer(request, sj, params);
        sj.add("ORDER BY a.name, a.id");

        Query query = entityManager.createNativeQuery(sj.toString(), Tuple.class);
        params.forEach(query::setParameter);
        return query;
    }

    private Query dataQueryManufacturer(PageRequest request, PageResponse<ManufacturerDto, ?> response) {
        int cursor = request.getCursor();

        @SuppressWarnings("unchecked")
        List<Tuple> listId = idsPageQueryManufacturer(request)
                .setFirstResult(cursor)
                .setMaxResults(request.getPageSize() + 1)
                .getResultList();

        response.setResponseValues(request, cursor, listId.size());

        return listId.isEmpty() ? null :
                entityManager.createQuery(
                                "SELECT a FROM " + ManufacturerSql.class.getName() + " a" +
                                        " WHERE a.id IN :ids" +
                                        " ORDER BY a.name, a.id",
                                ManufacturerSql.class)
                        .setParameter("ids",
                                listId.stream()
                                        .limit(request.getPageSize())
                                        .map(x -> x.get("id", Number.class).longValue())
                                        .collect(Collectors.toList()));
    }

    @Override
    public APIResult<ManufacturerDto> saveManufacturer(ManufacturerDto request) throws GamaApiException {
        return apiResultService.result(() ->
                manufacturerSqlMapper.toDto(dbServiceSQL.saveEntityInCompany(manufacturerSqlMapper.toEntity(request))));
    }

    @Override
    public APIResult<ManufacturerDto> getManufacturer(IdRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                manufacturerSqlMapper.toDto(dbServiceSQL.getById(ManufacturerSql.class, request.getId())));
    }

    @Override
    public APIResult<Void> deleteManufacturer(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> dbServiceSQL.deleteById(ManufacturerSql.class, request.getId()));
    }


//    /*
//     * price-list
//     */
//
//    @Override
//    public APIResult<PageResponse<Pl, Void>> listPricelist(PageRequest request) throws GamaApiException {
//        return apiResultService.result(() -> dbService.list(request, Pl.class));
//    }
//
//    @Override
//    public APIResult<Pl> savePricelist(Pl request) throws GamaApiException {
//        return apiResultService.result(() -> dbService.saveNow(request));
//    }
//
//    @Override
//    public APIResult<Pl> getPricelist(IdRequest request) throws GamaApiException {
//        return apiResultService.result(() -> dbService.getEntityViewById(request.getId(), Pl.class));
//    }
//
//    @Override
//    public APIResult<Void> deletePricelist(IdRequest request) throws GamaApiException {
//        return apiResultService.result(() -> dbService.deleteById(request.getId(), Pl.class));
//    }
//
//    /*
//     * Pricelist part methods:
//     */
//
//    @Override
//    public APIResult<PageResponse<PlPart, Void>> listPricelistPart(PageRequest request) throws GamaApiException {
//        return apiResultService.result(() -> {
//            if (request.getParentId() == null || request.getParentId() == 0)
//                throw new GamaException(TranslationService.getInstance().translate(TranslationService.DB.NoParentId, auth.getLanguage()));
//            return dbService.list(request, PlPart.class, Key.create(Pl.class, request.getParentId()));
//        });
//    }
//
//    @Override
//    public APIResult<PlPart> savePricelistPart(PlPart request) throws GamaApiException {
//        return apiResultService.result(() -> {
//            if (request.getParentId() == null || request.getParentId() == 0)
//                throw new GamaException(TranslationService.getInstance().translate(TranslationService.DB.NoParentId, auth.getLanguage()));
//            request.setParent(Key.create(Pl.class, request.getParentId()));
//
//            // always update parent pricelist info
//            long partId = request.getId();
//            long plId = request.getParentId();
//
//            Pl pl = dbService.getAndCheck(Pl.class, plId);
//            request.setPl(new DocPl(pl));
//
//            inventoryService.preparePeriodLists(request.getPrice());
//
//            PlPart entity = dbService.getAndCheckNullable(PlPart.class, partId, Key.create(Pl.class, plId));
//
//            boolean needUpdateActual = entity == null || !PricelistUtils.isPeriodsEquals(entity.getPrice(), request.getPrice());
//
//            dbService.saveEntityInCompanyNow(request);
//
//            if (needUpdateActual) inventoryService.updateActualPriceFromPart(request.getPl().getId(), request.getId());
//
//            return request;
//        });
//    }
//
//    @Override
//    public APIResult<PlPart> getPricelistPart(IdRequest request) throws GamaApiException {
//        return apiResultService.result(() -> {
//            if (request.getParentId() == null || request.getParentId() == 0)
//                throw new GamaException(TranslationService.getInstance().translate(TranslationService.DB.NoParentId, auth.getLanguage()));
//            return dbService.getEntityViewById(request.getId(), PlPart.class, Key.create(Pl.class, request.getParentId()));
//        });
//    }
//
//    @Override
//    public APIResult<Void> deletePricelistPart(IdRequest request) throws GamaApiException {
//        return apiResultService.result(() -> {
//            if (request.getParentId() == null || request.getParentId() == 0)
//                throw new GamaException(TranslationService.getInstance().translate(TranslationService.DB.NoParentId, auth.getLanguage()));
//
//            long partId = request.getId();
//            long plId = request.getParentId();
//
//            inventoryService.deleteActualPrice(plId, partId);
//
//            dbService.deleteById(partId, PlPart.class, Key.create(Pl.class, plId));
//        });
//    }
//
//    /*
//     * Pricelist discount methods:
//     */
//
//    @Override
//    public APIResult<PageResponse<PlDiscount, Void>> listPricelistDiscount(PageRequest request) throws GamaApiException {
//        return apiResultService.result(() -> {
//            if (request.getParentId() == null || request.getParentId() == 0)
//                throw new GamaException(TranslationService.getInstance().translate(TranslationService.DB.NoParentId, auth.getLanguage()));
//            return dbService.list(request, PlDiscount.class, Key.create(Pl.class, request.getParentId()));
//        });
//    }
//
//    @Override
//    public APIResult<PlDiscount> savePricelistDiscount(PlDiscount request) throws GamaApiException {
//        return apiResultService.result(() -> {
//            if (request.getParentId() == null || request.getParentId() == 0) throw new GamaException(TranslationService.getInstance().translate(TranslationService.DB.NoParentId, auth.getLanguage()));
//            request.setParent(Key.create(Pl.class, request.getParentId()));
//
//            // always update parent pricelist info
//            long plId = request.getParentId();
//
//            Pl pl = dbService.getAndCheck(Pl.class, plId);
//            request.setPl(new DocPl(pl));
//            inventoryService.preparePeriodLists(request.getDiscount());
//
//            //check if record with key (part + part label + manufacturer) exists already
//            List<Key<Search>> searchKeys = dbService.querySearchInCompany(PlDiscount.class, Key.create(Pl.class, plId), true)
//                    .filter(EntityUtils.SEARCH, EntityUtils.makeCustomIndexFromValues(CustomSearchType.PART_ID,
//                            !EntityUtils.isIdNull(request.getPart()) ? request.getPart().getId() : null))
//                    .filter(EntityUtils.SEARCH, EntityUtils.makeCustomIndexFromValues(CustomSearchType.PL_PART_LABEL,
//                            StringHelper.hasValue(request.getPartLabel()) ? request.getPartLabel().trim() : null))
//                    .filter(EntityUtils.SEARCH, EntityUtils.makeCustomIndexFromValues(CustomSearchType.MANUFACTURER,
//                            !EntityUtils.isIdNull(request.getManufacturer()) ? request.getManufacturer().getId() : null))
//                    .keys().list();
//
//            if (searchKeys != null && searchKeys.size() > 0) {
//                if (request.getId() == null || searchKeys.size() > 1 || !request.getId().equals(searchKeys.get(0).getParent().getId())) {
//                    throw new GamaException(TranslationService.getInstance().translate(TranslationService.INVENTORY.DiscountRecordExistsAlready, auth.getLanguage()));
//                }
//            }
//
//            PlDiscount entity = request.getId() == null ? null : dbService.getAndCheckNullable(PlDiscount.class, request.getId(), Key.create(Pl.class, plId));
//
//            boolean needUpdateActual = entity == null ||
//                    !EntityUtils.isIdEquals(entity.getPart(), request.getPart()) ||
//                    !EntityUtils.isIdEquals(entity.getManufacturer(), request.getManufacturer()) ||
//                    !StringHelper.isEquals(entity.getPartLabel(), request.getPartLabel()) ||
//                    !PricelistUtils.isPeriodsEquals(entity.getDiscount(), request.getDiscount());
//
//            dbService.saveEntityInCompanyNow(request);
//
//            request.setTaskId(null);
//            if (needUpdateActual) request.setTaskId(inventoryService.updateActualPriceFromDiscount(request.getPl().getId(), request.getId()));
//
//            return request;
//        });
//    }
//
//    @Override
//    public APIResult<PlDiscount> getPricelistDiscount(IdRequest request) throws GamaApiException {
//        return apiResultService.result(() -> {
//            if (request.getParentId() == null || request.getParentId() == 0)
//                throw new GamaException(TranslationService.getInstance().translate(TranslationService.DB.NoParentId, auth.getLanguage()));
//            return dbService.getEntityViewById(request.getId(), PlDiscount.class, Key.create(Pl.class, request.getParentId()));
//        });
//    }
//
//    @Override
//    public APIResult<String> deletePricelistDiscount(IdRequest request) throws GamaApiException {
//        return apiResultService.result(() -> {
//            if (request.getParentId() == null || request.getParentId() == 0)
//                throw new GamaException(TranslationService.getInstance().translate(TranslationService.DB.NoParentId, auth.getLanguage()));
//
//            long plId = request.getParentId();
//
//            dbService.deleteById(request.getId(), PlDiscount.class, Key.create(Pl.class, plId));
//            return inventoryService.updateActualPriceFromDiscount(plId, request.getId());
//        });
//    }
//
//    /*
//     * Others
//     */
//
//    @Override
//    public APIResult<PageResponse<PlPart, Void>> listPartPricelist(PageRequest request) throws GamaApiException {
//        return apiResultService.result(() -> dbService.list(request, PlPart.class));
//    }
//
//    @Override
//    public APIResult<List<PlPrice>> actualPrice(IdRequest request) throws GamaApiException {
//        return apiResultService.result(() -> {
//            if (request.getParentId() == null || request.getParentId() == 0)
//                throw new GamaException(TranslationService.getInstance().translate(TranslationService.DB.NoParentId, auth.getLanguage()));
//            return inventoryService.calcActualPrice(request.getParentId(), request.getId(), null);
//        });
//    }
//
//    @Override
//    public APIResult<PageResponse<PlActual, Void>> listPricelistActual(PageRequest request) throws GamaApiException {
//        return apiResultService.result(() -> {
//            if (request.getParentId() == null || request.getParentId() == 0)
//                throw new GamaException(TranslationService.getInstance().translate(TranslationService.DB.NoParentId, auth.getLanguage()));
//            return dbService.list(request, PlActual.class, Key.create(Pl.class, request.getParentId()));
//        });
//    }
//
//    @Override
//    public APIResult<PlActual> getPricelistActual(IdRequest request) throws GamaApiException {
//        return apiResultService.result(() -> {
//            if (request.getParentId() == null || request.getParentId() == 0)
//                throw new GamaException(TranslationService.getInstance().translate(TranslationService.DB.NoParentId, auth.getLanguage()));
//            return dbService.getEntityViewById(request.getId(), PlActual.class, Key.create(Pl.class, request.getParentId()));
//        });
//    }

    @Override
    public APIResult<String> syncPartTask() throws GamaApiException {
        return apiResultService.result(() -> tradeService.syncTask());
    }

    @Override
    public APIResult<String> syncWarehousePartsTask() throws GamaApiException {
        return apiResultService.result(() -> tradeService.syncWarehousePartsTask());
    }
}
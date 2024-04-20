package lt.gama.api.impl.v4;

import com.google.common.base.Objects;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.PageRequest;
import lt.gama.api.request.PageRequestCondition;
import lt.gama.api.response.PageResponse;
import lt.gama.api.service.v4.PartApi;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.*;
import lt.gama.model.dto.entities.GLAccountDto;
import lt.gama.model.dto.entities.PartApiDto;
import lt.gama.model.mappers.PartApiSqlMapper;
import lt.gama.model.sql.entities.PartSql;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.model.type.enums.CustomSearchType;
import lt.gama.model.type.enums.PartType;
import lt.gama.model.type.gl.GLDC;
import lt.gama.model.type.gl.GLOperationAccount;
import lt.gama.model.type.part.VATRate;
import lt.gama.service.APIResultService;
import lt.gama.service.DBServiceSQL;
import lt.gama.service.GLService;
import lt.gama.service.TranslationService;
import lt.gama.service.ex.rt.GamaNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController("PartApiImplv4")
public class PartApiImpl implements PartApi {

    @PersistenceContext
    private EntityManager entityManager;

    private final Auth auth;
    private final GLService glServiceSQL;
    private final PartApiSqlMapper partApiSqlMapper;
    private final DBServiceSQL dbServiceSQL;
    private final APIResultService apiResultService;

    public PartApiImpl(Auth auth, GLService glServiceSQL, PartApiSqlMapper partApiSqlMapper, DBServiceSQL dbServiceSQL, APIResultService apiResultService) {
        this.auth = auth;
        this.glServiceSQL = glServiceSQL;
        this.partApiSqlMapper = partApiSqlMapper;
        this.dbServiceSQL = dbServiceSQL;
        this.apiResultService = apiResultService;
    }


    @Override
    public PageResponse<PartApiDto, Void> list(Integer cursor, int pageSize) throws GamaApiException {
        return apiResultService.execute(() -> {
            Validators.checkArgument(pageSize > 0 && pageSize <= PageRequest.MAX_PAGE_SIZE,
                    MessageFormat.format(
                            TranslationService.getInstance().translate(TranslationService.VALIDATORS.InvalidPageSize, auth.getLanguage()),
                            pageSize, PageRequest.MAX_PAGE_SIZE));

            PageRequest pageRequest = new PageRequest();
            pageRequest.setPageSize(pageSize);
            pageRequest.setCursor(cursor);
            pageRequest.setOrder("mainIndex");

            return dbServiceSQL.queryPage(pageRequest, PartSql.class, null, partApiSqlMapper,
                    () -> allQueryPart(pageRequest),
                    () -> countQueryPart(pageRequest),
                    (resp) -> dataQueryPart(pageRequest, resp));
        });
    }

    private Query allQueryPart(PageRequest ignoredRequest) {
        Query query = entityManager.createQuery(
                "SELECT p FROM " + PartSql.class.getName() + " p" +
                        " WHERE companyId = :companyId" +
                        " AND (p.archive IS null OR p.archive = false)" +
                        " AND (p.hidden IS null OR p.hidden = false)" +
                        " ORDER BY p.name, p.id");
        query.setParameter("companyId", auth.getCompanyId());
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

    private void makeQueryPart(PageRequest request, StringJoiner sj, Map<String, Object> params) {
        sj.add("WHERE p.company_id = :companyId");
        params.put("companyId", auth.getCompanyId());
        sj.add("AND (p.archive IS null OR p.archive = false)");
        sj.add("AND (p.hidden IS null OR p.hidden = false)");
        String sku = (String) PageRequestUtils.getFieldValue(request.getConditions(), CustomSearchType.PART_SKU);
        if (StringHelper.hasValue(sku)) {
            sj.add("AND unaccent(trim(p.sku)) ILIKE :sku");
            params.put("sku", StringUtils.stripAccents(sku.toLowerCase().trim()));
        }
        if (StringHelper.hasValue(request.getFilter())) {
            String filter = StringUtils.stripAccents(request.getFilter()).toLowerCase().trim();
            sj.add("AND (");
            sj.add("trim(unaccent(sku)) ILIKE :filterSku");
            sj.add("OR trim(unaccent(p.name)) ILIKE :filter");
            sj.add("OR trim(regexp_replace(unaccent(p.name), '[^[:alnum:]]+', ' ', 'g')) ILIKE :filter");
            sj.add(")");
            params.put("filter", "%" + filter + "%");
            params.put("filterSku", (filter.charAt(0) == '\"' ? filter.substring(1) : filter) + "%");
        }
    }

    private Query idsPageQueryPart(PageRequest request) {
        Map<String, Object> params = new HashMap<>();
        StringJoiner sj = new StringJoiner(" ");
        sj.add("SELECT DISTINCT p.id AS id, p.name FROM parts p");
        makeQueryPart(request, sj, params);
        sj.add("ORDER BY p.name, p.id");

        Query query = entityManager.createNativeQuery(sj.toString(), Tuple.class);
        params.forEach(query::setParameter);
        return query;
    }

    private Query dataQueryPart(PageRequest request, PageResponse<PartApiDto, ?> response) {
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
                                        " WHERE p.id IN :ids" +
                                        " ORDER BY p.name, p.id",
                                PartSql.class)
                        .setParameter("ids",
                                listId.stream()
                                        .limit(request.getPageSize())
                                        .map(x -> x.get("id", Number.class).longValue())
                                        .collect(Collectors.toList()));
    }

    @Override
    public PartApiDto get(long id) throws GamaApiException {
        return apiResultService.execute(() -> {
            PartApiDto entity = partApiSqlMapper.toDto(dbServiceSQL.getAndCheckNullable(PartSql.class, id, PartSql.GRAPH_ALL));
            if (entity == null) {
                throw new GamaNotFoundException(MessageFormat.format(
                        TranslationService.getInstance().translate(TranslationService.INVENTORY.NoPart, auth.getLanguage()),
                        id));
            }
            return entity;
        });
    }

    @Override
    public PageResponse<PartApiDto, Void> findBy(String sku, String filter, Integer cursor, int pageSize) throws GamaApiException {
        return apiResultService.execute(() -> {
            Validators.checkArgument(StringHelper.hasValue(sku) || StringHelper.hasValue(filter),
                    TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoPartSku, auth.getLanguage()) + ", " +
                            TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoFilter, auth.getLanguage())
            );
            Validators.checkArgument(pageSize > 0 && pageSize <= PageRequest.MAX_PAGE_SIZE,
                    MessageFormat.format(
                            TranslationService.getInstance().translate(TranslationService.VALIDATORS.InvalidPageSize, auth.getLanguage()),
                            pageSize, PageRequest.MAX_PAGE_SIZE));

            PageRequest pageRequest = new PageRequest();
            pageRequest.setPageSize(pageSize);
            pageRequest.setCursor(cursor);
            pageRequest.setOrder("mainIndex");
            pageRequest.setConditions(new ArrayList<>());

            if (StringHelper.hasValue(sku)) pageRequest.getConditions().add(new PageRequestCondition(CustomSearchType.PART_SKU, sku));
            if (StringHelper.hasValue(filter)) pageRequest.setFilter(filter);

            return dbServiceSQL.queryPage(pageRequest, PartSql.class, null, partApiSqlMapper,
                    () -> allQueryPart(pageRequest),
                    () -> countQueryPart(pageRequest),
                    (resp) -> dataQueryPart(pageRequest, resp));
        });
    }

    private GLAccountDto getGLAccount(String number) {
        GLAccountDto glAccount = glServiceSQL.getAccount(number);
        if (glAccount == null) {
            throw new GamaNotFoundException(MessageFormat.format(
                    TranslationService.getInstance().translate(TranslationService.GL.NoGLAccountWithNumber, auth.getLanguage()),
                    number));
        }
        return glAccount;
    }

    /**
     * Update entity G.L. accounts (assets, income and expense) info
     * @param entity Part entity
     * @param companySettings company settings
     * @return true if entity was updated
     */
    private boolean updateGLAccounts(PartApiDto entity, CompanySettings companySettings) {
        if (companySettings.isDisableGL()) return false;

        boolean updated = false;

        // Asset
        if (!Validators.isValid(entity.getAccountAsset())) {
            if (entity.getType() != PartType.SERVICE) {
                if (!Objects.equal(entity.getAccountAsset(), companySettings.getGl().getProductAsset())) {
                    entity.setAccountAsset(companySettings.getGl().getProductAsset());
                    updated = true;
                }
            }
        }
        // Income
        if (!Validators.isValidDebit(entity.getGlIncome()) && !Validators.isValidCredit(entity.getGlIncome())) {
            GLDC gldc;
            if (entity.getType() == PartType.SERVICE) {
                gldc = new GLDC(companySettings.getGl().getServiceIncome());
            } else {
                gldc = new GLDC(companySettings.getGl().getProductIncome());
            }
            if (!Objects.equal(entity.getGlIncome(), gldc)) {
                entity.setGlIncome(gldc);
                updated = true;
            }
        }
        // Expense
        if (!Validators.isValidDebit(entity.getGlIncome()) && !Validators.isValidCredit(entity.getGlIncome())) {
            GLDC gldc;
            if (entity.getType() == PartType.SERVICE) {
                gldc = new GLDC(companySettings.getGl().getServiceExpense());
            } else {
                gldc = new GLDC(companySettings.getGl().getProductExpense());
            }
            if (!Objects.equal(entity.getGlExpense(), gldc)) {
                entity.setGlExpense(gldc);
                updated = true;
            }
        }

        // Updating accounts info...
        // Asset
        if (Validators.isValid(entity.getAccountAsset())) {
            GLAccountDto glAccount = getGLAccount(entity.getAccountAsset().getNumber());
            GLOperationAccount account = new GLOperationAccount(glAccount);
            if (!Objects.equal(entity.getAccountAsset(), account)) {
                entity.setAccountAsset(account);
                updated = true;
            }
        }
        // Income
        if (Validators.isValidDebit(entity.getGlIncome()) || Validators.isValidCredit(entity.getGlIncome())) {
            GLOperationAccount glAccountDebit = null, glAccountCredit = null;
            if (Validators.isValidDebit(entity.getGlIncome())) {
                glAccountDebit = new GLOperationAccount(getGLAccount(entity.getGlIncome().getDebit().getNumber()));
            }
            if (Validators.isValidCredit(entity.getGlIncome())) {
                glAccountCredit = new GLOperationAccount(getGLAccount(entity.getGlIncome().getCredit().getNumber()));
            }
            GLDC gldc = new GLDC(glAccountDebit, glAccountCredit);
            if (!Objects.equal(entity.getGlIncome(), gldc)) {
                entity.setGlIncome(gldc);
                updated = true;
            }
        }
        // Expense
        if (Validators.isValidDebit(entity.getGlExpense()) || Validators.isValidCredit(entity.getGlExpense())) {
            GLOperationAccount glAccountDebit = null, glAccountCredit = null;
            if (Validators.isValidDebit(entity.getGlExpense())) {
                glAccountDebit = new GLOperationAccount(getGLAccount(entity.getGlExpense().getDebit().getNumber()));
            }
            if (Validators.isValidCredit(entity.getGlExpense())) {
                glAccountCredit = new GLOperationAccount(getGLAccount(entity.getGlExpense().getCredit().getNumber()));
            }
            GLDC gldc = new GLDC(glAccountDebit, glAccountCredit);
            if (!Objects.equal(entity.getGlExpense(), gldc)) {
                entity.setGlExpense(gldc);
                updated = true;
            }
        }
        return updated;
    }

    @Override
    public PartApiDto create(PartApiDto dto) throws GamaApiException {
        return apiResultService.execute(() -> {
            final long companyId = auth.getCompanyId();
            final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");

            Validators.checkArgument(dto.getId() == null, "id specified");
            Validators.checkArgument(StringHelper.hasValue(dto.getName()),
                    TranslationService.getInstance().translate(TranslationService.INVENTORY.NoName, auth.getLanguage()));
            Validators.checkNotNull(dto.getType(),
                    TranslationService.getInstance().translate(TranslationService.INVENTORY.NoType, auth.getLanguage()));

            dto.setId(null);
            dto.setCompanyId(companyId);

            updateGLAccounts(dto, companySettings);

            PartSql entity = partApiSqlMapper.toEntity(dto);

            if (companySettings.isVatPayer()) {
                VATRate vatRate = dbServiceSQL.getMaxVATRate(companySettings.getCountry(), DateUtils.date());
                if (vatRate != null) {
                    entity.setTaxable(true);
                    entity.setVatRateCode(vatRate.getCode());
                }
            }

            return partApiSqlMapper.toDto(dbServiceSQL.saveEntityInCompany(entity));
        });
    }

    @Override
    public PartApiDto update(PartApiDto dto) throws GamaApiException {
        return apiResultService.execute(() -> {
            Validators.checkArgument(dto.getId() != null && dto.getId() != 0,
                    TranslationService.getInstance().translate(TranslationService.INVENTORY.NoPartId, auth.getLanguage()));

            Validators.checkArgument(StringHelper.hasValue(dto.getName()),
                    TranslationService.getInstance().translate(TranslationService.INVENTORY.NoName, auth.getLanguage()));
            Validators.checkNotNull(dto.getType(),
                    TranslationService.getInstance().translate(TranslationService.INVENTORY.NoType, auth.getLanguage()));

            PartApiDto entity = partApiSqlMapper.toDto(dbServiceSQL.getAndCheckNullable(PartSql.class, dto.getId()));
            if (entity == null) {
                throw new GamaNotFoundException(MessageFormat.format(
                        TranslationService.getInstance().translate(TranslationService.INVENTORY.NoPart, auth.getLanguage()),
                        dto.getId()));
            }

            boolean updated = false;
            if (!Objects.equal(entity.getType(), dto.getType())) {
                entity.setType(dto.getType());
                updated = true;
            }
            if (!StringHelper.isEquals(entity.getName(), dto.getName())) {
                entity.setName(dto.getName());
                updated = true;
            }
            if (!StringHelper.isEquals(entity.getDescription(), dto.getDescription())) {
                entity.setDescription(dto.getDescription());
                updated = true;
            }
            if (!StringHelper.isEquals(entity.getBarcode(), dto.getBarcode())) {
                entity.setBarcode(dto.getBarcode());
                updated = true;
            }
            if (!StringHelper.isEquals(entity.getSku(), dto.getSku())) {
                entity.setSku(dto.getSku());
                updated = true;
            }
            if (!StringHelper.isEquals(entity.getUnit(), dto.getUnit())) {
                entity.setUnit(dto.getUnit());
                updated = true;
            }
            if (!GLUtils.isNumbersEqual(entity.getAccountAsset(), dto.getAccountAsset())) {
                entity.setAccountAsset(dto.getAccountAsset());
                updated = true;
            }
            if (!GLUtils.isNumbersEqual(entity.getGlIncome(), dto.getGlIncome())) {
                entity.setGlIncome(dto.getGlIncome());
                updated = true;
            }
            if (!GLUtils.isNumbersEqual(entity.getGlExpense(), dto.getGlExpense())) {
                entity.setGlExpense(dto.getGlExpense());
                updated = true;
            }

            final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
            updated |= updateGLAccounts(entity, companySettings);

            if (updated) {
                return partApiSqlMapper.toDto(dbServiceSQL.saveEntityInCompany(partApiSqlMapper.toEntity(entity)));
            }
            return entity;
        });
    }
}

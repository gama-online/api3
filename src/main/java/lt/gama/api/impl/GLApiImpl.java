package lt.gama.api.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.*;
import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.*;
import lt.gama.api.response.PageResponse;
import lt.gama.api.service.GLApi;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.BooleanUtils;
import lt.gama.helpers.EntityUtils;
import lt.gama.helpers.StringHelper;
import lt.gama.helpers.Validators;
import lt.gama.model.dto.documents.DoubleEntryDto;
import lt.gama.model.dto.documents.GLOpeningBalanceDto;
import lt.gama.model.dto.entities.GLAccountDto;
import lt.gama.model.dto.entities.GLOperationDto;
import lt.gama.model.dto.entities.GLSaftAccountDto;
import lt.gama.model.dto.entities.ResponsibilityCenterDto;
import lt.gama.model.mappers.DoubleEntrySqlMapper;
import lt.gama.model.mappers.GLAccountSqlMapper;
import lt.gama.model.mappers.GLOpeningBalanceSqlMapper;
import lt.gama.model.mappers.ResponsibilityCenterSqlMapper;
import lt.gama.model.sql.documents.DoubleEntrySql;
import lt.gama.model.sql.documents.DoubleEntrySql_;
import lt.gama.model.sql.documents.GLOpeningBalanceSql;
import lt.gama.model.sql.documents.items.GLOperationSql;
import lt.gama.model.sql.documents.items.GLOperationSql_;
import lt.gama.model.sql.entities.GLAccountSql;
import lt.gama.model.sql.entities.GLAccountSql_;
import lt.gama.model.sql.entities.ResponsibilityCenterSql;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.model.type.enums.CustomSearchType;
import lt.gama.report.RepTrialBalance;
import lt.gama.service.*;
import lt.gama.service.ex.rt.GamaException;
import lt.gama.service.ex.rt.GamaNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RestController;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class GLApiImpl implements GLApi {

    private final GLAccountSqlMapper glAccountSqlMapper;
    private final DoubleEntrySqlMapper doubleEntrySqlMapper;
    private final ResponsibilityCenterSqlMapper responsibilityCenterSqlMapper;
    private final GLOpeningBalanceSqlMapper glOpeningBalanceSqlMapper;
    private final DBServiceSQL dbServiceSQL;
    private final GLService glService;
    private final GLUtilsService glUtilsService;
    private final Auth auth;
    private final TemplateService templateService;
    private final APIResultService apiResultService;
    private final ObjectMapper objectMapper;

    public GLApiImpl(GLAccountSqlMapper glAccountSqlMapper, DoubleEntrySqlMapper doubleEntrySqlMapper, ResponsibilityCenterSqlMapper responsibilityCenterSqlMapper, GLOpeningBalanceSqlMapper glOpeningBalanceSqlMapper, DBServiceSQL dbServiceSQL, GLService glService, GLUtilsService glUtilsService, Auth auth, TemplateService templateService, APIResultService apiResultService, ObjectMapper objectMapper) {
        this.glAccountSqlMapper = glAccountSqlMapper;
        this.doubleEntrySqlMapper = doubleEntrySqlMapper;
        this.responsibilityCenterSqlMapper = responsibilityCenterSqlMapper;
        this.glOpeningBalanceSqlMapper = glOpeningBalanceSqlMapper;
        this.dbServiceSQL = dbServiceSQL;
        this.glService = glService;
        this.glUtilsService = glUtilsService;
        this.auth = auth;
        this.templateService = templateService;
        this.apiResultService = apiResultService;
        this.objectMapper = objectMapper;
    }

    @Override
    public APIResult<PageResponse<ResponsibilityCenterDto, Void>> listRC(PageRequest request) throws GamaApiException {
        return apiResultService.result(() -> glService.listRC(request));
    }

    @Override
    public APIResult<ResponsibilityCenterDto> getRC(IdRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                responsibilityCenterSqlMapper.toDto(
                        dbServiceSQL.getById(ResponsibilityCenterSql.class, request.getId(), ResponsibilityCenterSql.GRAPH_ALL)));
    }

    @Override
    public APIResult<ResponsibilityCenterDto> saveRC(ResponsibilityCenterDto requestDto) throws GamaApiException {
        return apiResultService.result(() -> {
            ResponsibilityCenterSql request = responsibilityCenterSqlMapper.toEntity(requestDto);
            request = dbServiceSQL.saveEntityInCompany(request);
            return responsibilityCenterSqlMapper.toDto(request);
        });
	}

	@Override
    public APIResult<Map<String, Integer>> deleteRC(IdRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                Collections.singletonMap("deleted", glService.deleteRC(request.getId())));
    }

    @Override
    public APIResult<Map<String, Integer>> hideRC(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> 
                Collections.singletonMap("hidden", glService.hideRC(request.getId())));
    }

    @Override
    public APIResult<Map<String, Integer>> showRC(IdRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                Collections.singletonMap("visible", glService.showRC(request.getId())));
    }

    @Override
    public APIResult<List<ResponsibilityCenterDto>> filterRC(FilterRCRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                glService.filterRC(request.getFilter(), request.getMaxItems())
                        .stream()
                        .map(responsibilityCenterSqlMapper::toDto)
                        .collect(Collectors.toList()));
    }

    @Override
    public APIResult<Integer> countRC() throws GamaApiException {
        return apiResultService.result(() -> glService.countRC());
    }

    @Override
    public APIResult<PageResponse<GLAccountDto, Void>> listAccount(PageRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            request.setPageSize(PageRequest.MAX_PAGE_SIZE); // retrieve all
            return dbServiceSQL.list(request, GLAccountSql.class, GLAccountSql.GRAPH_ALL, glAccountSqlMapper,
                    null,
                    (cb, root) -> Collections.singletonList(cb.asc(root.get(GLAccountSql_.NUMBER))),
                    (cb, root) -> Arrays.asList(root.get(GLAccountSql_.ID).alias("id"), root.get(GLAccountSql_.NUMBER)));
        });
    }

    @Override
    public APIResult<GLAccountDto> getAccount(IdRequest request) throws GamaApiException {
        return apiResultService.result(() ->
            glAccountSqlMapper.toDto(dbServiceSQL.getById(GLAccountSql.class, request.getId(), GLAccountSql.GRAPH_ALL)));
    }

    @Override
    public APIResult<GLAccountDto> saveAccount(GLAccountRequest request) throws GamaApiException {
        return apiResultService.result(() -> glService.saveAccount(request));
    }

    @Override
    public APIResult<Void> deleteAccount(GLAccountRequest request) throws GamaApiException {
        return apiResultService.result(() -> glService.deleteAccount(request));
    }

    @Override
	public APIResult<Void> templateAccount() throws GamaApiException {
        return apiResultService.result(() -> glService.templateAccount());
    }

    @Override
    public APIResult<PageResponse<GLSaftAccountDto, Void>> listSaftAccount(PageRequest request) throws GamaApiException {
        return apiResultService.result(() -> glService.getGLSaftAccounts());
    }

    @Override
    public APIResult<Void> assignSaft() throws GamaApiException {
        return apiResultService.result(() -> glService.assignSaft());
    }

    /*
     * Double Entry
     */

    private Predicate whereDoubleEntry(PageRequest request, CriteriaBuilder cb, Root<?> root) {
        Predicate where = null;
        Join<DoubleEntrySql, GLOperationSql> operations = null;
        if (request.getConditions() != null) {
            for (PageRequestCondition condition : request.getConditions()) {
                if (condition.getField().equals(CustomSearchType.GL_RC.getField()) &&
                        (condition.getValue() instanceof String || condition.getValue() instanceof Number)) {
                    // example: jsonb_path_exists(debit_rc, '$[*] ? (@.id == $id)', jsonb_build_object('id', 1234))
                    try {
                        long rcId = condition.getValue() instanceof Number
                                ? ((Number) condition.getValue()).longValue()
                                : Long.parseLong((String) condition.getValue());

                        if (operations == null) operations = root.join(DoubleEntrySql_.OPERATIONS, JoinType.LEFT);
                        Predicate rcPredicate = cb.or(
                                cb.isTrue(
                                        cb.function("jsonb_path_exists", Boolean.class,
                                                operations.get(GLOperationSql_.DEBIT_RC),
                                                cb.literal("$[*] ? (@.id == $id)"),
                                                cb.function("jsonb_build_object", String.class,
                                                        cb.literal("id"), cb.literal(rcId))
                                        )
                                ),
                                cb.isTrue(
                                        cb.function("jsonb_path_exists", Boolean.class,
                                                operations.get(GLOperationSql_.CREDIT_RC),
                                                cb.literal("$[*] ? (@.id == $id)"),
                                                cb.function("jsonb_build_object", String.class,
                                                        cb.literal("id"), cb.literal(rcId))
                                        )
                                )
                        );
                        where = where == null ? rcPredicate : cb.and(where, rcPredicate);

                    } catch (NumberFormatException ignore) { }

                }
            }
        }
        if (StringHelper.hasValue(request.getFilter())) {
            String filter = StringUtils.stripAccents(request.getFilter().trim()).toLowerCase();
            if (operations == null) operations = root.join(DoubleEntrySql_.OPERATIONS, JoinType.LEFT);
            Predicate contentFilter = cb.or(
                    cb.like(cb.lower(root.get(DoubleEntrySql_.NUMBER)), "%" + filter + "%"),
                    cb.like(cb.lower(cb.function("unaccent", String.class, root.get(DoubleEntrySql_.CONTENT))), "%" + filter + "%"),
                    cb.like(cb.lower(operations.get(GLOperationSql_.DEBIT).get("number")), "%" + filter + "%"),
                    cb.like(cb.lower(cb.function("unaccent", String.class, operations.get(GLOperationSql_.DEBIT).get("name"))), "%" + filter + "%"),
                    cb.like(cb.lower(operations.get(GLOperationSql_.CREDIT).get("number")), "%" + filter + "%"),
                    cb.like(cb.lower(cb.function("unaccent", String.class, operations.get(GLOperationSql_.CREDIT).get("name"))), "%" + filter + "%")
            );
            where = where == null ? contentFilter : cb.and(where, contentFilter);
        }
        return where;
    }

    @Override
    public APIResult<PageResponse<DoubleEntryDto, Void>> listDoubleEntry(PageRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                dbServiceSQL.list(request, DoubleEntrySql.class, DoubleEntrySql.GRAPH_ALL, doubleEntrySqlMapper,
                        (cb, root) -> whereDoubleEntry(request, cb, root),
                        (cb, root) -> EntityUtils.orderDoc(request.getOrder(), cb, root),
                        (cb, root) -> EntityUtils.selectIdDoc(request.getOrder(), cb, root)));
    }

    @Override
    public APIResult<DoubleEntryDto> saveDoubleEntry(DoubleEntryDto request) throws GamaApiException {
        return apiResultService.result(() -> {
            if (StringHelper.isEmpty(request.getNumber())) {
                request.setAutoNumber(true);
            }
            return doubleEntrySqlMapper.toDto(glService.regenerateDoubleEntry(doubleEntrySqlMapper.toEntity(request)));
        });
    }

    @Override
    public APIResult<DoubleEntryDto> getDoubleEntry(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            DoubleEntryDto entityDto;
            if (request.getParentId() != null || BooleanUtils.isTrue(request.getParent())) {
                final long parentId = request.getParentId() != null ? request.getParentId() : request.getId();
                entityDto = doubleEntrySqlMapper.toDto(glUtilsService.getDoubleEntryByParentId(parentId));
                if (entityDto == null) {
                    throw new GamaNotFoundException(MessageFormat.format(
                            TranslationService.getInstance().translate(TranslationService.GL.NoDoubleEntryDocumentWithParentId, auth.getLanguage()),
                            parentId));
                }
            } else {
                entityDto = doubleEntrySqlMapper.toDto(dbServiceSQL.getById(DoubleEntrySql.class, request.getId(), DoubleEntrySql.GRAPH_ALL));
                if (entityDto == null) {
                    throw new GamaNotFoundException(MessageFormat.format(
                            TranslationService.getInstance().translate(TranslationService.GL.NoDoubleEntryDocumentWithId, auth.getLanguage()),
                            request.getId()));
                }
            }
            return entityDto;
        });
    }

    @Override
    public APIResult<DoubleEntryDto> finishDoubleEntry(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            Boolean parent = request.getParent();
            long id = request.getId() > 0 ? request.getId() : request.getParentId() != null ? request.getParentId() : 0;
            if (id <= 0)
                throw new GamaNotFoundException(TranslationService.getInstance().translate(TranslationService.DB.WrongId, auth.getLanguage()));
            return glService.finish(id, request.getDb(), parent, DoubleEntrySql.GRAPH_ALL);
        });
    }

    @Override
    public APIResult<DoubleEntryDto> recallDoubleEntry(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            Boolean parent = request.getParent();
            long id = request.getId() > 0 ? request.getId() : request.getParentId() != null ? request.getParentId() : 0;
            if (id <= 0)
                throw new GamaNotFoundException(TranslationService.getInstance().translate(TranslationService.DB.WrongId, auth.getLanguage()));
            return glService.recall(id, request.getDb(), parent, DoubleEntrySql.GRAPH_ALL);
        });
    }

    @Override
    public APIResult<Void> deleteDoubleEntry(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            long id = request.getId() > 0 ? request.getId() : request.getParentId() != null ? request.getParentId() : 0;
            if (id <= 0) throw new GamaException(TranslationService.getInstance().translate(TranslationService.DB.WrongId, auth.getLanguage()));
            dbServiceSQL.deleteById(DoubleEntrySql.class, id);
        });
    }

    @Override
    public APIResult<List<GLOperationDto>> closeProfitLoss(DateRequest request) throws GamaApiException {
        return apiResultService.result(() -> glService.closeProfitLoss(request.getDate()));
    }

    /*
     * Opening Balance
     */
    private Predicate whereOpeningBalance(PageRequest request, CriteriaBuilder cb, Root<?> root) {
        Predicate where = null;
        if (StringHelper.hasValue(request.getFilter())) {
            where = cb.like(root.get("note"), "%" + request.getFilter() + "%");
        }
        return where;
    }



    @Override
    public APIResult<PageResponse<GLOpeningBalanceDto, Void>> listOpeningBalance(PageRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                dbServiceSQL.list(request, GLOpeningBalanceSql.class, GLOpeningBalanceSql.GRAPH_ALL, glOpeningBalanceSqlMapper,
                        (cb, root) -> whereOpeningBalance(request, cb, root),
                        (cb, root) -> EntityUtils.orderDoc(request.getOrder(), cb, root),
                        (cb, root) -> EntityUtils.selectIdDoc(request.getOrder(), cb, root)));
    }

    @Override
    public APIResult<GLOpeningBalanceDto> saveOpeningBalance(GLOpeningBalanceDto document) throws GamaApiException {
        return apiResultService.result(() -> {
            long companyId = auth.getCompanyId();
            CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");

            if (Boolean.TRUE.equals(document.getIBalance())) {
                Validators.checkNotNull(document.getDate(), TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDocumentDate, auth.getLanguage()));
                int docYear = document.getDate().getYear();
                int docMonth = document.getDate().getMonthValue();
                int docDay =  document.getDate().getDayOfMonth();
                int iBMonth =  companySettings.getStartAccountingPeriod().minusMonths(1).getMonthValue();
                int iBDay = companySettings.getStartAccountingPeriod().minusDays(1).getDayOfMonth();
                int iBYear = companySettings.getStartAccounting().minusDays(1).getYear();
                int accYear = companySettings.getAccYear();
                if ( docMonth != iBMonth || docDay != iBDay || docYear < iBYear || docYear >= accYear)
                    throw new GamaException("Wrong period");
            } else {
                Validators.checkOpeningBalanceDate(companySettings, document, auth.getLanguage());
            }

            // clear details id's if new master
            if ((document.getId() == null || document.getId() == 0) && document.getBalances() != null) {
                document.getBalances().forEach(bal -> {
                    bal.setId(null);
                    bal.setCompanyId(companyId);
                });
            }
            GLOpeningBalanceSql documentSql = glOpeningBalanceSqlMapper.toEntity(document);
            documentSql = dbServiceSQL.saveWithCounter(documentSql);
            return glOpeningBalanceSqlMapper.toDto(documentSql);
        });
	}

    @Override
    public APIResult<GLOpeningBalanceDto> getOpeningBalance(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> glOpeningBalanceSqlMapper.toDto(
                dbServiceSQL.getById(GLOpeningBalanceSql.class, request.getId(), GLOpeningBalanceSql.GRAPH_ALL)));
    }

    @Override
    public APIResult<GLOpeningBalanceDto> finishOpeningBalance(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> glService.finishOpeningBalance(request.getId(), GLOpeningBalanceSql.GRAPH_ALL));
    }

    @Override
    public APIResult<GLOpeningBalanceDto> recallOpeningBalance(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> glService.recallOpeningBalance(request.getId(), GLOpeningBalanceSql.GRAPH_ALL));
    }

    @Override
    public APIResult<GLOpeningBalanceDto> importOpeningBalance(ImportDocRequest request) throws GamaApiException {
        return apiResultService.result(() -> glService.importOpeningBalance(request.getId(), request.getFileName()));
    }

    @Override
    public APIResult<Void> deleteOpeningBalance(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> dbServiceSQL.deleteById(GLOpeningBalanceSql.class, request.getId()));
    }

    @Override
    public APIResult<GLOpeningBalanceDto> countIntermediateBalance(IntermediateBalanceRequest request) throws GamaApiException {
        return apiResultService.result(() -> glService.countIntermediateBalance(request));
    }

    /*
     * Reports
     */

	@Override
    public APIResult<List<RepTrialBalance>> reportTrialBalance(GLReportBalanceRequest request) throws GamaApiException {
        return apiResultService.result(() -> glService.reportTrialBalance(request));
    }

    @Override
    public APIResult<PageResponse<DoubleEntryDto, RepTrialBalance>> reportFlow(PageRequest request) throws GamaApiException {
        return apiResultService.result(() -> glService.reportFlow(request));
    }

    @Override
    public APIResult<JsonNode> getProfitLossJson(ProfitLossJsonRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            String template = templateService.getJsonData(auth.getCompanyId(), "profit-loss", request.getLanguage());
            if (StringHelper.isEmpty(template)) {
                throw new GamaException("profit-loss.json not found, language=" + request.getLanguage());
            }
            return objectMapper.readTree(template);
        });
    }
}

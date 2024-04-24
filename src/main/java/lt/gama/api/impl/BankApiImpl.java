package lt.gama.api.impl;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.*;
import lt.gama.api.response.PageResponse;
import lt.gama.api.service.BankApi;
import lt.gama.auth.annotation.RequiresPermissions;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.EntityUtils;
import lt.gama.helpers.PageRequestUtils;
import lt.gama.helpers.StringHelper;
import lt.gama.impexp.entity.ISO20022Statements;
import lt.gama.model.dto.documents.BankOpeningBalanceDto;
import lt.gama.model.dto.documents.BankOperationDto;
import lt.gama.model.dto.documents.BankRateInfluenceDto;
import lt.gama.model.dto.entities.BankAccountDto;
import lt.gama.model.dto.entities.MoneyHistoryDto;
import lt.gama.model.mappers.BankAccountSqlMapper;
import lt.gama.model.mappers.BankOpeningBalanceSqlMapper;
import lt.gama.model.mappers.BankOperationSqlMapper;
import lt.gama.model.mappers.BankRateInfluenceSqlMapper;
import lt.gama.model.sql.base.BaseDocumentSql_;
import lt.gama.model.sql.documents.*;
import lt.gama.model.sql.entities.BankAccountSql;
import lt.gama.model.sql.entities.BankAccountSql_;
import lt.gama.model.type.Location_;
import lt.gama.model.type.doc.DocBank_;
import lt.gama.model.type.enums.AccountType;
import lt.gama.model.type.enums.CustomSearchType;
import lt.gama.model.type.enums.Permission;
import lt.gama.report.RepMoneyBalance;
import lt.gama.report.RepMoneyBalanceInterval;
import lt.gama.report.RepMoneyDetail;
import lt.gama.service.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
public class BankApiImpl implements BankApi {

    private final DBServiceSQL dbServiceSQL;
    private final BankService bankService;
    private final DocumentService documentService;
    private final MoneyAccountService moneyAccountService;
    private final BankAccountSqlMapper bankAccountSqlMapper;
    private final Auth auth;
    private final BankOpeningBalanceSqlMapper bankOpeningBalanceSqlMapper;
    private final BankOperationSqlMapper bankOperationSqlMapper;
    private final BankRateInfluenceSqlMapper bankRateInfluenceSqlMapper;
    private final APIResultService apiResultService;


    public BankApiImpl(DBServiceSQL dbServiceSQL, BankService bankService, DocumentService documentService, MoneyAccountService moneyAccountService, BankAccountSqlMapper bankAccountSqlMapper, Auth auth, BankOpeningBalanceSqlMapper bankOpeningBalanceSqlMapper, BankOperationSqlMapper bankOperationSqlMapper, BankRateInfluenceSqlMapper bankRateInfluenceSqlMapper, APIResultService apiResultService) {
        this.dbServiceSQL = dbServiceSQL;
        this.bankService = bankService;
        this.documentService = documentService;
        this.moneyAccountService = moneyAccountService;
        this.bankAccountSqlMapper = bankAccountSqlMapper;
        this.auth = auth;
        this.bankOpeningBalanceSqlMapper = bankOpeningBalanceSqlMapper;
        this.bankOperationSqlMapper = bankOperationSqlMapper;
        this.bankRateInfluenceSqlMapper = bankRateInfluenceSqlMapper;
        this.apiResultService = apiResultService;
    }


    /*
     *  Bank accounts
     */

    @Override
    public APIResult<PageResponse<BankAccountDto, Void>> listBankAccount(PageRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                dbServiceSQL.list(request, BankAccountSql.class, null, bankAccountSqlMapper,
                    (cb, root) -> {
                        if (StringHelper.hasValue(request.getFilter())) {
                            String filter = StringUtils.stripAccents(request.getFilter().trim()).toLowerCase();
                            return cb.or(
                                    cb.like(cb.lower(cb.function("unaccent", String.class, root.get(BankAccountSql_.ACCOUNT))), "%" + filter + "%"),
                                    cb.like(cb.lower(cb.function("unaccent", String.class, root.get(BankAccountSql_.BANK).get(DocBank_.SWIFT))), "%" + filter + "%"),
                                    cb.like(cb.lower(cb.function("unaccent", String.class, root.get(BankAccountSql_.BANK).get(Location_.NAME))), "%" + filter + "%"));
                        }
                        return null;
                    },
                    (cb, root) -> Arrays.asList(cb.asc(root.get(BankAccountSql_.BANK).get(Location_.NAME)), cb.asc(root.get(BankAccountSql_.ID))),
                    (cb, root) -> Arrays.asList(root.get(BankAccountSql_.BANK).get(Location_.NAME), root.get(BankAccountSql_.ID).alias("id"))));
    }

    @Override
    public APIResult<BankAccountDto> saveBankAccount(final BankAccountDto request) throws GamaApiException {
        return apiResultService.result(() -> bankService.saveBankAccount(request));
    }

    @Override
    public APIResult<BankAccountDto> getBankAccount(IdRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                bankAccountSqlMapper.toDto(
                        dbServiceSQL.getByIdOrForeignId(BankAccountSql.class, request.getId(), request.getDb())));
    }

    @Override
    public APIResult<Void> deleteBankAccount(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> dbServiceSQL.deleteById(BankAccountSql.class, request.getId()));
    }

    @Override
    public APIResult<BankAccountDto> undeleteBankAccount(IdRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                bankAccountSqlMapper.toDto(dbServiceSQL.undeleteById(BankAccountSql.class, request.getId())));
    }

    /*
     *  Bank accounts opening balance
     */

    @Override
    public APIResult<PageResponse<BankOpeningBalanceDto, Void>> listOpeningBalance(PageRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                dbServiceSQL.list(request, BankOpeningBalanceSql.class,
                        BankOpeningBalanceSql.GRAPH_ALL, bankOpeningBalanceSqlMapper,
                        (cb, root) -> EntityUtils.whereDoc(request, cb, root, null, null),
                        (cb, root) -> EntityUtils.orderDoc(request.getOrder(), cb, root),
                        (cb, root) -> EntityUtils.selectIdDoc(request.getOrder(), cb, root)));
    }

    @Override
    public APIResult<BankOpeningBalanceDto> saveOpeningBalance(BankOpeningBalanceDto request) throws GamaApiException {
        return apiResultService.result(() -> bankService.saveBankOpeningBalance(request));
    }

    @Override
    public APIResult<BankOpeningBalanceDto> getOpeningBalance(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> 
                documentService.getDocument(BankOpeningBalanceSql.class, request.getId(), request.getDb()));
    }

    @Override
    public APIResult<BankOpeningBalanceDto> finishOpeningBalance(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> bankService.finishBankOpeningBalance(request.getId()));
    }

    @Override
    public APIResult<BankOpeningBalanceDto> importOpeningBalance(ImportDocRequest request) throws GamaApiException {
        return apiResultService.result(() -> bankService.importOpeningBalance(request.getId(), request.getFileName()));
    }

    @Override
    public APIResult<Void> deleteOpeningBalance(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> documentService.deleteDocument(request.getId(), request.getDb()));
    }

    /*
     *  Bank operation
     */

    @Override
    public APIResult<PageResponse<BankOperationDto, Void>> listOperation(PageRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                dbServiceSQL.list(request, BankOperationSql.class,
                    BankOperationSql.GRAPH_ALL, bankOperationSqlMapper,
                    root -> Map.of(
                            BaseDocumentSql_.COUNTERPARTY, root.join(BaseDocumentSql_.COUNTERPARTY, JoinType.LEFT),
                            BaseDocumentSql_.EMPLOYEE, root.join(BaseDocumentSql_.EMPLOYEE, JoinType.LEFT),
                            BankOperationSql_.BANK_ACCOUNT, root.join(BankOperationSql_.BANK_ACCOUNT, JoinType.LEFT),
                            BankOperationSql_.BANK_ACCOUNT2, root.join(BankOperationSql_.BANK_ACCOUNT2, JoinType.LEFT)),
                    (cb, root, joins) -> {
                        Predicate where = EntityUtils.whereDoc(request, cb, root, patterns -> {
                            Predicate[] predicates = new Predicate[patterns.length];
                            for (int i = 0; i < patterns.length; i++) {
                                String pattern = patterns[i];
                                predicates[i] = cb.or(
                                        cb.like(cb.lower(cb.function("unaccent", String.class,
                                                root.get(BankOperationSql_.BANK_ACCOUNT).get(BankAccountSql_.ACCOUNT))), '%' + pattern + '%'),
                                        cb.like(cb.lower(cb.function("unaccent", String.class,
                                                root.get(BankOperationSql_.BANK_ACCOUNT2).get(BankAccountSql_.ACCOUNT))), '%' + pattern + '%'));
                            }
                            return patterns.length == 1 ? predicates[0] : cb.and(predicates);
                        }, joins);
                        Long bankId = (Long) PageRequestUtils.getFieldValue(request.getConditions(), CustomSearchType.BANK_ID);
                        if (bankId != null) {
                            Predicate predicate = cb.equal(root.get(BankOperationSql_.BANK_ACCOUNT).get(BankAccountSql_.ID), bankId);
                            where = where == null ? predicate : cb.and(where, predicate);
                        }
                        return where;
                    },
                    (cb, root, joins) -> EntityUtils.orderMoneyDoc(request.getOrder(), cb, root, joins),
                    (cb, root, joins) -> EntityUtils.selectIdMoneyDoc(request.getOrder(), cb, root, joins)));
    }

    @Override
    public APIResult<BankOperationDto> saveOperation(BankOperationDto request) throws GamaApiException {
        return apiResultService.result(() -> bankService.saveBankOperation(request));
   }

    @Override
    @RequiresPermissions({Permission.BANK_OP_R, Permission.BANK_OP_M, Permission.GL})
    public APIResult<BankOperationDto> getOperation(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> 
                documentService.getDocument(BankOperationSql.class, request.getId(), request.getDb()));
    }

    @Override
    public APIResult<BankOperationDto> finishOperation(FinishRequest request) throws GamaApiException {
        return apiResultService.result(() -> bankService.finishBankOperation(request.id, request.finishGL));
    }

    @Override
    public APIResult<ISO20022Statements> parseOperations(ParseOperationRequest request) throws GamaApiException {
        return apiResultService.result(() -> bankService.parseOperations(request.type, request.fileName));
    }

    @Override
    public APIResult<ISO20022Statements> importOperations(ISO20022Statements request) throws GamaApiException {
        return apiResultService.result(() -> bankService.importOperations(request));
     }

    @Override
    public APIResult<Void> deleteOperation(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> documentService.deleteDocument(request.getId(), request.getDb()));
    }

    @Override
    public APIResult<BankOperationDto> recallOperation(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> bankService.recallBankOperation(request.getId()));
   }

    /*
     *  Bank $$$ Rate Influence
     */

    @Override
    public APIResult<PageResponse<BankRateInfluenceDto, Void>> listRateInfluence(PageRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                dbServiceSQL.list(request, BankRateInfluenceSql.class,
                        BankRateInfluenceSql.GRAPH_ALL, bankRateInfluenceSqlMapper,
                        (cb, root) -> EntityUtils.whereDoc(request, cb, root, null, null),
                        (cb, root) -> EntityUtils.orderDoc(request.getOrder(), cb, root),
                        (cb, root) -> EntityUtils.selectIdDoc(request.getOrder(), cb, root)));
    }

    @Override
    public APIResult<BankRateInfluenceDto> saveRateInfluence(BankRateInfluenceDto request) throws GamaApiException {
        return apiResultService.result(() -> bankService.saveBankRateInfluence(request));
    }

    @Override
    public APIResult<BankRateInfluenceDto> getRateInfluence(IdRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                documentService.getDocument(BankRateInfluenceSql.class, request.getId(), request.getDb()));
    }

    @Override
    public APIResult<BankRateInfluenceDto> finishRateInfluence(FinishRequest request) throws GamaApiException {
        return apiResultService.result(() -> bankService.finishBankRateInfluence(request.id, request.finishGL));
    }

    @Override
    public APIResult<Void> deleteRateInfluence(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> documentService.deleteDocument(request.getId(), request.getDb()));
    }

    @Override
    public APIResult<BankRateInfluenceDto> recallRateInfluence(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> bankService.recallBankRateInfluence(request.getId()));
    }

    @Override
    public APIResult<List<RepMoneyBalance<BankAccountDto>>> genRateInfluence(DateRequest request) throws GamaApiException {
        return apiResultService.result(() -> moneyAccountService.genRateInfluence(request.getDate(), AccountType.BANK));
    }

    /*
     * Reports
     */

    @Override
    public APIResult<List<RepMoneyBalance<BankAccountDto>>> reportBalance(ReportBalanceRequest request) throws GamaApiException {
        return apiResultService.result(() -> bankService.reportBankBalance(request));
    }

    @Override
    public APIResult<PageResponse<MoneyHistoryDto, RepMoneyDetail<BankAccountDto>>> reportFlow(PageRequest request) throws GamaApiException {
        return apiResultService.result(() -> bankService.reportBankFlow(request));
    }

    @Override
    public APIResult<RepMoneyBalanceInterval> reportBalanceInterval(ReportBalanceIntervalRequest request) throws GamaApiException {
        return apiResultService.result(() -> moneyAccountService.reportBalanceInterval(request, AccountType.BANK));
    }
}

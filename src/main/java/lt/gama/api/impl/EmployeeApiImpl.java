package lt.gama.api.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.*;
import lt.gama.api.response.PageResponse;
import lt.gama.api.service.EmployeeApi;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.*;
import lt.gama.model.dto.documents.EmployeeOpeningBalanceDto;
import lt.gama.model.dto.documents.EmployeeOperationDto;
import lt.gama.model.dto.documents.EmployeeRateInfluenceDto;
import lt.gama.model.dto.entities.EmployeeDto;
import lt.gama.model.dto.entities.MoneyHistoryDto;
import lt.gama.model.dto.entities.RoleDto;
import lt.gama.model.i.IEmployee;
import lt.gama.model.mappers.*;
import lt.gama.model.sql.base.BaseDocumentSql_;
import lt.gama.model.sql.documents.BankOperationSql_;
import lt.gama.model.sql.documents.EmployeeOpeningBalanceSql;
import lt.gama.model.sql.documents.EmployeeOperationSql;
import lt.gama.model.sql.documents.EmployeeRateInfluenceSql;
import lt.gama.model.sql.entities.EmployeeSql;
import lt.gama.model.sql.entities.EmployeeSql_;
import lt.gama.model.sql.entities.RoleSql;
import lt.gama.model.sql.entities.RoleSql_;
import lt.gama.model.sql.system.AccountSql;
import lt.gama.model.sql.system.CompanySql;
import lt.gama.model.type.auth.AccountInfo;
import lt.gama.model.type.enums.AccountType;
import lt.gama.model.type.enums.CustomSearchType;
import lt.gama.model.type.enums.EmployeeType;
import lt.gama.report.RepMoneyBalance;
import lt.gama.report.RepMoneyBalanceInterval;
import lt.gama.report.RepMoneyDetail;
import lt.gama.service.*;
import lt.gama.service.ex.rt.GamaException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
public class EmployeeApiImpl implements EmployeeApi {

    private final DBServiceSQL dbServiceSQL;
    private final AccountService accountService;
    private final EmployeeService employeeService;
    private final DocumentService documentService;
    private final MoneyAccountService moneyAccountService;
    private final EmployeeSqlMapper employeeSqlMapper;
    private final RoleSqlMapper roleSqlMapper;
    private final Auth auth;
    private final CompanySqlMapper companySqlMapper;
    private final EmployeeOperationSqlMapper employeeOperationSqlMapper;
    private final EmployeeOpeningBalanceSqlMapper employeeOpeningBalanceSqlMapper;
    private final EmployeeRateInfluenceSqlMapper employeeRateInfluenceSqlMapper;
    private final APIResultService apiResultService;


    public EmployeeApiImpl(DBServiceSQL dbServiceSQL, AccountService accountService, EmployeeService employeeService, DocumentService documentService, MoneyAccountService moneyAccountService, EmployeeSqlMapper employeeSqlMapper, RoleSqlMapper roleSqlMapper, Auth auth, CompanySqlMapper companySqlMapper, EmployeeOperationSqlMapper employeeOperationSqlMapper, EmployeeOpeningBalanceSqlMapper employeeOpeningBalanceSqlMapper, EmployeeRateInfluenceSqlMapper employeeRateInfluenceSqlMapper, APIResultService apiResultService) {
        this.dbServiceSQL = dbServiceSQL;
        this.accountService = accountService;
        this.employeeService = employeeService;
        this.documentService = documentService;
        this.moneyAccountService = moneyAccountService;
        this.employeeSqlMapper = employeeSqlMapper;
        this.roleSqlMapper = roleSqlMapper;
        this.auth = auth;
        this.companySqlMapper = companySqlMapper;
        this.employeeOperationSqlMapper = employeeOperationSqlMapper;
        this.employeeOpeningBalanceSqlMapper = employeeOpeningBalanceSqlMapper;
        this.employeeRateInfluenceSqlMapper = employeeRateInfluenceSqlMapper;
        this.apiResultService = apiResultService;
    }

    private Predicate whereEmployee(PageRequest request, CriteriaBuilder cb, Root<?> root) {
        Predicate where = null;
        if (request.getConditions() != null) {
            Object employeeTypeValue = PageRequestUtils.getFieldValue(request.getConditions(), CustomSearchType.EMPLOYEE_TYPE);
            EmployeeType employeeType = employeeTypeValue instanceof String
                    ? EmployeeType.from((String) employeeTypeValue)
                    : employeeTypeValue instanceof EmployeeType
                    ? (EmployeeType) employeeTypeValue
                    : null;
            if (employeeType != null) {
                where = cb.equal(root.get(EmployeeSql_.TYPE), employeeType);
            }
            Boolean remainder = (Boolean) PageRequestUtils.getFieldValue(request.getConditions(), CustomSearchType.REMAINDER);
            if (BooleanUtils.isTrue(remainder)) {
                Predicate predicate = cb.isTrue(
                        cb.function("jsonb_path_exists", Boolean.class,
                                root.get(EmployeeSql_.REMAINDER),
                                cb.literal("$.* ? (@.amount != 0)")
                        )
                );
                where = where == null ? predicate : cb.and(where, predicate);
            }
        }
        if (StringUtils.isNotBlank(request.getLabel())) {
            String filter = request.getLabel();
            Predicate predicate = cb.or(
                    cb.isTrue(
                            cb.function("jsonb_path_exists", Boolean.class,
                                    root.get(EmployeeSql_.LABELS),
                                    cb.literal("$[*] ? (@ == $filter)"),
                                    cb.function("jsonb_build_object", String.class,
                                            cb.literal("filter"), cb.literal(filter))
                            )
                    )
            );
            where = where == null ? predicate : cb.and(where, predicate);
        }
        if (StringHelper.hasValue(request.getFilter())) {
            String filter = StringUtils.stripAccents(request.getFilter().trim()).toLowerCase();
            Predicate predicate = cb.or(
                    cb.like(cb.lower(cb.function("unaccent", String.class, root.get(EmployeeSql_.NAME))), "%" + filter + "%"),
                    cb.like(cb.lower(cb.function("unaccent", String.class, root.get(EmployeeSql_.DEPARTMENT))), "%" + filter + "%"),
                    cb.like(cb.lower(cb.function("unaccent", String.class, root.get(EmployeeSql_.OFFICE))), "%" + filter + "%"),
                    cb.like(cb.lower(cb.function("unaccent", String.class, root.get(EmployeeSql_.EMAIL))), "%" + filter + "%"),
                    cb.like(cb.lower(cb.function("unaccent", String.class, root.join(EmployeeSql_.ROLES, JoinType.LEFT).get(RoleSql_.NAME))), "%" + filter + "%")
            );
            where = where == null ? predicate : cb.and(where, predicate);
        }
        return where;
    }


    @Override
    public APIResult<PageResponse<EmployeeDto, Void>> listEmployee(PageRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                dbServiceSQL.list(request, EmployeeSql.class, EmployeeSql.GRAPH_ALL, employeeSqlMapper,
                    (cb, root) -> whereEmployee(request, cb, root),
                    (cb, root) -> List.of(
                            cb.asc(root.get(EmployeeSql_.NAME)),
                            cb.asc(root.get(EmployeeSql_.DEPARTMENT)),
                            cb.asc(root.get(EmployeeSql_.office)),
                            cb.asc(root.get(EmployeeSql_.ID))),
                    (cb, root) -> List.of(
                            root.get(EmployeeSql_.NAME),
                            root.get(EmployeeSql_.DEPARTMENT),
                            root.get(EmployeeSql_.office),
                            root.get(EmployeeSql_.ID).alias("id"))));
    }

    @Override
    public APIResult<EmployeeDto> saveEmployee(final EmployeeDto request) throws GamaApiException {
        return apiResultService.result(() ->
                employeeService.saveEmployee(request, () -> auth.getPermissions()));
    }

    @Override
    public APIResult<EmployeeDto> getEmployee(IdRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                employeeSqlMapper.toDto(dbServiceSQL.getByIdOrForeignId(EmployeeSql.class, request.getId(), request.getDb())));
    }

    @Override
    public APIResult<Void> deleteEmployee(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> dbServiceSQL.executeInTransaction(entityManager -> {
            accountService.removeEmployeeFromAccounts(request.getId());
            dbServiceSQL.deleteById(EmployeeSql.class, request.getId());
        }));
    }

    @Override
    public APIResult<EmployeeDto> undeleteEmployee(IdRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                employeeSqlMapper.toDto(dbServiceSQL.undeleteById(EmployeeSql.class, request.getId())));
    }

    /*
     * Roles
     */

    @Override
    public APIResult<PageResponse<RoleDto, Void>> listRole(PageRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                dbServiceSQL.list(request, RoleSql.class, null, roleSqlMapper,
                    null,
                    (cb, root) -> Collections.singletonList(cb.asc(root.get(RoleSql_.NAME))),
                    (cb, root) -> Arrays.asList(root.get(RoleSql_.ID).alias("id"), root.get(RoleSql_.NAME))));
    }

    @Override
    public APIResult<RoleDto> saveRole(RoleDto request) throws GamaApiException {
        return apiResultService.result(() -> {
            RoleDto role = roleSqlMapper.toDto(dbServiceSQL.saveEntityInCompany(roleSqlMapper.toEntity(request)));
            accountService.updateAccountPermissions(role);
            return role;
        });
    }

    @Override
    public APIResult<RoleDto> getRole(IdRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                roleSqlMapper.toDto(dbServiceSQL.getByIdOrForeignId(RoleSql.class, request.getId(), request.getDb())));
    }

    /*
     * Accounts
     */

    @Override
    public APIResult<String> getAccount(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            AccountSql account = accountService.findAccountByEmployee(request.getId());
            return account != null ? account.getId() : null;
        });
    }

    @Override
    public APIResult<String> activateAccount(ActivateAccountRequest request) throws GamaApiException {
        return apiResultService.result(() -> dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            final long companyId = auth.getCompanyId();
            final CompanySql company = Validators.checkNotNull(dbServiceSQL.getById(CompanySql.class, companyId), "No company");
            if (company.getSettings() != null && company.getSettings().getValidUntil() != null &&
                    company.getSettings().getValidUntil().isBefore(DateUtils.date()))
                throw new GamaException("Your company account is not active");

            IEmployee employee = dbServiceSQL.getAndCheck(EmployeeSql.class, request.id);
            return accountService.activateAccount(request.email, new AccountInfo(company, employee), DateUtils.date());
        }));
    }

    @Override
    public APIResult<Void> suspendAccount(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> dbServiceSQL.executeInTransaction(entityManager -> {
            final long companyId = auth.getCompanyId();
            CompanySql company = Validators.checkNotNull(dbServiceSQL.getById(CompanySql.class, companyId), "No company");
            if (company.getSettings() != null && company.getSettings().getValidUntil() != null &&
                    company.getSettings().getValidUntil().isBefore(DateUtils.date()))
                throw new GamaException("Your company account is not active");

            dbServiceSQL.getAndCheck(EmployeeSql.class, request.getId());
            accountService.removeEmployeeFromAccounts(request.getId());
        }));
    }

    @Override
    public APIResult<Void> resetPassword(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            EmployeeDto employee = employeeSqlMapper.toDto(dbServiceSQL.getAndCheck(EmployeeSql.class, request.getId()));
            if (employee == null) throw new GamaException("Invalid employee id=" + request.getId());

            accountService.resetPassword(employee.getEmail());
        });
    }

   	/*
	 * Advances Opening Balance
	 */

    @Override
    public APIResult<PageResponse<EmployeeOpeningBalanceDto, Void>> listOpeningBalance(PageRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                dbServiceSQL.list(request, EmployeeOpeningBalanceSql.class,
                        EmployeeOpeningBalanceSql.GRAPH_ALL, employeeOpeningBalanceSqlMapper,
                        (cb, root) -> EntityUtils.whereDoc(request, cb, root, null, null),
                        (cb, root) -> EntityUtils.orderDoc(request.getOrder(), cb, root),
                        (cb, root) -> EntityUtils.selectIdDoc(request.getOrder(), cb, root)));
    }

    @Override
    public APIResult<EmployeeOpeningBalanceDto> saveOpeningBalance(EmployeeOpeningBalanceDto request) throws GamaApiException {
        return apiResultService.result(() ->
                employeeService.saveEmployeeOpeningBalance(request));
    }

    @Override
    public APIResult<EmployeeOpeningBalanceDto> getOpeningBalance(IdRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                documentService.getDocument(EmployeeOpeningBalanceSql.class, request.getId(), request.getDb()));
    }

    @Override
    public APIResult<EmployeeOpeningBalanceDto> finishOpeningBalance(IdRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                employeeService.finishEmployeeOpeningBalance(request.getId()));
    }

    @Override
    public APIResult<EmployeeOpeningBalanceDto> importOpeningBalance(ImportDocRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                employeeService.importEmployeeOpeningBalance(request.getId(), request.getFileName()));
    }

    @Override
    public APIResult<Void> deleteOpeningBalance(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> documentService.deleteDocument(request.getId(), request.getDb()));
    }

    /*
	 * Advances
	 */

    @Override
    public APIResult<PageResponse<EmployeeOperationDto, Void>> listAdvance(PageRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                dbServiceSQL.list(request, EmployeeOperationSql.class,
                    EmployeeOperationSql.GRAPH_ALL, employeeOperationSqlMapper,
                    root -> Map.of(
                            BaseDocumentSql_.COUNTERPARTY, root.join(BaseDocumentSql_.COUNTERPARTY, JoinType.LEFT),
                            BaseDocumentSql_.EMPLOYEE, root.join(BaseDocumentSql_.EMPLOYEE, JoinType.LEFT)),
                    (cb, root, joins) -> EntityUtils.whereDoc(request, cb, root, null, joins),
                    (cb, root, joins) -> EntityUtils.orderMoneyDoc(request.getOrder(), cb, root, joins),
                    (cb, root, joins) -> EntityUtils.selectIdMoneyDoc(request.getOrder(), cb, root, joins)));
    }

    @Override
    public APIResult<EmployeeOperationDto> saveAdvance(EmployeeOperationDto request) throws GamaApiException {
        return apiResultService.result(() ->
                employeeService.saveEmployeeOperation(request));
    }

    @Override
    public APIResult<EmployeeOperationDto> getAdvance(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> 
                documentService.getDocument(EmployeeOperationSql.class, request.getId(), request.getDb()));
    }

    @Override
    public APIResult<EmployeeOperationDto> finishAdvance(FinishRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                employeeService.finishEmployeeOperation(request.id, request.finishGL));
    }

    @Override
    public APIResult<Void> deleteAdvance(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> documentService.deleteDocument(request.getId(), request.getDb()));
    }

    @Override
    public APIResult<EmployeeOperationDto> recallAdvance(IdRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                employeeService.recallEmployeeOperation(request.getId()));
    }

    /*
     *  Advances $$$ Rate Influence
     */

    @Override
    public APIResult<PageResponse<EmployeeRateInfluenceDto, Void>> listRateInfluence(PageRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                dbServiceSQL.list(request, EmployeeRateInfluenceSql.class,
                        EmployeeRateInfluenceSql.GRAPH_ALL, employeeRateInfluenceSqlMapper,
                        (cb, root) -> EntityUtils.whereDoc(request, cb, root, null, null),
                        (cb, root) -> EntityUtils.orderDoc(request.getOrder(), cb, root),
                        (cb, root) -> EntityUtils.selectIdDoc(request.getOrder(), cb, root)));
    }

    @Override
    public APIResult<EmployeeRateInfluenceDto> saveRateInfluence(EmployeeRateInfluenceDto request) throws GamaApiException {
        return apiResultService.result(() ->
                employeeService.saveEmployeeRateInfluence(request));
    }

    @Override
    public APIResult<EmployeeRateInfluenceDto> getRateInfluence(IdRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                documentService.getDocument(EmployeeRateInfluenceSql.class, request.getId(), request.getDb()));
    }

    @Override
    public APIResult<EmployeeRateInfluenceDto> finishRateInfluence(FinishRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                employeeService.finishEmployeeRateInfluence(request.id, request.finishGL));
    }

    @Override
    public APIResult<Void> deleteRateInfluence(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> documentService.deleteDocument(request.getId(), request.getDb()));
    }

    @Override
    public APIResult<EmployeeRateInfluenceDto> recallRateInfluence(IdRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                employeeService.recallEmployeeRateInfluence(request.getId()));
    }

    @Override
    public APIResult<List<RepMoneyBalance<EmployeeDto>>> genRateInfluence(DateRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                moneyAccountService.genRateInfluence(request.getDate(), AccountType.EMPLOYEE));
    }

    /*
	 * Reports
	 */

    @Override
    public APIResult<List<RepMoneyBalance<EmployeeDto>>> reportBalance(ReportBalanceRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                employeeService.reportEmployeeBalance(request));
    }

    @Override
    public APIResult<PageResponse<MoneyHistoryDto, RepMoneyDetail<EmployeeDto>>> reportFlow(PageRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                employeeService.reportEmployeeFlow(request));
    }

    @Override
    public APIResult<RepMoneyBalanceInterval> reportBalanceInterval(ReportBalanceIntervalRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                moneyAccountService.reportBalanceInterval(request, AccountType.EMPLOYEE));
    }
}

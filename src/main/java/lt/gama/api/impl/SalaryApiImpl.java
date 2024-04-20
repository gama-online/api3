package lt.gama.api.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.*;
import lt.gama.api.response.EmployeeVacationResponse;
import lt.gama.api.response.PageResponse;
import lt.gama.api.response.SalaryEmployeeChargeResponse;
import lt.gama.api.service.SalaryApi;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.*;
import lt.gama.model.dto.documents.SalaryDto;
import lt.gama.model.dto.entities.*;
import lt.gama.model.mappers.*;
import lt.gama.model.sql.base.BaseDocumentSql_;
import lt.gama.model.sql.documents.SalarySql;
import lt.gama.model.sql.documents.SalarySql_;
import lt.gama.model.sql.documents.items.EmployeeChargeSql;
import lt.gama.model.sql.documents.items.EmployeeChargeSql_;
import lt.gama.model.sql.entities.*;
import lt.gama.model.type.auth.CompanySalarySettings;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.model.type.enums.CustomSearchType;
import lt.gama.model.type.enums.DBType;
import lt.gama.model.type.salary.WorkHoursPosition;
import lt.gama.model.type.salary.WorkTimeCode;
import lt.gama.service.*;
import lt.gama.service.ex.rt.GamaException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

/**
 * gama-online
 * Created by valdas on 2016-02-09.
 */
@RestController
public class SalaryApiImpl implements SalaryApi {

    @PersistenceContext
    private EntityManager entityManager;

    private final DBServiceSQL dbServiceSQL;
    private final SalaryService salaryService;
    private final DocumentService documentService;
    private final EmployeeCardSqlMapper employeeCardSqlMapper;
    private final WorkScheduleSqlMapper workScheduleSqlMapper;
    private final PositionSqlMapper positionSqlMapper;
    private final Auth auth;
    private final EmployeeChargeSqlMapper employeeChargeSqlMapper;
    private final EmployeeAbsenceSqlMapper employeeAbsenceSqlMapper;
    private final EmployeeSqlMapper employeeSqlMapper;
    private final ChargeSqlMapper chargeSqlMapper;
    private final EmployeeVacationSqlMapper employeeVacationSqlMapper;
    private final WorkHoursSqlMapper workHoursSqlMapper;
    private final SalarySqlMapper salarySqlMapper;
    private final SalarySettingsService salarySettingsService;
    private final APIResultService apiResultService;

    public SalaryApiImpl(DBServiceSQL dbServiceSQL, SalaryService salaryService, DocumentService documentService, EmployeeCardSqlMapper employeeCardSqlMapper, WorkScheduleSqlMapper workScheduleSqlMapper, PositionSqlMapper positionSqlMapper, Auth auth, EmployeeChargeSqlMapper employeeChargeSqlMapper, EmployeeAbsenceSqlMapper employeeAbsenceSqlMapper, EmployeeSqlMapper employeeSqlMapper, ChargeSqlMapper chargeSqlMapper, EmployeeVacationSqlMapper employeeVacationSqlMapper, WorkHoursSqlMapper workHoursSqlMapper, SalarySqlMapper salarySqlMapper, SalarySettingsService salarySettingsService, APIResultService apiResultService) {
        this.dbServiceSQL = dbServiceSQL;
        this.salaryService = salaryService;
        this.documentService = documentService;
        this.employeeCardSqlMapper = employeeCardSqlMapper;
        this.workScheduleSqlMapper = workScheduleSqlMapper;
        this.positionSqlMapper = positionSqlMapper;
        this.auth = auth;
        this.employeeChargeSqlMapper = employeeChargeSqlMapper;
        this.employeeAbsenceSqlMapper = employeeAbsenceSqlMapper;
        this.employeeSqlMapper = employeeSqlMapper;
        this.chargeSqlMapper = chargeSqlMapper;
        this.employeeVacationSqlMapper = employeeVacationSqlMapper;
        this.workHoursSqlMapper = workHoursSqlMapper;
        this.salarySqlMapper = salarySqlMapper;
        this.salarySettingsService = salarySettingsService;
        this.apiResultService = apiResultService;
    }

    /*
     *  Charge
     */

    @Override
    public APIResult<PageResponse<ChargeDto, Void>> listCharge(PageRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                dbServiceSQL.list(request, ChargeSql.class, null, chargeSqlMapper,
                        (cb, root) -> {
                            if (StringHelper.hasValue(request.getFilter())) {
                                String filter = StringUtils.stripAccents(request.getFilter().trim()).toLowerCase();
                                return cb.or(
                                        cb.like(cb.lower(cb.function("unaccent", String.class, root.get(ChargeSql_.NAME))), "%" + filter + "%"),
                                        cb.like(cb.lower(cb.function("unaccent", String.class, root.get(ChargeSql_.DEBIT).get("name"))), "%" + filter + "%"),
                                        cb.like(cb.lower(cb.function("unaccent", String.class, root.get(ChargeSql_.CREDIT).get("name"))), "%" + filter + "%"),
                                        cb.like(root.get(ChargeSql_.DEBIT).get("number"), "%" + filter + "%"),
                                        cb.like(root.get(ChargeSql_.CREDIT).get("number"), "%" + filter + "%")
                                );
                            }
                            return null;
                        },
                        (cb, root) -> Arrays.asList(cb.asc(root.get(ChargeSql_.NAME)), cb.asc(root.get(ChargeSql_.ID))),
                        (cb, root) -> Arrays.asList(root.get(ChargeSql_.NAME), root.get(ChargeSql_.ID).alias("id"))));
    }

    @Override
    public APIResult<ChargeDto> saveCharge(ChargeDto request) throws GamaApiException {
        return apiResultService.result(() -> salaryService.saveCharge(request));
    }

    @Override
    public APIResult<ChargeDto> getCharge(IdRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                chargeSqlMapper.toDto(dbServiceSQL.getById(ChargeSql.class, request.getId())));
    }

    @Override
    public APIResult<Void> deleteCharge(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> dbServiceSQL.deleteById(ChargeSql.class, request.getId()));
    }


    /*
     *  WorkSchedule
     */

    @Override
    public APIResult<PageResponse<WorkScheduleDto, Void>> listWorkSchedule(PageRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                dbServiceSQL.list(request, WorkScheduleSql.class, null, workScheduleSqlMapper, null, null, null));
    }

    @Override
    public APIResult<WorkScheduleDto> saveWorkSchedule(WorkScheduleDto request) throws GamaApiException {
        return apiResultService.result(() ->
                workScheduleSqlMapper.toDto(dbServiceSQL.saveEntityInCompany(workScheduleSqlMapper.toEntity(request))));
    }

    @Override
    public APIResult<WorkScheduleDto> getWorkSchedule(IdRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                workScheduleSqlMapper.toDto(dbServiceSQL.getById(WorkScheduleSql.class, request.getId())));
    }

    @Override
    public APIResult<Void> deleteWorkSchedule(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> dbServiceSQL.deleteById(WorkScheduleSql.class, request.getId()));
    }

    /*
     * Work hours
     */

    @Override
    public APIResult<PageResponse<WorkHoursDto, Void>> listWorkHours(PageRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                dbServiceSQL.list(request, WorkHoursSql.class, WorkHoursSql.GRAPH_ALL, workHoursSqlMapper,
                        (cb, root) -> {
                            Object value = PageRequestUtils.getFieldValue(request.getConditions(), CustomSearchType.DATE);
                            LocalDate dt = value instanceof LocalDate
                                    ? (LocalDate) value
                                    : value instanceof String ? DateUtils.parseLocalDate((String) value) : null;
                            if (dt == null) {
                                throw new GamaException("No date");
                            }
                            Predicate where = cb.equal(root.get(WorkHoursSql_.DATE), cb.literal(dt));

                            if (StringHelper.hasValue(request.getFilter())) {
                                String filter = StringUtils.stripAccents(request.getFilter().trim()).toLowerCase();
                                Predicate predicate = cb.or(
                                        cb.like(cb.lower(cb.function("unaccent", String.class, root.get(WorkHoursSql_.EMPLOYEE).get(EmployeeSql_.NAME))), "%" + filter + "%"),
                                        cb.like(cb.lower(cb.function("unaccent", String.class, root.get(WorkHoursSql_.EMPLOYEE).get(EmployeeSql_.EMAIL))), "%" + filter + "%")
                                );
                                where = where == null ? predicate : cb.and(where, predicate);
                            }
                            return where;
                        },
                        (cb, root) -> Arrays.asList(
                                cb.asc(root.get(WorkHoursSql_.DATE)),
                                cb.asc(root.get(WorkHoursSql_.EMPLOYEE).get(EmployeeSql_.NAME)),
                                cb.asc(root.get(WorkHoursSql_.ID))),
                        (cb, root) -> Arrays.asList(
                                root.get(WorkHoursSql_.DATE),
                                root.get(WorkHoursSql_.EMPLOYEE).get(EmployeeSql_.NAME),
                                root.get(WorkHoursSql_.ID).alias("id"))));
    }

    @Override
    public APIResult<WorkHoursDto> saveWorkHours(WorkHoursDto request) throws GamaApiException {
        return apiResultService.result(() -> salaryService.saveWorkHours(request, true));
    }

    @Override
    public APIResult<WorkHoursDto> getWorkHours(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            LocalDate dateParam = LocalDate.parse(request.getParentName());
            return salaryService.getWorkHours(request.getId(), dateParam.getYear(), dateParam.getMonthValue());
        });
    }

    @Override
    public APIResult<Void> deleteWorkHours(WorkHoursRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                salaryService.deleteWorkHours(request.getEmployeeId(), request.getYear(), request.getMonth()));
    }

    @Override
    public APIResult<WorkHoursDto> generateWorkHours(GenerateWorkHoursRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
            CompanySalarySettings companySalarySettings = salarySettingsService.getCompanySalarySettings(request.getYear(), request.getMonth());
            EmployeeDto employee = employeeSqlMapper.toDto(dbServiceSQL.getAndCheck(EmployeeSql.class, request.getEmployeeId()));
            EmployeeCardDto employeeCard = employeeCardSqlMapper.toDto(dbServiceSQL.getAndCheck(EmployeeCardSql.class, request.getEmployeeId()));
            return salaryService.generateWorkHoursForEmployee(companySalarySettings,
                    companySettings.getCountry(), request.getYear(), request.getMonth(), employee, employeeCard);
        });
    }

    @Override
    public APIResult<WorkHoursPosition> generateWorkHoursPosition(GenerateWorkHoursPositionRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
            return salaryService.generateWorkHoursPosition(companySettings,
                    request.getYear(), request.getMonth(), request.getPosition(), request.getHired(), request.getFired());
        });
    }

    @Override
    public APIResult<String> refreshWorkHoursTask(RefreshWorkHoursRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                salaryService.refreshWorkHoursTask(request.getYear(), request.getMonth(),
                        BooleanUtils.isTrue(request.getFresh())));
    }

    /*
     *  Work Time Codes
     */

    @Override
    public APIResult<List<WorkTimeCode>> listWorkTimeCodes() throws GamaApiException {
        return apiResultService.result(() -> {
            final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
            return salaryService.listWorkTimeCodes(companySettings.getCountry());
        });
    }

    /*
     *  Position
     */

    @Override
    public APIResult<PageResponse<PositionDto, Void>> listPosition(PageRequest request) throws GamaApiException {
        return apiResultService.result(() -> dbServiceSQL.list(request, PositionSql.class, null, positionSqlMapper,
                (cb, root) -> {
                    if (StringHelper.hasValue(request.getFilter())) {
                        String filter = StringUtils.stripAccents(request.getFilter().trim()).toLowerCase();
                        return cb.or(
                                cb.like(cb.lower(cb.function("unaccent", String.class, root.get(PositionSql_.NAME))), "%" + filter + "%"),
                                cb.like(cb.lower(cb.function("unaccent", String.class, root.get(PositionSql_.DESCRIPTION))), "%" + filter + "%")
                        );
                    }
                    return null;
                },
                (cb, root) -> Arrays.asList(cb.asc(root.get(PositionSql_.NAME)), cb.asc(root.get(PositionSql_.ID))),
                (cb, root) -> Arrays.asList(root.get(PositionSql_.NAME), root.get(PositionSql_.ID).alias("id"))));
    }

    @Override
    public APIResult<PositionDto> savePosition(PositionDto request) throws GamaApiException {
        return apiResultService.result(() -> salaryService.savePosition(request));
    }

    @Override
    public APIResult<PositionDto> getPosition(IdRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                positionSqlMapper.toDto(dbServiceSQL.getById(PositionSql.class, request.getId())));
    }

    @Override
    public APIResult<Void> deletePosition(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> dbServiceSQL.deleteById(PositionSql.class, request.getId()));
    }

    /*
     *  Employee Card
     */

    @Override
    public APIResult<EmployeeCardDto> saveEmployeeCard(EmployeeCardDto request) throws GamaApiException {
        return apiResultService.result(() -> salaryService.saveEmployeeCard(request));
    }

    @Override
    public APIResult<EmployeeCardDto> getEmployeeCard(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> salaryService.getEmployeeCard(request.getId()));
    }

    @Override
    public APIResult<Void> deleteEmployeeCard(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> dbServiceSQL.deleteById(EmployeeCardSql.class, request.getId()));
    }

    /*
     *  Employee Salary History
     */

    @Override
    public APIResult<EmployeeCardDto> saveEmployeeCardSalaryHistory(EmployeeCardDto request) throws GamaApiException {
        return apiResultService.result(() -> salaryService.saveEmployeeCardSalaryHistory(request));
    }

    /*
     *  Employee Vacation
     */

    @Override
    public APIResult<PageResponse<EmployeeVacationDto, Void>> listEmployeeVacation(PageRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                dbServiceSQL.list(request, EmployeeVacationSql.class, EmployeeVacationSql.GRAPH_ALL, employeeVacationSqlMapper,
                        (cb, root) -> {
                            if (StringHelper.hasValue(request.getFilter())) {
                                String filter = StringUtils.stripAccents(request.getFilter().trim()).toLowerCase();
                                return cb.like(cb.lower(cb.function("unaccent", String.class,
                                        root.get(EmployeeVacationSql_.EMPLOYEE).get(EmployeeSql_.NAME))), "%" + filter + "%");
                            }
                            return null;
                        },
                        (cb, root) -> Arrays.asList(
                                cb.asc(root.get(EmployeeVacationSql_.EMPLOYEE).get(EmployeeSql_.NAME)),
                                cb.asc(root.get(EmployeeVacationSql_.ID))),
                        (cb, root) -> Arrays.asList(
                                root.get(EmployeeVacationSql_.EMPLOYEE).get(EmployeeSql_.NAME),
                                root.get(EmployeeVacationSql_.ID).alias("id"))));
    }

    @Override
    public APIResult<EmployeeVacationDto> saveEmployeeVacation(EmployeeVacationDto request) throws GamaApiException {
        return apiResultService.result(() -> salaryService.saveEmployeeVacation(request));
    }

    @Override
    public APIResult<EmployeeVacationDto> getEmployeeVacation(IdRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                employeeVacationSqlMapper.toDto(dbServiceSQL.getById(EmployeeVacationSql.class, request.getId())));
    }

    @Override
    public APIResult<Void> deleteEmployeeVacation(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> dbServiceSQL.deleteById(EmployeeVacationSql.class, request.getId()));
    }

    @Override
    public APIResult<EmployeeVacationResponse> calcEmployeeVacation(CalcEmployeeVacationRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                salaryService.calculateEmployeeVacation(request.getId(), request.getDate()));
    }

    /*
     *  Salary
     */

    @Override
    public APIResult<PageResponse<SalaryDto, Void>> listSalary(PageRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                dbServiceSQL.list(request, SalarySql.class, null, salarySqlMapper,
                        null,
                        (cb, root) -> orderDoc(request.getOrder(), cb, root),
                        this::selectIdDoc));
    }

    private List<Order> orderDoc(String orderBy, CriteriaBuilder cb, Root<?> root) {
        if ("-mainIndex".equals(orderBy)) {
            return Arrays.asList(
                    cb.desc(root.get(BaseDocumentSql_.DATE)),
                    cb.desc(root.get(BaseDocumentSql_.ORDINAL)),
                    cb.desc(root.get(BaseDocumentSql_.SERIES)),
                    cb.desc(root.get(BaseDocumentSql_.NUMBER)),
                    cb.desc(root.get(BaseDocumentSql_.ID)));
        }
        return Arrays.asList(
                cb.asc(root.get(BaseDocumentSql_.DATE)),
                cb.asc(root.get(BaseDocumentSql_.ORDINAL)),
                cb.asc(root.get(BaseDocumentSql_.SERIES)),
                cb.asc(root.get(BaseDocumentSql_.NUMBER)),
                cb.asc(root.get(BaseDocumentSql_.ID)));
    }

    private List<Selection<?>> selectIdDoc(CriteriaBuilder cb, Root<?> root) {
        return Arrays.asList(
                root.get(BaseDocumentSql_.DATE),
                root.get(BaseDocumentSql_.ORDINAL),
                root.get(BaseDocumentSql_.SERIES),
                root.get(BaseDocumentSql_.NUMBER),
                root.get(BaseDocumentSql_.ID).alias("id"));
    }

    @Override
    public APIResult<SalaryDto> saveSalary(SalaryDto request) throws GamaApiException {
        return apiResultService.result(() -> salaryService.saveSalary(request));
    }

    @Override
    public APIResult<SalaryDto> getSalary(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> documentService.getDocument(SalarySql.class, request.getId(), DBType.POSTGRESQL));
    }

    @Override
    public APIResult<Void> deleteSalary(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> salaryService.deleteSalary(request.getId()));
    }

    @Override
    public APIResult<SalaryDto> finishSalary(FinishSalaryRequest request) throws GamaApiException {
        return apiResultService.result(() -> salaryService.finishSalary(request.id, true, request.finishGL));
    }

    @Override
    public APIResult<SalaryDto> recallSalary(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> salaryService.recallSalary(request.getId()));
    }

    @Override
    public APIResult<String> refreshSalary(RefreshSalaryRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            LocalDate salaryDate;
            try {
                salaryDate = entityManager.createQuery(
                                "SELECT " + SalarySql_.DATE + " FROM " + SalarySql.class.getName() + " a" +
                                        " WHERE " + SalarySql_.ID + " = :id" +
                                        " AND " + SalarySql_.COMPANY_ID + " = :companyId",
                                LocalDate.class)
                        .setParameter("id", request.getId())
                        .setParameter("companyId", auth.getCompanyId())
                        .getSingleResult();
            } catch (NoResultException e) {
                throw new GamaException("No salary with id=" + request.getId());
            }

            final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
            Validators.checkDocumentDate(companySettings, salaryDate, auth.getLanguage());

            return salaryService.refreshSalary(request.getId(), request.getSalaryType(), BooleanUtils.isTrue(request.getFresh()));
        });
    }



    /*
     *  EmployeeCharge
     */

    @Override
    public APIResult<PageResponse<EmployeeChargeDto, Void>> listEmployeeCharge(PageRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                dbServiceSQL.list(request, EmployeeChargeSql.class, EmployeeChargeSql.GRAPH_ALL, employeeChargeSqlMapper,
                        listEmployeeChargeWhere(request),
                        listEmployeeChargeOrderBy(request),
                        (cb, root) -> Arrays.asList(
                                root.get(EmployeeChargeSql_.DATE),
                                root.get(EmployeeChargeSql_.EMPLOYEE).get(EmployeeSql_.NAME),
                                root.get(EmployeeChargeSql_.ID).alias("id"))));
    }

    private BiFunction<CriteriaBuilder, Root<EmployeeChargeSql>, Predicate> listEmployeeChargeWhere(PageRequest request) {
        Object employeeId = PageRequestUtils.getFieldValue(request.getConditions(), CustomSearchType.EMPLOYEE);
        if (request.getParentId() == null && employeeId == null) return null;
        if (request.getParentId() == null) {
            return (cb, root) -> cb.equal(root.get(EmployeeChargeSql_.EMPLOYEE).get(EmployeeSql_.ID), employeeId);
        } else {
            return (cb, root) -> employeeId == null
                    ? cb.equal(root.get(EmployeeChargeSql_.SALARY).get(SalarySql_.ID), request.getParentId())
                    : cb.and(
                    cb.equal(root.get(EmployeeChargeSql_.SALARY).get(SalarySql_.ID), request.getParentId()),
                    cb.equal(root.get(EmployeeChargeSql_.EMPLOYEE).get(EmployeeSql_.ID), employeeId)
            );
        }
    }

    private BiFunction<CriteriaBuilder, Root<EmployeeChargeSql>, List<Order>> listEmployeeChargeOrderBy(PageRequest request) {
        String order = request.getOrder() == null ? "x" : request.getOrder();
        return (cb, root) -> switch (order) {
            case "-name" -> Arrays.asList(
                    cb.desc(root.get(EmployeeChargeSql_.EMPLOYEE).get(EmployeeSql_.NAME)),
                    cb.desc(root.get(EmployeeChargeSql_.DATE)),
                    cb.desc(root.get(EmployeeChargeSql_.ID)));
            case "name" -> Arrays.asList(
                    cb.asc(root.get(EmployeeChargeSql_.EMPLOYEE).get(EmployeeSql_.NAME)),
                    cb.asc(root.get(EmployeeChargeSql_.DATE)),
                    cb.asc(root.get(EmployeeChargeSql_.ID)));

//            case "-employeeId" -> Arrays.asList(
//                    cb.desc(root.get(EmployeeChargeSql_.EMPLOYEE).get(EmployeeSql_.EMPLOYEE_ID)),
//                    cb.desc(root.get(EmployeeChargeSql_.EMPLOYEE).get(EmployeeSql_.NAME)),
//                    cb.desc(root.get(EmployeeChargeSql_.DATE)),
//                    cb.desc(root.get(EmployeeChargeSql_.ID)));
//            case "employeeId" -> Arrays.asList(
//                    cb.asc(root.get(EmployeeChargeSql_.EMPLOYEE).get(EmployeeSql_.EMPLOYEE_ID)),
//                    cb.asc(root.get(EmployeeChargeSql_.EMPLOYEE).get(EmployeeSql_.NAME)),
//                    cb.asc(root.get(EmployeeChargeSql_.DATE)),
//                    cb.asc(root.get(EmployeeChargeSql_.ID)));

            case "-mainIndex", "-date" -> Arrays.asList(
                    cb.desc(root.get(EmployeeChargeSql_.DATE)),
                    cb.desc(root.get(EmployeeChargeSql_.EMPLOYEE).get(EmployeeSql_.NAME)),
                    cb.desc(root.get(EmployeeChargeSql_.ID)));
            default -> Arrays.asList(
                    cb.asc(root.get(EmployeeChargeSql_.DATE)),
                    cb.asc(root.get(EmployeeChargeSql_.EMPLOYEE).get(EmployeeSql_.NAME)),
                    cb.asc(root.get(EmployeeChargeSql_.ID)));
        };
    }

    @Override
    public APIResult<SalaryEmployeeChargeResponse> saveEmployeeCharge(EmployeeChargeDto request) throws GamaApiException {
        return apiResultService.result(() -> salaryService.saveEmployeeCharge(request));
    }

    @Override
    public APIResult<EmployeeChargeDto> getEmployeeCharge(IdRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                employeeChargeSqlMapper.toDto(
                        dbServiceSQL.getById(EmployeeChargeSql.class, request.getId(), EmployeeChargeSql.GRAPH_ALL)));
    }

    @Override
    public APIResult<SalaryEmployeeChargeResponse> deleteEmployeeCharge(IdRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                salaryService.deleteEmployeeCharge(request.getParentId(), request.getId()));
    }

    @Override
    public APIResult<SalaryEmployeeChargeResponse> finishEmployeeCharge(IdRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                salaryService.finishEmployeeCharge(request.getParentId(), request.getId()));
    }

    @Override
    public APIResult<SalaryEmployeeChargeResponse> recallEmployeeCharge(IdRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                salaryService.recallEmployeeCharge(request.getParentId(), request.getId()));
    }

    @Override
    public APIResult<EmployeeChargeDto> generateEmployeeCharge(GenerateEmployeeChargeRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                salaryService.generateEmployeeCharge(request.getParentId(), request.getSalaryType(), request.getId()));
    }

    /*
     *  EmployeeAbsence
     */

    @Override
    public APIResult<PageResponse<EmployeeAbsenceDto, Void>> listAbsence(PageRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                dbServiceSQL.list(request, EmployeeAbsenceSql.class, EmployeeAbsenceSql.GRAPH_ALL, employeeAbsenceSqlMapper,
                        (cb, root) -> {
                            Object employeeId = PageRequestUtils.getFieldValue(request.getConditions(), CustomSearchType.EMPLOYEE);
                            Predicate wherePredicate = employeeId == null ? null : cb.equal(root.get(EmployeeAbsenceSql_.EMPLOYEE).get(EmployeeSql_.ID), employeeId);
                            if (StringHelper.hasValue(request.getFilter())) {
                                String filter = StringUtils.stripAccents(request.getFilter().trim()).toLowerCase();
                                Predicate filterPredicate = cb.or(
                                        cb.like(cb.lower(cb.function("unaccent", String.class, root.get(EmployeeAbsenceSql_.EMPLOYEE).get(EmployeeSql_.NAME))), "%" + filter + "%"),
                                        cb.like(cb.lower(cb.function("unaccent", String.class, root.get(EmployeeAbsenceSql_.DOCUMENT))), "%" + filter + "%")
                                );
                                wherePredicate = wherePredicate == null ? filterPredicate : cb.and(wherePredicate, filterPredicate);
                            }
                            return wherePredicate;
                        },
                        (cb, root) -> Arrays.asList(cb.asc(root.get(EmployeeAbsenceSql_.DATE_FROM)), cb.asc(root.get(EmployeeAbsenceSql_.ID))),
                        (cb, root) -> Arrays.asList(root.get(EmployeeAbsenceSql_.DATE_FROM), root.get(EmployeeAbsenceSql_.ID).alias("id"))));
    }

    @Override
    public APIResult<EmployeeAbsenceDto> saveAbsence(EmployeeAbsenceDto request) throws GamaApiException {
        return apiResultService.result(() -> salaryService.saveAbsence(request));
    }

    @Override
    public APIResult<EmployeeAbsenceDto> getAbsence(IdRequest request) throws GamaApiException {
        return apiResultService.result(() ->
                employeeAbsenceSqlMapper.toDto(dbServiceSQL.getById(EmployeeAbsenceSql.class, request.getId())));
    }

    @Override
    public APIResult<Void> deleteAbsence(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> dbServiceSQL.deleteById(EmployeeAbsenceSql.class, request.getId()));
    }
}

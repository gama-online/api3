package lt.gama.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import jakarta.persistence.*;
import lt.gama.api.request.SalaryType;
import lt.gama.api.response.EmployeeVacationResponse;
import lt.gama.api.response.SalaryEmployeeChargeResponse;
import lt.gama.api.response.TaskResponse;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.*;
import lt.gama.model.dto.documents.DoubleEntryDto;
import lt.gama.model.dto.documents.SalaryDto;
import lt.gama.model.dto.entities.*;
import lt.gama.model.i.IEmployeeCharge;
import lt.gama.model.i.IPosition;
import lt.gama.model.mappers.*;
import lt.gama.model.sql.documents.DoubleEntrySql;
import lt.gama.model.sql.documents.SalarySql;
import lt.gama.model.sql.documents.SalarySql_;
import lt.gama.model.sql.documents.items.EmployeeChargeSql;
import lt.gama.model.sql.documents.items.EmployeeChargeSql_;
import lt.gama.model.sql.entities.*;
import lt.gama.model.sql.system.CompanySql;
import lt.gama.model.sql.system.CountryWorkTimeCodeSql;
import lt.gama.model.type.*;
import lt.gama.model.type.auth.CompanySalarySettings;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.model.type.auth.CompanyTaxSettings;
import lt.gama.model.type.doc.*;
import lt.gama.model.type.enums.*;
import lt.gama.model.type.gl.GLDC;
import lt.gama.model.type.gl.GLDCActive;
import lt.gama.model.type.gl.GLOperationAccount;
import lt.gama.model.type.salary.*;
import lt.gama.service.ex.GamaWarningException;
import lt.gama.service.ex.rt.GamaException;
import lt.gama.service.ex.rt.GamaUnauthorizedException;
import lt.gama.tasks.EmployeeChargeTask;
import lt.gama.tasks.UpdateEmployeeVacationTask;
import lt.gama.tasks.WorkHoursTask;
import org.hibernate.graph.GraphSemantic;
import org.hibernate.query.NativeQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static lt.gama.Constants.DB_BATCH_SIZE;

/**
 * gama-online
 * Created by valdas on 2016-04-01.
 */
@Service
public class SalaryService {

    private static final Logger log = LoggerFactory.getLogger(SalaryService.class);

    @PersistenceContext
    private EntityManager entityManager;


    private final Auth auth;
    private final DBServiceSQL dbServiceSQL;
    private final GLUtilsService glUtilsService;
    private final GLOperationsService glOperationsService;
    private final CalendarService calendarService;
    private final DoubleEntrySqlMapper doubleEntrySqlMapper;
    private final SalarySqlMapper salarySqlMapper;
    private final EmployeeVacationSqlMapper employeeVacationSqlMapper;
    private final EmployeeCardSqlMapper employeeCardSqlMapper;
    private final WorkHoursSqlMapper workHoursSqlMapper;
    private final EmployeeAbsenceSqlMapper employeeAbsenceSqlMapper;
    private final EmployeeChargeSqlMapper employeeChargeSqlMapper;
    private final ChargeSqlMapper chargeSqlMapper;
    private final EmployeeSqlMapper employeeSqlMapper;
    private final SalarySettingsService salarySettingsService;
    private final PositionSqlMapper positionSqlMapper;
    private final AuthSettingsCacheService authSettingsCacheService;
    private final TaskQueueService taskQueueService;

    
    public SalaryService(Auth auth, DBServiceSQL dbServiceSQL, GLUtilsService glUtilsService, GLOperationsService glOperationsService, CalendarService calendarService, DoubleEntrySqlMapper doubleEntrySqlMapper, SalarySqlMapper salarySqlMapper, EmployeeVacationSqlMapper employeeVacationSqlMapper, EmployeeCardSqlMapper employeeCardSqlMapper, WorkHoursSqlMapper workHoursSqlMapper, EmployeeAbsenceSqlMapper employeeAbsenceSqlMapper, EmployeeChargeSqlMapper employeeChargeSqlMapper, ChargeSqlMapper chargeSqlMapper, EmployeeSqlMapper employeeSqlMapper, SalarySettingsService salarySettingsService, PositionSqlMapper positionSqlMapper, AuthSettingsCacheService authSettingsCacheService, TaskQueueService taskQueueService) {
        this.auth = auth;
        this.dbServiceSQL = dbServiceSQL;
        this.glUtilsService = glUtilsService;
        this.glOperationsService = glOperationsService;
        this.calendarService = calendarService;
        this.doubleEntrySqlMapper = doubleEntrySqlMapper;
        this.salarySqlMapper = salarySqlMapper;
        this.employeeVacationSqlMapper = employeeVacationSqlMapper;
        this.employeeCardSqlMapper = employeeCardSqlMapper;
        this.workHoursSqlMapper = workHoursSqlMapper;
        this.employeeAbsenceSqlMapper = employeeAbsenceSqlMapper;
        this.employeeChargeSqlMapper = employeeChargeSqlMapper;
        this.chargeSqlMapper = chargeSqlMapper;
        this.employeeSqlMapper = employeeSqlMapper;
        this.salarySettingsService = salarySettingsService;
        this.positionSqlMapper = positionSqlMapper;
        this.authSettingsCacheService = authSettingsCacheService;
        this.taskQueueService = taskQueueService;
    }

    public EmployeeChargeDto calculateTaxes(final CompanySettings companySettings,
                                            final CompanyTaxSettings companyTaxSettings,
                                            final EmployeeTaxSettings employeeTaxSettings,
                                            final EmployeeChargeDto charge) {
        Validators.checkValid(charge, "No charge");
        return employeeChargeSqlMapper.toDto(dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
                    EmployeeChargeSql entity = Validators.checkNotNull(
                            dbServiceSQL.getById(EmployeeChargeSql.class, charge.getId()),
                            "No charge with id {0}", charge.getId());
                    doTaxes(companySettings, companyTaxSettings, employeeTaxSettings, entity);
                    entity = dbServiceSQL.saveEntityInCompany(entity);
                    return entity;
                }));
    }

    private boolean isTaxApplied(GLDCActive tax) {
        return tax != null && tax.isActive();
    }

    private GLOperationAccount getDebit(GLDC companyTaxSettings, GLDC chargeSettings) {
        return chargeSettings != null && chargeSettings.getDebitEx() != null ? chargeSettings.getDebitEx() :
                companyTaxSettings != null ? companyTaxSettings.getDebitEx() : null;
    }

    private GLOperationAccount getCredit(GLDC companyTaxSettings, GLDC chargeSettings) {
        return chargeSettings != null && chargeSettings.getCreditEx() != null ? chargeSettings.getCreditEx() :
                companyTaxSettings != null ? companyTaxSettings.getCreditEx() : null;
    }

    private String getKey(GLOperationAccount debit, GLOperationAccount credit) {
        return Validators.isValid(debit) && Validators.isValid(credit) ? debit.getNumber() + '\u0001' + credit.getNumber() : null;
    }


    private void doGenerateOperation(String title, GLDC companyTaxSettings, GLDC chargeSettings, GamaMoney amount, Map<String, GLOperationDto> operations) {
        GLOperationAccount debit = getDebit(companyTaxSettings, chargeSettings);
        GLOperationAccount credit = getCredit(companyTaxSettings, chargeSettings);
        String key = getKey(debit, credit);
        if (key == null) {
            throw new GamaException(title + " - No debit and/or credit");
        }

        GLOperationDto operation = operations.get(key);
        if (operation == null) {
            operation = new GLOperationDto(debit, credit, amount);
        } else {
            operation.setSum(GamaMoneyUtils.add(operation.getAmount(), amount));
        }
        operations.put(key, operation);
    }

    private void shareProportional(GamaMoney total, GamaMoney tax, Collection<GLOperationDto> operations) {
        GamaMoney remainder = tax;
        Iterator<GLOperationDto> it = operations.iterator();
        while (it.hasNext()) {
            GLOperationDto operation = it.next();
            if (GamaMoneyUtils.isZero(tax) || GamaMoneyUtils.isZero(operation.getAmount())) {
                it.remove();
            } else {
                GamaMoney a = tax.multipliedBy(operation.getAmount().getAmount().doubleValue() / total.getAmount().doubleValue());
                operation.setSum(a);
                remainder = GamaMoneyUtils.subtract(remainder, a);
            }
        }

        // if remainder no zero - add to first operation
        if (GamaMoneyUtils.isNonZero(remainder)) {
            it = operations.iterator();
            if (it.hasNext()) {
                GLOperationDto operation = it.next();
                operation.setSum(GamaMoneyUtils.add(operation.getAmount(), remainder));
            } else {
                throw new GamaException("Can't share tax amount: " + total);
            }
        }
    }

    private GamaMoney doShareOperation(GamaMoney total, Map<String, GLOperationDto> operations, BigDecimal taxRate) {
        GamaMoney tax = BigDecimalUtils.isZero(taxRate) || GamaMoneyUtils.isZero(total) ? null :
                total.multipliedBy(taxRate).dividedBy(100);

        shareProportional(total, tax, operations.values());

        return tax;
    }

    private void mergeOperations(Map<String, GLOperationDto> dst, Map<String, GLOperationDto> src) {
        for (Map.Entry<String, GLOperationDto> entry : src.entrySet()) {
            GLOperationDto operation = dst.get(entry.getKey());
            if (operation == null) {
                dst.put(entry.getKey(), entry.getValue());
            } else {
                operation.setSum(GamaMoneyUtils.add(operation.getAmount(), entry.getValue().getAmount()));
            }
        }
    }

    private void add(Map<Optional<BigDecimal>, GamaMoney> map, BigDecimal rate, GamaMoney amount) {
        Optional<BigDecimal> key = Optional.ofNullable(rate);
        GamaMoney value = map.get(key);
        if (value == null) map.put(key, amount);
        else map.put(key, GamaMoneyUtils.add(value, amount));
    }

    public void doTaxes(CompanySettings companySettings, CompanyTaxSettings companyTaxSettings,
                        EmployeeTaxSettings employeeTaxSettings, IEmployeeCharge charge) {
        if (companyTaxSettings == null || charge == null || CollectionsHelper.isEmpty(charge.getCharges()) || employeeTaxSettings == null) return;

        charge.setTotal(null);
        charge.setTotalIncome(null);
        charge.setTotalSS(null);
        charge.setNet(null);

        charge.setIncomeTax(null);
        charge.setCompanySSTax(null);
        charge.setEmployeeSSTax(null);
        charge.setGuarantyFundTax(null);
        charge.setTaxExempt(null);
        charge.setShiTax(null);

        Map<String, GLOperationDto> operationCharges = new HashMap<>();

        Map<Optional<BigDecimal>, Map<String, GLOperationDto>> operationIncome = new HashMap<>();
        Map<String, GLOperationDto> operationCompanySS = new HashMap<>();
        Map<String, GLOperationDto> operationEmployeeSS = new HashMap<>();
        Map<String, GLOperationDto> operationGuarantyFund = new HashMap<>();
        Map<String, GLOperationDto> operationShi = new HashMap<>();

        Map<Optional<BigDecimal>, GamaMoney> totalIncomeByRate = new HashMap<>();
        GamaMoney totalIncome = null;
        GamaMoney totalCompanySS = null;
        GamaMoney totalEmployeeSS = null;
        GamaMoney totalGuarantyFund = null;
        GamaMoney totalShi = null;

        for (DocChargeAmount chargeAmount : charge.getCharges()) {

            DocCharge docCharge = chargeAmount.getCharge();

            // skip nulls and 'Advance'
            if (docCharge == null || GamaMoneyUtils.isZero(chargeAmount.getAmount()) ||
                    (companySettings != null && companySettings.getChargeAdvance() != null &&
                            Objects.equals(docCharge.getId(), companySettings.getChargeAdvance().getId())))
                continue;

            GamaMoney amount = chargeAmount.getAmount();
            charge.setTotal(GamaMoneyUtils.add(charge.getTotal(), amount));

            doGenerateOperation("Charge", companyTaxSettings.getIncome(), new GLDC(docCharge.getDebit(), docCharge.getCredit()), amount, operationCharges);

            if (isTaxApplied(docCharge.getIncomeTax())) {
                totalIncome = GamaMoneyUtils.add(totalIncome, amount);
                BigDecimal rate = BigDecimalUtils.firstNotNull(docCharge.getIncomeTax().getRate(), employeeTaxSettings.getIncomeTaxRate(), companyTaxSettings.getIncomeTaxRate());
                add(totalIncomeByRate, rate, amount);
                doGenerateOperation("Income", companyTaxSettings.getIncome(), docCharge.getIncomeTax(), amount,
                        operationIncome.computeIfAbsent(Optional.ofNullable(rate), k -> new HashMap<>()));
            }
            if (isTaxApplied(docCharge.getCompanySSTax())) {
                totalCompanySS = GamaMoneyUtils.add(totalCompanySS, amount);
                doGenerateOperation("Company S.S.", companyTaxSettings.getCompanySS(), docCharge.getCompanySSTax(), amount, operationCompanySS);
            }
            if (isTaxApplied(docCharge.getEmployeeSSTax())) {
                totalEmployeeSS = GamaMoneyUtils.add(totalEmployeeSS, amount);
                doGenerateOperation("Employee S.S.", companyTaxSettings.getEmployeeSS(), docCharge.getEmployeeSSTax(), amount, operationEmployeeSS);
            }
            if (isTaxApplied(docCharge.getGuarantyFund())) {
                totalGuarantyFund = GamaMoneyUtils.add(totalGuarantyFund, amount);
                doGenerateOperation("Guaranty Fund", companyTaxSettings.getGuarantyFund(), docCharge.getGuarantyFund(), amount, operationGuarantyFund);
            }
            if (isTaxApplied(docCharge.getShiTax())) {
                totalShi = GamaMoneyUtils.add(totalShi, amount);
                doGenerateOperation("S.H.I.", companyTaxSettings.getShi(), docCharge.getShiTax(), amount, operationShi);
            }
        }

        charge.setCompanySSTax(doShareOperation(totalCompanySS, operationCompanySS, companyTaxSettings.getCompanySSTaxRate()));
        Integer addTaxIndex = employeeTaxSettings.getEmployeeSSAddTaxRateIndex();
        BigDecimal tax = BigDecimalUtils.isNonZero(employeeTaxSettings.getEmployeeSSTaxRate()) ? employeeTaxSettings.getEmployeeSSTaxRate() :
                companyTaxSettings.getEmployeeSSTaxRate();
        charge.setEmployeeSSTax(doShareOperation(totalEmployeeSS, operationEmployeeSS,
                BigDecimalUtils.add(tax, companyTaxSettings.getEmployeeSSAddTaxRates() != null &&
                        addTaxIndex != null && addTaxIndex >= 0 && addTaxIndex < companyTaxSettings.getEmployeeSSAddTaxRates().size() ?
                        companyTaxSettings.getEmployeeSSAddTaxRates().get(addTaxIndex) : BigDecimal.ZERO)));
        charge.setGuarantyFundTax(doShareOperation(totalGuarantyFund, operationGuarantyFund, companyTaxSettings.getGuarantyFundTaxRate()));
        charge.setShiTax(doShareOperation(totalShi, operationShi, companyTaxSettings.getShiTaxRate()));

        if (employeeTaxSettings.getTaxExempt() != null) {
            charge.setTaxExempt(employeeTaxSettings.getTaxExempt());

        } else {
            charge.setTaxExempt(taxExempt(charge.getDate(), totalIncome));
        }
        charge.setAddTaxExempt(employeeTaxSettings.getAddTaxExempt());

        Map<Optional<BigDecimal>, GamaMoney> incomeTaxByRate = incomeTax(charge.getTaxExempt(), charge.getAddTaxExempt(), totalIncome, totalIncomeByRate);
        GamaMoney incomeTax = null;
        if (incomeTaxByRate != null) {
            for (GamaMoney incomeTaxValue : incomeTaxByRate.values()) {
                incomeTax = GamaMoneyUtils.add(incomeTax, incomeTaxValue);
            }
        }
        for (Map.Entry<Optional<BigDecimal>, GamaMoney> optionalMoneyEntry : totalIncomeByRate.entrySet()) {
            GamaMoney income = optionalMoneyEntry.getValue();
            Optional<BigDecimal> rate = optionalMoneyEntry.getKey();
            shareProportional(income, incomeTaxByRate != null ? incomeTaxByRate.get(rate) : null, operationIncome.get(rate).values());
        }

        charge.setIncomeTax(incomeTax);
        charge.setTotalIncome(totalIncome);
        charge.setTotalSS(GamaMoneyUtils.max(totalCompanySS, totalEmployeeSS));

        charge.setNet(GamaMoneyUtils.subtract(charge.getTotal(),
                GamaMoneyUtils.total(charge.getIncomeTax(), charge.getEmployeeSSTax(), charge.getShiTax())));
        charge.setNetTotal(GamaMoneyUtils.subtract(charge.getNet(), charge.getAdvance()));

        // write G.L. operation
        Map<String, GLOperationDto> operations = new HashMap<>();
        mergeOperations(operations, operationCharges);
        for (Map<String, GLOperationDto> ops : operationIncome.values()) {
            mergeOperations(operations, ops);
        }
        mergeOperations(operations, operationCompanySS);
        mergeOperations(operations, operationEmployeeSS);
        mergeOperations(operations, operationGuarantyFund);
        mergeOperations(operations, operationShi);

        charge.setOperations(new ArrayList<>());
        List<GLOperationDto> operationsSorted = new ArrayList<>(operations.values());
        operationsSorted.sort(Comparator.comparing(GLOperationDto::getDebit).thenComparing(GLOperationDto::getCredit));

        for (GLOperationDto operation : operationsSorted) {
            if (GamaMoneyUtils.isNonZero(operation.getAmount())) {
                charge.getOperations().add(operation);
            }
        }
    }

    // taxExempt - coefficient * (amount - minSalary)
    // or (amount - minSalary) * (-coefficient) + taxExempt
    private GamaMoney taxExemptFormula(GamaMoney amount, int minSalary, int taxExempt, double coefficient) {
        if (GamaMoneyUtils.isZero(amount) || GamaMoneyUtils.isNegativeOrZero(amount.minus(minSalary))) {
            return amount.withAmount(taxExempt);
        }
        GamaMoney result = amount.minus(minSalary).multipliedBy(-coefficient).plus(taxExempt);
        return GamaMoneyUtils.isNegative(result) ? result.withAmount(0) : result;
    }


    public GamaMoney taxExempt(LocalDate date, GamaMoney amount) {
        if (date.isBefore(LocalDate.of(2016, 1, 1))) {
            log.error(this.getClass().getSimpleName() + ": Invalid date=" + date + " - no formula to calculate tax exempt");
            String msg = MessageFormat.format(
                    TranslationService.getInstance().translate(TranslationService.SALARY.WrongDateForTaxExempt, auth.getLanguage()),
                    date);
            throw new IllegalArgumentException(msg);
        }

        if (amount == null) return null;

        if (date.isBefore(LocalDate.of(2017, 1, 1))) {
            // 2016-01-01 - 2016.12.31: 200 - 0.34 * (Suma - 350)
            return taxExemptFormula(amount, 350, 200, 0.34);

        } else if (date.isBefore(LocalDate.of(2018, 1, 1))) {
            // 2017-01-01 - 2017-12-31: 310 - 0.5 * (Suma - 380)
            return taxExemptFormula(amount, 380, 310, 0.5);

        } else if (date.isBefore(LocalDate.of(2019, 1, 1))) {
            // 2018-01-01 - 2018-12-31: 380 - 0.5 * (Suma - 400)
            return taxExemptFormula(amount, 400, 380, 0.5);

        } else if (date.isBefore(LocalDate.of(2020, 1, 1))) {
            // 2019-01-01 - 2019-12-31: 300 - 0.15 * (Suma - 555)
            return taxExemptFormula(amount, 555, 300, 0.15);

        } else if (date.isBefore(LocalDate.of(2020, 7, 1))) {
            // 2020-01-01 - 2020-12-31: 350 - 0.17 * (Suma - 607)
            return taxExemptFormula(amount, 607, 350, 0.17);

        } else if (date.isBefore(LocalDate.of(2021, 1, 1))) {
            // 2020-07-01 - 2020-12-31: 400 - 0.19 * (Suma - 607)
            return taxExemptFormula(amount, 607, 400, 0.19);

        } else if (date.isBefore(LocalDate.of(2022, 1, 1))) {
            // 2021-01-01 - 2021-12-31: 400 - 0.18 * (Suma - 642)
            return taxExemptFormula(amount, 642, 400, 0.18);

        } else if (date.isBefore(LocalDate.of(2022, 6, 1))) {
            // 2022-01-01 - 2022-05-31:
            if (BigDecimalUtils.isLessThanOrEqual(amount.getAmount(), BigDecimal.valueOf(1678))) {
                //  if amount < 1678 then NPD = 460 – 0.26 x (amount - MMA)
                return taxExemptFormula(amount, 730, 460, 0.26);
            } else {
                //  else NPD = 400 – 0.18 x (amount - 642)
                return taxExemptFormula(amount, 642, 400, 0.18);
            }

        } else if (date.isBefore(LocalDate.of(2023, 1, 1))) {
            // 2022-06-01 - 2022-12-31:
            if (BigDecimalUtils.isLessThanOrEqual(amount.getAmount(), BigDecimal.valueOf(1704))) {
                //  if amount < 1704 then NPD = 540 – 0.34 x (amount - 730)
                return taxExemptFormula(amount, 730, 540, 0.34);
            } else {
                //  else NPD = 400 – 0.18 x (amount - 642)
                return taxExemptFormula(amount, 642, 400, 0.18);
            }

        } else if (date.isBefore(LocalDate.of(2024, 1, 1))) {
            // 2023-01-01 - 2022-13-31:
            if (BigDecimalUtils.isLessThanOrEqual(amount.getAmount(), BigDecimal.valueOf(1926))) {
                //  if amount < 1926 then NPD = 625 – 0.42 x (amount - 840)
                return taxExemptFormula(amount, 840, 625, 0.42);
            } else {
                //  else NPD = 400 – 0.18 x (amount - 642)
                return taxExemptFormula(amount, 642, 400, 0.18);
            }

        } else {
            // 2024-01-01 -
            if (BigDecimalUtils.isLessThanOrEqual(amount.getAmount(), BigDecimal.valueOf(2167))) {
                //  if amount < 2167 then NPD = 747 – 0.5 x (amount - 924)
                return taxExemptFormula(amount, 924, 747, 0.5);
            } else {
                //  else NPD = 400 – 0.18 x (amount - 642)
                return taxExemptFormula(amount, 642, 400, 0.18);
            }
        }
    }

    private Map<Optional<BigDecimal>, GamaMoney> incomeTax(GamaMoney taxExempt, GamaMoney addTaxExempt, GamaMoney total, Map<Optional<BigDecimal>, GamaMoney> amountByRate) {
        if (GamaMoneyUtils.isZero(total) || CollectionsHelper.isEmpty(amountByRate)) return null;
        if (amountByRate.size() == 1 && BigDecimalUtils.isZero(amountByRate.keySet().iterator().next().orElse(null))) return null;

        GamaMoney totalExempt = GamaMoneyUtils.total(taxExempt, addTaxExempt);

        if (GamaMoneyUtils.isLessThanOrEqual(total, totalExempt)) return null;

        double totalExemptNumber = GamaMoneyUtils.isZero(totalExempt) ? 0 : totalExempt.getAmount().doubleValue();
        double totalNumber = total.getAmount().doubleValue();

        Map<Optional<BigDecimal>, GamaMoney> result = new HashMap<>();
        for (Map.Entry<Optional<BigDecimal>, GamaMoney> entry : amountByRate.entrySet()) {
            // result = (amount - amount / total * totalExempt) * taxRate
            //        = amount * (1 - totalExempt / total) * taxRate
            if (!BigDecimalUtils.isZero(entry.getKey().orElse(null))) {
                GamaMoney tax = GamaMoneyUtils.multipliedBy(entry.getValue(),
                        (1.0 - totalExemptNumber / totalNumber) * entry.getKey().get().doubleValue() / 100.0);
                if (GamaMoneyUtils.isPositive(tax)) {
                    add(result, entry.getKey().get(), tax);
                }
            }
        }
        return result;
    }

    public EmployeeCardDto getEmployeeCard(long employeeId) {
        EmployeeCardDto employeeCard = employeeCardSqlMapper.toDto(dbServiceSQL.getAndCheckNullable(EmployeeCardSql.class, employeeId));
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        LocalDate now = DateUtils.date();
        // get and set active
        salarySettingsService.generateEmployeeTaxSettings(companySettings, salarySettingsService.getCompanyTaxSettings(now),
                salarySettingsService.getCompanySalarySettings(now), employeeCard, now);
        return employeeCard;
    }

    public EmployeeCardDto saveEmployeeCard(final EmployeeCardDto document) {
        Validators.checkArgument(document.getEmployee() != null && document.getEmployee().getId() != null,
                TranslationService.getInstance().translate(TranslationService.SALARY.NoEmployeeId, auth.getLanguage()));

        Validators.checkArgument(CollectionsHelper.hasValue(document.getPositions()), MessageFormat.format(
                TranslationService.getInstance().translate(TranslationService.SALARY.NoEmployeePosition, auth.getLanguage()),
                document.getEmployee().getName()));

        // check if there is only one main position
        int main = 0;
        for (DocPosition position : document.getPositions()) {
            if (position.isMain()) main++;
        }
        Validators.checkArgument(main <= 1, TranslationService.getInstance().translate(TranslationService.SALARY.OnlyOneMainPosition, auth.getLanguage()));

        final Long employeeId = document.getId() != null ? document.getId() : document.getEmployee().getId();

        return employeeCardSqlMapper.toDto(dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
                    Validators.checkNotNull(dbServiceSQL.getById(EmployeeSql.class, employeeId),
                            MessageFormat.format(TranslationService.getInstance().translate(TranslationService.SALARY.NoEmployee, auth.getLanguage()), employeeId));
                    EmployeeCardSql entity = dbServiceSQL.getById(EmployeeCardSql.class, employeeId);
                    if (entity == null) {
                        entity = new EmployeeCardSql();
                        entity.setEmployee(entityManager.getReference(EmployeeSql.class, employeeId));
                    }
                    entity.setHired(document.getHired());
                    entity.setHireNote(document.getHireNote());
                    entity.setFired(document.getFired());
                    entity.setFireNote(document.getFireNote());

                    entity.setSsn(document.getSsn());
                    entity.setNin(document.getNin());
                    entity.setSex(document.getSex());

                    List<EmployeeTaxSettings> taxes = document.getTaxes();
                    if (CollectionsHelper.hasValue(taxes)) {
                        // null goes to the end of the list
                        taxes.sort(Comparator.comparing(EmployeeTaxSettings::getDate, Comparator.nullsLast(Comparator.naturalOrder())));
                    }
                    entity.setTaxes(taxes);
                    entity.setPositions(document.getPositions());
                    entity.setSalaryHistory(document.getSalaryHistory());
                    entity.setArchive(document.getArchive());

                    entity = dbServiceSQL.saveEntityInCompany(entity);
                    return entity;
                }));
    }

    public EmployeeCardDto saveEmployeeCardSalaryHistory(EmployeeCardDto document) {
        final Long id = document.getId() != null ? document.getId() : document.getEmployee().getId();

        dbServiceSQL.getAndCheck(EmployeeCardSql.class, id);

        EmployeeCardDto employeeCard = employeeCardSqlMapper.toDto(dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            EmployeeCardSql entity = dbServiceSQL.getById(EmployeeCardSql.class, id);
            entity.setSalaryHistory(document.getSalaryHistory());
            entityManager.persist(entity);
            return entity;
        }));
        EmployeeCardDto result = new EmployeeCardDto();
        result.setSalaryHistory(employeeCard == null ? null : employeeCard.getSalaryHistory());
        return result;
    }

    public EmployeeVacationDto saveEmployeeVacation(EmployeeVacationDto document) {
        final long employeeId = document.getEmployee().getId();
        document.setId(null);

        return employeeVacationSqlMapper.toDto(dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            EmployeeSql employee = dbServiceSQL.getAndCheck(EmployeeSql.class, employeeId);
            EmployeeCardSql employeeCard = dbServiceSQL.getAndCheck(EmployeeCardSql.class, employeeId);

            document.setEmployee(new DocEmployee(employee));
            document.setEmployeeCard(new EmployeeCardInfo(employeeCard));

            calculateTotalBalances(document.getVacations());

            EmployeeVacationSql vacation = dbServiceSQL.getById(EmployeeVacationSql.class, employeeId);
            if (vacation != null) {
                vacation.setVacations(document.getVacations());
                vacation.setArchive(document.getArchive());
            } else {
                vacation = dbServiceSQL.saveEntityInCompany(employeeVacationSqlMapper.toEntity(document));
            }
            return vacation;
        }));
    }

    public WorkHoursDto saveWorkHours(final WorkHoursDto workHours) {
        return saveWorkHours(workHours, false);
    }

    public WorkHoursDto saveWorkHours(final WorkHoursDto workHours, boolean updateVacations) {
        Validators.checkArgument(workHours.getEmployee() != null && workHours.getEmployee().getId() != null, "No employee");
        Validators.checkNotNull(workHours.getDate(), "No date");

        workHours.setDate(workHours.getDate().withDayOfMonth(1));
        final long employeeId = workHours.getEmployee().getId();

        return workHoursSqlMapper.toDto(dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            final EmployeeCardSql employeeCard = dbServiceSQL.getAndCheck(EmployeeCardSql.class, employeeId, EmployeeCardSql.GRAPH_ALL);
            final EmployeeSql employee = employeeCard.getEmployee();

            workHours.setEmployee(employeeSqlMapper.toDto(employee));
            workHours.setEmployeeCard(employeeCardSqlMapper.toDto(employeeCard));

            if (workHours.getPositions() != null) {
                for (WorkHoursPosition position : workHours.getPositions()) {
                    calculateSummary(position);
                }
            }

            // reset main position
            workHours.setMainPosition(findMainWorkHoursPosition(workHours));

            if (updateVacations) updateVacations(workHours);
            return dbServiceSQL.saveEntityInCompany(workHoursSqlMapper.toEntity(workHours));
        }));
    }

    private void calculateSummary(WorkHoursPosition position) {
        if (position != null && position.getPeriod() != null) {
            position.setWorkData(new WorkData());
            for (WorkHoursDay day : position.getPeriod()) {
                addSummary(position.getWorkData(), day);
            }
        }
    }

    private void addSummary(WorkData summary, WorkHoursDay day) {
        if (day.getWorkData() == null) return;

        WorkTimeCodeType type = day.getCode() != null ? day.getCode().getType() : null;

        if (type == WorkTimeCodeType.VACATION) summary.setVacation(IntegerUtils.inc(summary.getVacation()));
        else if (type == WorkTimeCodeType.ILLNESS) summary.setIllness(IntegerUtils.inc(summary.getIllness()));
        else if (type == WorkTimeCodeType.CHILDDAY) summary.setChildDays(IntegerUtils.inc(summary.getChildDays()));

        if (type == WorkTimeCodeType.VACATION || type == WorkTimeCodeType.ILLNESS || type == WorkTimeCodeType.CHILDDAY) {
            day.getWorkData().setWorked(null);
            day.getWorkData().setWeekend(null);
            day.getWorkData().setHoliday(null);
            day.getWorkData().setNight(null);
            day.getWorkData().setOvertime(null);
            day.getWorkData().setOvertimeHoliday(null);
            day.getWorkData().setOvertimeNight(null);
            day.getWorkData().setOvertimeWeekend(null);
        } else {
            if (!day.isHoliday() && !day.isWeekend()) {
                summary.setDays(IntegerUtils.inc(summary.getDays()));
            }
        }

        summary.setHours(IntegerUtils.add(summary.getHours(), day.getWorkData().getHours()));
        summary.setWorked(IntegerUtils.add(summary.getWorked(), day.getWorkData().getWorked()));
        summary.setWeekend(IntegerUtils.add(summary.getWeekend(), day.getWorkData().getWeekend()));
        summary.setHoliday(IntegerUtils.add(summary.getHoliday(), day.getWorkData().getHoliday()));
        summary.setOvertime(IntegerUtils.add(summary.getOvertime(), day.getWorkData().getOvertime()));
        summary.setOvertimeHoliday(IntegerUtils.add(summary.getOvertimeHoliday(), day.getWorkData().getOvertimeHoliday()));
        summary.setOvertimeNight(IntegerUtils.add(summary.getOvertimeNight(), day.getWorkData().getOvertimeNight()));
        summary.setOvertimeWeekend(IntegerUtils.add(summary.getOvertimeWeekend(), day.getWorkData().getOvertimeWeekend()));
    }

    private WorkHoursSql getWorkHoursByEmployeeId(long employeeId, int year, int month) {
        return getWorkHoursByEmployeeId(employeeId, year, month, false);
    }

    private WorkHoursSql getWorkHoursByEmployeeId(long employeeId, int year, int month, boolean nullable) {
        try {
            return entityManager.createQuery(
                    "SELECT a FROM " + WorkHoursSql.class.getName() + " a" +
                            " WHERE companyId = :companyId" +
                            " AND date = :date" +
                            " AND employee.id = :employeeId",
                            WorkHoursSql.class)
                    .setParameter("companyId", auth.getCompanyId())
                    .setParameter("date", LocalDate.of(year, month, 1))
                    .setParameter("employeeId", employeeId)
                    .setHint(GraphSemantic.FETCH.getJpaHintName(), entityManager.getEntityGraph(WorkHoursSql.GRAPH_ALL))
                    .getSingleResult();

        } catch (NoResultException e) {
            if (nullable) {
                return null;
            } else {
                throw new GamaException("No " + WorkHoursSql.class.getSimpleName());
            }
        } catch (NonUniqueResultException e) {
            throw new GamaException("Too many " + WorkHoursSql.class.getSimpleName());
        }
    }

    public void deleteWorkHours(long employeeId, int year, int month) {
        dbServiceSQL.executeInTransaction(entityManager -> {
            WorkHoursSql workHours = getWorkHoursByEmployeeId(employeeId, year, month);
            workHours.setArchive(true);
            workHours = dbServiceSQL.saveEntityInCompany(workHours);

            updateVacations(workHoursSqlMapper.toDto(workHours));
        });
    }

    public WorkHoursDto getWorkHours(long employeeId, int year, int month) {
        WorkHoursSql workHours = getWorkHoursByEmployeeId(employeeId, year, month, true);
        return workHoursSqlMapper.toDto(workHours);
    }

    public void updateVacations(final WorkHoursDto workHours) {
        if (workHours == null) return;

        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        final LocalDate date = workHours.getDate().withDayOfMonth(1);
        final long employeeId = workHours.getEmployee().getId();

        // if employee position is set - check for vacations and create or update EmployeeVacation if needed
        if (workHours.getMainPosition() == null || workHours.getMainPosition().getPeriod() == null) return;

        dbServiceSQL.executeInTransaction(entityManager -> {
            final int accountYear = workHours.getDate().getYear() +
                    (workHours.getDate().getMonthValue() < companySettings.getAccMonth() ? -1 : 0);

            int vacationsDays = IntegerUtils.value(workHours.getMainPosition().getWorkData().getVacation());
            EmployeeVacationDto employeeVacation = employeeVacationSqlMapper.toDto(dbServiceSQL.getById(EmployeeVacationSql.class, employeeId));

            if (employeeVacation == null || vacationsDays > 0) {

                EmployeeCardDto employeeCard = Validators.checkNotNull(
                        employeeCardSqlMapper.toDto(dbServiceSQL.getById(EmployeeCardSql.class, employeeId, EmployeeCardSql.GRAPH_ALL)),
                        MessageFormat.format(TranslationService.getInstance().translate(TranslationService.SALARY.NoEmployeeCard, auth.getLanguage()), employeeId));
                EmployeeDto employee = Validators.checkNotNull(employeeCard.getEmployee(),
                        MessageFormat.format(TranslationService.getInstance().translate(TranslationService.SALARY.NoEmployee, auth.getLanguage()), employeeId));

                CompanyTaxSettings companyTaxSettings = salarySettingsService.getCompanyTaxSettings(date);
                CompanySalarySettings companySalarySettings = salarySettingsService.getCompanySalarySettings(date);
                EmployeeTaxSettings employeeTaxSettings = Validators.checkNotNull(
                        salarySettingsService.generateEmployeeTaxSettings(companySettings, companyTaxSettings, companySalarySettings, employeeCard, date),
                        MessageFormat.format(
                                TranslationService.getInstance().translate(TranslationService.SALARY.NoEmployeeTax, auth.getLanguage()),
                                employeeCard.getEmployee().getName()));

                Validators.checkArgument(IntegerUtils.isPositive(employeeTaxSettings.getVacationLength()), MessageFormat.format(
                        TranslationService.getInstance().translate(TranslationService.SALARY.NoEmployeeVacationLength, auth.getLanguage()),
                        employeeCard.getEmployee().getName()));

                if (employeeVacation == null) {
                    employeeVacation = new EmployeeVacationDto();
                    employeeVacation.setId(null);
                    employeeVacation.setCompanyId(auth.getCompanyId());
                }
                employeeVacation.setEmployee(new DocEmployee(employee));
                employeeVacation.setEmployeeCard(new EmployeeCardInfo(employeeCard));
                if (employeeVacation.getVacations() == null) employeeVacation.setVacations(new ArrayList<>());

                updateVacationBalance(employeeVacation.getVacations(), accountYear, companySettings.getAccMonth(), workHours, vacationsDays, employeeTaxSettings.getVacationLength());
                saveEmployeeVacation(employeeVacation);
            }
        });
    }

    /**
     *
     * @param vacationBalances list of balances by working years
     * @param year working year
     * @param month accounting month, i.e. month of start of accounting period
     * @param workHours Employee monthly work hours document
     * @param daysByLaw total vacation days annually by law
     */
    private void updateVacationBalance(List<VacationBalance> vacationBalances, int year, int month,
                                       WorkHoursDto workHours, int vacationsDays, int daysByLaw) {
        for (VacationBalance vacationBalance : vacationBalances) {
            if (vacationBalance.getYear() == year) {

                calcVacationAnnualDays(vacationBalance, year, month, workHours.getEmployeeCard().getHired(), daysByLaw);

                if (vacationBalance.getDocs() == null) vacationBalance.setDocs(new ArrayList<>());
                boolean foundDoc = false;
                Iterator<DocWorkHours> docWorkHoursIterator = vacationBalance.getDocs().iterator();
                while (docWorkHoursIterator.hasNext()) {
                    DocWorkHours doc = docWorkHoursIterator.next();
                    if (doc.getDate().isEqual(workHours.getDate())) {

                        if (BooleanUtils.isTrue(workHours.getArchive())) {
                            vacationBalance.setUsed(IntegerUtils.subtract(vacationBalance.getUsed(), vacationsDays));

                            if (doc.getVacationsDays() == vacationsDays) {
                                docWorkHoursIterator.remove();

                            } else {
                                // doc.vacationDays = 10 and docWorkHours.vacationDays = 9  - used -= 9 and doc.vacationDays = 1
                                // doc.vacationDays = 10 and docWorkHours.vacationDays = 11 - used -= 11 and doc.vacationDays -1
                                doc.setVacationsDays(doc.getVacationsDays() - vacationsDays);
                            }

                        } else {
                            if (doc.getVacationsDays() != vacationsDays) {
                                // doc.vacationDays = 10 and docWorkHours.vacationDays = 9  - used -= 1
                                // doc.vacationDays = 10 and docWorkHours.vacationDays = 11 - used += 1
                                vacationBalance.setUsed(IntegerUtils.add(vacationBalance.getUsed(), vacationsDays - doc.getVacationsDays()));
                                doc.setVacationsDays(vacationsDays);
                            }
                        }

                        foundDoc = true;
                        break;
                    }
                }
                if (!foundDoc) {
                    vacationBalance.setUsed(IntegerUtils.add(vacationBalance.getUsed(), vacationsDays));
                    vacationBalance.getDocs().add(new DocWorkHours(workHours, vacationsDays));
                    vacationBalance.getDocs().sort((o1, o2) -> o2.getDate().compareTo(o1.getDate()));
                }

                calculateTotalBalances(vacationBalances);
                return;
            }
        }

        // if 'year' not found
        VacationBalance balance = new VacationBalance();
        balance.setYear(year);

        calcVacationAnnualDays(balance, year, month, workHours.getEmployeeCard().getHired(), daysByLaw);

        balance.setUsed(vacationsDays);
        if (vacationsDays > 0) {
            balance.setDocs(new ArrayList<>());
            balance.getDocs().add(new DocWorkHours(workHours, vacationsDays));
        }
        vacationBalances.add(balance);
        calculateTotalBalances(vacationBalances);
    }

    private void calcVacationAnnualDays(VacationBalance balance, int year, int month, LocalDate hired, int daysByLaw) {
        LocalDate dateFrom = LocalDate.of(year, month, 1);
        if (hired == null || !hired.isAfter(dateFrom)) {
            balance.setDays(daysByLaw);
            return;
        }

        LocalDate dateTo = dateFrom.plusYears(1);

        long days = ChronoUnit.DAYS.between(hired, dateTo);
        long daysInYear = ChronoUnit.DAYS.between(dateFrom, dateTo);

        /*
         *  daysInYear  <-> vacationsDaysByLaw
         *  days        <-> x
         *  ------------------------------------------
         *  x = days * vacationsDaysByLaw / daysInYear
         */
        int x = (int) Math.round(Math.floor((double)days * (double)daysByLaw / (double)daysInYear));
        balance.setDays(x);
    }

    private void calculateTotalBalances(List<VacationBalance> vacationBalances) {

        if (CollectionsHelper.isEmpty(vacationBalances)) return;

        vacationBalances.sort((o1, o2) -> o2.getYear() - o1.getYear());

        Integer past = null;
        for (VacationBalance vacationBalance : Lists.reverse(vacationBalances)) {
            vacationBalance.setPast(past);
            past = vacationBalance.getBalance();
        }
    }

    public SalaryEmployeeChargeResponse saveEmployeeCharge(final EmployeeChargeDto document) {

        Validators.checkArgument(document.getParentId() != null && document.getParentId() != 0, "No salary");
        Validators.checkArgument(document.getEmployee() != null && document.getEmployee().getId() != 0, "No employee");

        final long employeeId = document.getEmployee().getId();
        final long salaryId = document.getParentId();

        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");

        SalaryDoubleEntry result = dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            // adjust document date by salary date
            SalarySql salary = dbServiceSQL.getAndCheck(SalarySql.class, salaryId);
            document.setDate(salary.getDate().with(lastDayOfMonth()));
            Validators.checkDocumentDate(companySettings, document.getDate(), auth.getLanguage());

            final CompanyTaxSettings companyTaxSettings = Validators.checkNotNull(salarySettingsService.getCompanyTaxSettings(document.getDate()), "No Company Tax Settings");
            final CompanySalarySettings companySalarySettings = Validators.checkNotNull(salarySettingsService.getCompanySalarySettings(document.getDate()), "No Company Salary Settings");

            EmployeeChargeSql entity = getSalaryEmployeeChargeSql(salaryId, employeeId);
            if (entity == null || BooleanUtils.isNotTrue(entity.getFinished())) {
                final EmployeeCardSql employeeCard = entity != null
                        ? entity.getEmployeeCard()
                        : dbServiceSQL.getAndCheckNullable(EmployeeCardSql.class, employeeId);

                final EmployeeTaxSettings employeeTaxSettings = salarySettingsService.generateEmployeeTaxSettings(companySettings, companyTaxSettings,
                        companySalarySettings, employeeCard, document.getDate());

                balanceTotals(companySettings, document);
                doTaxes(companySettings, companyTaxSettings, employeeTaxSettings, document);

                if (entity == null) {
                    entity = employeeChargeSqlMapper.toEntity(document);
                    entity.setEmployee(entityManager.getReference(EmployeeSql.class, employeeId));

                } else {
                    Validators.checkArgument(document.getId() != null || !document.getEmployee().getId().equals(employeeId),
                            MessageFormat.format(TranslationService.getInstance().translate(TranslationService.SALARY.EmployeeExists, auth.getLanguage()),
                                    document.getEmployee().getName()));
                    Validators.checkDocumentVersion(entity, document, auth.getLanguage());
                    Validators.checkArgument(BooleanUtils.isNotTrue(entity.getFinished()),
                            TranslationService.getInstance().translate(TranslationService.VALIDATORS.DocumentFinishedAlready, auth.getLanguage()));

                    entity.setTaxExempt(document.getTaxExempt());
                    entity.setAddTaxExempt(document.getAddTaxExempt());
                    entity.setCharges(document.getCharges());
                    entity.setEmployeeSSTax(document.getEmployeeSSTax());
                    entity.setCompanySSTax(document.getCompanySSTax());
                    entity.setIncomeTax(document.getIncomeTax());
                    entity.setGuarantyFundTax(document.getGuarantyFundTax());
                    entity.setShiTax(document.getShiTax());

                    entity.setTotal(document.getTotal());
                    entity.setTotalIncome(document.getTotalIncome());
                    entity.setTotalSS(document.getTotalSS());

                    entity.setOperations(document.getOperations());

                    entity.setFinished(document.getFinished());
                    entity.setArchive(document.getArchive());

                    entity.setWorkData(document.getWorkData());
                }

                entity.setDate(document.getDate());

                balanceTotals(companySettings, entity);
                doTaxes(companySettings, companyTaxSettings, employeeTaxSettings, entity);

                entity = dbServiceSQL.saveEntityInCompany(entity);

                //update parent Salary record
                calculateSalaryTotals(salary, false, false);
                salary = dbServiceSQL.saveEntityInCompany(salary);

            } else {
                log.info(this.getClass().getSimpleName() + ": " + entity.getClass().getSimpleName() + ", id=" + entity.getId() + " is finished");
            }
            DoubleEntrySql doubleEntry = glOperationsService.finishSalary(salary, null, false);
            return new SalaryDoubleEntry(salary, doubleEntry, entity);
        });
        SalaryDto salary = salarySqlMapper.toDto(result.getDocument());
        salary.setDoubleEntry(doubleEntrySqlMapper.toDto(result.getDoubleEntry()));
        return new SalaryEmployeeChargeResponse(salary, employeeChargeSqlMapper.toDto(result.getEmployeeCharge()));
    }

    public EmployeeChargeSql getSalaryEmployeeChargeSql(long salaryId, long employeeId) {
        return getSalaryEmployeeChargeSqlCheck(salaryId, employeeId, false);
    }

    private EmployeeChargeSql getSalaryEmployeeChargeSqlCheck(long salaryId, long employeeId, boolean check) {
        try {
            return entityManager.createQuery(
                            "SELECT a FROM " + EmployeeChargeSql.class.getName() + " a " +
                                    " JOIN a." + EmployeeChargeSql_.EMPLOYEE + " e" +
                                    " JOIN a." + EmployeeChargeSql_.EMPLOYEE_CARD + " ec" +
                                    " WHERE a." + EmployeeChargeSql_.COMPANY_ID + " = :companyId" +
                                    " AND a." + EmployeeChargeSql_.SALARY + "." + SalarySql_.ID + " = :salaryId" +
                                    " AND a." + EmployeeChargeSql_.EMPLOYEE + "." + EmployeeSql_.ID + " = :employeeId",
                            EmployeeChargeSql.class)
                    .setParameter("companyId", auth.getCompanyId())
                    .setParameter("salaryId", salaryId)
                    .setParameter("employeeId", employeeId)
                    .getSingleResult();
        } catch (NoResultException e) {
            if (check) {
                throw new GamaException(
                        MessageFormat.format("No Employee {0} charges in Salary {1} group", employeeId, salaryId));
            }
            return null;
        } catch (NonUniqueResultException e) {
            throw new GamaException(
                    MessageFormat.format("Too many Employee {0} charges in one Salary {1} group", employeeId, salaryId));
        }
    }

    public EmployeeChargeDto getSalaryEmployeeCharge(long salaryId, long employeeId) {
        return getSalaryEmployeeChargeCheck(salaryId, employeeId, false);
    }

    public EmployeeChargeDto getSalaryEmployeeChargeCheck(long salaryId, long employeeId, boolean check) {
        return employeeChargeSqlMapper.toDto(getSalaryEmployeeChargeSqlCheck(salaryId, employeeId, check));
    }

    private EmployeeChargeDto changeStatusEmployeeCharge(long salaryId, long employeeId, boolean finished) {
        if (salaryId == 0) throw new GamaException("No salaryId");

        SalaryDto salary = salarySqlMapper.toDto(dbServiceSQL.getAndCheck(SalarySql.class, salaryId));

        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        final CompanyTaxSettings companyTaxSettings = salarySettingsService.getCompanyTaxSettings(salary.getDate());
        final CompanySalarySettings companySalarySettings = salarySettingsService.getCompanySalarySettings(salary.getDate());

        return employeeChargeSqlMapper.toDto(dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
                    EmployeeChargeSql entity = Validators.checkNotNull(getSalaryEmployeeChargeSql(salaryId, employeeId),
                            "No employee charge record " + employeeId);
                    final EmployeeCardSql employeeCard = entity.getEmployeeCard();
                    final EmployeeTaxSettings employeeTaxSettings = salarySettingsService.generateEmployeeTaxSettings(companySettings, companyTaxSettings,
                            companySalarySettings, employeeCard, salary.getDate());

                    if (BooleanUtils.isTrue(entity.getFinished()) != finished) {
                        if (finished) {
                            doTaxes(companySettings, companyTaxSettings, employeeTaxSettings, entity);
                        }
                        entity.setFinished(finished);
                        entity = dbServiceSQL.saveEntityInCompany(entity);
                    }
                    return entity;
                }));
    }

    public SalaryEmployeeChargeResponse finishEmployeeCharge(long salaryId, long employeeId) {
        EmployeeChargeDto employeeCharge = changeStatusEmployeeCharge(salaryId, employeeId, true);
        return new SalaryEmployeeChargeResponse(employeeCharge);
    }

    public SalaryEmployeeChargeResponse recallEmployeeCharge(long salaryId, long employeeId) {
        SalaryDto salary = recallSalary(salaryId);
        EmployeeChargeDto employeeCharge = changeStatusEmployeeCharge(salaryId, employeeId, false);
        return new SalaryEmployeeChargeResponse(salary, employeeCharge);
    }

    public SalaryEmployeeChargeResponse deleteEmployeeCharge(long salaryId, long employeeId) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        DocumentDoubleEntry<SalarySql> pair = dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            SalarySql salary = dbServiceSQL.getAndCheck(SalarySql.class, salaryId);
            Validators.checkDocumentDate(companySettings, salary, auth.getLanguage());
            entityManager.createQuery(
                            "DELETE FROM " + EmployeeChargeSql.class.getName() + " a" +
                                    " WHERE " + EmployeeChargeSql_.EMPLOYEE + "." + EmployeeSql_.ID + " = :employeeId" +
                                    " AND " + EmployeeChargeSql_.SALARY + "." + SalarySql_.ID + " = :salaryId" +
                                    " AND " + EmployeeChargeSql_.COMPANY_ID + " = :companyId")
                    .setParameter("companyId", auth.getCompanyId())
                    .setParameter("salaryId", salaryId)
                    .setParameter("employeeId", employeeId)
                    .executeUpdate();
            calculateSalaryTotals(salary, false, false);
            salary = dbServiceSQL.saveEntityInCompany(salary);
            DoubleEntrySql doubleEntry = glOperationsService.finishSalary(salary, null, false);
            return new DocumentDoubleEntry<>(salary, doubleEntry); // new SalaryEmployeeChargeResponse(salarySqlMapper.toDto(salary), null);
        });
        SalaryDto salary = salarySqlMapper.toDto(pair.getDocument());
        salary.setDoubleEntry(doubleEntrySqlMapper.toDto(pair.getDoubleEntry()));
        return new SalaryEmployeeChargeResponse(salary, null);
    }

    public SalaryDto saveSalary(SalaryDto document) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        // adjust date on the end of the month
        document.setDate(document.getDate().with(lastDayOfMonth()));
        Validators.checkDocumentDate(companySettings, document, auth.getLanguage());

        DoubleEntryDto doubleEntry = document.getDoubleEntry();

        // uuid need for generating printing form
        if (document.getUuid() == null) document.setUuid(UUID.randomUUID());

        DocumentDoubleEntry<SalarySql> pair = dbServiceSQL.executeAndReturnInTransaction((EntityManager em) -> {
            if (document.getId() != null) {
                SalarySql entity = dbServiceSQL.getAndCheck(SalarySql.class, document.getId());
                if (BooleanUtils.isTrue(entity.getFinished())) {
                    log.warn(this.getClass().getSimpleName() + ": " + MessageFormat.format("Operation {0} {1} is finished",
                            entity.getClass().getSimpleName(), document.getId()));
                    throw new GamaException(TranslationService.getInstance().translate(TranslationService.VALIDATORS.DocumentFinishedAlready, auth.getLanguage()));
                }
            }
            SalarySql salarySql = salarySqlMapper.toEntity(document);
            if (document.getId() != null) calculateSalaryTotals(salarySql,true, false);

            salarySql = dbServiceSQL.saveWithCounter(salarySql);
            DoubleEntrySql doubleEntrySql = glOperationsService.finishSalary(salarySql, doubleEntry, false);
            return new DocumentDoubleEntry<>(salarySql, doubleEntrySql);
        });
        SalaryDto result = salarySqlMapper.toDto(pair.getDocument());
        result.setDoubleEntry(doubleEntrySqlMapper.toDto(pair.getDoubleEntry()));
        return result;
    }

    public void deleteSalary(long id) {
        dbServiceSQL.executeInTransaction(entityManager -> {
            SalarySql document = Validators.checkNotNull(dbServiceSQL.getById(SalarySql.class, id), TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDocumentToDelete, auth.getLanguage()));
            if (!document.isFullyFinished()) {
                DoubleEntrySql doubleEntry = glUtilsService.getDoubleEntryByParentId(id);
                if (doubleEntry != null && !BooleanUtils.isTrue(doubleEntry.getArchive())) {
                    doubleEntry.setArchive(true);
                    dbServiceSQL.saveEntityInCompany(doubleEntry);
                }
                document.setArchive(true);
                dbServiceSQL.saveEntityInCompany(document);
                entityManager.createQuery(
                        "DELETE FROM " + EmployeeChargeSql.class.getName() + " a " +
                                " WHERE " + EmployeeChargeSql_.COMPANY_ID + " = :companyId" +
                                " AND " + EmployeeChargeSql_.SALARY + "." + SalarySql_.ID + " = :salaryId")
                        .setParameter("companyId", auth.getCompanyId())
                        .setParameter("salaryId", document.getId())
                        .executeUpdate();
            } else {
                throw new GamaException(TranslationService.getInstance().translate(TranslationService.VALIDATORS.DocumentFinishedAlready, auth.getLanguage()));
            }
        });
    }


    public SalaryDto finishSalary(final long id, final boolean finish, final Boolean finishGL) {
        DocumentDoubleEntry<SalarySql> pair = dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            SalarySql entity = dbServiceSQL.getAndCheck(SalarySql.class, id);
            calculateSalaryTotals(entity, true, finish);
            entity.setFinished(finish);
            entity = dbServiceSQL.saveEntityInCompany(entity);
            DoubleEntrySql doubleEntry = glOperationsService.finishSalary(entity, null, finishGL);
            return new DocumentDoubleEntry<>(entity, doubleEntry);
        });
        SalaryDto result = salarySqlMapper.toDto(pair.getDocument());
        result.setDoubleEntry(doubleEntrySqlMapper.toDto(pair.getDoubleEntry()));
        return result;
    }

    private void calculateSalaryTotals(final SalarySql salary, final boolean updateTaxes, final boolean finish) {

        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");

        LocalDate salaryDate = salary.getDate().with(lastDayOfMonth());

        final CompanyTaxSettings companyTaxSettings = Validators.checkNotNull(salarySettingsService.getCompanyTaxSettings(salaryDate),
                TranslationService.getInstance().translate(TranslationService.SALARY.NoTaxSettings, auth.getLanguage()));
        final CompanySalarySettings companySalarySettings = Validators.checkNotNull(salarySettingsService.getCompanySalarySettings(salaryDate),
                TranslationService.getInstance().translate(TranslationService.SALARY.NoSalarySettings, auth.getLanguage()));

        Map<Long, DocChargeAmount> mapCharges = new HashMap<>();

        // just read everything and update not archived only
        List<EmployeeChargeSql> charges = entityManager.createQuery(
                        "SELECT a FROM " + EmployeeChargeSql.class.getName() + " a" +
                                " WHERE " + EmployeeChargeSql_.SALARY + "." + SalarySql_.ID + " = :salaryId" +
                                " AND " + EmployeeChargeSql_.COMPANY_ID + " = :companyId",
                        EmployeeChargeSql.class)
                .setParameter("companyId", auth.getCompanyId())
                .setParameter("salaryId", salary.getId())
                .getResultList();

        // salary need to be modified here because hibernate AutoFlush

        // adjust date on the end of the month
        salary.setDate(salaryDate);

        salary.setCharges(new ArrayList<>());

        salary.setTotal(null);
        salary.setTotalIncome(null);
        salary.setTotalSS(null);

        salary.setNet(null);
        salary.setAdvance(null);
        salary.setNetTotal(null);

        salary.setCompanySSTax(null);
        salary.setEmployeeSSTax(null);
        salary.setGuarantyFundTax(null);
        salary.setShiTax(null);
        salary.setIncomeTax(null);

        salary.setIncomeTaxRate(companyTaxSettings.getIncomeTaxRate());
        salary.setCompanySSTaxRate(companyTaxSettings.getCompanySSTaxRate());
        salary.setGuarantyFundTaxRate(companyTaxSettings.getGuarantyFundTaxRate());
        salary.setShiTaxRate(companyTaxSettings.getShiTaxRate());

        charges.forEach(charge -> {
            if (addSalaryEmployeeCharge(salary, charge, mapCharges, updateTaxes,
                    companySettings, companyTaxSettings, companySalarySettings)) {
                if (BooleanUtils.isNotTrue(charge.getFinished()) && updateTaxes) {
                    if (finish) charge.setFinished(true);
                }
            }
         });

        salary.setNetTotal(GamaMoneyUtils.subtract(salary.getNet(), salary.getAdvance()));

        // update document
        salary.setCharges(new ArrayList<>(mapCharges.values()));
        if (finish) salary.setFinished(true);
    }

    private boolean addSalaryEmployeeCharge(SalarySql salary, EmployeeChargeSql employeeCharge,
                                            Map<Long, DocChargeAmount> mapCharges, boolean updateTaxes,
                                            CompanySettings companySettings,
                                            CompanyTaxSettings companyTaxSettings,
                                            CompanySalarySettings companySalarySettings) {
        if (employeeCharge == null || BooleanUtils.isTrue(employeeCharge.getArchive())) return false;

        if (BooleanUtils.isNotTrue(employeeCharge.getFinished()) && updateTaxes) {
            EmployeeCardDto employeeCard = employeeCardSqlMapper.toDto(dbServiceSQL.getById(EmployeeCardSql.class, employeeCharge.getEmployeeId()));
            if (employeeCard != null && employeeCard.getCompanyId() == auth.getCompanyId()) {
                EmployeeTaxSettings employeeTaxSettings = salarySettingsService.generateEmployeeTaxSettings(
                        companySettings, companyTaxSettings, companySalarySettings, employeeCard, salary.getDate());
                doTaxes(companySettings, companyTaxSettings, employeeTaxSettings, employeeCharge);
            } else {
                log.error(this.getClass().getSimpleName() + ": " +
                        (employeeCard == null
                                ? "No EmployeeCard " + employeeCharge.getId()
                                : "EmployeeCard " + employeeCharge.getId() + ": wrong company: " + auth.getCompanyId()));
            }
        }

        salary.setTotal(GamaMoneyUtils.add(salary.getTotal(), employeeCharge.getTotal()));
        salary.setTotalSS(GamaMoneyUtils.add(salary.getTotalSS(), employeeCharge.getTotalSS()));
        salary.setTotalIncome(GamaMoneyUtils.add(salary.getTotalIncome(), employeeCharge.getTotalIncome()));

        salary.setNet(GamaMoneyUtils.add(salary.getNet(), employeeCharge.getNet()));
        salary.setAdvance(GamaMoneyUtils.add(salary.getAdvance(), employeeCharge.getAdvance()));

        salary.setCompanySSTax(GamaMoneyUtils.add(salary.getCompanySSTax(), employeeCharge.getCompanySSTax()));
        salary.setEmployeeSSTax(GamaMoneyUtils.add(salary.getEmployeeSSTax(), employeeCharge.getEmployeeSSTax()));
        salary.setGuarantyFundTax(GamaMoneyUtils.add(salary.getGuarantyFundTax(), employeeCharge.getGuarantyFundTax()));
        salary.setShiTax(GamaMoneyUtils.add(salary.getShiTax(), employeeCharge.getShiTax()));
        salary.setIncomeTax(GamaMoneyUtils.add(salary.getIncomeTax(), employeeCharge.getIncomeTax()));

        if (employeeCharge.getCharges() != null) {
            for (DocChargeAmount docChargeAmount : employeeCharge.getCharges()) {
                DocChargeAmount amount = mapCharges.get(docChargeAmount.getCharge().getId());
                if (amount == null) {
                    mapCharges.put(docChargeAmount.getCharge().getId(), new DocChargeAmount(docChargeAmount));
                } else {
                    amount.setAmount(GamaMoneyUtils.add(amount.getAmount(), docChargeAmount.getAmount()));
                }
            }
        }
        return true;
    }

    public SalaryDto recallSalary(final long id) {
        return salarySqlMapper.toDto(dbServiceSQL.executeAndReturnInTransaction((EntityManager em) -> {
            SalarySql entity = Validators.checkNotNull(dbServiceSQL.getById(SalarySql.class, id), TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDocumentToRecall, auth.getLanguage()));

            em.createQuery(
                    "UPDATE " + EmployeeChargeSql.class.getName() + " a" +
                            " SET " + EmployeeChargeSql_.FINISHED + " = false" +
                            " WHERE " + EmployeeChargeSql_.COMPANY_ID + " = :companyId" +
                            " AND " + EmployeeChargeSql_.SALARY + "." + SalarySql_.ID + " = :salaryId")
                    .setParameter("companyId", auth.getCompanyId())
                    .setParameter("salaryId", id)
                    .executeUpdate();

            // update document
            if (BooleanUtils.isTrue(entity.getFinished())) {
                entity.setFinished(null);
            }

            // recall G.L operations
            DoubleEntrySql doubleEntry = glUtilsService.updateState(id, DBType.POSTGRESQL, true, false, true);
            entity.setDoubleEntry(doubleEntrySqlMapper.toDto(doubleEntry));

            return entity;
        }));
    }

    public String refreshSalary(long salaryId, SalaryType salaryType, boolean fresh) {
        return taskQueueService.queueTask(new EmployeeChargeTask(auth.getCompanyId(), salaryId, salaryType, fresh));
    }

    private boolean hasFixedCharge(List<DocChargeAmount> charges, DocCharge charge) {
        if (CollectionsHelper.isEmpty(charges)) return false;
        for (DocChargeAmount chargeAmount : charges) {
            if (chargeAmount.getCharge() != null && Objects.equals(chargeAmount.getCharge().getId(), charge.getId()) &&
                    chargeAmount.isFixed()) return true;
        }
        return false;
    }

    public TaskResponse<Void> refreshSalaryTask(final long salaryId, final SalaryType salaryType, final boolean fresh) {
        final LocalDate salaryDate;
        try {
            Tuple tuple = entityManager.createQuery(
                    "SELECT " + SalarySql_.FINISHED + " AS finished, " + SalarySql_.DATE + " AS date" +
                            " FROM " + SalarySql.class.getName() + " a" +
                            " WHERE " + SalarySql_.ID + " = :salaryId" +
                            " AND " + SalarySql_.COMPANY_ID + " = :companyId" +
                            " AND (a.archive IS null OR a.archive = false)",
                            Tuple.class)
                    .setParameter("salaryId", salaryId)
                    .setParameter("companyId", auth.getCompanyId())
                    .getSingleResult();
            if (BooleanUtils.isTrue(tuple.get("finished", Boolean.class))) {
                throw new GamaException(TranslationService.getInstance()
                        .translate(TranslationService.VALIDATORS.DocumentFinishedAlready, auth.getLanguage()));
            }
            salaryDate = tuple.get("date", LocalDate.class);
        } catch (NoResultException e) {
            throw new GamaException(MessageFormat.format(
                    TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDocumentWithId, auth.getLanguage()), salaryId));
        }

        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        final CompanyTaxSettings companyTaxSettings = Validators.checkNotNull(salarySettingsService.getCompanyTaxSettings(salaryDate), TranslationService.getInstance().translate(TranslationService.SALARY.NoTaxSettings, auth.getLanguage()));
        final CompanySalarySettings companySalarySettings = Validators.checkNotNull(salarySettingsService.getCompanySalarySettings(salaryDate), TranslationService.getInstance().translate(TranslationService.SALARY.NoSalarySettings, auth.getLanguage()));

        final DocCharge chargeAdvance = companySettings.getChargeAdvance();
        final DocCharge chargeWork = companySettings.getChargeWork();
        final DocCharge chargeIllness = companySettings.getChargeIllness();
        final DocCharge chargeVacation = companySettings.getChargeVacation();
        final DocCharge chargeChildDays = companySettings.getChargeChildDays();

        Validators.checkNotNull(chargeAdvance, TranslationService.getInstance().translate(TranslationService.SALARY.NoChargeAdvanceSettings, auth.getLanguage()));
        Validators.checkNotNull(chargeWork, TranslationService.getInstance().translate(TranslationService.SALARY.NoChargeWorkSettings, auth.getLanguage()));
        Validators.checkNotNull(chargeIllness, TranslationService.getInstance().translate(TranslationService.SALARY.NoChargeIllnessSettings, auth.getLanguage()));
        Validators.checkNotNull(chargeVacation, TranslationService.getInstance().translate(TranslationService.SALARY.NoChargeVacationSettings, auth.getLanguage()));
        Validators.checkNotNull(chargeChildDays, TranslationService.getInstance().translate(TranslationService.SALARY.NoChargeChildDaysSettings, auth.getLanguage()));

        final Set<String> warnings = new HashSet<>();

        // -- find generated already --
        dbServiceSQL.executeInTransaction(entityManager -> {
            List<Long> except = entityManager.createQuery(
                            "SELECT " + EmployeeChargeSql_.EMPLOYEE + "." + EmployeeSql_.ID +
                                    " FROM " + EmployeeChargeSql.class.getName() + " a" +
                                    " WHERE " + EmployeeChargeSql_.SALARY + "." + SalarySql_.ID + " = :salaryId" +
                                    " AND " + EmployeeChargeSql_.COMPANY_ID + " = :companyId",
                            Long.class)
                    .setParameter("companyId", auth.getCompanyId())
                    .setParameter("salaryId", salaryId)
                    .getResultList();

            List<EmployeeCardSql> cards = entityManager.createQuery(
                    "SELECT a FROM " + EmployeeCardSql.class.getName() + " a" +
                            " JOIN a." + EmployeeChargeSql_.EMPLOYEE + " e" +
                            " WHERE a." + EmployeeCardSql_.COMPANY_ID + " = :companyId" +
                            " AND a." + EmployeeCardSql_.TAXES + " IS NOT NULL" +
                            " AND (a.archive IS null OR a.archive = false)",
                            EmployeeCardSql.class)
                    .setParameter("companyId", auth.getCompanyId())
                    .getResultList();

            List<EmployeeChargeDto> employeeCharges = new ArrayList<>();

            for (EmployeeCardSql employeeCard : cards) {
                if (!fresh && except.contains(employeeCard.getId())) continue;

                EmployeeSql employee = employeeCard.getEmployee();
                if (employee == null) continue;

                try {
                    EmployeeChargeDto employeeCharge = generateEmployeeCharge(salaryDate,
                            employeeSqlMapper.toDto(employee),
                            employeeCardSqlMapper.toDto(employeeCard),
                            companySettings, companyTaxSettings, companySalarySettings, salaryType);
                    if (employeeCharge != null) employeeCharges.add(employeeCharge);

                } catch (NullPointerException | IllegalArgumentException | GamaException e) {
                    log.info(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
                    warnings.add(e.getMessage());
                }
            }

            if (!employeeCharges.isEmpty()) {

                int count = 0;

                // if fresh == true - "delete" all not updated records before
                if (fresh && !except.isEmpty()) {
                    for (EmployeeChargeDto item : employeeCharges) {
                        except.remove(item.getEmployee().getId());
                    }
                    if (!except.isEmpty()) {
                        // delete
                        entityManager.createNativeQuery(
                                "DELETE FROM employee_charge" +
                                        " WHERE company_id = :companyId" +
                                        " AND parent_id = :salaryId" +
                                        " AND employee_id IN :ids" +
                                        " AND NOT jsonb_path_query_array(charges, '$[*].charge.id') @> CAST(CAST(:id AS text) AS jsonb)")
                                .setParameter("companyId", auth.getCompanyId())
                                .setParameter("salaryId", salaryId)
                                .setParameter("ids", except)
                                .setParameter("id", salaryType == SalaryType.ADVANCE ? chargeWork.getId() : chargeAdvance.getId())
                                .executeUpdate();
                    }
                }

                // save (overwrite if exists) all others, except they are finished or fixed
                for (EmployeeChargeDto item : employeeCharges) {
                    EmployeeChargeDto employeeCharge = createEmployeeCharge(salaryDate, salaryId, salaryType,
                            chargeAdvance, chargeWork, item, companySettings);
                    if (employeeCharge == null) continue;

                    count++;

                    dbServiceSQL.saveEntityInCompany(employeeChargeSqlMapper.toEntity(employeeCharge));

                    if (count % DB_BATCH_SIZE == 0) {
                        entityManager.flush();
                        entityManager.clear();
                    }
                }
                entityManager.flush();
                entityManager.clear();
                log.info(this.getClass().getSimpleName() + ": refreshSalary count=" + count);
            }
        });

        // calculate totals only and save but not finish
        finishSalary(salaryId, false, false);

        return TaskResponse.<Void>success().withWarnings(warnings);
    }

    private EmployeeChargeDto createEmployeeCharge(final LocalDate date, final long salaryId, SalaryType salaryType,
                                                   DocCharge chargeAdvance, DocCharge chargeWork,
                                                   EmployeeChargeDto generated, CompanySettings companySettings) {
        EmployeeChargeDto employeeCharge = getSalaryEmployeeCharge(salaryId, generated.getEmployee().getId());
        if (employeeCharge != null) {
            if (BooleanUtils.isTrue(employeeCharge.getArchive())) {
                employeeCharge.setArchive(false);
                employeeCharge.setFinished(null);
                employeeCharge.setCharges(new ArrayList<>(generated.getCharges()));
                employeeCharge.setWorkData(generated.getWorkData());

            } else {
                if (BooleanUtils.isTrue(employeeCharge.getFinished())) return null;

                if (salaryType == SalaryType.SALARY) {
                    // check if charge is fixed - if yes - do nothing, i.e. skip this record
                    if (hasFixedCharge(employeeCharge.getCharges(), chargeWork))
                        return null;

                    // add advances and fixed items
                    List<DocChargeAmount> charges = new ArrayList<>();
                    if (employeeCharge.getCharges() != null) {
                        for (DocChargeAmount charge : employeeCharge.getCharges()) {
                            if (charge.getCharge().getId().equals(chargeAdvance.getId()) || charge.isFixed())
                                charges.add(charge);
                        }
                    }
                    charges.addAll(generated.getCharges());
                    employeeCharge.setCharges(charges);
                    employeeCharge.setWorkData(generated.getWorkData());

                } else if (salaryType == SalaryType.ADVANCE) {
                    // check if some work charge is fixed - if yes - do nothing, i.e. skip this record
                    if (hasFixedCharge(employeeCharge.getCharges(), chargeAdvance))
                        return null;

                    // add everything except advances and fixed items
                    List<DocChargeAmount> charges = new ArrayList<>();
                    for (DocChargeAmount charge : employeeCharge.getCharges()) {
                        if (!charge.getCharge().getId().equals(chargeAdvance.getId()) || charge.isFixed())
                            charges.add(charge);
                    }
                    charges.addAll(generated.getCharges());
                    employeeCharge.setCharges(charges);
                }
            }

            employeeCharge.setEmployee(generated.getEmployee());
            employeeCharge.setEmployeeCard(generated.getEmployeeCard());
            employeeCharge.setEmployeeTaxSettings(generated.getEmployeeTaxSettings());
            employeeCharge.setCompanyTaxSettings(generated.getCompanyTaxSettings());

            employeeCharge.setTaxExempt(generated.getTaxExempt());
            employeeCharge.setAddTaxExempt(generated.getAddTaxExempt());

            employeeCharge.setEmployeeSSTax(null);
            employeeCharge.setCompanySSTax(null);
            employeeCharge.setGuarantyFundTax(null);
            employeeCharge.setIncomeTax(null);
            employeeCharge.setIncomeTax(null);
            employeeCharge.setShiTax(null);

            employeeCharge.setTotal(null);
            employeeCharge.setTotalSS(null);
            employeeCharge.setTotalIncome(null);

            employeeCharge.setOperations(null);

        } else {
            employeeCharge = generated;
            employeeCharge.setParentId(salaryId);
            employeeCharge.setId(generated.getEmployee().getId());
        }

        employeeCharge.setCompanyId(auth.getCompanyId());
        employeeCharge.setDate(date);

        balanceTotals(companySettings, employeeCharge);

        return employeeCharge;
    }

    private void balanceTotals(CompanySettings companySettings, IEmployeeCharge employeeCharge) {
        GamaMoney advance = null;
        GamaMoney net = null;
        if (employeeCharge.getCharges() != null) {
            for (DocChargeAmount docChargeAmount : employeeCharge.getCharges()) {
                if (docChargeAmount.getCharge() != null && companySettings.getChargeAdvance() != null &&
                        Objects.equals(docChargeAmount.getCharge().getId(), companySettings.getChargeAdvance().getId())) {
                    advance = GamaMoneyUtils.add(advance, docChargeAmount.getAmount());
                } else {
                    net = GamaMoneyUtils.add(net, docChargeAmount.getAmount());
                }
            }
        }
        if (!GamaMoneyUtils.isEqual(net, employeeCharge.getNet()) || !GamaMoneyUtils.isEqual(advance, employeeCharge.getAdvance())) {
            employeeCharge.setAdvance(advance);
            employeeCharge.setNet(net);
            employeeCharge.setNetTotal(GamaMoneyUtils.subtract(net, advance));
        }
    }

    /**
     *
     * @param employeeId - employee id
     * @param date - date
     * @param employeeName - employee name used on error message only
     * @param nullable - true - returns null if not found, false - throw exception if not found or found too many
     * @return one WorkHours record by date and employeeId
     */
    private WorkHoursDto getAndCheckWorkHours(long employeeId, LocalDate date, String employeeName, boolean nullable) {
        WorkHoursSql hours;
        try {
            hours = entityManager.createQuery(
                    "SELECT a FROM " + WorkHoursSql.class.getName() + " a" +
                            " JOIN a." + WorkHoursSql_.EMPLOYEE +
                            " JOIN a." + WorkHoursSql_.EMPLOYEE_CARD +
                            " WHERE a.companyId = :companyId" +
                            " AND a.archive IS NOT true" +
                            " AND a.employee.id = :employeeId" +
                            " AND a.date = :date", WorkHoursSql.class)
                    .setParameter("companyId", auth.getCompanyId())
                    .setParameter("employeeId", employeeId)
                    .setParameter("date", date.withDayOfMonth(1))
                    .getSingleResult();
        } catch (NoResultException e) {
            if (nullable) return null;
            throw new GamaException(MessageFormat.format(
                    TranslationService.getInstance().translate(TranslationService.SALARY.NoEmployeeWorkHours, auth.getLanguage()),
                    employeeName != null ? employeeName : employeeId));
        } catch (NonUniqueResultException e) {
            if (nullable) return null;
            throw new GamaException(MessageFormat.format(
                    TranslationService.getInstance().translate(TranslationService.SALARY.EmployeeWorkHoursDuplicates, auth.getLanguage()),
                    employeeName != null ? employeeName : employeeId));
        }
        Validators.checkArgument(CollectionsHelper.hasValue(hours.getPositions()), MessageFormat.format(
                TranslationService.getInstance().translate(TranslationService.SALARY.NoEmployeeWorkHoursByPosition, auth.getLanguage()),
                employeeName != null ? employeeName : employeeId));
        return workHoursSqlMapper.toDto(hours);
    }


    private WorkHoursPosition getMainWorkHoursPosition(WorkHoursDto hours) {
        if (hours == null) return null;
        if (hours.getMainPosition() != null) return hours.getMainPosition();
        return findMainWorkHoursPosition(hours);
    }

    private WorkHoursPosition findMainWorkHoursPosition(WorkHoursDto hours) {
        if (hours == null) return null;
        if (CollectionsHelper.hasValue(hours.getPositions())) {
            for (WorkHoursPosition p : hours.getPositions()) {
                if (p.getPosition() != null && p.getPosition().isMain()) {
                    return p;
                }
            }
            return hours.getPositions().get(0);
        }
        return null;
    }

    /**
     * check if there are registered worked hours
     */
    public boolean checkIfWork(WorkHoursPosition position) {
        if (position == null || position.getPeriod() == null || position.getWorkData() == null) return false;
        return IntegerUtils.isPositive(position.getWorkData().getHours());
    }

    /**
     * check if there are registered illness days
     */
    public boolean checkIfIllness(WorkHoursPosition position) {
        if (position == null || position.getPeriod() == null || position.getWorkData() == null) return false;
        return IntegerUtils.isPositive(position.getWorkData().getIllness());
    }

    /**
     * check if there are registered vacations days
     */
    public boolean checkIfVacation(WorkHoursPosition position) {
        if (position == null || position.getPeriod() == null || position.getWorkData() == null) return false;
        return IntegerUtils.isPositive(position.getWorkData().getVacation());
    }

    /**
     * check if there are registered child days
     */
    public boolean checkIfChildDays(WorkHoursPosition position) {
        if (position == null || position.getPeriod() == null || position.getWorkData() == null) return false;
        return IntegerUtils.isPositive(position.getWorkData().getChildDays());
    }


    /**
     * check if employee last working day in the month is type 'type'
     */
    public int calcLastDaysType(WorkHoursPosition position, WorkTimeCodeType type) {
        if (position == null || CollectionsHelper.isEmpty(position.getPeriod())) return 0;

        return (int) ImmutableList.copyOf(position.getPeriod()).reverse().stream()
                .filter(day -> IntegerUtils.isPositive(day.getWorkData().getHours()))
                .takeWhile(day -> day.getCode() != null && day.getCode().getType() == type)
                .count();
    }

    /**
     * check if employee first working day in the month is type 'type'
     */
    public boolean checkIfFirstDayIsType(WorkHoursPosition position, WorkTimeCodeType type) {
        if (position == null || CollectionsHelper.isEmpty(position.getPeriod())) return false;

        return position.getPeriod().stream()
                .filter(day -> IntegerUtils.isPositive(day.getWorkData().getHours()))
                .takeWhile(day -> day.getCode() != null && day.getCode().getType() == type)
                .findFirst()
                .isPresent();
    }

    public EmployeeChargeDto generateEmployeeCharge(final long salaryId, SalaryType salaryType, final long employeeId) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        return dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            SalarySql salary = dbServiceSQL.getAndCheck(SalarySql.class, salaryId);
            EmployeeCardSql employeeCard = dbServiceSQL.getAndCheck(EmployeeCardSql.class, employeeId, EmployeeCardSql.GRAPH_ALL);
            EmployeeSql employee = employeeCard.getEmployee();

            CompanyTaxSettings companyTaxSettings = salarySettingsService.getCompanyTaxSettings(salary.getDate());
            CompanySalarySettings companySalarySettings = salarySettingsService.getCompanySalarySettings(salary.getDate());

            EmployeeChargeDto employeeChargeNew = generateEmployeeCharge(salary.getDate(),
                    employeeSqlMapper.toDto(employee), employeeCardSqlMapper.toDto(employeeCard),
                    companySettings, companyTaxSettings, companySalarySettings, salaryType);
            employeeChargeNew.setParentId(salaryId);

            EmployeeChargeDto employeeCharge = getSalaryEmployeeChargeCheck(salaryId, employeeId, true);

            return mergeEmployeeCharge(employeeCharge, employeeChargeNew, salaryType, companySettings);
        });
    }

    public EmployeeChargeDto generateEmployeeCharge(LocalDate date, EmployeeDto employee, EmployeeCardDto employeeCard,
                                                    CompanySettings companySettings,
                                                    CompanyTaxSettings companyTaxSettings,
                                                    CompanySalarySettings companySalarySettings,
                                                    SalaryType salaryType) {

        LocalDate dateFrom = date.withDayOfMonth(1);
        LocalDate dateTo = date.with(lastDayOfMonth());

        LocalDate hired = employeeCard.getHired();
        if (hired != null && hired.isAfter(dateTo)) {
            log.warn(this.getClass().getSimpleName() + ": Employee " + employee + " hired after " + dateTo);
            return null;
        }
        LocalDate fired = employeeCard.getFired();
        if (fired != null && fired.isBefore(dateFrom)) {
            log.warn(this.getClass().getSimpleName() + ": Employee " + employee + " fired before " + dateFrom);
            return null;
        }

        EmployeeChargeDto employeeCharge = new EmployeeChargeDto(null, employee, employeeCard, companyTaxSettings);
        employeeCharge.setDate(date);
        employeeCharge.setCharges(new ArrayList<>());

        EmployeeTaxSettings employeeTaxSettings = Validators.checkNotNull(
                salarySettingsService.generateEmployeeTaxSettings(companySettings, companyTaxSettings, companySalarySettings, employeeCard, date),
                MessageFormat.format(
                        TranslationService.getInstance().translate(TranslationService.SALARY.NoEmployeeTax, auth.getLanguage()),
                        employee.getName()));
        employeeCharge.setEmployeeTaxSettings(employeeTaxSettings);

        long employeeId = employee.getId();

        WorkHoursDto hours = getAndCheckWorkHours(employeeId, date, employee.getName(), false);
        WorkHoursPosition mainWorkHoursPosition = Validators.checkNotNull(getMainWorkHoursPosition(hours), "No main position");
        if (mainWorkHoursPosition.getWorkData() == null) mainWorkHoursPosition.setWorkData(new WorkData());

        // check if employee worked
        boolean isEmployeeWorked = checkIfWork(mainWorkHoursPosition);

        if (salaryType == SalaryType.ADVANCE && isEmployeeWorked) {
            Validators.checkArgument(Validators.isValid(companySettings.getChargeAdvance()),
                    TranslationService.getInstance().translate(TranslationService.SALARY.NoChargeAdvanceSettings, auth.getLanguage()));

            for (DocPosition p : employeeCard.getPositions()) {
                if (GamaMoneyUtils.isNonZero(p.getAdvance())) {
                    WorkHoursPosition workHoursPosition = new WorkHoursPosition();
                    workHoursPosition.setPosition(p);
                    DocChargeAmount chargeAmount = new DocChargeAmount(workHoursPosition, companySettings.getChargeAdvance(), p.getAdvance());
                    employeeCharge.getCharges().add(chargeAmount);
                }
            }

        } else if (salaryType == SalaryType.SALARY) {
            Validators.checkArgument(Validators.isValid(companySettings.getChargeWork()),
                    TranslationService.getInstance().translate(TranslationService.SALARY.NoChargeWorkSettings, auth.getLanguage()));
            Validators.checkArgument(Validators.isValid(companySettings.getChargeIllness()),
                    TranslationService.getInstance().translate(TranslationService.SALARY.NoChargeIllnessSettings, auth.getLanguage()));
            Validators.checkArgument(Validators.isValid(companySettings.getChargeVacation()),
                    TranslationService.getInstance().translate(TranslationService.SALARY.NoChargeVacationSettings, auth.getLanguage()));

            boolean isVacation = checkIfVacation(mainWorkHoursPosition);
            boolean isIllness = checkIfIllness(mainWorkHoursPosition);
            boolean isChildDays = checkIfChildDays(mainWorkHoursPosition);

            if (isVacation || isIllness || isChildDays) {

                GamaMoney ave = getAverageDaySalary(companySettings, employeeId, date, 3, false);
                if (GamaMoneyUtils.isNonZero(ave)) {

                    // calculate vacations
                    if (isVacation) {
                        GamaMoney amount = GamaMoneyUtils.multipliedBy(ave, mainWorkHoursPosition.getWorkData().getVacation());
                        DocChargeAmount chargeAmount = new DocChargeAmount(mainWorkHoursPosition, companySettings.getChargeVacation(), amount);
                        employeeCharge.getCharges().add(chargeAmount);
                    }

                    // calculate child days
                    if (isChildDays) {
                        GamaMoney amount = GamaMoneyUtils.multipliedBy(ave, mainWorkHoursPosition.getWorkData().getChildDays());
                        DocChargeAmount chargeAmount = new DocChargeAmount(mainWorkHoursPosition, companySettings.getChargeChildDays(), amount);
                        employeeCharge.getCharges().add(chargeAmount);
                    }

                    // calculate illness
                    if (isIllness && IntegerUtils.isPositive(companySalarySettings.getIllnessDays())) {
                        // need to check all days because illness period can start and end several times per month
                        // special case - check first day

                        int totalDays = 0, payDays = 0;

                        if (checkIfFirstDayIsType(mainWorkHoursPosition, WorkTimeCodeType.ILLNESS)) {
                            WorkHoursDto hoursPrev = getAndCheckWorkHours(employeeId, employeeCharge.getDate().minusMonths(1),
                                    employeeCharge.getEmployee().getName(), true);
                            if (hoursPrev != null) {
                                WorkHoursPosition mainWorkHoursPositionPrev = getMainWorkHoursPosition(hoursPrev);
                                totalDays = calcLastDaysType(mainWorkHoursPositionPrev, WorkTimeCodeType.ILLNESS);
                            }
                        }

                        for (WorkHoursDay day : mainWorkHoursPosition.getPeriod()) {
                            if (day.getCode() != null && day.getCode().getType() == WorkTimeCodeType.ILLNESS) {
                                totalDays++;
                                // if first illness working days
                                if (totalDays <= companySalarySettings.getIllnessDays() && IntegerUtils.isPositive(day.getWorkData().getHours())) {
                                    payDays++;
                                }
                            } else if (payDays > 0) {
                                makeIllnessCharge(payDays, ave, companySalarySettings, companySettings, employeeCharge, mainWorkHoursPosition);

                                totalDays = 0;
                                payDays = 0;
                            }
                        }
                        makeIllnessCharge(payDays, ave, companySalarySettings, companySettings, employeeCharge, mainWorkHoursPosition);
                    }
                }
            }

            boolean hasMain = false;
            if (hours != null && CollectionsHelper.hasValue(hours.getPositions())) {
                for (WorkHoursPosition p : hours.getPositions()) {
                    GamaMoney amount = calculateAmount(employeeCharge.getEmployee(), companySalarySettings, p);
                    DocChargeAmount chargeAmount = new DocChargeAmount(p, companySettings.getChargeWork(), amount);
                    employeeCharge.getCharges().add(chargeAmount);

                    if (p.getPosition() != null && p.getPosition().isMain()) {
                        hasMain = true;
                        employeeCharge.setWorkData(p.getWorkData());
                    }
                }
            }
            // if no main position - set workData from first position
            if (!hasMain && hours != null && CollectionsHelper.hasValue(hours.getPositions())) {
                employeeCharge.setWorkData(hours.getPositions().get(0).getWorkData());
            }
        }

        return !employeeCharge.getCharges().isEmpty() ? employeeCharge : null;
    }

    private void makeIllnessCharge(int days, GamaMoney ave, CompanySalarySettings companySalarySettings,
                                   CompanySettings companySettings, EmployeeChargeDto employeeCharge, WorkHoursPosition workHoursPosition) {
        if (days > 0) {
            GamaMoney amount = GamaMoneyUtils.multipliedBy(ave, days * companySalarySettings.getIllnessPct() / 100.0);
            DocChargeAmount chargeAmount = new DocChargeAmount(workHoursPosition, companySettings.getChargeIllness(), amount);
            employeeCharge.getCharges().add(chargeAmount);
        }
    }

    public EmployeeChargeDto mergeEmployeeCharge(EmployeeChargeDto employeeCharge, EmployeeChargeDto employeeChargeNew,
                                                 SalaryType salaryType, CompanySettings companySettings) {
        if (employeeCharge == null) return employeeChargeNew;
        if (employeeChargeNew == null) return employeeCharge;

        if (salaryType == SalaryType.SALARY) {
            // check if salaries are fixed - if yes - do nothing
            if (hasFixedCharge(employeeCharge.getCharges(), companySettings.getChargeWork()))
                return employeeCharge;

            // add advances and fixed items
            List<DocChargeAmount> charges = new ArrayList<>();
            for (DocChargeAmount charge : employeeCharge.getCharges()) {
                if (charge.getCharge().getId().equals(companySettings.getChargeAdvance().getId()) || charge.isFixed())
                    charges.add(charge);
            }
            charges.addAll(employeeChargeNew.getCharges());
            employeeCharge.setCharges(charges);
            employeeCharge.setWorkData(employeeChargeNew.getWorkData());

        } else if (salaryType == SalaryType.ADVANCE) {
            // check if some work charge is fixed - if yes - do nothing
            if (hasFixedCharge(employeeCharge.getCharges(), companySettings.getChargeAdvance()))
                return employeeCharge;

            // add everything except advances and fixed items
            List<DocChargeAmount> charges = new ArrayList<>();
            if (employeeCharge.getCharges() != null) {
                for (DocChargeAmount charge : employeeCharge.getCharges()) {
                    if (!charge.getCharge().getId().equals(companySettings.getChargeAdvance().getId()) || charge.isFixed())
                        charges.add(charge);
                }
            }
            charges.addAll(employeeChargeNew.getCharges());
            employeeCharge.setCharges(charges);
        }

        return employeeCharge;
    }

    private GamaMoney calculateAmount(EmployeeDto employee, CompanySalarySettings settings, WorkHoursPosition p) {
        GamaMoney total = calculateAmountByPosition(employee, settings, p.getPosition(), p);
        if (p.getSubPositions() != null) {
            for (DocPosition position : p.getSubPositions()) {
                total = GamaMoneyUtils.add(total, calculateAmountByPosition(employee, settings, position, p));
            }
        }
        return total;
    }

    private GamaMoney calculateAmountByPosition(EmployeeDto employee, CompanySalarySettings settings, DocPosition position, WorkHoursPosition wh) {

        Validators.checkNotNull(position, MessageFormat.format(TranslationService.getInstance().translate(TranslationService.SALARY.NoEmployeePosition, auth.getLanguage()), employee.getName()));
        Validators.checkNotNull(position.getWageType(), MessageFormat.format(TranslationService.getInstance().translate(TranslationService.SALARY.NoEmployeePositionWageType, auth.getLanguage()), employee.getName(), position.getName()));
        Validators.checkNotNull(position.getWage(), MessageFormat.format(TranslationService.getInstance().translate(TranslationService.SALARY.NoEmployeePositionWage, auth.getLanguage()), employee.getName(), position.getName()));

        if (WageType.FIXED.equals(position.getWageType())) {
            return GamaMoneyUtils.toMoney(position.getWage());
        }

        if (wh.getWorkData() != null) {
            if (WageType.HOURLY.equals(position.getWageType())) {
                GamaMoney normal = GamaMoneyUtils.toMoney(GamaMoneyUtils.multipliedBy(position.getWage(), wh.getWorkData().getWorked()));
                GamaMoney overtime = GamaMoneyUtils.toMoney(GamaMoneyUtils.multipliedBy(position.getWage(), NumberUtils.multipliedBy(wh.getWorkData().getOvertime(), settings.getOvertimeC())));
                GamaMoney night = GamaMoneyUtils.toMoney(GamaMoneyUtils.multipliedBy(position.getWage(), NumberUtils.multipliedBy(wh.getWorkData().getNight(), settings.getNightC())));
                GamaMoney weekend = GamaMoneyUtils.toMoney(GamaMoneyUtils.multipliedBy(position.getWage(), NumberUtils.multipliedBy(wh.getWorkData().getWeekend(), settings.getWeekendC())));
                GamaMoney holiday = GamaMoneyUtils.toMoney(GamaMoneyUtils.multipliedBy(position.getWage(), NumberUtils.multipliedBy(wh.getWorkData().getHoliday(), settings.getHolidayC())));

                GamaMoney overtimeNight = GamaMoneyUtils.toMoney(GamaMoneyUtils.multipliedBy(position.getWage(), NumberUtils.multipliedBy(wh.getWorkData().getOvertimeNight(), settings.getOvertimeNightC())));
                GamaMoney overtimeWeekend = GamaMoneyUtils.toMoney(GamaMoneyUtils.multipliedBy(position.getWage(), NumberUtils.multipliedBy(wh.getWorkData().getOvertimeWeekend(), settings.getOvertimeWeekendC())));
                GamaMoney overtimeHoliday = GamaMoneyUtils.toMoney(GamaMoneyUtils.multipliedBy(position.getWage(), NumberUtils.multipliedBy(wh.getWorkData().getOvertimeHoliday(), settings.getOvertimeHolidayC())));

                return GamaMoneyUtils.total(normal, overtime, night, weekend, holiday, overtimeNight, overtimeWeekend, overtimeHoliday);
            }

            if (WageType.MONTHLY.equals(position.getWageType())) {
                GamaBigMoney hour = position.getWage().withScale(position.getWage().getScale() + 2).dividedBy(wh.getWorkData().getHours());
                GamaMoney normal = IntegerUtils.isEqual(wh.getWorkData().getHours(), wh.getWorkData().getWorked()) ?
                        GamaMoneyUtils.toMoney(position.getWage()) :
                        GamaMoneyUtils.toMoney(hour.multipliedBy(wh.getWorkData().getWorked()));
                GamaMoney overtime = GamaMoneyUtils.toMoney(GamaMoneyUtils.multipliedBy(hour, NumberUtils.multipliedBy(wh.getWorkData().getOvertime(), settings.getOvertimeC())));
                GamaMoney night = GamaMoneyUtils.toMoney(GamaMoneyUtils.multipliedBy(hour, NumberUtils.multipliedBy(wh.getWorkData().getNight(), settings.getNightC())));
                GamaMoney weekend = GamaMoneyUtils.toMoney(GamaMoneyUtils.multipliedBy(hour, NumberUtils.multipliedBy(wh.getWorkData().getWeekend(), settings.getWeekendC())));
                GamaMoney holiday = GamaMoneyUtils.toMoney(GamaMoneyUtils.multipliedBy(hour, NumberUtils.multipliedBy(wh.getWorkData().getHoliday(), settings.getHolidayC())));

                GamaMoney overtimeNight = GamaMoneyUtils.toMoney(GamaMoneyUtils.multipliedBy(hour, NumberUtils.multipliedBy(wh.getWorkData().getOvertimeNight(), settings.getOvertimeNightC())));
                GamaMoney overtimeWeekend = GamaMoneyUtils.toMoney(GamaMoneyUtils.multipliedBy(hour, NumberUtils.multipliedBy(wh.getWorkData().getOvertimeWeekend(), settings.getOvertimeWeekendC())));
                GamaMoney overtimeHoliday = GamaMoneyUtils.toMoney(GamaMoneyUtils.multipliedBy(hour, NumberUtils.multipliedBy(wh.getWorkData().getOvertimeHoliday(), settings.getOvertimeHolidayC())));

                return GamaMoneyUtils.total(normal, overtime, night, weekend, holiday, overtimeNight, overtimeWeekend, overtimeHoliday);
            }
        }

        return null;
    }


    static class WorkCodesCalendar {
        int year;
        int month;
        CalendarMonth calendarMonth;
        CalendarMonth calendarMonth2;
        WorkTimeCode workCodeHoliday;
        WorkTimeCode workCodeRest;
        WorkTimeCode workCodeChildday;
        Map<String, WorkTimeCode> workCodeMap;
    }

    private WorkCodesCalendar retrieveWorkCodesCalendar(String country, int year, int month) {
        WorkCodesCalendar result = new WorkCodesCalendar();
        result.year = year;
        result.month = month;

        List<WorkTimeCode> workCodes = listWorkTimeCodes(country);
        if (CollectionsHelper.isEmpty(workCodes))
            throw new GamaException("No Work Codes");

        result.workCodeMap = new HashMap<>();
        for (WorkTimeCode code : workCodes) {
            result.workCodeMap.put(code.getCode(), code);

            if (result.workCodeHoliday == null && code.getType() == WorkTimeCodeType.HOLIDAY)
                result.workCodeHoliday = code;
            else if (result.workCodeRest == null && code.getType() == WorkTimeCodeType.WEEKEND)
                result.workCodeRest = code;
            else if (result.workCodeChildday == null && code.getType() == WorkTimeCodeType.CHILDDAY)
                result.workCodeChildday = code;
        }

        result.calendarMonth = calendarService.getMonth(year, month, false);
        result.calendarMonth2 = calendarService.getMonth(month < 12 ? year : year + 1, month < 12 ? month + 1 : 1, false);
        return result;
    }

    private WorkHoursPosition setAbsencesInWorkHoursPosition(WorkHoursPosition workHoursPosition, LocalDate date,
                                                             List<EmployeeAbsenceDto> absences, Map<String, WorkTimeCode> workCodeMap) {
        if (workHoursPosition == null || CollectionsHelper.isEmpty(absences)) return workHoursPosition;

        for (EmployeeAbsenceDto absence : absences) {
            LocalDate dateFrom = date.isBefore(absence.getDateFrom()) ? absence.getDateFrom() : date;
            LocalDate dateTo = absence.getDateTo().isBefore(date.plusMonths(1))
                    ? absence.getDateTo()
                    : date.plusMonths(1).minusDays(1);

            if (dateFrom.isAfter(dateTo)) continue;

            int day = dateFrom.getDayOfMonth() - 1;
            int dayTo = dateTo.getDayOfMonth() - 1;
            int periodSize = workHoursPosition.getPeriod().size();
            while (day <= dayTo && day < periodSize) {
                WorkHoursDay workHoursDay = workHoursPosition.getPeriod().get(day);
                if (workHoursPosition.getWorkData() == null) workHoursPosition.setWorkData(new WorkData());

                if ((BooleanUtils.isTrue(absence.getWeekends()) && workHoursDay.getCode() != null && workHoursDay.getCode().getType() == WorkTimeCodeType.WEEKEND) ||
                        (BooleanUtils.isTrue(absence.getHolidays()) && workHoursDay.getCode() != null && workHoursDay.getCode().getType() == WorkTimeCodeType.HOLIDAY) ||
                        workHoursDay.getCode() == null) {

                    //workHoursDay.setCode(absence.getCode());
                    workHoursDay.setCode(absence.getCode() == null ? null : workCodeMap.get(absence.getCode().getCode()));

                    workHoursDay.getWorkData().setWorked(null);
                    workHoursDay.getWorkData().setNight(null);
                    workHoursDay.getWorkData().setHoliday(null);
                    workHoursDay.getWorkData().setWeekend(null);
                    workHoursDay.getWorkData().setOvertime(null);
                    workHoursDay.getWorkData().setOvertimeNight(null);
                    workHoursDay.getWorkData().setOvertimeHoliday(null);
                    workHoursDay.getWorkData().setOvertimeWeekend(null);
                }
                day++;
            }

        }
        calculateSummary(workHoursPosition);
        return workHoursPosition;
    }


    public WorkHoursDto generateWorkHoursForEmployee(CompanySalarySettings settings, String country, int year, int month,
                                                     EmployeeDto employee, EmployeeCardDto employeeCard) throws GamaWarningException {
        if (employee == null) {
            log.warn(this.getClass().getSimpleName() + ": No employee");
            return null;
        }
        if (employeeCard == null) {
            log.warn(this.getClass().getSimpleName() + ": No employee card");
            return null;
        }
        final long companyId = auth.getCompanyId();
        final long employeeId = employee.getId();
        if (employee.getCompanyId() != companyId || employeeCard.getCompanyId() != companyId) {
            throw new GamaUnauthorizedException("Wrong company, Employee id/companyId=" + employeeId + "/" + employee.getCompanyId() +
                    ", companyId=" + companyId);
        }

        if (CollectionsHelper.isEmpty(employeeCard.getPositions())) {
            log.warn(this.getClass().getSimpleName() + ": No positions for employee " + employeeId + " in company " + companyId);
            throw new GamaWarningException(MessageFormat.format(
                    TranslationService.getInstance().translate(TranslationService.SALARY.NoEmployeePosition, auth.getLanguage()),
                    employeeCard.getEmployee().getName()));
        }

        LocalDate date1 = LocalDate.of(year, month, 1);

        LocalDate hired = employeeCard.getHired();
        if (hired != null && !hired.isBefore(date1.plusMonths(1))) {
            log.warn(this.getClass().getSimpleName() + ": Employee " + employeeId + " in company " + companyId + " hired after " + year + "." + month);
            return null;
        }

        LocalDate fired = employeeCard.getFired();
        if (fired != null && fired.isBefore(date1)) {
            log.warn(this.getClass().getSimpleName() + ": Employee " + employeeId + " in company " + companyId + " fired before " + year + "." + month);
            return null;
        }

        return dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            List<EmployeeAbsenceDto> absences = entityManager.createQuery(
                            "SELECT a FROM " + EmployeeAbsenceSql.class.getName() + " a" +
                                    " WHERE a." + EmployeeAbsenceSql_.COMPANY_ID + " = :companyId" +
                                    " AND a." + EmployeeAbsenceSql_.EMPLOYEE + "." + EmployeeSql_.ID + " = :employeeId" +
                                    " AND NOT (a." + EmployeeAbsenceSql_.DATE_FROM + " >= :dateTo" +
                                    " OR a." + EmployeeAbsenceSql_.DATE_TO + " < :dateFrom)" +
                                    " AND a.archive IS NOT true",
                            EmployeeAbsenceSql.class)
                    .setParameter("companyId", auth.getCompanyId())
                    .setParameter("employeeId", employeeId)
                    .setParameter("dateFrom", date1)
                    .setParameter("dateTo", date1.plusMonths(1))
                    .getResultStream()
                    .map(employeeAbsenceSqlMapper::toDto)
                    .collect(Collectors.toList());

            WorkHoursDto hours = getAndCheckWorkHours(employeeId, date1, employee.getName(), true);
            if (hours == null) {
                hours = new WorkHoursDto(date1, employee, employeeCard);
                hours.setCompanyId(companyId);
                hours.setId(null);
            } else {
                if (BooleanUtils.isTrue(hours.getArchive())) {
                    hours.setArchive(false);
                    hours.setFinished(false);
                    hours.setFixed(false);
                } else if (BooleanUtils.isTrue(hours.getFixed()) || BooleanUtils.isTrue(hours.getFinished())) {
                    return hours;
                }
            }
            hours.setEmployee(employee);
            hours.setEmployeeCard(employeeCard);
            hours.setPositions(null);
            List<WorkHoursPosition> positions = new ArrayList<>();

            WorkCodesCalendar workCodesCalendar = retrieveWorkCodesCalendar(country, year, month);

            // add 'main' first
            DocPosition mainPosition = salarySettingsService.getMainPosition(employeeCard.getPositions());
            WorkHoursPosition workHoursPositionMain = null;
            if (mainPosition != null) {
                workHoursPositionMain = setAbsencesInWorkHoursPosition(
                        generateWorkHoursPosition(settings, workCodesCalendar, mainPosition, hired, fired),
                        date1, absences, workCodesCalendar.workCodeMap);
                if (workHoursPositionMain != null) positions.add(workHoursPositionMain);
            }

            // add the rest
            for (DocPosition position : employeeCard.getPositions()) {
                if (position == mainPosition) continue;
                if (position.isAggregate() && workHoursPositionMain != null) {
                    if (workHoursPositionMain.getSubPositions() == null) {
                        workHoursPositionMain.setSubPositions(new ArrayList<>());
                    }
                    workHoursPositionMain.getSubPositions().add(position);
                } else {
                    WorkHoursPosition workHoursPosition = setAbsencesInWorkHoursPosition(
                            generateWorkHoursPosition(settings, workCodesCalendar, position, hired, fired),
                            date1, absences, workCodesCalendar.workCodeMap);
                    if (workHoursPosition != null) positions.add(workHoursPosition);
                }
            }

            hours.setPositions(positions);

            return hours;
        });
    }

    public List<WorkTimeCode> listWorkTimeCodes(String country) {
        CountryWorkTimeCodeSql countryWorkTimeCode = dbServiceSQL.getById(CountryWorkTimeCodeSql.class, country);
        return countryWorkTimeCode == null ? null : countryWorkTimeCode.getCodes();
    }

    public WorkHoursPosition generateWorkHoursPosition(CompanySettings companySettings, int year, int month,
                                                       DocPosition position, LocalDate hired, LocalDate fired) {
        CompanySalarySettings settings = salarySettingsService.getCompanySalarySettings(year, month);
        return generateWorkHoursPosition(settings, retrieveWorkCodesCalendar(companySettings.getCountry(), year, month), position, hired, fired);
    }

    private WorkHoursPosition generateWorkHoursPosition(CompanySalarySettings settings,
                                                        WorkCodesCalendar workCodesCalendar,
                                                        DocPosition position,
                                                        LocalDate hired, LocalDate fired) {
        LocalDate dateWT = LocalDate.of(workCodesCalendar.year, workCodesCalendar.month, 1);

        LocalDate dateFrom = position == null ? hired :
                DateUtils.compare(position.getDateFrom(), true, hired, true) < 0 ? hired : position.getDateFrom();
        LocalDate dateTo = position == null ? fired :
                DateUtils.compare(fired, false, position.getDateTo(), false) < 0 ? fired : position.getDateTo();

        if (dateTo != null && dateTo.isBefore(dateWT)) return null;
        if (dateFrom != null && dateFrom.isAfter(dateWT.with(lastDayOfMonth())))
            return null;

        WorkHoursPosition result = new WorkHoursPosition();
        result.setPosition(position);

        DocWorkSchedule workSchedule = position != null ? position.getWorkSchedule() : null;
        WorkScheduleType type = WorkScheduleType.WEEKLY;
        List<WorkScheduleDay> schedule = null;
        LocalDate start = null;
        int periodLength = 7;
        if (workSchedule != null) {
            type = workSchedule.getType();
            periodLength = workSchedule.getPeriod();
            schedule = workSchedule.getSchedule();
            start = workSchedule.getStart();
        }
        if (type == null) type = WorkScheduleType.WEEKLY;
        periodLength = type == WorkScheduleType.WEEKLY ? 7 : periodLength > 0 ? periodLength : 7;
        if (CollectionsHelper.isEmpty(schedule)) {
            schedule = Lists.newArrayList(
                    new WorkScheduleDay(BigDecimal.valueOf(8)),
                    new WorkScheduleDay(BigDecimal.valueOf(8)),
                    new WorkScheduleDay(BigDecimal.valueOf(8)),
                    new WorkScheduleDay(BigDecimal.valueOf(8)),
                    new WorkScheduleDay(BigDecimal.valueOf(8))
            );
        }

        long periodIndexStart = 0;
        if (type == WorkScheduleType.WEEKLY) {
            periodIndexStart = dateWT.getDayOfWeek().getValue() - 1; // 0 for Monday ... 6 for Sunday
        } else if (type == WorkScheduleType.PERIODIC && start != null) {
            periodIndexStart = Math.abs(ChronoUnit.DAYS.between(dateWT, start)) % periodLength;
        }

        List<WorkHoursDay> period = calendarMonth2Pos(workCodesCalendar.workCodeHoliday, workCodesCalendar.workCodeRest, workCodesCalendar.calendarMonth);
        int days = 0, workHours = 0, holidayHours = 0;
        for (int i = 0, len = period.size(); i < len; ++i) {
            LocalDate date = dateWT.plusDays(i);
            if (dateFrom != null && dateFrom.isAfter(date)) {
                period.get(i).setCode(null);
                continue;
            }
            if (dateTo != null && dateTo.isBefore(date)) {
                for (int j = i; j < len; ++j) {
                    period.get(j).setCode(null);
                }
                break;
            }

            WorkHoursDay day = period.get(i);
            int periodIndex = (int) (periodIndexStart + i) % periodLength;
            BigDecimal dayHours = periodIndex < schedule.size() ? schedule.get(periodIndex).getHours() : null;
            if (BigDecimalUtils.isPositive(dayHours)) {

                if (!day.isHoliday() || type == WorkScheduleType.PERIODIC) {

                    day.getWorkData().setHours(dayHours.intValue());
                    if (settings.isShorterWorkingDay() && (i < len - 1 ? period.get(i + 1).isHoliday() : workCodesCalendar.calendarMonth2.getDays().get(0).isHoliday())) {
                        day.getWorkData().setHours(day.getWorkData().getHours() - 1);
                    }

                    if (!day.isHoliday()) {
                        ++days;
                        workHours += day.getWorkData().getHours();
                        day.getWorkData().setWorked(day.getWorkData().getHours());

                        if (type == WorkScheduleType.PERIODIC) {
                            day.setCode(null);
                        }

                    } else if (type == WorkScheduleType.PERIODIC) {
                        ++days;
                        holidayHours += day.getWorkData().getHours();
                        day.getWorkData().setHoliday(day.getWorkData().getHours());
                    }
                }
            } else {
                if (day.getCode() == null) day.setCode(workCodesCalendar.workCodeRest);
                day.setRest(true);
            }
        }
        result.setWorkData(new WorkData());
        result.getWorkData().setDays(days);
        if (workHours + holidayHours > 0) result.getWorkData().setHours(workHours + holidayHours);
        if (workHours > 0) result.getWorkData().setWorked(workHours);
        if (holidayHours > 0) result.getWorkData().setHoliday(holidayHours);
        result.setPeriod(period);

        return result;
    }

    private List<WorkHoursDay> calendarMonth2Pos(WorkTimeCode workCodeHoliday, WorkTimeCode workCodeWeekend, CalendarMonth month) {
        List<WorkHoursDay> pos = new ArrayList<>();
        if (month != null && month.getDays() != null) {
            for (CalendarDay day : month.getDays()) {
                WorkHoursDay workHoursDay = new WorkHoursDay();
                workHoursDay.setWorkData(new WorkData());
                workHoursDay.setHoliday(day.isHoliday());
                workHoursDay.setWeekend(day.isWeekend());

                if (workHoursDay.isHoliday()) {
                    workHoursDay.setCode(workCodeHoliday);
                } else if (workHoursDay.isWeekend()) {
                    workHoursDay.setCode(workCodeWeekend);
                }

                pos.add(workHoursDay);
            }
        }
        return pos;
    }

    public String refreshWorkHoursTask(int year, int month, boolean fresh) {
        return taskQueueService.queueTask(new WorkHoursTask(auth.getCompanyId(), year, month, fresh));
    }

    public TaskResponse<Void> refreshWorkHoursTask(int year, int month, boolean fresh, List<Long> ids) {
        LocalDate date = LocalDate.of(year, month, 1);

        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        CompanySalarySettings companySalarySettings = salarySettingsService.getCompanySalarySettings(date);

        Set<String> warnings = new HashSet<>();

        try {
            dbServiceSQL.executeInTransaction(entityManager -> {
                List<Long> except = null;
                if (fresh) {
                    entityManager.createQuery("DELETE FROM " + WorkHoursSql.class.getName() + " a" +
                                    " WHERE companyId = :companyId" +
                                    " AND date = :date")
                            .setParameter("companyId", auth.getCompanyId())
                            .setParameter("date", date)
                            .executeUpdate();
                } else {
                    except = entityManager.createQuery("SELECT employee.id FROM " + WorkHoursSql.class.getName() + " a" +
                                    " WHERE companyId = :companyId" +
                                    " AND date = :date" +
                                    " AND a.archive IS NOT true", Long.class)
                            .setParameter("companyId", auth.getCompanyId())
                            .setParameter("date", date)
                            .getResultList();
                }
                entityManager.createQuery("SELECT a FROM " + EmployeeCardSql.class.getName() + " a" +
                                " JOIN a.employee " +
                                " WHERE a.companyId = :companyId" +
                                " AND a.id NOT IN :ids" +
                                " AND a.archive IS NOT true", EmployeeCardSql.class)
                        .setParameter("companyId", auth.getCompanyId())
                        .setParameter("ids", except != null ? except : Collections.singletonList(-1L))
                        .getResultStream()
                        .forEach(employeeCard -> {
                            try {
                                WorkHoursDto wh = generateWorkHoursForEmployee(companySalarySettings, companySettings.getCountry(),
                                        year, month,
                                        employeeSqlMapper.toDto(employeeCard.getEmployee()),
                                        employeeCardSqlMapper.toDto(employeeCard));
                                if (wh != null) {
                                    dbServiceSQL.saveEntityInCompany(workHoursSqlMapper.toEntity(wh));
                                    updateVacationsTask(year, month, employeeCard.getId());
                                    if (ids != null) ids.add(employeeCard.getId());
                                }
                            } catch (GamaWarningException e) {
                                warnings.add(e.getMessage());
                            } catch (GamaException e) {
                                throw e;
                            } catch (Exception e) {
                                throw new GamaException(e.getMessage(), e);
                            }
                        });
            });
            return TaskResponse.<Void>success().withWarnings(warnings);

        } catch (GamaException e) {
            log.info(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
            return TaskResponse.error(e.getMessage());
        }
    }

    private void updateVacationsTask(final int year, final int month, final long employeeId) {
        taskQueueService.queueTask(new UpdateEmployeeVacationTask(auth.getCompanyId(), year, month, employeeId));
    }

    public EmployeeAbsenceDto saveAbsence(final EmployeeAbsenceDto document) {
        Validators.checkNotNull(document.getDateFrom(),
                TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDateFrom, auth.getLanguage()));

        Validators.checkNotNull(document.getDateTo(),
                TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDateTo, auth.getLanguage()));

        Validators.checkArgument(!document.getDateFrom().isAfter(document.getDateTo()),
                MessageFormat.format(
                        TranslationService.getInstance().translate(TranslationService.VALIDATORS.WrongPeriod, auth.getLanguage()),
                        document.getDateFrom(), document.getDateTo()));

        // check if is not an absence record in this period already
        int count = ((Number) entityManager.createQuery("SELECT COUNT(a) FROM " + EmployeeAbsenceSql.class.getName() + " a" +
                        " WHERE companyId = :companyId" +
                        " AND employee.id = :employeeId" +
                        " AND NOT (dateTo < :dateFrom OR dateFrom > :dateTo)" +
                        " AND id <> :id" +
                        " AND (a.archive IS null OR a.archive = false)")
                .setParameter("companyId", auth.getCompanyId())
                .setParameter("employeeId", document.getEmployee().getId())
                .setParameter("dateFrom", document.getDateFrom())
                .setParameter("dateTo", document.getDateTo())
                .setParameter("id", document.getId() == null ? -1 : document.getId())
                .getSingleResult()).intValue();
        if (count > 0) {
            throw new GamaException(MessageFormat.format(
                    TranslationService.getInstance().translate(TranslationService.SALARY.EmployeeAbsenceExists, auth.getLanguage()),
                    document.getDateFrom(), document.getDateTo()));
        }

        count = ((Number) entityManager.createQuery("SELECT COUNT(a) FROM " + WorkHoursSql.class.getName() + " a" +
                        " WHERE companyId = :companyId" +
                        " AND employee.id = :employeeId" +
                        " AND NOT (date < :dateFrom OR date > :dateTo)" +
                        " AND archive IS NOT true")
                .setParameter("companyId", auth.getCompanyId())
                .setParameter("employeeId", document.getEmployee().getId())
                .setParameter("dateFrom", document.getDateFrom().withDayOfMonth(1))
                .setParameter("dateTo", document.getDateTo())
                .getSingleResult()).intValue();
        if (count > 0) {
            throw new GamaException(MessageFormat.format(
                    TranslationService.getInstance().translate(TranslationService.SALARY.WorkHoursRecordExists, auth.getLanguage()),
                    document.getDateFrom(), document.getDateTo()));
        }

        return employeeAbsenceSqlMapper.toDto(dbServiceSQL.saveEntityInCompany(employeeAbsenceSqlMapper.toEntity(document)));
    }

    private boolean checkSettingsCharge(DocCharge docCharge, ChargeDto charge) {
        return docCharge != null && docCharge.getId().equals(charge.getId());
    }

    public ChargeDto saveCharge(ChargeDto charge) {
        return chargeSqlMapper.toDto(dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            ChargeSql entity;
            if (charge.getId() == null || charge.getId() == 0) {
                entity = new ChargeSql();
            } else {
                entity = dbServiceSQL.getAndCheck(ChargeSql.class, charge.getId());
                Validators.checkDocumentVersion(entity, charge, auth.getLanguage());
            }
            entity.setName(charge.getName());
            entity.setDebit(charge.getDebit());
            entity.setCredit(charge.getCredit());
            entity.setAvgSalary(charge.getAvgSalary());
            entity.setPeriod(charge.getPeriod());
            entity.setEmployeeSSTax(charge.getEmployeeSSTax());
            entity.setCompanySSTax(charge.getCompanySSTax());
            entity.setIncomeTax(charge.getIncomeTax());
            entity.setGuarantyFund(charge.getGuarantyFund());
            entity.setShiTax(charge.getShiTax());

            entity = dbServiceSQL.saveEntityInCompany(entity);

            updateCompanyCharges(charge);

            return entity;
        }));
    }

    void updateCompanyCharges(ChargeDto charge) {
        CompanySql company = Validators.checkNotNull(dbServiceSQL.getById(CompanySql.class, auth.getCompanyId()), "No company");
        CompanySettings companySettings = Validators.checkNotNull(company.getSettings(), "No company");

        if (checkSettingsCharge(companySettings.getChargeAdvance(), charge)) {
            companySettings.setChargeAdvance(new DocCharge(charge));

        } else if (checkSettingsCharge(companySettings.getChargeWork(), charge)) {
            companySettings.setChargeWork(new DocCharge(charge));

        } else if (checkSettingsCharge(companySettings.getChargeIllness(), charge)) {
            companySettings.setChargeIllness(new DocCharge(charge));

        } else if (checkSettingsCharge(companySettings.getChargeVacation(), charge)) {
            companySettings.setChargeVacation(new DocCharge(charge));

        }
        authSettingsCacheService.put(company.getId(), companySettings);
    }

    public PositionDto savePosition(PositionDto position) {
        return positionSqlMapper.toDto(dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            PositionSql entity;
            if (position.getId() == null || position.getId() == 0) {
                entity = new PositionSql();
            } else {
                entity = dbServiceSQL.getAndCheck(PositionSql.class, position.getId());
                Validators.checkDocumentVersion(entity, position, auth.getLanguage());
            }

            entity.setName(position.getName());
            entity.setDescription(position.getDescription());
            entity.setWorkSchedule(position.getWorkSchedule());
            entity.setWageType(position.getWageType());
            entity.setWage(position.getWage());
            entity.setStart(position.getStart());
            entity.setAdvance(position.getAdvance());

            entity = dbServiceSQL.saveEntityInCompany(entity);

            if (position.getId() != null && position.getId() > 0) {
                updateEmployeePosition(entity);
            }
            return entity;
        }));
    }

    private void updateEmployeePosition(final IPosition position) {
        entityManager.createNativeQuery(
                "WITH UPD AS (" +
                        "SELECT E.id," +
                        " jsonb_agg(" +
                          " CASE" +
                          " WHEN P.position->>'id' = CAST(:positionId AS text) THEN :updatedPosition" +
                          " ELSE P.position" +
                          " END) AS positions" +
                        " FROM employee_cards E" +
                        " CROSS JOIN jsonb_array_elements(E.positions) WITH ORDINALITY P(position, position_index)" +
                        " WHERE E.company_Id = :companyId" +
                          " AND jsonb_path_query_array(E.positions, '$[*].id') @> CAST(CAST(:positionId AS text) AS jsonb)" +
                          " AND (E.archive IS null OR E.archive = false)" +
                        " GROUP BY E.id" +
                        ")" +
                        " UPDATE employee_cards E SET positions = UPD.positions" +
                        " FROM UPD" +
                        " WHERE E.id = UPD.id")
                .unwrap(NativeQuery.class)
                .setParameter("companyId", auth.getCompanyId())
                .setParameter("positionId", position.getId())
                .setParameter("updatedPosition", new DocPosition(position)) //TODO migrate spring ->, JsonBinaryType.INSTANCE)
                .executeUpdate();
    }

    public LocalDate calculateVacationStartYear(LocalDate date, int accMonth) {
        LocalDate start = LocalDate.of(date.getYear(), accMonth, 1);
        return start.isAfter(date) ? start.minusYears(1) : start;
    }

    public EmployeeVacationResponse calculateEmployeeVacation(long employeeId, LocalDate date) {
        LocalDate dateFrom = calculateVacationStartYear(date, auth.getSettings().getAccMonth());
        return calculateEmployeeVacationForPeriod(employeeId, dateFrom, date);
    }

    public EmployeeVacationResponse calculateEmployeeVacationForPeriod(long employeeId, LocalDate dateFrom, LocalDate dateTo) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        EmployeeCardDto employeeCard = employeeCardSqlMapper.toDto(dbServiceSQL.getAndCheck(EmployeeCardSql.class, employeeId));
        Validators.checkNotNull(employeeCard.getHired(), MessageFormat.format(
                TranslationService.getInstance().translate(TranslationService.SALARY.NoEmployeeHiringDate, auth.getLanguage()),
                employeeCard.getEmployee().getName()));
        CompanyTaxSettings companyTaxSettings = salarySettingsService.getCompanyTaxSettings(dateTo);
        CompanySalarySettings companySalarySettings = salarySettingsService.getCompanySalarySettings(dateTo);
        EmployeeTaxSettings employeeTaxSettings = Validators.checkNotNull(salarySettingsService.generateEmployeeTaxSettings(companySettings,
                        companyTaxSettings, companySalarySettings, employeeCard, dateTo),
                MessageFormat.format(
                        TranslationService.getInstance().translate(TranslationService.SALARY.NoEmployeeTax, auth.getLanguage()),
                        employeeCard.getEmployee().getName()));

        Validators.checkArgument(IntegerUtils.isPositive(employeeTaxSettings.getVacationLength()), MessageFormat.format(
                TranslationService.getInstance().translate(TranslationService.SALARY.NoEmployeeVacationLength, auth.getLanguage()),
                employeeCard.getEmployee().getName()));

        Validators.checkPeriod(dateFrom, dateTo, auth.getLanguage());

        LocalDate startDate = dateFrom;
        if (employeeCard.getHired() != null && employeeCard.getHired().isAfter(dateFrom)) {
            startDate = employeeCard.getHired();
            Validators.checkPeriod(startDate, dateTo, auth.getLanguage());
        }

        EmployeeVacationResponse response = new EmployeeVacationResponse();

        long days = ChronoUnit.DAYS.between(startDate, dateTo) + 1;
        long daysInYear = ChronoUnit.DAYS.between(dateFrom, dateFrom.plusYears(1));

        if (!IntegerUtils.isPositive(employeeTaxSettings.getVacationLength())) {
            throw new IllegalArgumentException(MessageFormat.format(
                    TranslationService.getInstance().translate(TranslationService.SALARY.NoEmployeeVacationLength, auth.getLanguage()),
                    employeeCard.getEmployee().getName()));
        }

        // check how many days came from previous year
        int pastDays = 0;
        EmployeeVacationDto employeeVacation = employeeVacationSqlMapper.toDto(dbServiceSQL.getById(EmployeeVacationSql.class, employeeId));
        if (employeeVacation != null && employeeVacation.getVacations() != null) {
            for (VacationBalance vacationBalance : employeeVacation.getVacations()) {
                if (vacationBalance.getYear() == dateFrom.getYear() - 1) {
                    pastDays = IntegerUtils.value(vacationBalance.getBalance());
                    break;
                } else if (vacationBalance.getYear() == dateFrom.getYear()) {
                    pastDays = IntegerUtils.value(vacationBalance.getPast());
                    break;
                }
            }
        }

        /*
         *  daysInYear  <-> vacations
         *  days        <-> x
         *  ---------------------------------
         *  x = days * vacations / daysInYear
         */
        int x = (int) Math.round(Math.floor((double) days * (double) employeeTaxSettings.getVacationLength() / (double) daysInYear));

        response.setDays(pastDays + x);
        response.setDayAvg(getAverageDaySalary(companySettings, employeeId, dateTo, 3, false));
        response.setAmount(GamaMoneyUtils.multipliedBy(response.getDayAvg(), response.getDays()));

        return response;
    }

    public GamaMoney getAverageDaySalary(CompanySettings companySettings, long employeeId, LocalDate date, int months, boolean include) {
        final LocalDate specDate;
        final double specCeof;
        LocalDate specDateFrom = LocalDate.of(2019, 1, 1);
        if ("LT".equals(companySettings.getCountry()) && !date.isBefore(specDateFrom)) {
            specDate = specDateFrom;
            specCeof = 1.289;
        } else {
            specDate = null;
            specCeof = 0;
        }

        LocalDate date1 = include ? date.withDayOfMonth(1).plusMonths(1) : date.withDayOfMonth(1);
        LocalDate date0 = date1.minusMonths(months);

        // get salary employee charge for period >= date0 and < date1
        final List<EmployeeChargeDto> charges;
        charges = dbServiceSQL.executeAndReturnInTransaction(entityManager -> entityManager.createQuery(
                "SELECT a FROM " + EmployeeChargeSql.class.getName() + " a" +
                        " WHERE " + EmployeeChargeSql_.COMPANY_ID + " = :companyId" +
                        " AND " + EmployeeChargeSql_.EMPLOYEE + "." + EmployeeSql_.ID + " = :employeeId" +
                        " AND " + EmployeeChargeSql_.DATE + " >= :date0 AND " + EmployeeChargeSql_.DATE + " < :date1",
                        EmployeeChargeSql.class)
                .setParameter("companyId", auth.getCompanyId())
                .setParameter("employeeId", employeeId)
                .setParameter("date0", date0)
                .setParameter("date1", date1)
                .getResultStream()
                .map(employeeChargeSqlMapper::toDto)
                .collect(Collectors.toList()));

        int chargesCount = CollectionsHelper.hasValue(charges) ? charges.size() : 0;

        class Avg {
            int days;
            GamaMoney total;
            GamaMoney spec;
        }

        Avg avg = new Avg();

        if (chargesCount < months) {
            EmployeeCardDto employeeCard = employeeCardSqlMapper.toDto(dbServiceSQL.getById(EmployeeCardSql.class, employeeId));
            if (employeeCard != null && employeeCard.getSalaryHistory() != null) {
                int count = months - chargesCount;
                employeeCard.getSalaryHistory().stream()
                        .filter(x -> x.getMonth().isBefore(date1) && !x.getMonth().isBefore(date0))
                        .sorted(Comparator.comparing(SalaryPerMonth::getMonth).reversed())
                        .limit(count)
                        .forEach(x -> {
                            if (specDate != null && x.getMonth().isBefore(specDate)) {
                                avg.spec = GamaMoneyUtils.add(avg.spec, x.getSalary());
                            } else {
                                avg.total = GamaMoneyUtils.add(avg.total, x.getSalary());
                            }
                            avg.days += x.getWorkDays();
                        });
            }
        }

        if (chargesCount > 0) {
            for (EmployeeChargeDto charge : charges) {
                if (charge.getWorkData() != null) avg.days += IntegerUtils.value(charge.getWorkData().getDays());
                if (charge.getCharges() != null) {
                    for (DocChargeAmount chargeAmount : charge.getCharges()) {
                        if (chargeAmount.getCharge() == null || chargeAmount.getCharge().getAvgSalary() == null ||
                                chargeAmount.getCharge().getAvgSalary() == AvgSalaryType.NONE) continue;

                        if (chargeAmount.getCharge().getAvgSalary() == AvgSalaryType.ALL) {
                            if (specDate != null && charge.getDate().isBefore(specDate)) {
                                avg.spec = GamaMoneyUtils.add(avg.spec, chargeAmount.getAmount());
                            } else {
                                avg.total = GamaMoneyUtils.add(avg.total, chargeAmount.getAmount());
                            }
                        } else if (chargeAmount.getCharge().getAvgSalary() == AvgSalaryType.PROPORTIONAL) {
                            // calculation period Y.01 - Y.03
                            // 1) proportional amount X with period 3 on Y.01 - monthsBetween = 1, amount = X / 3
                            // 2) proportional amount X with period 3 on Y.02 - monthsBetween = 2, amount = X / 3 * 2
                            // 3) proportional amount X with period 3 on Y.03 - monthsBetween = 3, amount = X
                            long monthsBetween = ChronoUnit.MONTHS.between(date0, charge.getDate()) + 1;
                            GamaMoney amount = monthsBetween >= chargeAmount.getCharge().getPeriod() ?
                                    chargeAmount.getAmount() :
                                    chargeAmount.getAmount().dividedBy(chargeAmount.getCharge().getPeriod()).multipliedBy(monthsBetween);

                            if (specDate != null && charge.getDate().isBefore(specDate)) {
                                avg.spec = GamaMoneyUtils.add(avg.spec, amount);
                            } else {
                                avg.total = GamaMoneyUtils.add(avg.total, amount);
                            }
                        }
                    }
                }
            }
        }

        return avg.days > 0 ?
                GamaMoneyUtils.dividedBy(
                        GamaMoneyUtils.add(avg.total,
                                GamaMoneyUtils.isNonZero(avg.spec) ?
                                        GamaMoneyUtils.multipliedBy(avg.spec, specCeof) : null),
                        avg.days) : null;
    }

    public int getWorkDaysForPeriod(long employeeId, LocalDate dateFrom, LocalDate dateTo) {
        LocalDate date0 = dateFrom.withDayOfMonth(1);
        LocalDate date1 = dateTo.withDayOfMonth(1).plusMonths(1);
        return entityManager.createQuery(
                "SELECT SUM(" + EmployeeChargeSql_.WORK_DATA + "." + WorkData_.DAYS + ")" +
                        " FROM " + EmployeeChargeSql.class.getName() + " a " +
                        " WHERE " + EmployeeChargeSql_.COMPANY_ID + " = :companyId" +
                        " AND " + EmployeeChargeSql_.EMPLOYEE + "." + EmployeeSql_.ID + " = :employeeId" +
                        " AND " + EmployeeChargeSql_.DATE + " >= :dateFrom AND " + EmployeeChargeSql_.DATE + " < :dateTo",
                        Number.class)
                .setParameter("companyId", auth.getCompanyId())
                .setParameter("employeeId", employeeId)
                .setParameter("dateFrom", date0)
                .setParameter("dateTo", date1)
                .getSingleResult().intValue();
    }
}

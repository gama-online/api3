package lt.gama.service;

import lt.gama.auth.i.IAuth;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.BigDecimalUtils;
import lt.gama.helpers.CollectionsHelper;
import lt.gama.helpers.IntegerUtils;
import lt.gama.helpers.Validators;
import lt.gama.model.i.IEmployeeCard;
import lt.gama.model.type.auth.CompanySalarySettings;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.model.type.auth.CompanyTaxSettings;
import lt.gama.model.type.doc.DocPosition;
import lt.gama.model.type.enums.WorkScheduleType;
import lt.gama.model.type.salary.EmployeeTaxSettings;
import lt.gama.model.type.salary.WorkScheduleDay;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class SalarySettingsService {

    private final Auth auth;

    public SalarySettingsService(Auth auth) {
        this.auth = auth;
    }

    public CompanyTaxSettings getCompanyTaxSettings(LocalDate date) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        if (companySettings == null || date == null || companySettings.getTaxes() == null) return null;
        CompanyTaxSettings result = null;
        for (CompanyTaxSettings settings : companySettings.getTaxes()) {
            if (settings.getDate().isAfter(date)) break;
            result = settings;
        }
        return result;
    }

    public CompanySalarySettings getCompanySalarySettings(int year, int month) {
        return getCompanySalarySettings(LocalDate.of(year, month, 1));
    }

    public CompanySalarySettings getCompanySalarySettings(LocalDate date) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        if (companySettings == null || date == null || companySettings.getSalary() == null) return null;
        CompanySalarySettings result = null;
        for (CompanySalarySettings settings : companySettings.getSalary()) {
            if (settings.getDate() != null && settings.getDate().isAfter(date)) break;
            result = settings;
        }
        return result;
    }

    public CompanyTaxSettings getCompanyTaxSettings(int year, int month) {
        return getCompanyTaxSettings(LocalDate.of(year, month, 1));
    }

    public EmployeeTaxSettings generateEmployeeTaxSettings(CompanySettings companySettings, CompanyTaxSettings companyTaxSettings,
                                                           CompanySalarySettings companySalarySettings,
                                                           IEmployeeCard employeeCard, LocalDate date) {
        if (companyTaxSettings == null || employeeCard == null || date == null || CollectionsHelper.isEmpty(employeeCard.getTaxes()))
            return null;
        EmployeeTaxSettings employeeTaxSettings = null;
        for (EmployeeTaxSettings taxSettings : employeeCard.getTaxes()) {
            if (taxSettings.getDate() != null && taxSettings.getDate().isAfter(date)) break;
            employeeTaxSettings = taxSettings;
        }
        if (employeeTaxSettings != null) {
            Integer addTaxIndex = employeeTaxSettings.getEmployeeSSAddTaxRateIndex();
            BigDecimal tax = BigDecimalUtils.isNonZero(employeeTaxSettings.getEmployeeSSTaxRate()) ? employeeTaxSettings.getEmployeeSSTaxRate() :
                    companyTaxSettings.getEmployeeSSTaxRate();
            employeeTaxSettings.setEmployeeSSTaxRateTotal(BigDecimalUtils.add(tax, companyTaxSettings.getEmployeeSSAddTaxRates() != null &&
                    addTaxIndex != null && addTaxIndex >= 0 && addTaxIndex < companyTaxSettings.getEmployeeSSAddTaxRates().size() ?
                    companyTaxSettings.getEmployeeSSAddTaxRates().get(addTaxIndex) : BigDecimal.ZERO));

            employeeTaxSettings.setVacationLength(getVacationLength(companySalarySettings, employeeTaxSettings,
                    getMainPosition(employeeCard.getPositions())));
        }
        return employeeTaxSettings;
    }

    public int getVacationLength(CompanySalarySettings companySalarySettings, EmployeeTaxSettings employeeTaxSettings, DocPosition position) {
        if (employeeTaxSettings != null && IntegerUtils.isPositive(employeeTaxSettings.getVacationLength())) {
            return employeeTaxSettings.getVacationLength();
        }

        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        int vacationLength = 0;

        // for LT - Lithuania
        if (companySettings != null && "LT".equals(companySettings.getCountry())) {
            vacationLength = 20;

            if (position != null && position.getWorkSchedule() != null) {
                if (WorkScheduleType.WEEKLY.equals(position.getWorkSchedule().getType()) || position.getWorkSchedule().getPeriod() == 7) {
                    if (position.getWorkSchedule().getSchedule() != null) {
                        int wd = 0;
                        for (WorkScheduleDay day : position.getWorkSchedule().getSchedule()) {
                            if (BigDecimalUtils.isPositive(day.getHours())) wd++;
                        }
                        if (wd > 0) vacationLength = wd * 4;
                    }
                }
            }
        }

        if (companySalarySettings != null && companySalarySettings.getAddVacationLength() != null) {
            vacationLength += companySalarySettings.getAddVacationLength();
        }
        return vacationLength;
    }

    public DocPosition getMainPosition(List<DocPosition> positions) {
        if (positions == null) return null;

        for (DocPosition position : positions) {
            if (position.isMain()) return position;
        }
        return null;
    }
}

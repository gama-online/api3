package lt.gama.service;

import lt.gama.helpers.CollectionsHelper;
import lt.gama.helpers.StringHelper;
import lt.gama.model.type.l10n.LangBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;
import java.lang.invoke.MethodHandles;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import static java.util.Map.entry;

/**
 * gama-online
 * Created by valdas on 2016-05-18.
 */
public final class TranslationService {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    public enum ASSET {
        NoFullYearPeriod,
        NoPeriodEndDateInHistory,
        NoPeriodStartDateInHistory,
        WrongPeriodInHistory
    }

    public enum MAIL {
        ResetPassword,
        ResetPasswordSubject,
        CreatedCompany,
        CreatedCompanySubject,
        AddedCompany,
        AddedCompanySubject,
        EmptyMessage
    }

    public enum DB {
        InsufficientPrivileges,
        NoCompany,
        NoCompanySettings,
        NoEntityToDelete,
        NoEntityToUpdate,
        NoParentId,
        RecordModifiedAlready,
        Unauthorized,
        WrongId,
        WrongLogin,
        WrongToken,
    }

    public enum INVENTORY {
        DiscountRecordExistsAlready,
        NoPartId,
        NoProductionParts,
        NotEnoughQuantity,
        NotEnoughCost,
        NotEnoughQuantityInvoiceRecall,
        NoOriginInvoice,
        NoCostPercent100,
        NoPartsInOriginInvoice,
        NoForwardSellQuantity,
        NoName,
        NoNumber,
        NoPart,
        NoPartIdIn,
        NoTaxFreeDeclaration,
        NoType,
        ReservedNoFinish,
        SameDocumentNumber,
        SameDocumentNumberVendor,
        InvoiceEmailTemplate,
        TaxFreeDeclarationAlreadyCompleted,
        TaxFreeDeclarationWrongState,
        NoUuid,
        NoProductsToSync,
        ProductsSynced,
        ProductWithSkuAlreadyExists,
        NoOrdersToSync,
        OrdersSynced,
        NoSyncOrderStatus,
        NoSyncTransportation,
        NoSyncWarehouse,
        BillingAddress,
    }

    public enum VALIDATORS {
        DateBeforeStartAccounting,
        DateFromAfterDateEnd,
        DateFromBeforeStartAccounting,
        DateNotFromAccountingPeriod,
        DocumentFinishedAlready,
        Id0,
        InvalidPageSize,
        NoAccountingPeriodEndDate,
        NoAccountingPeriodStartDate,
        NoAccountingStartDate,
        NoBankAccount,
        NoCash,
        NoCounterparty,
        NoCounterpartyCode,
        NoCounterpartyId,
        NoCounterpartyWithId,
        NoCustomer,
        NoDateFrom,
        NoDateTo,
        NoDebtType,
        NoDocument,
        NoDocumentChangeStatus,
        NoDocumentDate,
        NoDocumentId,
        NoDocumentNumber,
        NoDocumentToDelete,
        NoDocumentToFinish,
        NoDocumentToRecall,
        NoDocumentWithId,
        NoEmployee,
        NoEmployeeId,
        NoFilter,
        NoLabelName,
        NoName,
        NoPartSku,
        NoSomeEntityWithId,
        NoVendor,
        NoWarehouse,
        NoWarehouseReserved,
        NoWarehouseTag,
        NoWarehouseWithId,
        NotFoundIdIn,
        NotFoundIn,
        WholeAmountNotCovered,
        WrongDocumentType,
        WrongOpeningBalanceDate,
        WrongPeriod,
        YourTrialPeriodIsEnded,
    }

    public enum SALARY {
        EmployeeAbsenceExists,
        EmployeeExists,
        EmployeeFiredBefore,
        EmployeeHiredAfter,
        NoChargeAdvanceSettings,
        NoChargeChildDaysSettings,
        NoChargeIllnessSettings,
        NoChargeVacationSettings,
        NoChargeWorkSettings,
        NoEmployee,
        NoEmployeeCard,
        NoEmployeeHiringDate,
        NoEmployeeId,
        NoEmployeePosition,
        NoEmployeePositionWage,
        NoEmployeePositionWageType,
        NoEmployeeSalaryRecord,
        NoEmployeeTax,
        NoEmployeeVacationLength,
        NoEmployeeWorkHours,
        EmployeeWorkHoursDuplicates,
        NoEmployeeWorkHoursByPosition,
        NoSalarySettings,
        NoTaxSettings,
        OnlyOneMainPosition,
        WorkHoursRecordExists,
        WrongDateForTaxExempt
    }

    public enum GL {
        NoBankOtherAcc,
        NoCurrRateNegAcc,
        NoCurrRatePosAcc,
        NoCustomerAcc,
        NoCustomerNoVendorAcc,
        NoDoubleEntryDocumentWithId,
        NoDoubleEntryDocumentWithParentId,
        NoEmployeeAdvAcc,
        NoGLAccountFor,
        NoGLAccountForFor,
        NoGLAccountNumber,
        NoGLAccountWithNumber,
        NoGLSettings,
        NoInterimAcc,
        NoPartAssetAcc,
        NoPartExpenseAcc,
        NoPartIncomeAcc,
        NoPartType,
        NoProfitLossAcc,
        NoVATPayAcc,
        NoVATRecAcc,
        NoVendorAcc,
        SameGLAccountNumberFound
    }

    public final Map<Class<?>, String> BundleName = Map.ofEntries(
            entry(ASSET.class, "asset"),
            entry(DB.class, "db"),
            entry(GL.class, "gl"),
            entry(INVENTORY.class, "inventory"),
            entry(MAIL.class, "mail"),
            entry(SALARY.class, "salary"),
            entry(VALIDATORS.class, "validators"));


    private TranslationService() {
    }

    private static class LazyHolder {
        static final TranslationService INSTANCE = new TranslationService();
    }

    public static TranslationService getInstance() {
        return LazyHolder.INSTANCE;
    }

    public String translate(Enum<?> key, String language) {
        String name = BundleName.get(key.getClass());
        try {
            Locale locale = Locale.forLanguageTag(language == null ? "en" : language);
            ResourceBundle resourceBundle = locale == null ? ResourceBundle.getBundle("i18n." + name) : ResourceBundle.getBundle("i18n." + name, locale);
            return resourceBundle.getString(key.name());
        } catch (Exception e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
            return "Error: " + key + " in " + name + ", language: " + language;
        }
    }

    public String translate(Enum<?> key) {
        return translate(key, null);
    }

    public String translate(String original, Map<String, ? extends LangBase> translation, String language, String fieldName) {
        if (CollectionsHelper.isEmpty(translation) || StringHelper.isEmpty(language) || StringHelper.isEmpty(fieldName)) return original;
        LangBase langBase = translation.get(language);
        if (langBase == null) return original;

        Object value;

        try {
            //value = PropertyUtils.getProperty(langBase, fieldName);
            value = new PropertyDescriptor(fieldName, langBase.getClass()).getReadMethod().invoke(langBase);

        } catch (Exception e) {
            return original;
        }

        if (value instanceof String) {
            return (String) value;
        }
        return original;
    }
}

package lt.gama.helpers;

import lt.gama.model.i.IDate;
import lt.gama.model.i.IId;
import lt.gama.model.i.IVersion;
import lt.gama.model.sql.entities.DebtCoverageSql;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.model.type.doc.DocDebt;
import lt.gama.model.type.gl.GLDC;
import lt.gama.model.type.gl.GLOperationAccount;
import lt.gama.service.ex.rt.GamaForbiddenException;
import lt.gama.service.TranslationService;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.text.MessageFormat;
import java.time.LocalDate;

/**
 * Gama
 * Created by valdas on 15-05-07.
 */
public final class Validators {

    private Validators() {}

    public static void checkArgument(boolean expression) {
        if (!expression) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the calling method.
     *
     * @param expression a boolean expression
     * @param errorMessage the exception message to use if the check fails; will be converted to a
     *     string using {@link String#valueOf(Object)}
     * @throws IllegalArgumentException if {@code expression} is false
     */
    public static void checkArgument(boolean expression, @CheckForNull Object errorMessage) {
        if (!expression) {
            throw new IllegalArgumentException(String.valueOf(errorMessage));
        }
    }

    /**
     * Ensures the truth of an expression involving one or more parameters to the calling method.
     *
     * @param expression a boolean expression
     * @param errorMessageTemplate a template for the exception message should the check fail. The
     *     message is formed by replacing each {@code %s} placeholder in the template with an
     *     argument. These are matched by position - the first {@code %s} gets {@code
     *     errorMessageArgs[0]}, etc. Unmatched arguments will be appended to the formatted message in
     *     square braces. Unmatched placeholders will be left as-is.
     * @param errorMessageArgs the arguments to be substituted into the message template. Arguments
     *     are converted to strings using {@link String#valueOf(Object)}.
     * @throws IllegalArgumentException if {@code expression} is false
     */
    public static void checkArgument(
            boolean expression,
            String errorMessageTemplate,
            @CheckForNull @Nullable Object... errorMessageArgs) {
        if (!expression) {
            throw new IllegalArgumentException(MessageFormat.format(errorMessageTemplate, errorMessageArgs));
        }
    }

    public static <T> T checkNotNull(@CheckForNull T reference) {
        if (reference == null) {
            throw new NullPointerException();
        }
        return reference;
    }

    /**
     * Ensures that an object reference passed as a parameter to the calling method is not null.
     *
     * @param reference an object reference
     * @param errorMessage the exception message to use if the check fails; will be converted to a
     *     string using {@link String#valueOf(Object)}
     * @return the non-null reference that was validated
     * @throws NullPointerException if {@code reference} is null
     */
    public static <T> T checkNotNull(@CheckForNull T reference, @CheckForNull String errorMessage) {
        if (reference == null) {
            throw new NullPointerException(errorMessage);
        }
        return reference;
    }

    /**
     * Ensures that an object reference passed as a parameter to the calling method is not null.
     *
     * @param reference an object reference
     * @param errorMessageTemplate a template for the exception message should the check fail. The
     *     message is formed by replacing each {@code %s} placeholder in the template with an
     *     argument. These are matched by position - the first {@code %s} gets {@code
     *     errorMessageArgs[0]}, etc. Unmatched arguments will be appended to the formatted message in
     *     square braces. Unmatched placeholders will be left as-is.
     * @param errorMessageArgs the arguments to be substituted into the message template. Arguments
     *     are converted to strings using {@link String#valueOf(Object)}.
     * @return the non-null reference that was validated
     * @throws NullPointerException if {@code reference} is null
     */
    public static <T> T checkNotNull(
            @CheckForNull T reference,
            String errorMessageTemplate,
            @CheckForNull @Nullable Object... errorMessageArgs) {
        if (reference == null) {
            throw new NullPointerException(MessageFormat.format(errorMessageTemplate, errorMessageArgs));
        }
        return reference;
    }


    public static <E extends IId<?>> E checkValid(E obj, String message) {
        Validators.checkArgument(obj != null && obj.getId() != null, message);
        return obj;
    }

    public static void checkValid(GLOperationAccount account, String message) {
        Validators.checkArgument(isValid(account), message);
    }

    public static boolean isValid(GLOperationAccount account) {
        return account != null && StringHelper.hasValue(account.getNumber());
    }

    public static void checkValid(IId<Long> obj, String name, String src, String language) {
        Validators.checkNotNull(obj, MessageFormat.format(TranslationService.getInstance().translate(TranslationService.VALIDATORS.NotFoundIn, language), name, src));
        Validators.checkNotNull(obj.getId(), MessageFormat.format(TranslationService.getInstance().translate(TranslationService.VALIDATORS.NotFoundIdIn, language), name, src));
        Validators.checkArgument(obj.getId() != 0, MessageFormat.format(TranslationService.getInstance().translate(TranslationService.VALIDATORS.Id0, language), name, src));
    }

    public static void notEmpty(String value, String message) {
        Validators.checkArgument(StringHelper.hasValue(value), message);
    }

    public static boolean isValid(IId<Long> obj) {
        return obj != null && obj.getId() != null && obj.getId() != 0;
    }


    /**
     * Check if GLDC has valid debit or credit accounts or both
     * @param account Debit and credit accounts
     * @return true or false
     */
    public static boolean isPartialValid(GLDC account) {
        return account != null && (isValid(account.getDebit()) || isValid(account.getCredit()));
    }

    public static boolean isValidDebit(GLDC account) {
        return account != null && Validators.isValid(account.getDebit());
    }

    public static boolean isValidCredit(GLDC account) {
        return account != null && Validators.isValid(account.getCredit());
    }

    public static boolean isValidTrialPeriod(LocalDate date) {
        return date == null || !date.isBefore(DateUtils.date());
    }

    public static void checkTrialPeriod(LocalDate date, String language) {
        if (!isValidTrialPeriod(date))
            throw new GamaForbiddenException(TranslationService.getInstance().translate(TranslationService.VALIDATORS.YourTrialPeriodIsEnded, language));
    }

    public static void checkDocumentDate(CompanySettings settings, IDate document, String language) {
        checkDocumentDate(settings, document.getDate(), language);
    }

    public static void checkReportDate(LocalDate startAccounting, LocalDate date) {
        checkReportDate(startAccounting, date, null);
    }

    public static void checkReportDate(LocalDate startAccounting, LocalDate date, String language) {
        Validators.checkNotNull(startAccounting, TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoAccountingStartDate, language));
        Validators.checkNotNull(date, TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDateFrom, language));
        Validators.checkArgument(!startAccounting.isAfter(date), MessageFormat.format(
                TranslationService.getInstance().translate(TranslationService.VALIDATORS.DateFromBeforeStartAccounting, language),
                date, startAccounting));
    }

    public static void checkPeriod(LocalDate dateFrom, LocalDate dateTo, String language) {
        Validators.checkNotNull(dateFrom, TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDateFrom, language));
        Validators.checkNotNull(dateTo, TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDateTo, language));
        Validators.checkArgument(!dateFrom.isAfter(dateTo), MessageFormat.format(
                TranslationService.getInstance().translate(TranslationService.VALIDATORS.DateFromAfterDateEnd, language),
                dateFrom, dateTo));
    }
    public static void checkDocumentDate(CompanySettings settings, LocalDate date) {
        checkDocumentDate(settings, date, null);
    }

    public static void checkDocumentDate(CompanySettings settings, LocalDate date, String language) {
        Validators.checkNotNull(settings.getStartAccountingPeriod(), TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoAccountingPeriodStartDate, language));
        Validators.checkNotNull(date, TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDocumentDate, language));

        LocalDate endAccountingPeriod = DateUtils.max(settings.getStartAccountingPeriod().withYear(DateUtils.date().getYear()), settings.getStartAccountingPeriod()).plusYears(5);

        Validators.checkArgument(!date.isBefore(settings.getStartAccountingPeriod()) && date.isBefore(endAccountingPeriod),
                MessageFormat.format(
                        TranslationService.getInstance().translate(TranslationService.VALIDATORS.DateNotFromAccountingPeriod, language),
                        date, settings.getStartAccountingPeriod(), endAccountingPeriod));
    }

    public static void checkOpeningBalanceDate(CompanySettings settings, IDate document, String language) {
        Validators.checkNotNull(document.getDate(), TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDocumentDate, language));
        Validators.checkArgument(document.getDate().isEqual(settings.getStartAccounting().minusDays(1)),
                MessageFormat.format(
                        TranslationService.getInstance().translate(TranslationService.VALIDATORS.WrongOpeningBalanceDate, language),
                        settings.getStartAccounting().minusDays(1)));
    }


    public static void checkDocumentVersion(IVersion org, IVersion updated, String language) {
        Validators.checkArgument(org.getVersion() == updated.getVersion(),
                MessageFormat.format(
                        TranslationService.getInstance().translate(TranslationService.DB.RecordModifiedAlready, language),
                        org.getVersion(), updated.getVersion(), org.getClass().getSimpleName()));
    }

    public static void checkDebtCoverage(DebtCoverageSql debtCoverage) {
        Validators.checkArgument(GamaMoneyUtils.isZero(debtCoverage.getAmount()) && GamaMoneyUtils.isZero(debtCoverage.getCovered()) ||
                        GamaMoneyUtils.isPositive(debtCoverage.getAmount()) &&
                                GamaMoneyUtils.isPositiveOrZero(debtCoverage.getCovered()) &&
                                GamaMoneyUtils.isGreaterThanOrEqual(debtCoverage.getAmount(), debtCoverage.getCovered()) ||
                        GamaMoneyUtils.isNegative(debtCoverage.getAmount()) &&
                                GamaMoneyUtils.isNegativeOrZero(debtCoverage.getCovered()) &&
                                GamaMoneyUtils.isLessThanOrEqual(debtCoverage.getAmount(), debtCoverage.getCovered()),
                "Wrong DebtCoverage: " + debtCoverage);
    }

    /**
     * Check debt document.
     * Throw IllegalArgumentException (runtime exception) on error.
     * @param debtCoverage DebtCoverage record - used for printing error only.
     * @param docDebt debt document to check
     */
    public static void checkDebtCoverageDocDebt(DebtCoverageSql debtCoverage, DocDebt docDebt) {
        Validators.checkArgument(GamaMoneyUtils.isZero(docDebt.getAmount()) && GamaMoneyUtils.isZero(docDebt.getCovered()) ||
                        GamaMoneyUtils.isPositive(docDebt.getAmount()) &&
                                GamaMoneyUtils.isPositiveOrZero(docDebt.getCovered()) &&
                                GamaMoneyUtils.isGreaterThanOrEqual(docDebt.getAmount(), docDebt.getCovered()) ||
                        GamaMoneyUtils.isNegative(docDebt.getAmount()) &&
                                GamaMoneyUtils.isNegativeOrZero(docDebt.getCovered()) &&
                                GamaMoneyUtils.isLessThanOrEqual(docDebt.getAmount(), docDebt.getCovered()),
                "Wrong DocDebt: " + docDebt + ", DebtCoverage=" + debtCoverage);
    }
}

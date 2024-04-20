package lt.gama.service;

import com.google.common.collect.Lists;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.api.response.BatchFixResponse;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.CollectionsHelper;
import lt.gama.helpers.GamaMoneyUtils;
import lt.gama.helpers.StringHelper;
import lt.gama.helpers.Validators;
import lt.gama.model.dto.documents.DoubleEntryDto;
import lt.gama.model.dto.entities.AssetDto;
import lt.gama.model.dto.type.AssetTotal;
import lt.gama.model.mappers.AssetSqlMapper;
import lt.gama.model.mappers.DoubleEntrySqlMapper;
import lt.gama.model.sql.documents.DoubleEntrySql;
import lt.gama.model.sql.documents.items.GLOperationSql;
import lt.gama.model.sql.entities.AssetSql;
import lt.gama.model.sql.entities.EmployeeSql;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.asset.AssetHistory;
import lt.gama.model.type.asset.Depreciation;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.model.type.auth.CounterDesc;
import lt.gama.model.type.doc.DocRC;
import lt.gama.model.type.enums.AssetStatusType;
import lt.gama.model.type.enums.DepreciationType;
import lt.gama.model.type.gl.GLOperationAccount;
import lt.gama.service.ex.rt.GamaException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.time.temporal.TemporalAdjusters.firstDayOfMonth;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

/**
 * gama-online
 * Created by valdas on 2015-10-28.
 */
@Service
public class DepreciationService {

    @PersistenceContext
    private EntityManager entityManager;
    
    private final DBServiceSQL dbServiceSQL;
    private final DoubleEntrySqlMapper doubleEntrySqlMapper;
    private final AssetSqlMapper assetSqlMapper;
    private final CounterService counterService;
    private final Auth auth;

    public DepreciationService(DBServiceSQL dbServiceSQL, DoubleEntrySqlMapper doubleEntrySqlMapper, AssetSqlMapper assetSqlMapper, CounterService counterService, Auth auth) {
        this.dbServiceSQL = dbServiceSQL;
        this.doubleEntrySqlMapper = doubleEntrySqlMapper;
        this.assetSqlMapper = assetSqlMapper;
        this.counterService = counterService;
        this.auth = auth;
    }
    
    public AssetDto save(AssetDto request) {
        return assetSqlMapper.toDto(dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            AssetSql entity = assetSqlMapper.toEntity(request);
            reset(entity);

            if (request.isAutoCode() && StringHelper.isEmpty(request.getCode())) {
                final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
                CounterDesc desc = companySettings.getCounterByClass(entity.getClass());
                if (desc == null) desc = new CounterDesc(entity.getClass());
                entity.setCode(counterService.next(desc).getNumber());
            }

            return dbServiceSQL.saveEntityInCompany(entity);
        }));
    }

    public void reset(AssetSql asset) {
        if (asset == null) return;

        if (asset.getHistory() == null || asset.getHistory().size() == 0 || asset.getDate() == null) {
            asset.setDepreciation(null);
            asset.setStatus(null);
            return;
        }

        if (asset.getDate().getDayOfMonth() != 1) {
            asset.setDate(asset.getDate().withDayOfMonth(1).plusMonths(1));
        }

        asset.getHistory().sort(Comparator.comparing(AssetHistory::getDate));

        LocalDate endDate = null;
        for (AssetHistory history : Lists.reverse(asset.getHistory())) {
            if (history.getDate().getDayOfMonth() != 1) {
                history.setDate(history.getDate().withDayOfMonth(1));
            }
            if (history.getDate().isBefore(asset.getDate())) {
                history.setDate(asset.getDate());
            }
            if (history.getFinalDate() != null && history.getFinalDate().getDayOfMonth() != 1) {
                history.setFinalDate(history.getFinalDate().withDayOfMonth(1).plusMonths(1));
            }
            if (history.getStatus() == AssetStatusType.WRITTEN_OFF || history.getStatus() == AssetStatusType.CONSERVED) {
                history.setType(null);
                history.setFinalDate(null);
                history.setRate(null);
                history.setAmount(null);
            }
            // set period ending date
            history.setEndDate(endDate != null ? endDate : history.getFinalDate());
            endDate = history.getDate();
        }

        asset.setDepreciation(new ArrayList<>());
        asset.getHistory().forEach(history -> makeRecords(asset, history));

        // last history record
        AssetHistory lastHistory = asset.getHistory().get(asset.getHistory().size() - 1);
        asset.setResponsible(Validators.isValid(lastHistory.getResponsible())
                ? entityManager.getReference(EmployeeSql.class, lastHistory.getResponsible().getId())
                : null);
        asset.setLocation(lastHistory.getLocation());
        asset.setStatus(lastHistory.getStatus());
        asset.setLastDate(lastHistory.getFinalDate() != null ? lastHistory.getFinalDate() : lastHistory.getDate());
    }

    @Transactional
    public DoubleEntryDto generateDE(LocalDate month) {
        final LocalDate date = month.withDayOfMonth(1);

        /*
            Debit                        | Credit                      | amount
            -----------------------------|-----------------------------|----------------------------
            expense (rc)                 | depreciation                | amount
         */
        Map<DebitCreditKey, GamaMoney> depreciationVsExpense = new HashMap<>();
        entityManager.createQuery(
                "SELECT a FROM " + AssetSql.class.getName() + " a" +
                        " WHERE a.companyId = :companyId" +
                        " AND (a.archive IS null OR a.archive = false)" +
                        " AND a.date <= :date " +
                        " AND (a.lastDate IS NULL OR a.lastDate > :date)" +
                        " ORDER BY a.date, a.id",
                        AssetSql.class)
                .setParameter("companyId", auth.getCompanyId())
                .setParameter("date", date)
                .getResultStream()
                .filter(asset -> asset.getDepreciation() != null && asset.getDepreciation().size() > 0)
                .forEach(asset -> {
                    Depreciation depreciation = asset.getDepreciation().stream()
                            .filter(e -> e.getDate().equals(date))
                            .findAny().orElse(null);
                    if (depreciation != null) {
                        DebitCreditKey key = new DebitCreditKey(asset.getAccountExpense(),
                                asset.getAccountDepreciation(), asset.getRcExpense());
                        GamaMoney amount = depreciationVsExpense.remove(key);
                        amount = GamaMoneyUtils.total(amount, depreciation.getExpense(), depreciation.getDtExpense());
                        if (GamaMoneyUtils.isNonZero(amount)) depreciationVsExpense.put(key, amount);
                    }
                });

        if (depreciationVsExpense.size() == 0) {
            throw new GamaException("No data for double-entry of " + date);
        }

        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");

        LocalDate endOfMonth = date.with(lastDayOfMonth());
        DoubleEntrySql doubleEntry = new DoubleEntrySql();
        doubleEntry.setCompanyId(auth.getCompanyId());
        doubleEntry.setDate(endOfMonth);
        doubleEntry.setAutoNumber(true);
        doubleEntry.setContent(MessageFormat.format("{0} {1}",
                companySettings.getDocName(AssetSql.class.getSimpleName()), endOfMonth.toString()));

        depreciationVsExpense.keySet().stream()
                .sorted(Comparator.comparing(DebitCreditKey::getDebit).thenComparing(DebitCreditKey::getCredit))
                .forEach(key -> doubleEntry.addOperation(
                        new GLOperationSql(key.debit, key.rcs, key.credit, null, depreciationVsExpense.get(key))));

        return doubleEntrySqlMapper.toDto(dbServiceSQL.saveWithCounter(doubleEntry));
    }

    private void makeRecords(AssetSql asset, AssetHistory history) {
        Validators.checkNotNull(history.getStatus(), MessageFormat.format("No Status in asset {0} history record {1}", asset, history));

        final LocalDate date = history.getDate();

        // clear all in the future
        asset.getDepreciation().removeIf(d -> !d.getDate().isBefore(date));

        Depreciation prevDep = null;
        if (asset.getDepreciation().size() > 0) {
            asset.getDepreciation().sort(Comparator.comparing(Depreciation::getDate));
            int lastIndex = asset.getDepreciation().size() - 1;
            prevDep = asset.getDepreciation().get(lastIndex);
            if (date.isAfter(asset.getDepreciation().get(lastIndex).getDate())) {
                prevDep = asset.getDepreciation().get(lastIndex);
            }
        }

        GamaMoney beginning = prevDep != null ? prevDep.getEnding() : asset.getValue();
        GamaMoney totalDep = prevDep != null ? prevDep.getDepreciation() : asset.getExpenses();

        history.setBeginning(beginning);

        if (history.getStatus().equals(AssetStatusType.CONSERVED)) {
            conserved(asset, history, beginning, totalDep);

        } else if (history.getStatus().equals(AssetStatusType.WRITTEN_OFF)) {
            writtenOff(asset, history, beginning);

        } else {
            Validators.checkNotNull(history.getType(), MessageFormat.format("No Depreciation type in asset {0} history record {1}", asset, history));

            if (DepreciationType.LINE.equals(history.getType())) {
                straightLineMethod(asset, history, beginning, totalDep);

            } else if (DepreciationType.DOUBLE.equals(history.getType())) {
                doubleDecliningMethod(asset, history, beginning, totalDep);

            } else if (DepreciationType.OTHER.equals(history.getType())) {
                otherMethod(asset, history, beginning, totalDep);

            }
        }
    }

    private void writtenOff(AssetSql asset, AssetHistory history, GamaMoney beginning) {
        asset.setWrittenOff(beginning);
        history.setBeginning(beginning);
        history.setEnding(beginning);
    }

    private void conserved(AssetSql asset, AssetHistory history, GamaMoney beginning, GamaMoney totalDep) {
        history.setBeginning(beginning);
        history.setEnding(beginning);

        if (history.getEndDate() == null) {
            Depreciation depreciation = new Depreciation(history.getDate());
            asset.getDepreciation().add(depreciation);

            depreciation.setBeginning(beginning);
            depreciation.setEnding(beginning);
            depreciation.setDepreciation(totalDep);

        } else {
            for (LocalDate date = history.getDate(); date.isBefore(history.getEndDate()); date = date.plusMonths(1)) {
                Depreciation depreciation = new Depreciation(date);
                asset.getDepreciation().add(depreciation);

                depreciation.setBeginning(beginning);
                depreciation.setEnding(beginning);
                depreciation.setDepreciation(totalDep);
            }
        }
    }

    private Depreciation straightLineFill(AssetSql asset, LocalDate startDate, LocalDate endDate, LocalDate untilDate,
                                          GamaMoney beginning, GamaMoney ending, GamaMoney totalDep, GamaMoney dtExpense, GamaMoney dtValue) {
        long months = ChronoUnit.MONTHS.between(startDate, endDate);
        if (months <= 0) return null;

        // amount = beginning + dtValue - dtExpense - ending
        GamaMoney amount = GamaMoneyUtils.total(beginning, dtValue,
                GamaMoneyUtils.negated(dtExpense), GamaMoneyUtils.negated(ending));
        Validators.checkNotNull(amount);
        GamaMoney perMonth = amount.dividedBy(months);

        // special first month
        GamaMoney firstMonth = GamaMoneyUtils.subtract(amount, perMonth.multipliedBy(months - 1));

        Depreciation depreciation = new Depreciation(startDate);
        asset.getDepreciation().add(depreciation);

        depreciation.setBeginning(beginning);
        depreciation.setExpense(firstMonth);
        depreciation.setDtExpense(dtExpense);
        depreciation.setDtValue(dtValue);
        // ending = beginning - expense - dtExpense + dtValue
        depreciation.setEnding(GamaMoneyUtils.add(
                GamaMoneyUtils.subtract(
                        GamaMoneyUtils.subtract(depreciation.getBeginning(), depreciation.getExpense()), depreciation.getDtExpense()),
                depreciation.getDtValue()));
        // totalDep = totalDep + expense + dtExpense
        totalDep = GamaMoneyUtils.add(GamaMoneyUtils.add(totalDep, depreciation.getExpense()), depreciation.getDtExpense());
        depreciation.setDepreciation(totalDep);

        // others months
        LocalDate dateTo = endDate.isBefore(untilDate) ? endDate : untilDate;
        for (LocalDate date = startDate.plusMonths(1); date.isBefore(dateTo); date = date.plusMonths(1)) {
            Depreciation depreciationNext = new Depreciation(date);
            asset.getDepreciation().add(depreciationNext);

            depreciationNext.setBeginning(depreciation.getEnding());
            depreciationNext.setExpense(perMonth);
            // ending = beginning - expense
            depreciationNext.setEnding(GamaMoneyUtils.subtract(depreciationNext.getBeginning(), depreciationNext.getExpense()));
            // totalDep = totalDep + expense
            totalDep = GamaMoneyUtils.add(totalDep, depreciationNext.getExpense());
            depreciationNext.setDepreciation(totalDep);

            depreciation = depreciationNext;
        }
        return depreciation;
    }

    // Straight-line: https://en.wikipedia.org/wiki/Depreciation#Straight-line_depreciation
    private void straightLineMethod(AssetSql asset, AssetHistory history, GamaMoney beginning, GamaMoney totalDep) {
        Validators.checkNotNull(history.getDate(),
                MessageFormat.format(
                        TranslationService.getInstance().translate(TranslationService.ASSET.NoPeriodStartDateInHistory, auth.getLanguage()),
                        asset.toMessage(), history.toMessage()));

        Validators.checkNotNull(history.getFinalDate(),
                MessageFormat.format(
                        TranslationService.getInstance().translate(TranslationService.ASSET.NoPeriodEndDateInHistory, auth.getLanguage()),
                        asset.toMessage(), history.toMessage()));

        Validators.checkArgument(!history.getDate().isAfter(history.getFinalDate()),
                MessageFormat.format(
                        TranslationService.getInstance().translate(TranslationService.ASSET.WrongPeriodInHistory, auth.getLanguage()),
                        asset.toMessage(), history.toMessage(), history.getDate(), history.getFinalDate()));

        straightLineFill(asset, history.getDate(), history.getFinalDate(), history.getEndDate(),
                beginning, history.getEnding(), totalDep, history.getDtExpense(), history.getDtValue());
    }

    // Double declining:
    private void doubleDecliningMethod(AssetSql asset, AssetHistory history, GamaMoney beginning, GamaMoney totalDep) {
        Validators.checkNotNull(history.getDate(),
                MessageFormat.format(
                        TranslationService.getInstance().translate(TranslationService.ASSET.NoPeriodStartDateInHistory, auth.getLanguage()),
                        asset.toMessage(), history.toMessage()));

        Validators.checkNotNull(history.getFinalDate(),
                MessageFormat.format(
                        TranslationService.getInstance().translate(TranslationService.ASSET.NoPeriodEndDateInHistory, auth.getLanguage()),
                        asset.toMessage(), history.toMessage()));

        Validators.checkArgument(!history.getDate().isAfter(history.getFinalDate()),
                MessageFormat.format(
                        TranslationService.getInstance().translate(TranslationService.ASSET.WrongPeriodInHistory, auth.getLanguage()),
                        asset.toMessage(), history.toMessage(), history.getDate(), history.getFinalDate()));

        Validators.checkArgument(history.getDate().getMonthValue() == history.getFinalDate().getMonthValue(),
                MessageFormat.format(
                        TranslationService.getInstance().translate(TranslationService.ASSET.NoFullYearPeriod, auth.getLanguage()),
                        asset.toMessage(), history.toMessage(), history.getDate(), history.getFinalDate()));

        long years = ChronoUnit.YEARS.between(history.getDate(), history.getFinalDate());
        GamaMoney expense;
        GamaMoney value = beginning;
        GamaMoney dtExpense = history.getDtExpense();
        GamaMoney dtValue = history.getDtValue();
        double rate = 1.0 / years * 2.0;
        LocalDate date = history.getDate();
        for (int year = 1; year <= years && date.isBefore(history.getEndDate()); ++year) {
            if (year == 2) {
                dtValue = null;
                dtExpense = null;
            }

            GamaMoney correctedValue = GamaMoneyUtils.total(value, dtValue, GamaMoneyUtils.negated(dtExpense));
            Validators.checkNotNull(correctedValue);

            if (year == years) {
                Validators.checkArgument(GamaMoneyUtils.isGreaterThan(value, history.getEnding()),
                        MessageFormat.format("The Ending value is not greater than calculated beginning in last year of depreciation in asset {0} history record {1}",
                                asset, history));

                expense = GamaMoneyUtils.subtract(correctedValue, history.getEnding());
            } else {
                expense = correctedValue.multipliedBy(rate);
            }

            Depreciation depreciation = straightLineFill(asset, date, date.plusYears(1), history.getEndDate(),
                    value, GamaMoneyUtils.subtract(correctedValue, expense),
                    totalDep, dtExpense, dtValue);

            Validators.checkNotNull(depreciation, "Depreciation is null");
            Validators.checkArgument(GamaMoneyUtils.isEqual(depreciation.getEnding(), GamaMoneyUtils.subtract(correctedValue, expense)),
                    MessageFormat.format("Wrong calculations in asset {0} history record {1}", asset, history));

            value = depreciation.getEnding();
            date = date.plusYears(1);
            totalDep = depreciation.getDepreciation();
        }
    }

    private void otherMethod(AssetSql asset, AssetHistory history, GamaMoney beginning, GamaMoney totalDep) {
        Validators.checkArgument(!history.getDate().isAfter(history.getFinalDate()),
                MessageFormat.format(
                        TranslationService.getInstance().translate(TranslationService.ASSET.WrongPeriodInHistory, auth.getLanguage()),
                        asset.toMessage(), history.toMessage(), history.getDate(), history.getFinalDate()));

        Validators.checkArgument(history.getRate() != null || history.getAmount() != null,
                MessageFormat.format("No rate or no amount in asset {0} history record {1}", asset, history));

        GamaMoney correctedValue = GamaMoneyUtils.total(beginning, history.getDtValue(), GamaMoneyUtils.negated(history.getDtExpense()));
        Validators.checkNotNull(correctedValue);

        GamaMoney expense = history.getAmount() != null ? history.getAmount() :
                correctedValue.multipliedBy(history.getRate() / 100.0);

        straightLineFill(asset, history.getDate(), history.getFinalDate(), history.getEndDate(),
                beginning, GamaMoneyUtils.subtract(correctedValue, expense),
                totalDep, history.getDtExpense(), history.getDtValue());
    }


    static class DebitCreditKey {

        GLOperationAccount debit;

        GLOperationAccount credit;

        List<DocRC> rcs;

        @SuppressWarnings("unused")
        protected DebitCreditKey() {}

        public DebitCreditKey(GLOperationAccount debit, GLOperationAccount credit, List<DocRC> rcs) {
            this.debit = debit;
            this.credit = credit;
            this.rcs = rcs;
        }

        // generated

        public GLOperationAccount getDebit() {
            return debit;
        }

        public void setDebit(GLOperationAccount debit) {
            this.debit = debit;
        }

        public GLOperationAccount getCredit() {
            return credit;
        }

        public void setCredit(GLOperationAccount credit) {
            this.credit = credit;
        }

        public List<DocRC> getRcs() {
            return rcs;
        }

        public void setRcs(List<DocRC> rcs) {
            this.rcs = rcs;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DebitCreditKey that = (DebitCreditKey) o;
            return Objects.equals(debit, that.debit) && Objects.equals(credit, that.credit) && Objects.equals(rcs, that.rcs);
        }

        @Override
        public int hashCode() {
            return Objects.hash(debit, credit, rcs);
        }

        @Override
        public String toString() {
            return "DebitCreditKey{" +
                    "debit=" + debit +
                    ", credit=" + credit +
                    ", rcs=" + rcs +
                    '}';
        }
    }

    public BatchFixResponse fixAsset(AssetSql asset, LocalDate now) {
        BatchFixResponse batchFixResponse = new BatchFixResponse();

        batchFixResponse.setProcessed(1);

        if (asset.getHistory() != null && asset.getHistory().size() > 0) {
            AssetHistory lastHistory = asset.getHistory().get(asset.getHistory().size() - 1);
            if (lastHistory != null) {
                asset.setResponsible(Validators.isValid(lastHistory.getResponsible())
                        ? entityManager.getReference(EmployeeSql.class, lastHistory.getResponsible().getId())
                        : null);
                asset.setLocation(lastHistory.getLocation());
                asset.setLastDate(lastHistory.getFinalDate() != null ? lastHistory.getFinalDate() : lastHistory.getDate());
            }
        }

        dbServiceSQL.saveEntityInCompany(asset);
        batchFixResponse.setFixed(1);

        return batchFixResponse;
    }

    public AssetTotal calcAssetTotal(List<AssetDto> assets, LocalDate date) {
        return calcAssetTotal(assets, date, date);
    }

    public AssetTotal calcAssetTotal(List<AssetDto> assets, LocalDate pDateFrom, LocalDate pDateTo) {
        if (CollectionsHelper.isEmpty(assets) || pDateFrom == null || pDateTo == null) {
            return null;
        }
        final var dateFrom = pDateFrom.with(firstDayOfMonth());
        final var dateTo = pDateTo.with(firstDayOfMonth());
        final var dateToEnd = pDateTo.with(lastDayOfMonth());

        final var result = new AssetTotal(dateFrom, dateToEnd);

        assets.forEach(asset -> {
            asset.setPerPeriod(null);

            if (CollectionsHelper.isEmpty(asset.getDepreciation())) {
                if ((asset.getStatus() == AssetStatusType.OPERATING) && !asset.getDate().isAfter(dateToEnd)) {
                    asset.setPerPeriod(new AssetTotal(dateFrom, dateToEnd));
                    asset.getPerPeriod().setStatus(asset.getStatus());

                    if (!asset.getDate().isBefore(dateFrom)) { // date >= dateFrom
                        asset.getPerPeriod().setAcquired(asset.getValue());
                        result.setAcquired(GamaMoneyUtils.add(result.getAcquired(), asset.getPerPeriod().getAcquired()));
                    } else {
                        asset.getPerPeriod().setBeginning(asset.getValue());
                        result.setBeginning(GamaMoneyUtils.add(result.getBeginning(), asset.getPerPeriod().getBeginning()));
                    }

                    asset.getPerPeriod().setEnding(asset.getValue());
                    asset.getPerPeriod().setDepreciation(asset.getExpenses());

                    result.setEnding(GamaMoneyUtils.add(result.getEnding(), asset.getPerPeriod().getEnding()));
                    result.setDepreciation(GamaMoneyUtils.add(result.getDepreciation(), asset.getPerPeriod().getDepreciation()));
                }
                return;
            }

            asset.getDepreciation().stream()
                    .filter(dep -> !dep.getDate().isBefore(dateFrom) && !dep.getDate().isAfter(dateTo))
                    .forEach(dep -> {
                        if (asset.getPerPeriod() == null) asset.setPerPeriod(new AssetTotal(dateFrom, dateToEnd));

                        asset.getPerPeriod().setDtValue(GamaMoneyUtils.add(asset.getPerPeriod().getDtValue(), dep.getDtValue()));
                        asset.getPerPeriod().setDtExpense(GamaMoneyUtils.add(asset.getPerPeriod().getDtExpense(), dep.getDtExpense()));
                        asset.getPerPeriod().setExpense(GamaMoneyUtils.add(asset.getPerPeriod().getExpense(), dep.getExpense()));

                        if (asset.getDate() != null && dep.getDate().isEqual(asset.getDate())) {
                            asset.getPerPeriod().setIncoming(GamaMoneyUtils.add(asset.getPerPeriod().getIncoming(), dep.getBeginning()));
                        } else if (dep.getDate().isEqual(dateFrom)) {
                            asset.getPerPeriod().setBeginning(GamaMoneyUtils.add(asset.getPerPeriod().getBeginning(), dep.getBeginning()));
                        }

                        if (dep.getDate().isEqual(dateTo)) {
                            asset.getPerPeriod().setEnding(GamaMoneyUtils.add(asset.getPerPeriod().getEnding(), dep.getEnding()));
                            asset.getPerPeriod().setDepreciation(GamaMoneyUtils.add(asset.getPerPeriod().getDepreciation(), dep.getDepreciation()));
                        }
                    });

            // get status at the end of period, i.e. at 'dateTo'
            AssetStatusType lastStatus = CollectionsHelper.isEmpty(asset.getHistory()) ? null : asset.getHistory().stream()
                    .filter(history -> !history.getDate().isAfter(dateTo))  // date <= dateTo
                    .max(Comparator.comparing(AssetHistory::getDate))
                    .map(AssetHistory::getStatus)
                    .orElse(null);

            // if asset conserved before last period date, i.e. before 'dateTo'
            if (lastStatus == AssetStatusType.CONSERVED) {
                // copy conservation values
                asset.getDepreciation().stream()
                        .filter(dep -> dep.getDate().isBefore(dateTo))
                        .max(Comparator.comparing(Depreciation::getDate))
                        .ifPresent(dep -> {
                            if (asset.getPerPeriod() == null) {
                                asset.setPerPeriod(new AssetTotal(dateFrom, dateToEnd));
                                asset.getPerPeriod().setBeginning(dep.getBeginning());
                            }
                            asset.getPerPeriod().setEnding(dep.getEnding());
                            asset.getPerPeriod().setDepreciation(dep.getDepreciation());
                        });
            } else if (lastStatus == AssetStatusType.OPERATING) {
                // copy last operating values
                asset.getDepreciation().stream()
                        .max(Comparator.comparing(Depreciation::getDate))
                        .ifPresent(dep -> {
                            if (dep.getDate().isBefore(dateTo)) {
                                if (asset.getPerPeriod() == null) {
                                    asset.setPerPeriod(new AssetTotal(dateFrom, dateToEnd));
                                    asset.getPerPeriod().setBeginning(dep.getEnding());
                                }
                                asset.getPerPeriod().setEnding(dep.getEnding());
                                asset.getPerPeriod().setDepreciation(dep.getDepreciation());
                            }
                        });
            }

            if (asset.getPerPeriod() == null) {
                asset.setPerPeriod(new AssetTotal(dateFrom, dateToEnd));
            }
            asset.getPerPeriod().setStatus(lastStatus);

            if (asset.getAcquisitionDate() != null && !dateFrom.isAfter(asset.getAcquisitionDate()) && !asset.getAcquisitionDate().isAfter(dateToEnd)) {
                asset.getPerPeriod().setAcquired(asset.getCost());
            }

            result.setAcquired(GamaMoneyUtils.add(result.getAcquired(), asset.getPerPeriod().getAcquired()));
            result.setIncoming(GamaMoneyUtils.add(result.getIncoming(), asset.getPerPeriod().getIncoming()));
            result.setBeginning(GamaMoneyUtils.add(result.getBeginning(), asset.getPerPeriod().getBeginning()));
            result.setDtValue(GamaMoneyUtils.add(result.getDtValue(), asset.getPerPeriod().getDtValue()));
            result.setDtExpense(GamaMoneyUtils.add(result.getDtExpense(), asset.getPerPeriod().getDtExpense()));
            result.setExpense(GamaMoneyUtils.add(result.getExpense(), asset.getPerPeriod().getExpense()));
            result.setEnding(GamaMoneyUtils.add(result.getEnding(), asset.getPerPeriod().getEnding()));
            result.setDepreciation(GamaMoneyUtils.add(result.getDepreciation(), asset.getPerPeriod().getDepreciation()));

        });
        return result;
    }
}

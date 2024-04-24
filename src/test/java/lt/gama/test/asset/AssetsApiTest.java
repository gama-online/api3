package lt.gama.test.asset;

import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.PageRequest;
import lt.gama.api.request.PageRequestCondition;
import lt.gama.api.response.PageResponse;
import lt.gama.helpers.CollectionsHelper;
import lt.gama.helpers.DateUtils;
import lt.gama.helpers.GamaMoneyUtils;
import lt.gama.model.dto.entities.AssetDto;
import lt.gama.model.dto.entities.EmployeeDto;
import lt.gama.model.dto.type.AssetTotal;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.asset.AssetHistory;
import lt.gama.model.type.asset.Depreciation;
import lt.gama.model.type.doc.DocEmployee;
import lt.gama.model.type.enums.AssetStatusType;
import lt.gama.model.type.enums.CustomSearchType;
import lt.gama.model.type.enums.DepreciationType;
import lt.gama.model.type.gl.GLOperationAccount;
import lt.gama.test.base.BaseDBTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static org.assertj.core.api.Assertions.assertThat;

public class AssetsApiTest extends BaseDBTest {

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        // set system time and time zone
        DateUtils.mockClock = Clock.fixed(
                LocalDateTime.of(2018, 4, 15, 0, 0).toInstant(ZoneOffset.UTC),
                ZoneId.systemDefault());
        login();

        {
            AssetDto asset = new AssetDto();
            asset.setCode("A");
            asset.setName("Asset A");
            asset.setLabels(Set.of("LabelA", "LabelB"));

            asset.setAccountCost(new GLOperationAccount("11", "A1"));
            asset.setAccountDepreciation(new GLOperationAccount("21", "B1"));
            asset.setAccountRevaluation(new GLOperationAccount("31", "C1"));
            asset.setAccountExpense(new GLOperationAccount("41", "D1"));

            asset.setAcquisitionDate(LocalDate.of(2014, 9, 30));
            asset.setCost(GamaMoney.parse("EUR 22000.00"));

            asset.setDate(LocalDate.of(2015, 2, 1));
            asset.setValue(GamaMoney.parse("EUR 20000.00"));
            asset.setExpenses(GamaMoney.parse("EUR 2000.00"));
            asset.setHistory(new ArrayList<>());

            AssetHistory change = new AssetHistory();
            change.setDate(asset.getDate());
            change.setFinalDate(LocalDate.of(2018, 2, 1));
            change.setType(DepreciationType.LINE);
            change.setStatus(AssetStatusType.OPERATING);
            change.setDtValue(GamaMoney.parse("EUR 1500.00"));
            change.setDtExpense(GamaMoney.parse("EUR 1000.00"));
            asset.getHistory().add(change);

            APIResult<AssetDto> apiResult = assetApi.saveAsset(asset);
            assertThat(apiResult.getError()).isNull();

            AssetDto assetDto = apiResult.getData();
            assertThat(assetDto.getStatus()).isEqualTo(AssetStatusType.OPERATING);
            assertThat(assetDto.getLastDate()).isEqualTo(LocalDate.of(2018, 2, 1));
            assertThat(assetDto.getDepreciation().size()).isEqualTo(36);
            Depreciation dep = assetDto.getDepreciation().stream()
                    .filter(d -> d.getDate().equals(LocalDate.of(2015, 2, 1)))
                    .findFirst()
                    .orElseThrow();
            assertThat(dep.getBeginning()).isEqualTo(GamaMoney.parse("EUR 20000.00"));
            assertThat(dep.getDtValue()).isEqualTo(GamaMoney.parse("EUR 1500.00"));
            assertThat(dep.getDtExpense()).isEqualTo(GamaMoney.parse("EUR 1000.00"));
            assertThat(dep.getExpense()).isEqualTo(GamaMoney.parse("EUR 569.60"));
            assertThat(dep.getEnding()).isEqualTo(GamaMoney.parse("EUR 19930.40"));
            assertThat(dep.getDepreciation()).isEqualTo(GamaMoney.parse("EUR 3569.60"));
            checkValue(assetDto);
        }
        {
            AssetDto asset = new AssetDto();
            asset.setCode("B AbC 123");
            asset.setName("Asset B");
            asset.setLabels(Set.of("LabelA"));

            asset.setAccountCost(new GLOperationAccount("11", "A1"));
            asset.setAccountDepreciation(new GLOperationAccount("21", "B1"));
            asset.setAccountRevaluation(new GLOperationAccount("31", "C1"));
            asset.setAccountExpense(new GLOperationAccount("41", "D1"));

            asset.setAcquisitionDate(LocalDate.of(2014, 10, 31));
            asset.setCost(GamaMoney.parse("EUR 22000.00"));

            asset.setDate(LocalDate.of(2015, 1, 1));
            asset.setValue(GamaMoney.parse("EUR 20099.00"));
            asset.setExpenses(GamaMoney.parse("EUR 1901.00"));
            asset.setHistory(new ArrayList<>());

            AssetHistory change = new AssetHistory();
            change.setDate(asset.getDate());
            change.setFinalDate(LocalDate.of(2020, 1, 1));
            change.setType(DepreciationType.DOUBLE);
            change.setStatus(AssetStatusType.OPERATING);
            change.setDtValue(GamaMoney.parse("EUR 100.00"));
            change.setDtExpense(GamaMoney.parse("EUR 200.00"));
            change.setEnding(GamaMoney.parse("EUR 1.00"));
            asset.getHistory().add(change);

            APIResult<AssetDto> apiResult = assetApi.saveAsset(asset);
            assertThat(apiResult.getError()).isNull();

            AssetDto assetDto = apiResult.getData();
            assertThat(assetDto.getStatus()).isEqualTo(AssetStatusType.OPERATING);
            assertThat(assetDto.getLastDate()).isEqualTo(LocalDate.of(2020, 1, 1));
            assertThat(assetDto.getDepreciation().size()).isEqualTo(60);
            Depreciation dep = assetDto.getDepreciation().stream()
                    .filter(d -> d.getDate().equals(LocalDate.of(2015, 1, 1)))
                    .findFirst()
                    .orElseThrow();
            assertThat(dep.getBeginning()).isEqualTo(GamaMoney.parse("EUR 20099.00"));
            assertThat(dep.getDtValue()).isEqualTo(GamaMoney.parse("EUR 100.00"));
            assertThat(dep.getDtExpense()).isEqualTo(GamaMoney.parse("EUR 200.00"));
            assertThat(dep.getExpense()).isEqualTo(GamaMoney.parse("EUR 666.67"));
            assertThat(dep.getEnding()).isEqualTo(GamaMoney.parse("EUR 19332.33"));
            assertThat(dep.getDepreciation()).isEqualTo(GamaMoney.parse("EUR 2767.67"));
            checkValue(assetDto);
        }
        {
            AssetDto asset = new AssetDto();
            asset.setCode("C");
            asset.setName("Asset C");

            asset.setAccountCost(new GLOperationAccount("11", "A1"));
            asset.setAccountDepreciation(new GLOperationAccount("21", "B1"));
            asset.setAccountRevaluation(new GLOperationAccount("31", "C1"));
            asset.setAccountExpense(new GLOperationAccount("41", "D1"));

            asset.setAcquisitionDate(LocalDate.of(2014, 11, 30));
            asset.setCost(GamaMoney.parse("EUR 22000.00"));

            asset.setDate(LocalDate.of(2015, 4, 1));
            asset.setValue(GamaMoney.parse("EUR 20099.00"));
            asset.setExpenses(GamaMoney.parse("EUR 1901.00"));
            asset.setHistory(new ArrayList<>());

            asset.setNote("Note ABCDEF 123");

            AssetHistory change = new AssetHistory();
            change.setDate(asset.getDate());
            change.setFinalDate(LocalDate.of(2016, 1, 1));
            change.setType(DepreciationType.OTHER);
            change.setStatus(AssetStatusType.OPERATING);
            change.setDtValue(GamaMoney.parse("EUR 100.00"));
            change.setDtExpense(GamaMoney.parse("EUR 200.00"));
            change.setEnding(GamaMoney.parse("EUR 1.00"));
            change.setRate(40.0);
            asset.getHistory().add(change);

            APIResult<AssetDto> apiResult = assetApi.saveAsset(asset);
            assertThat(apiResult.getError()).isNull();

            AssetDto assetDto = apiResult.getData();
            assertThat(assetDto.getStatus()).isEqualTo(AssetStatusType.OPERATING);
            assertThat(assetDto.getLastDate()).isEqualTo(LocalDate.of(2016, 1, 1));
            assertThat(assetDto.getDepreciation().size()).isEqualTo(9);
            Depreciation dep = assetDto.getDepreciation().stream()
                    .filter(d -> d.getDate().equals(LocalDate.of(2015, 4, 1)))
                    .findFirst()
                    .orElseThrow();
            assertThat(dep.getBeginning()).isEqualTo(GamaMoney.parse("EUR 20099.00"));
            assertThat(dep.getDtValue()).isEqualTo(GamaMoney.parse("EUR 100.00"));
            assertThat(dep.getDtExpense()).isEqualTo(GamaMoney.parse("EUR 200.00"));
            assertThat(dep.getExpense()).isEqualTo(GamaMoney.parse("EUR 888.88"));
            assertThat(dep.getEnding()).isEqualTo(GamaMoney.parse("EUR 19110.12"));
            assertThat(dep.getDepreciation()).isEqualTo(GamaMoney.parse("EUR 2989.88"));

            dep = assetDto.getDepreciation().stream()
                    .filter(d -> d.getDate().equals(LocalDate.of(2015, 12, 1)))
                    .findFirst()
                    .orElseThrow();
            assertThat(dep.getBeginning()).isEqualTo(GamaMoney.parse("EUR 12888.24"));
            assertThat(dep.getDtValue()).isEqualTo(null);
            assertThat(dep.getDtExpense()).isEqualTo(null);
            assertThat(dep.getExpense()).isEqualTo(GamaMoney.parse("EUR 888.84"));
            assertThat(dep.getEnding()).isEqualTo(GamaMoney.parse("EUR 11999.40"));
            assertThat(dep.getDepreciation()).isEqualTo(GamaMoney.parse("EUR 10100.60"));

            checkValue(assetDto);
        }
        {
            AssetDto asset = new AssetDto();
            asset.setCode("D");
            asset.setName("Asset D abc");

            asset.setAccountCost(new GLOperationAccount("11", "A1"));
            asset.setAccountDepreciation(new GLOperationAccount("21", "B1"));
            asset.setAccountRevaluation(new GLOperationAccount("31", "C1"));
            asset.setAccountExpense(new GLOperationAccount("42", "D2"));

            asset.setAcquisitionDate(LocalDate.of(2014, 12, 31));
            asset.setCost(GamaMoney.parse("EUR 22000.00"));

            asset.setDate(LocalDate.of(2015, 2, 1));
            asset.setValue(GamaMoney.parse("EUR 20099.00"));
            asset.setExpenses(GamaMoney.parse("EUR 1901.00"));
            asset.setHistory(new ArrayList<>());

            AssetHistory change = new AssetHistory();
            change.setDate(asset.getDate());
            change.setFinalDate(LocalDate.of(2016, 1, 1));
            change.setType(DepreciationType.OTHER);
            change.setStatus(AssetStatusType.OPERATING);
            change.setDtValue(GamaMoney.parse("EUR 100.00"));
            change.setDtExpense(GamaMoney.parse("EUR 200.00"));
            change.setEnding(GamaMoney.parse("EUR 1.00"));
            change.setRate(40.0);
            asset.getHistory().add(change);

            change = new AssetHistory();
            change.setDate(LocalDate.of(2016, 2, 1));
            change.setStatus(AssetStatusType.WRITTEN_OFF);
            change.setEnding(GamaMoney.parse("EUR 1.00"));
            asset.getHistory().add(change);

            APIResult<AssetDto> apiResult = assetApi.saveAsset(asset);
            assertThat(apiResult.getError()).isNull();

            AssetDto assetDto = apiResult.getData();
            assertThat(assetDto.getStatus()).isEqualTo(AssetStatusType.WRITTEN_OFF);
            assertThat(assetDto.getLastDate()).isEqualTo(LocalDate.of(2016, 2, 1));
            assertThat(assetDto.getDepreciation().size()).isEqualTo(11);
            Depreciation dep = assetDto.getDepreciation().stream()
                    .filter(d -> d.getDate().equals(LocalDate.of(2015, 2, 1)))
                    .findFirst()
                    .orElseThrow();
            assertThat(dep.getBeginning()).isEqualTo(GamaMoney.parse("EUR 20099.00"));
            assertThat(dep.getDtValue()).isEqualTo(GamaMoney.parse("EUR 100.00"));
            assertThat(dep.getDtExpense()).isEqualTo(GamaMoney.parse("EUR 200.00"));
            assertThat(dep.getExpense()).isEqualTo(GamaMoney.parse("EUR 727.20"));
            assertThat(dep.getEnding()).isEqualTo(GamaMoney.parse("EUR 19271.80"));
            assertThat(dep.getDepreciation()).isEqualTo(GamaMoney.parse("EUR 2828.20"));
            checkValue(assetDto);
        }
        {
            AssetDto asset = new AssetDto();
            asset.setCode("E");
            asset.setName("Asset E");
            asset.setCipher("aAbcd");

            asset.setAccountCost(new GLOperationAccount("11", "A1"));
            asset.setAccountDepreciation(new GLOperationAccount("21", "B1"));
            asset.setAccountRevaluation(new GLOperationAccount("31", "C1"));
            asset.setAccountExpense(new GLOperationAccount("42", "D2"));

            asset.setAcquisitionDate(LocalDate.of(2014, 1, 1));
            asset.setCost(GamaMoney.parse("EUR 22000.00"));

            asset.setDate(LocalDate.of(2015, 2, 1));
            asset.setValue(GamaMoney.parse("EUR 20099.00"));
            asset.setExpenses(GamaMoney.parse("EUR 1901.00"));
            asset.setHistory(new ArrayList<>());

            AssetHistory change = new AssetHistory();
            change.setDate(asset.getDate());
            change.setFinalDate(LocalDate.of(2016, 1, 1));
            change.setType(DepreciationType.OTHER);
            change.setStatus(AssetStatusType.OPERATING);
            change.setDtValue(GamaMoney.parse("EUR 100.00"));
            change.setDtExpense(GamaMoney.parse("EUR 200.00"));
            change.setEnding(GamaMoney.parse("EUR 1.00"));
            change.setRate(40.0);
            asset.getHistory().add(change);

            EmployeeDto employee = createEmployee("Employee #1", "201");

            change = new AssetHistory();
            change.setDate(LocalDate.of(2015, 4, 1));
            change.setStatus(AssetStatusType.CONSERVED);
            change.setResponsible(new DocEmployee(employee));
            asset.getHistory().add(change);

            APIResult<AssetDto> apiResult = assetApi.saveAsset(asset);
            assertThat(apiResult.getError()).isNull();

            AssetDto assetDto = apiResult.getData();
            assertThat(assetDto.getStatus()).isEqualTo(AssetStatusType.CONSERVED);
            assertThat(assetDto.getLastDate()).isEqualTo(LocalDate.of(2015, 4, 1));
            assertThat(assetDto.getDepreciation().size()).isEqualTo(3);
            Depreciation dep = assetDto.getDepreciation().stream()
                    .filter(d -> d.getDate().equals(LocalDate.of(2015, 2, 1)))
                    .findFirst()
                    .orElseThrow();
            assertThat(dep.getBeginning()).isEqualTo(GamaMoney.parse("EUR 20099.00"));
            assertThat(dep.getDtValue()).isEqualTo(GamaMoney.parse("EUR 100.00"));
            assertThat(dep.getDtExpense()).isEqualTo(GamaMoney.parse("EUR 200.00"));
            assertThat(dep.getExpense()).isEqualTo(GamaMoney.parse("EUR 727.20"));
            assertThat(dep.getEnding()).isEqualTo(GamaMoney.parse("EUR 19271.80"));
            assertThat(dep.getDepreciation()).isEqualTo(GamaMoney.parse("EUR 2828.20"));
            checkValue(assetDto);
        }
        {
            AssetDto asset = new AssetDto();
            asset.setCode("F");
            asset.setName("Asset F");

            asset.setAccountCost(new GLOperationAccount("11", "A1"));
            asset.setAccountDepreciation(new GLOperationAccount("21", "B1"));
            asset.setAccountRevaluation(new GLOperationAccount("31", "C1"));
            asset.setAccountExpense(new GLOperationAccount("41", "D1"));

            asset.setAcquisitionDate(LocalDate.of(2010, 1, 30));
            asset.setCost(GamaMoney.parse("EUR 20000.00"));

            asset.setDate(LocalDate.of(2014, 2, 1));
            asset.setValue(GamaMoney.parse("EUR 1.00"));
            asset.setExpenses(GamaMoney.parse("EUR 19999.00"));
            asset.setHistory(new ArrayList<>());

            AssetHistory change = new AssetHistory();
            change.setDate(asset.getDate());
            change.setFinalDate(LocalDate.of(2014, 2, 1));
            change.setType(DepreciationType.LINE);
            change.setStatus(AssetStatusType.OPERATING);
            change.setBeginning(GamaMoney.parse("EUR 1.00"));
            change.setEnding(GamaMoney.parse("EUR 1.00"));
            asset.getHistory().add(change);

            APIResult<AssetDto> apiResult = assetApi.saveAsset(asset);
            assertThat(apiResult.getError()).isNull();

            AssetDto assetDto = apiResult.getData();
            assertThat(assetDto.getStatus()).isEqualTo(AssetStatusType.OPERATING);
            assertThat(assetDto.getLastDate()).isEqualTo(LocalDate.of(2014, 2, 1));
            assertThat(assetDto.getDepreciation().size()).isEqualTo(0);
        }
        {
            AssetDto asset = new AssetDto();
            asset.setCode("G");
            asset.setName("Asset G");

            asset.setAccountCost(new GLOperationAccount("11", "A1"));
            asset.setAccountDepreciation(new GLOperationAccount("21", "B1"));
            asset.setAccountRevaluation(new GLOperationAccount("31", "C1"));
            asset.setAccountExpense(new GLOperationAccount("41", "D1"));

            asset.setAcquisitionDate(LocalDate.of(2010, 1, 30));
            asset.setCost(GamaMoney.parse("EUR 20000.00"));

            asset.setDate(LocalDate.of(2014, 2, 1));
            asset.setValue(GamaMoney.parse("EUR 1000.00"));
            asset.setExpenses(GamaMoney.parse("EUR 19000.00"));
            asset.setHistory(new ArrayList<>());

            AssetHistory change = new AssetHistory();
            change.setDate(asset.getDate());
            change.setStatus(AssetStatusType.CONSERVED);
            change.setBeginning(GamaMoney.parse("EUR 1000.00"));
            change.setEnding(GamaMoney.parse("EUR 1000.00"));
            asset.getHistory().add(change);

            APIResult<AssetDto> apiResult = assetApi.saveAsset(asset);
            assertThat(apiResult.getError()).isNull();

            AssetDto assetDto = apiResult.getData();
            assertThat(assetDto.getStatus()).isEqualTo(AssetStatusType.CONSERVED);
            assertThat(assetDto.getLastDate()).isEqualTo("2014-02-01");
            assertThat(assetDto.getDepreciation().size()).isEqualTo(1);
            assertThat(assetDto.getDepreciation().get(0).getBeginning()).isEqualTo(GamaMoney.parse("EUR 1000.00"));
            assertThat(assetDto.getDepreciation().get(0).getDtValue()).isEqualTo(null);
            assertThat(assetDto.getDepreciation().get(0).getDtExpense()).isEqualTo(null);
            assertThat(assetDto.getDepreciation().get(0).getExpense()).isEqualTo(null);
            assertThat(assetDto.getDepreciation().get(0).getEnding()).isEqualTo(GamaMoney.parse("EUR 1000.00"));
            assertThat(assetDto.getDepreciation().get(0).getDepreciation()).isEqualTo(GamaMoney.parse("EUR 19000.00"));
        }
    }

    private void checkValue(AssetDto asset) {
        if (CollectionsHelper.isEmpty(asset.getDepreciation())) return;
        GamaMoney value = asset.getValue();
        for (Depreciation dep : asset.getDepreciation()) {
            assertThat(dep.getBeginning()).isEqualTo(value);
            value = dep.getEnding();
        }
    }

    private void checkValue(AssetTotal assetTotal) {
        assertThat(GamaMoneyUtils.isZero(GamaMoneyUtils.total(
                assetTotal.getIncoming(),   // new asset value
                assetTotal.getBeginning(),  // value from prev. month
                assetTotal.getDtValue(),
                GamaMoneyUtils.negated(assetTotal.getDtExpense()),
                GamaMoneyUtils.negated(assetTotal.getExpense()),
                GamaMoneyUtils.negated(assetTotal.getEnding())))).isTrue();
    }


    // Asset A:     2014-09-30  OPERATING       2015-02-01  2018-02-01
    // Asset B:     2014-10-31  OPERATING       2015-01-01  2020-01-01
    // Asset C:     2014-11-30  OPERATING       2015-04-01  2016-01-01
    // Asset D:     2014-12-31  OPERATING       2015-02-01  2016-01-01
    //                          WRITTEN_OFF     2016-02-01
    // Asset E:     2014-01-01  OPERATING       2015-02-01  2016-01-01
    //                          CONSERVED       2015-04-01
    // Asset F:     2010-01-30  OPERATING       2014-02-01  2014-02-01
    // Asset G:     2010-01-30  CONSERVED       2014-02-01  2014-02-01

    @Test
    void testListApiByDateBeforeOperating() throws GamaApiException {
        final LocalDate DATE = LocalDate.parse("2014-10-01");

        PageRequest request = new PageRequest();
        request.setConditions(new ArrayList<>());
        request.getConditions().add(new PageRequestCondition(CustomSearchType.DATE, DATE));

        APIResult<PageResponse<AssetDto, AssetTotal>> result = assetApi.listAsset(request);
        assertThat(result.getError()).isNull();

        PageResponse<AssetDto, AssetTotal> response = result.getData();
        assertThat(response.getTotal()).isEqualTo(5);

        assertThat(response.getItems().get(0).getName()).isEqualTo("Asset A");
        assertThat(response.getItems().get(0).getStatus()).isEqualTo(AssetStatusType.OPERATING);
        assertThat(response.getItems().get(0).getDepreciation().stream().filter(d -> d.getDate().equals(DATE)).findFirst()).isEqualTo(Optional.empty());
        assertThat(response.getItems().get(0).getPerPeriod().getDateFrom()).isEqualTo(DATE);
        assertThat(response.getItems().get(0).getPerPeriod().getDateTo()).isEqualTo(DATE.with(lastDayOfMonth()));
        assertThat(response.getItems().get(0).getPerPeriod().getStatus()).isEqualTo(null);
        assertThat(response.getItems().get(0).getPerPeriod().getAcquired()).isEqualTo(null);
        assertThat(response.getItems().get(0).getPerPeriod().getIncoming()).isEqualTo(null);
        assertThat(response.getItems().get(0).getPerPeriod().getBeginning()).isEqualTo(null);
        assertThat(response.getItems().get(0).getPerPeriod().getDtValue()).isEqualTo(null);
        assertThat(response.getItems().get(0).getPerPeriod().getDtExpense()).isEqualTo(null);
        assertThat(response.getItems().get(0).getPerPeriod().getExpense()).isEqualTo(null);
        assertThat(response.getItems().get(0).getPerPeriod().getEnding()).isEqualTo(null);
        assertThat(response.getItems().get(0).getPerPeriod().getDepreciation()).isEqualTo(null);
        checkValue(response.getItems().get(0).getPerPeriod());

        assertThat(response.getItems().get(1).getName()).isEqualTo("Asset B");
        assertThat(response.getItems().get(1).getStatus()).isEqualTo(AssetStatusType.OPERATING);
        assertThat(response.getItems().get(1).getDepreciation().stream().filter(d -> d.getDate().equals(DATE)).findFirst()).isEqualTo(Optional.empty());
        assertThat(response.getItems().get(1).getPerPeriod().getDateFrom()).isEqualTo(DATE);
        assertThat(response.getItems().get(1).getPerPeriod().getDateTo()).isEqualTo(DATE.with(lastDayOfMonth()));
        assertThat(response.getItems().get(1).getPerPeriod().getStatus()).isEqualTo(null);
        assertThat(response.getItems().get(1).getPerPeriod().getAcquired()).isEqualTo(GamaMoney.parse("EUR 22000.00"));
        assertThat(response.getItems().get(1).getPerPeriod().getIncoming()).isEqualTo(null);
        assertThat(response.getItems().get(1).getPerPeriod().getBeginning()).isEqualTo(null);
        assertThat(response.getItems().get(1).getPerPeriod().getDtValue()).isEqualTo(null);
        assertThat(response.getItems().get(1).getPerPeriod().getDtExpense()).isEqualTo(null);
        assertThat(response.getItems().get(1).getPerPeriod().getExpense()).isEqualTo(null);
        assertThat(response.getItems().get(1).getPerPeriod().getEnding()).isEqualTo(null);
        assertThat(response.getItems().get(1).getPerPeriod().getDepreciation()).isEqualTo(null);
        checkValue(response.getItems().get(1).getPerPeriod());

        assertThat(response.getItems().get(2).getName()).isEqualTo("Asset E");
        assertThat(response.getItems().get(2).getStatus()).isEqualTo(AssetStatusType.CONSERVED);
        assertThat(response.getItems().get(2).getDepreciation().stream().filter(d -> d.getDate().equals(DATE)).findFirst()).isEqualTo(Optional.empty());
        assertThat(response.getItems().get(2).getPerPeriod().getDateFrom()).isEqualTo(DATE);
        assertThat(response.getItems().get(2).getPerPeriod().getDateTo()).isEqualTo(DATE.with(lastDayOfMonth()));
        assertThat(response.getItems().get(2).getPerPeriod().getStatus()).isEqualTo(null);
        assertThat(response.getItems().get(2).getPerPeriod().getAcquired()).isEqualTo(null);
        assertThat(response.getItems().get(2).getPerPeriod().getIncoming()).isEqualTo(null);
        assertThat(response.getItems().get(2).getPerPeriod().getBeginning()).isEqualTo(null);
        assertThat(response.getItems().get(2).getPerPeriod().getDtValue()).isEqualTo(null);
        assertThat(response.getItems().get(2).getPerPeriod().getDtExpense()).isEqualTo(null);
        assertThat(response.getItems().get(2).getPerPeriod().getExpense()).isEqualTo(null);
        assertThat(response.getItems().get(2).getPerPeriod().getEnding()).isEqualTo(null);
        assertThat(response.getItems().get(2).getPerPeriod().getDepreciation()).isEqualTo(null);
        checkValue(response.getItems().get(2).getPerPeriod());

        assertThat(response.getItems().get(3).getName()).isEqualTo("Asset F");
        assertThat(response.getItems().get(3).getStatus()).isEqualTo(AssetStatusType.OPERATING);
        assertThat(response.getItems().get(3).getDepreciation().stream().filter(d -> d.getDate().equals(DATE)).findFirst()).isEqualTo(Optional.empty());
        assertThat(response.getItems().get(3).getPerPeriod().getDateFrom()).isEqualTo(DATE);
        assertThat(response.getItems().get(3).getPerPeriod().getDateTo()).isEqualTo(DATE.with(lastDayOfMonth()));
        assertThat(response.getItems().get(3).getPerPeriod().getStatus()).isEqualTo(AssetStatusType.OPERATING);
        assertThat(response.getItems().get(3).getPerPeriod().getAcquired()).isEqualTo(null);
        assertThat(response.getItems().get(3).getPerPeriod().getIncoming()).isEqualTo(null);
        assertThat(response.getItems().get(3).getPerPeriod().getBeginning()).isEqualTo(GamaMoney.parse("EUR 1.00"));
        assertThat(response.getItems().get(3).getPerPeriod().getDtValue()).isEqualTo(null);
        assertThat(response.getItems().get(3).getPerPeriod().getDtExpense()).isEqualTo(null);
        assertThat(response.getItems().get(3).getPerPeriod().getExpense()).isEqualTo(null);
        assertThat(response.getItems().get(3).getPerPeriod().getEnding()).isEqualTo(GamaMoney.parse("EUR 1.00"));
        assertThat(response.getItems().get(3).getPerPeriod().getDepreciation()).isEqualTo(GamaMoney.parse("EUR 19999.00"));
        checkValue(response.getItems().get(3).getPerPeriod());

        assertThat(response.getItems().get(4).getName()).isEqualTo("Asset G");
        assertThat(response.getItems().get(4).getStatus()).isEqualTo(AssetStatusType.CONSERVED);
        assertThat(response.getItems().get(4).getDepreciation().stream().filter(d -> d.getDate().equals(DATE)).findFirst()).isEqualTo(Optional.empty());
        assertThat(response.getItems().get(4).getPerPeriod().getDateFrom()).isEqualTo(DATE);
        assertThat(response.getItems().get(4).getPerPeriod().getDateTo()).isEqualTo(DATE.with(lastDayOfMonth()));
        assertThat(response.getItems().get(4).getPerPeriod().getStatus()).isEqualTo(AssetStatusType.CONSERVED);
        assertThat(response.getItems().get(4).getPerPeriod().getAcquired()).isEqualTo(null);
        assertThat(response.getItems().get(4).getPerPeriod().getIncoming()).isEqualTo(null);
        assertThat(response.getItems().get(4).getPerPeriod().getBeginning()).isEqualTo(GamaMoney.parse("EUR 1000.00"));
        assertThat(response.getItems().get(4).getPerPeriod().getDtValue()).isEqualTo(null);
        assertThat(response.getItems().get(4).getPerPeriod().getDtExpense()).isEqualTo(null);
        assertThat(response.getItems().get(4).getPerPeriod().getExpense()).isEqualTo(null);
        assertThat(response.getItems().get(4).getPerPeriod().getEnding()).isEqualTo(GamaMoney.parse("EUR 1000.00"));
        assertThat(response.getItems().get(4).getPerPeriod().getDepreciation()).isEqualTo(GamaMoney.parse("EUR 19000.00"));
        checkValue(response.getItems().get(4).getPerPeriod());

        assertThat(response.getAttachment().getDateFrom()).isEqualTo(DATE);
        assertThat(response.getAttachment().getDateTo()).isEqualTo(DATE.with(lastDayOfMonth()));
        assertThat(response.getAttachment().getAcquired()).isEqualTo(GamaMoney.parse("EUR 22000.00"));
        assertThat(response.getAttachment().getIncoming()).isEqualTo(null);
        assertThat(response.getAttachment().getBeginning()).isEqualTo(GamaMoney.parse("EUR 1001.00"));
        assertThat(response.getAttachment().getDtValue()).isEqualTo(null);
        assertThat(response.getAttachment().getDtExpense()).isEqualTo(null);
        assertThat(response.getAttachment().getExpense()).isEqualTo(null);
        assertThat(response.getAttachment().getEnding()).isEqualTo(GamaMoney.parse("EUR 1001.00"));
        assertThat(response.getAttachment().getDepreciation()).isEqualTo(GamaMoney.parse("EUR 38999.00"));
        checkValue(response.getAttachment());
    }

    @Test
    void testListApiByDateOnly() throws GamaApiException {
        final LocalDate DATE = LocalDate.parse("2015-02-01");
        PageRequest request = new PageRequest();
        request.setConditions(new ArrayList<>());
        request.getConditions().add(new PageRequestCondition(CustomSearchType.DATE, DATE));

        APIResult<PageResponse<AssetDto, AssetTotal>> result = assetApi.listAsset(request);
        assertThat(result.getError()).isNull();

        PageResponse<AssetDto, AssetTotal> response = result.getData();
        assertThat(response.getTotal()).isEqualTo(7);

        assertThat(response.getItems().get(0).getName()).isEqualTo("Asset A");
        assertThat(response.getItems().get(0).getStatus()).isEqualTo(AssetStatusType.OPERATING);
        assertThat(response.getItems().get(0).getDepreciation().stream().filter(d -> d.getDate().equals(DATE)).findFirst().orElseThrow())
                .isEqualTo(new Depreciation(DATE)
                        .setBeginning(GamaMoney.parse("EUR 20000.00"))
                        .setDtValue(GamaMoney.parse("EUR 1500.00"))
                        .setDtExpense(GamaMoney.parse("EUR 1000.00"))
                        .setExpense(GamaMoney.parse("EUR 569.60"))
                        .setEnding(GamaMoney.parse("EUR 19930.40"))
                        .setDepreciation(GamaMoney.parse("EUR 3569.60")));
        assertThat(response.getItems().get(0).getPerPeriod().getDateFrom()).isEqualTo(DATE);
        assertThat(response.getItems().get(0).getPerPeriod().getDateTo()).isEqualTo(DATE.with(lastDayOfMonth()));
        assertThat(response.getItems().get(0).getPerPeriod().getStatus()).isEqualTo(AssetStatusType.OPERATING);
        assertThat(response.getItems().get(0).getPerPeriod().getAcquired()).isEqualTo(null);
        assertThat(response.getItems().get(0).getPerPeriod().getIncoming()).isEqualTo(GamaMoney.parse("EUR 20000.00"));
        assertThat(response.getItems().get(0).getPerPeriod().getBeginning()).isEqualTo(null);
        assertThat(response.getItems().get(0).getPerPeriod().getDtValue()).isEqualTo(GamaMoney.parse("EUR 1500.00"));
        assertThat(response.getItems().get(0).getPerPeriod().getDtExpense()).isEqualTo(GamaMoney.parse("EUR 1000.00"));
        assertThat(response.getItems().get(0).getPerPeriod().getExpense()).isEqualTo(GamaMoney.parse("EUR 569.60"));
        assertThat(response.getItems().get(0).getPerPeriod().getEnding()).isEqualTo(GamaMoney.parse("EUR 19930.40"));
        assertThat(response.getItems().get(0).getPerPeriod().getDepreciation()).isEqualTo(GamaMoney.parse("EUR 3569.60"));
        checkValue(response.getItems().get(0).getPerPeriod());

        assertThat(response.getItems().get(1).getName()).isEqualTo("Asset B");
        assertThat(response.getItems().get(1).getStatus()).isEqualTo(AssetStatusType.OPERATING);
        assertThat(response.getItems().get(1).getDepreciation().stream().filter(d -> d.getDate().equals(DATE)).findFirst().orElseThrow())
                .isEqualTo(new Depreciation(DATE)
                        .setBeginning(GamaMoney.parse("EUR 19332.33"))
                        .setExpense(GamaMoney.parse("EUR 666.63"))
                        .setEnding(GamaMoney.parse("EUR 18665.70"))
                        .setDepreciation(GamaMoney.parse("EUR 3434.30")));
        assertThat(response.getItems().get(1).getPerPeriod().getDateFrom()).isEqualTo(DATE);
        assertThat(response.getItems().get(1).getPerPeriod().getDateTo()).isEqualTo(DATE.with(lastDayOfMonth()));
        assertThat(response.getItems().get(1).getPerPeriod().getStatus()).isEqualTo(AssetStatusType.OPERATING);
        assertThat(response.getItems().get(1).getPerPeriod().getAcquired()).isEqualTo(null);
        assertThat(response.getItems().get(1).getPerPeriod().getIncoming()).isEqualTo(null);
        assertThat(response.getItems().get(1).getPerPeriod().getBeginning()).isEqualTo(GamaMoney.parse("EUR 19332.33"));
        assertThat(response.getItems().get(1).getPerPeriod().getDtValue()).isEqualTo(null);
        assertThat(response.getItems().get(1).getPerPeriod().getDtExpense()).isEqualTo(null);
        assertThat(response.getItems().get(1).getPerPeriod().getExpense()).isEqualTo(GamaMoney.parse("EUR 666.63"));
        assertThat(response.getItems().get(1).getPerPeriod().getEnding()).isEqualTo(GamaMoney.parse("EUR 18665.70"));
        assertThat(response.getItems().get(1).getPerPeriod().getDepreciation()).isEqualTo(GamaMoney.parse("EUR 3434.30"));
        checkValue(response.getItems().get(1).getPerPeriod());

        assertThat(response.getItems().get(2).getName()).isEqualTo("Asset C");
        assertThat(response.getItems().get(2).getStatus()).isEqualTo(AssetStatusType.OPERATING);
        assertThat(response.getItems().get(2).getDepreciation().stream().filter(d -> d.getDate().equals(DATE)).findFirst()).isEqualTo(Optional.empty());
        assertThat(response.getItems().get(2).getPerPeriod().getDateFrom()).isEqualTo(DATE);
        assertThat(response.getItems().get(2).getPerPeriod().getDateTo()).isEqualTo(DATE.with(lastDayOfMonth()));
        assertThat(response.getItems().get(2).getPerPeriod().getStatus()).isEqualTo(null);
        assertThat(response.getItems().get(2).getPerPeriod().getAcquired()).isEqualTo(null);
        assertThat(response.getItems().get(2).getPerPeriod().getIncoming()).isEqualTo(null);
        assertThat(response.getItems().get(2).getPerPeriod().getBeginning()).isEqualTo(null);
        assertThat(response.getItems().get(2).getPerPeriod().getDtValue()).isEqualTo(null);
        assertThat(response.getItems().get(2).getPerPeriod().getDtExpense()).isEqualTo(null);
        assertThat(response.getItems().get(2).getPerPeriod().getExpense()).isEqualTo(null);
        assertThat(response.getItems().get(2).getPerPeriod().getEnding()).isEqualTo(null);
        assertThat(response.getItems().get(2).getPerPeriod().getDepreciation()).isEqualTo(null);
        checkValue(response.getItems().get(2).getPerPeriod());

        assertThat(response.getItems().get(3).getName()).isEqualTo("Asset D abc");
        assertThat(response.getItems().get(3).getStatus()).isEqualTo(AssetStatusType.WRITTEN_OFF);
        assertThat(response.getItems().get(3).getDepreciation().stream().filter(d -> d.getDate().equals(DATE)).findFirst().orElseThrow())
                .isEqualTo(new Depreciation(DATE)
                        .setBeginning(GamaMoney.parse("EUR 20099.00"))
                        .setDtValue(GamaMoney.parse("EUR 100.00"))
                        .setDtExpense(GamaMoney.parse("EUR 200.00"))
                        .setExpense(GamaMoney.parse("EUR 727.20"))
                        .setEnding(GamaMoney.parse("EUR 19271.80"))
                        .setDepreciation(GamaMoney.parse("EUR 2828.20")));
        assertThat(response.getItems().get(3).getPerPeriod().getDateFrom()).isEqualTo(DATE);
        assertThat(response.getItems().get(3).getPerPeriod().getDateTo()).isEqualTo(DATE.with(lastDayOfMonth()));
        assertThat(response.getItems().get(3).getPerPeriod().getStatus()).isEqualTo(AssetStatusType.OPERATING);
        assertThat(response.getItems().get(3).getPerPeriod().getAcquired()).isEqualTo(null);
        assertThat(response.getItems().get(3).getPerPeriod().getIncoming()).isEqualTo(GamaMoney.parse("EUR 20099.00"));
        assertThat(response.getItems().get(3).getPerPeriod().getBeginning()).isEqualTo(null);
        assertThat(response.getItems().get(3).getPerPeriod().getDtValue()).isEqualTo(GamaMoney.parse("EUR 100.00"));
        assertThat(response.getItems().get(3).getPerPeriod().getDtExpense()).isEqualTo(GamaMoney.parse("EUR 200.00"));
        assertThat(response.getItems().get(3).getPerPeriod().getExpense()).isEqualTo(GamaMoney.parse("EUR 727.20"));
        assertThat(response.getItems().get(3).getPerPeriod().getEnding()).isEqualTo(GamaMoney.parse("EUR 19271.80"));
        assertThat(response.getItems().get(3).getPerPeriod().getDepreciation()).isEqualTo(GamaMoney.parse("EUR 2828.20"));
        checkValue(response.getItems().get(3).getPerPeriod());

        assertThat(response.getItems().get(4).getName()).isEqualTo("Asset E");
        assertThat(response.getItems().get(4).getStatus()).isEqualTo(AssetStatusType.CONSERVED);
        assertThat(response.getItems().get(4).getDepreciation().stream().filter(d -> d.getDate().equals(DATE)).findFirst().orElseThrow())
                .isEqualTo(new Depreciation(DATE)
                        .setBeginning(GamaMoney.parse("EUR 20099.00"))
                        .setDtValue(GamaMoney.parse("EUR 100.00"))
                        .setDtExpense(GamaMoney.parse("EUR 200.00"))
                        .setExpense(GamaMoney.parse("EUR 727.20"))
                        .setEnding(GamaMoney.parse("EUR 19271.80"))
                        .setDepreciation(GamaMoney.parse("EUR 2828.20")));
        assertThat(response.getItems().get(4).getPerPeriod().getDateFrom()).isEqualTo(DATE);
        assertThat(response.getItems().get(4).getPerPeriod().getDateTo()).isEqualTo(DATE.with(lastDayOfMonth()));
        assertThat(response.getItems().get(4).getPerPeriod().getStatus()).isEqualTo(AssetStatusType.OPERATING);
        assertThat(response.getItems().get(4).getPerPeriod().getAcquired()).isEqualTo(null);
        assertThat(response.getItems().get(4).getPerPeriod().getIncoming()).isEqualTo(GamaMoney.parse("EUR 20099.00"));
        assertThat(response.getItems().get(4).getPerPeriod().getBeginning()).isEqualTo(null);
        assertThat(response.getItems().get(4).getPerPeriod().getDtValue()).isEqualTo(GamaMoney.parse("EUR 100.00"));
        assertThat(response.getItems().get(4).getPerPeriod().getDtExpense()).isEqualTo(GamaMoney.parse("EUR 200.00"));
        assertThat(response.getItems().get(4).getPerPeriod().getExpense()).isEqualTo(GamaMoney.parse("EUR 727.20"));
        assertThat(response.getItems().get(4).getPerPeriod().getEnding()).isEqualTo(GamaMoney.parse("EUR 19271.80"));
        assertThat(response.getItems().get(4).getPerPeriod().getDepreciation()).isEqualTo(GamaMoney.parse("EUR 2828.20"));
        checkValue(response.getItems().get(4).getPerPeriod());

        assertThat(response.getItems().get(5).getName()).isEqualTo("Asset F");
        assertThat(response.getItems().get(5).getStatus()).isEqualTo(AssetStatusType.OPERATING);
        assertThat(response.getItems().get(5).getDepreciation().size()).isEqualTo(0);
        assertThat(response.getItems().get(5).getPerPeriod().getDateFrom()).isEqualTo(DATE);
        assertThat(response.getItems().get(5).getPerPeriod().getDateTo()).isEqualTo(DATE.with(lastDayOfMonth()));
        assertThat(response.getItems().get(5).getPerPeriod().getStatus()).isEqualTo(AssetStatusType.OPERATING);
        assertThat(response.getItems().get(5).getPerPeriod().getAcquired()).isEqualTo(null);
        assertThat(response.getItems().get(5).getPerPeriod().getIncoming()).isEqualTo(null);
        assertThat(response.getItems().get(5).getPerPeriod().getBeginning()).isEqualTo(GamaMoney.parse("EUR 1.00"));
        assertThat(response.getItems().get(5).getPerPeriod().getDtValue()).isEqualTo(null);
        assertThat(response.getItems().get(5).getPerPeriod().getDtExpense()).isEqualTo(null);
        assertThat(response.getItems().get(5).getPerPeriod().getExpense()).isEqualTo(null);
        assertThat(response.getItems().get(5).getPerPeriod().getEnding()).isEqualTo(GamaMoney.parse("EUR 1.00"));
        assertThat(response.getItems().get(5).getPerPeriod().getDepreciation()).isEqualTo(GamaMoney.parse("EUR 19999.00"));
        checkValue(response.getItems().get(5).getPerPeriod());

        assertThat(response.getItems().get(6).getName()).isEqualTo("Asset G");
        assertThat(response.getItems().get(6).getStatus()).isEqualTo(AssetStatusType.CONSERVED);
        assertThat(response.getItems().get(6).getDepreciation().stream().filter(d -> d.getDate().equals(DATE)).findFirst()).isEqualTo(Optional.empty());
        assertThat(response.getItems().get(6).getDepreciation().stream().filter(d -> d.getDate().equals(LocalDate.parse("2014-02-01"))).findFirst().orElseThrow())
                .isEqualTo(new Depreciation(LocalDate.parse("2014-02-01"))
                        .setBeginning(GamaMoney.parse("EUR 1000.00"))
                        .setEnding(GamaMoney.parse("EUR 1000.00"))
                        .setDepreciation(GamaMoney.parse("EUR 19000.00")));
        assertThat(response.getItems().get(6).getPerPeriod().getDateFrom()).isEqualTo(DATE);
        assertThat(response.getItems().get(6).getPerPeriod().getDateTo()).isEqualTo(DATE.with(lastDayOfMonth()));
        assertThat(response.getItems().get(6).getPerPeriod().getStatus()).isEqualTo(AssetStatusType.CONSERVED);
        assertThat(response.getItems().get(6).getPerPeriod().getAcquired()).isEqualTo(null);
        assertThat(response.getItems().get(6).getPerPeriod().getIncoming()).isEqualTo(null);
        assertThat(response.getItems().get(6).getPerPeriod().getBeginning()).isEqualTo(GamaMoney.parse("EUR 1000.00"));
        assertThat(response.getItems().get(6).getPerPeriod().getDtValue()).isEqualTo(null);
        assertThat(response.getItems().get(6).getPerPeriod().getDtExpense()).isEqualTo(null);
        assertThat(response.getItems().get(6).getPerPeriod().getExpense()).isEqualTo(null);
        assertThat(response.getItems().get(6).getPerPeriod().getEnding()).isEqualTo(GamaMoney.parse("EUR 1000.00"));
        assertThat(response.getItems().get(6).getPerPeriod().getDepreciation()).isEqualTo(GamaMoney.parse("EUR 19000.00"));
        checkValue(response.getItems().get(6).getPerPeriod());

        assertThat(response.getAttachment().getDateFrom()).isEqualTo(DATE);
        assertThat(response.getAttachment().getDateTo()).isEqualTo(DATE.with(lastDayOfMonth()));
        assertThat(response.getAttachment().getAcquired()).isEqualTo(GamaMoneyUtils.total(
                response.getItems().get(0).getPerPeriod().getAcquired(),
                response.getItems().get(1).getPerPeriod().getAcquired(),
                response.getItems().get(2).getPerPeriod().getAcquired(),
                response.getItems().get(3).getPerPeriod().getAcquired(),
                response.getItems().get(4).getPerPeriod().getAcquired(),
                response.getItems().get(5).getPerPeriod().getAcquired(),
                response.getItems().get(6).getPerPeriod().getAcquired()));
        assertThat(response.getAttachment().getIncoming()).isEqualTo(GamaMoneyUtils.total(
                response.getItems().get(0).getPerPeriod().getIncoming(),
                response.getItems().get(1).getPerPeriod().getIncoming(),
                response.getItems().get(2).getPerPeriod().getIncoming(),
                response.getItems().get(3).getPerPeriod().getIncoming(),
                response.getItems().get(4).getPerPeriod().getIncoming(),
                response.getItems().get(5).getPerPeriod().getIncoming(),
                response.getItems().get(6).getPerPeriod().getIncoming()));
        assertThat(response.getAttachment().getBeginning()).isEqualTo(GamaMoneyUtils.total(
                response.getItems().get(0).getPerPeriod().getBeginning(),
                response.getItems().get(1).getPerPeriod().getBeginning(),
                response.getItems().get(2).getPerPeriod().getBeginning(),
                response.getItems().get(3).getPerPeriod().getBeginning(),
                response.getItems().get(4).getPerPeriod().getBeginning(),
                response.getItems().get(5).getPerPeriod().getBeginning(),
                response.getItems().get(6).getPerPeriod().getBeginning()));
        assertThat(response.getAttachment().getDtValue()).isEqualTo(GamaMoneyUtils.total(
                response.getItems().get(0).getPerPeriod().getDtValue(),
                response.getItems().get(1).getPerPeriod().getDtValue(),
                response.getItems().get(2).getPerPeriod().getDtValue(),
                response.getItems().get(3).getPerPeriod().getDtValue(),
                response.getItems().get(4).getPerPeriod().getDtValue(),
                response.getItems().get(5).getPerPeriod().getDtValue(),
                response.getItems().get(6).getPerPeriod().getDtValue()));
        assertThat(response.getAttachment().getDtExpense()).isEqualTo(GamaMoneyUtils.total(
                response.getItems().get(0).getPerPeriod().getDtExpense(),
                response.getItems().get(1).getPerPeriod().getDtExpense(),
                response.getItems().get(2).getPerPeriod().getDtExpense(),
                response.getItems().get(3).getPerPeriod().getDtExpense(),
                response.getItems().get(4).getPerPeriod().getDtExpense(),
                response.getItems().get(5).getPerPeriod().getDtExpense(),
                response.getItems().get(6).getPerPeriod().getDtExpense()));
        assertThat(response.getAttachment().getExpense()).isEqualTo(GamaMoneyUtils.total(
                response.getItems().get(0).getPerPeriod().getExpense(),
                response.getItems().get(1).getPerPeriod().getExpense(),
                response.getItems().get(2).getPerPeriod().getExpense(),
                response.getItems().get(3).getPerPeriod().getExpense(),
                response.getItems().get(4).getPerPeriod().getExpense(),
                response.getItems().get(5).getPerPeriod().getExpense(),
                response.getItems().get(6).getPerPeriod().getExpense()));
        assertThat(response.getAttachment().getEnding()).isEqualTo(GamaMoneyUtils.total(
                response.getItems().get(0).getPerPeriod().getEnding(),
                response.getItems().get(1).getPerPeriod().getEnding(),
                response.getItems().get(2).getPerPeriod().getEnding(),
                response.getItems().get(3).getPerPeriod().getEnding(),
                response.getItems().get(4).getPerPeriod().getEnding(),
                response.getItems().get(5).getPerPeriod().getEnding(),
                response.getItems().get(6).getPerPeriod().getEnding()));
        assertThat(response.getAttachment().getDepreciation()).isEqualTo(GamaMoneyUtils.total(
                response.getItems().get(0).getPerPeriod().getDepreciation(),
                response.getItems().get(1).getPerPeriod().getDepreciation(),
                response.getItems().get(2).getPerPeriod().getDepreciation(),
                response.getItems().get(3).getPerPeriod().getDepreciation(),
                response.getItems().get(4).getPerPeriod().getDepreciation(),
                response.getItems().get(5).getPerPeriod().getDepreciation(),
                response.getItems().get(6).getPerPeriod().getDepreciation()));
        checkValue(response.getAttachment());
    }

    @Test
    void testListApiByDateAfter() throws GamaApiException {
        final LocalDate DATE = LocalDate.parse("2016-03-01");
        PageRequest request = new PageRequest();
        request.setConditions(new ArrayList<>());
        request.getConditions().add(new PageRequestCondition(CustomSearchType.DATE, DATE));

        APIResult<PageResponse<AssetDto, AssetTotal>> result = assetApi.listAsset(request);
        assertThat(result.getError()).isNull();

        PageResponse<AssetDto, AssetTotal> response = result.getData();
        assertThat(response.getTotal()).isEqualTo(6);
        assertThat(response.getItems().get(0).getName()).isEqualTo("Asset A");
        assertThat(response.getItems().get(0).getStatus()).isEqualTo(AssetStatusType.OPERATING);
        assertThat(response.getItems().get(0).getPerPeriod().getDateFrom()).isEqualTo(DATE);
        assertThat(response.getItems().get(0).getPerPeriod().getDateTo()).isEqualTo(DATE.with(lastDayOfMonth()));
        assertThat(response.getItems().get(0).getPerPeriod().getStatus()).isEqualTo(AssetStatusType.OPERATING);
        assertThat(response.getItems().get(0).getDepreciation().stream().filter(d -> d.getDate().equals(DATE)).findFirst().orElseThrow())
                .isEqualTo(new Depreciation(DATE)
                        .setBeginning(GamaMoney.parse("EUR 13097.12"))
                        .setExpense(GamaMoney.parse("EUR 569.44"))
                        .setEnding(GamaMoney.parse("EUR 12527.68"))
                        .setDepreciation(GamaMoney.parse("EUR 10972.32")));
        assertThat(response.getItems().get(0).getPerPeriod().getAcquired()).isEqualTo(null);
        assertThat(response.getItems().get(0).getPerPeriod().getIncoming()).isEqualTo(null);
        assertThat(response.getItems().get(0).getPerPeriod().getBeginning()).isEqualTo(GamaMoney.parse("EUR 13097.12"));
        assertThat(response.getItems().get(0).getPerPeriod().getDtValue()).isEqualTo(null);
        assertThat(response.getItems().get(0).getPerPeriod().getDtExpense()).isEqualTo(null);
        assertThat(response.getItems().get(0).getPerPeriod().getExpense()).isEqualTo(GamaMoney.parse("EUR 569.44"));
        assertThat(response.getItems().get(0).getPerPeriod().getEnding()).isEqualTo(GamaMoney.parse("EUR 12527.68"));
        assertThat(response.getItems().get(0).getPerPeriod().getDepreciation()).isEqualTo(GamaMoney.parse("EUR 10972.32"));
        checkValue(response.getItems().get(0).getPerPeriod());

        assertThat(response.getItems().get(1).getName()).isEqualTo("Asset B");
        assertThat(response.getItems().get(1).getStatus()).isEqualTo(AssetStatusType.OPERATING);
        assertThat(response.getItems().get(1).getPerPeriod().getDateFrom()).isEqualTo(DATE);
        assertThat(response.getItems().get(1).getPerPeriod().getDateTo()).isEqualTo(DATE.with(lastDayOfMonth()));
        assertThat(response.getItems().get(1).getPerPeriod().getStatus()).isEqualTo(AssetStatusType.OPERATING);
        assertThat(response.getItems().get(1).getDepreciation().stream().filter(d -> d.getDate().equals(DATE)).findFirst().orElseThrow())
                .isEqualTo(new Depreciation(DATE)
                        .setBeginning(GamaMoney.parse("EUR 11199.44"))
                        .setExpense(GamaMoney.parse("EUR 399.98"))
                        .setEnding(GamaMoney.parse("EUR 10799.46"))
                        .setDepreciation(GamaMoney.parse("EUR 11300.54")));
        assertThat(response.getItems().get(1).getPerPeriod().getAcquired()).isEqualTo(null);
        assertThat(response.getItems().get(1).getPerPeriod().getIncoming()).isEqualTo(null);
        assertThat(response.getItems().get(1).getPerPeriod().getBeginning()).isEqualTo(GamaMoney.parse("EUR 11199.44"));
        assertThat(response.getItems().get(1).getPerPeriod().getDtValue()).isEqualTo(null);
        assertThat(response.getItems().get(1).getPerPeriod().getDtExpense()).isEqualTo(null);
        assertThat(response.getItems().get(1).getPerPeriod().getExpense()).isEqualTo(GamaMoney.parse("EUR 399.98"));
        assertThat(response.getItems().get(1).getPerPeriod().getEnding()).isEqualTo(GamaMoney.parse("EUR 10799.46"));
        assertThat(response.getItems().get(1).getPerPeriod().getDepreciation()).isEqualTo(GamaMoney.parse("EUR 11300.54"));
        checkValue(response.getItems().get(1).getPerPeriod());

        assertThat(response.getItems().get(2).getName()).isEqualTo("Asset C");
        assertThat(response.getItems().get(2).getStatus()).isEqualTo(AssetStatusType.OPERATING);
        assertThat(response.getItems().get(2).getPerPeriod().getDateFrom()).isEqualTo(DATE);
        assertThat(response.getItems().get(2).getPerPeriod().getDateTo()).isEqualTo(DATE.with(lastDayOfMonth()));
        assertThat(response.getItems().get(2).getPerPeriod().getStatus()).isEqualTo(AssetStatusType.OPERATING);
        assertThat(response.getItems().get(2).getDepreciation().stream().filter(d -> d.getDate().equals(DATE)).findFirst()).isEqualTo(Optional.empty());
        assertThat(response.getItems().get(2).getPerPeriod().getAcquired()).isEqualTo(null);
        assertThat(response.getItems().get(2).getPerPeriod().getIncoming()).isEqualTo(null);
        assertThat(response.getItems().get(2).getPerPeriod().getBeginning()).isEqualTo(GamaMoney.parse("EUR 11999.40"));
        assertThat(response.getItems().get(2).getPerPeriod().getDtValue()).isEqualTo(null);
        assertThat(response.getItems().get(2).getPerPeriod().getDtExpense()).isEqualTo(null);
        assertThat(response.getItems().get(2).getPerPeriod().getExpense()).isEqualTo(null);
        assertThat(response.getItems().get(2).getPerPeriod().getEnding()).isEqualTo(GamaMoney.parse("EUR 11999.40"));
        assertThat(response.getItems().get(2).getPerPeriod().getDepreciation()).isEqualTo(GamaMoney.parse("EUR 10100.60"));
        checkValue(response.getItems().get(2).getPerPeriod());

        assertThat(response.getItems().get(3).getName()).isEqualTo("Asset E");
        assertThat(response.getItems().get(3).getStatus()).isEqualTo(AssetStatusType.CONSERVED);
        assertThat(response.getItems().get(3).getPerPeriod().getDateFrom()).isEqualTo(DATE);
        assertThat(response.getItems().get(3).getPerPeriod().getDateTo()).isEqualTo(DATE.with(lastDayOfMonth()));
        assertThat(response.getItems().get(3).getPerPeriod().getStatus()).isEqualTo(AssetStatusType.CONSERVED);
        assertThat(response.getItems().get(3).getDepreciation().stream().filter(d -> d.getDate().equals(DATE)).findFirst()).isEqualTo(Optional.empty());
        assertThat(response.getItems().get(3).getDepreciation().stream().filter(d -> d.getDate().equals(LocalDate.parse("2015-04-01"))).findFirst().orElseThrow())
                .isEqualTo(new Depreciation(LocalDate.parse("2015-04-01"))
                        .setBeginning(GamaMoney.parse("EUR 18544.56"))
                        .setEnding(GamaMoney.parse("EUR 18544.56"))
                        .setDepreciation(GamaMoney.parse("EUR 3555.44")));
        assertThat(response.getItems().get(3).getPerPeriod().getDateFrom()).isEqualTo(DATE);
        assertThat(response.getItems().get(3).getPerPeriod().getDateTo()).isEqualTo(DATE.with(lastDayOfMonth()));
        assertThat(response.getItems().get(3).getPerPeriod().getStatus()).isEqualTo(AssetStatusType.CONSERVED);
        assertThat(response.getItems().get(3).getPerPeriod().getAcquired()).isEqualTo(null);
        assertThat(response.getItems().get(3).getPerPeriod().getIncoming()).isEqualTo(null);
        assertThat(response.getItems().get(3).getPerPeriod().getBeginning()).isEqualTo(GamaMoney.parse("EUR 18544.56"));
        assertThat(response.getItems().get(3).getPerPeriod().getDtValue()).isEqualTo(null);
        assertThat(response.getItems().get(3).getPerPeriod().getDtExpense()).isEqualTo(null);
        assertThat(response.getItems().get(3).getPerPeriod().getExpense()).isEqualTo(null);
        assertThat(response.getItems().get(3).getPerPeriod().getEnding()).isEqualTo(GamaMoney.parse("EUR 18544.56"));
        assertThat(response.getItems().get(3).getPerPeriod().getDepreciation()).isEqualTo(GamaMoney.parse("EUR 3555.44"));
        checkValue(response.getItems().get(3).getPerPeriod());

        assertThat(response.getItems().get(4).getName()).isEqualTo("Asset F");
        assertThat(response.getItems().get(4).getStatus()).isEqualTo(AssetStatusType.OPERATING);
        assertThat(response.getItems().get(4).getPerPeriod().getDateFrom()).isEqualTo(DATE);
        assertThat(response.getItems().get(4).getPerPeriod().getDateTo()).isEqualTo(DATE.with(lastDayOfMonth()));
        assertThat(response.getItems().get(4).getPerPeriod().getStatus()).isEqualTo(AssetStatusType.OPERATING);
        assertThat(response.getItems().get(4).getDepreciation().stream().filter(d -> d.getDate().equals(DATE)).findFirst()).isEqualTo(Optional.empty());
        assertThat(response.getItems().get(4).getPerPeriod().getDateFrom()).isEqualTo(DATE);
        assertThat(response.getItems().get(4).getPerPeriod().getDateTo()).isEqualTo(DATE.with(lastDayOfMonth()));
        assertThat(response.getItems().get(4).getPerPeriod().getStatus()).isEqualTo(AssetStatusType.OPERATING);
        assertThat(response.getItems().get(4).getPerPeriod().getAcquired()).isEqualTo(null);
        assertThat(response.getItems().get(4).getPerPeriod().getIncoming()).isEqualTo(null);
        assertThat(response.getItems().get(4).getPerPeriod().getBeginning()).isEqualTo(GamaMoney.parse("EUR 1.00"));
        assertThat(response.getItems().get(4).getPerPeriod().getDtValue()).isEqualTo(null);
        assertThat(response.getItems().get(4).getPerPeriod().getDtExpense()).isEqualTo(null);
        assertThat(response.getItems().get(4).getPerPeriod().getExpense()).isEqualTo(null);
        assertThat(response.getItems().get(4).getPerPeriod().getEnding()).isEqualTo(GamaMoney.parse("EUR 1.00"));
        assertThat(response.getItems().get(4).getPerPeriod().getDepreciation()).isEqualTo(GamaMoney.parse("EUR 19999.00"));
        checkValue(response.getItems().get(4).getPerPeriod());

        assertThat(response.getItems().get(5).getName()).isEqualTo("Asset G");
        assertThat(response.getItems().get(5).getStatus()).isEqualTo(AssetStatusType.CONSERVED);
        assertThat(response.getItems().get(5).getPerPeriod().getDateFrom()).isEqualTo(DATE);
        assertThat(response.getItems().get(5).getPerPeriod().getDateTo()).isEqualTo(DATE.with(lastDayOfMonth()));
        assertThat(response.getItems().get(5).getPerPeriod().getStatus()).isEqualTo(AssetStatusType.CONSERVED);
        assertThat(response.getItems().get(5).getDepreciation().stream().filter(d -> d.getDate().equals(DATE)).findFirst()).isEqualTo(Optional.empty());
        assertThat(response.getItems().get(5).getDepreciation().stream().filter(d -> d.getDate().equals(LocalDate.parse("2014-02-01"))).findFirst().orElseThrow())
                .isEqualTo(new Depreciation(LocalDate.parse("2014-02-01"))
                        .setBeginning(GamaMoney.parse("EUR 1000.00"))
                        .setEnding(GamaMoney.parse("EUR 1000.00"))
                        .setDepreciation(GamaMoney.parse("EUR 19000.00")));
        assertThat(response.getItems().get(5).getPerPeriod().getDateFrom()).isEqualTo(DATE);
        assertThat(response.getItems().get(5).getPerPeriod().getDateTo()).isEqualTo(DATE.with(lastDayOfMonth()));
        assertThat(response.getItems().get(5).getPerPeriod().getStatus()).isEqualTo(AssetStatusType.CONSERVED);
        assertThat(response.getItems().get(5).getPerPeriod().getAcquired()).isEqualTo(null);
        assertThat(response.getItems().get(5).getPerPeriod().getIncoming()).isEqualTo(null);
        assertThat(response.getItems().get(5).getPerPeriod().getBeginning()).isEqualTo(GamaMoney.parse("EUR 1000.00"));
        assertThat(response.getItems().get(5).getPerPeriod().getDtValue()).isEqualTo(null);
        assertThat(response.getItems().get(5).getPerPeriod().getDtExpense()).isEqualTo(null);
        assertThat(response.getItems().get(5).getPerPeriod().getExpense()).isEqualTo(null);
        assertThat(response.getItems().get(5).getPerPeriod().getEnding()).isEqualTo(GamaMoney.parse("EUR 1000.00"));
        assertThat(response.getItems().get(5).getPerPeriod().getDepreciation()).isEqualTo(GamaMoney.parse("EUR 19000.00"));
        checkValue(response.getItems().get(5).getPerPeriod());

        assertThat(response.getAttachment().getDateFrom()).isEqualTo(DATE);
        assertThat(response.getAttachment().getDateTo()).isEqualTo(DATE.with(lastDayOfMonth()));
        assertThat(response.getAttachment().getAcquired()).isEqualTo(GamaMoneyUtils.total(
                response.getItems().get(0).getPerPeriod().getAcquired(),
                response.getItems().get(1).getPerPeriod().getAcquired(),
                response.getItems().get(2).getPerPeriod().getAcquired(),
                response.getItems().get(3).getPerPeriod().getAcquired(),
                response.getItems().get(4).getPerPeriod().getAcquired(),
                response.getItems().get(5).getPerPeriod().getAcquired()));
        assertThat(response.getAttachment().getIncoming()).isEqualTo(GamaMoneyUtils.total(
                response.getItems().get(0).getPerPeriod().getIncoming(),
                response.getItems().get(1).getPerPeriod().getIncoming(),
                response.getItems().get(2).getPerPeriod().getIncoming(),
                response.getItems().get(3).getPerPeriod().getIncoming(),
                response.getItems().get(4).getPerPeriod().getIncoming(),
                response.getItems().get(5).getPerPeriod().getIncoming()));
        assertThat(response.getAttachment().getBeginning()).isEqualTo(GamaMoneyUtils.total(
                response.getItems().get(0).getPerPeriod().getBeginning(),
                response.getItems().get(1).getPerPeriod().getBeginning(),
                response.getItems().get(2).getPerPeriod().getBeginning(),
                response.getItems().get(3).getPerPeriod().getBeginning(),
                response.getItems().get(4).getPerPeriod().getBeginning(),
                response.getItems().get(5).getPerPeriod().getBeginning()));
        assertThat(response.getAttachment().getDtValue()).isEqualTo(GamaMoneyUtils.total(
                response.getItems().get(0).getPerPeriod().getDtValue(),
                response.getItems().get(1).getPerPeriod().getDtValue(),
                response.getItems().get(2).getPerPeriod().getDtValue(),
                response.getItems().get(3).getPerPeriod().getDtValue(),
                response.getItems().get(4).getPerPeriod().getDtValue(),
                response.getItems().get(5).getPerPeriod().getDtValue()));
        assertThat(response.getAttachment().getDtExpense()).isEqualTo(GamaMoneyUtils.total(
                response.getItems().get(0).getPerPeriod().getDtExpense(),
                response.getItems().get(1).getPerPeriod().getDtExpense(),
                response.getItems().get(2).getPerPeriod().getDtExpense(),
                response.getItems().get(3).getPerPeriod().getDtExpense(),
                response.getItems().get(4).getPerPeriod().getDtExpense(),
                response.getItems().get(5).getPerPeriod().getDtExpense()));
        assertThat(response.getAttachment().getExpense()).isEqualTo(GamaMoneyUtils.total(
                response.getItems().get(0).getPerPeriod().getExpense(),
                response.getItems().get(1).getPerPeriod().getExpense(),
                response.getItems().get(2).getPerPeriod().getExpense(),
                response.getItems().get(3).getPerPeriod().getExpense(),
                response.getItems().get(4).getPerPeriod().getExpense(),
                response.getItems().get(5).getPerPeriod().getExpense()));
        assertThat(response.getAttachment().getEnding()).isEqualTo(GamaMoneyUtils.total(
                response.getItems().get(0).getPerPeriod().getEnding(),
                response.getItems().get(1).getPerPeriod().getEnding(),
                response.getItems().get(2).getPerPeriod().getEnding(),
                response.getItems().get(3).getPerPeriod().getEnding(),
                response.getItems().get(4).getPerPeriod().getEnding(),
                response.getItems().get(5).getPerPeriod().getEnding()));
        assertThat(response.getAttachment().getDepreciation()).isEqualTo(GamaMoneyUtils.total(
                response.getItems().get(0).getPerPeriod().getDepreciation(),
                response.getItems().get(1).getPerPeriod().getDepreciation(),
                response.getItems().get(2).getPerPeriod().getDepreciation(),
                response.getItems().get(3).getPerPeriod().getDepreciation(),
                response.getItems().get(4).getPerPeriod().getDepreciation(),
                response.getItems().get(5).getPerPeriod().getDepreciation()));
        checkValue(response.getAttachment());
    }

    @Test
    void testListPeriod() throws GamaApiException {
        final LocalDate DATE_FROM = LocalDate.parse("2015-02-01");
        final LocalDate DATE_TO = LocalDate.parse("2015-05-01");

        PageRequest request = new PageRequest();
        request.setDateFrom(DATE_FROM);
        request.setDateTo(DATE_TO);

        APIResult<PageResponse<AssetDto, AssetTotal>> result = assetApi.periodListAsset(request);
        assertThat(result.getError()).isNull();

        PageResponse<AssetDto, AssetTotal> response = result.getData();
        assertThat(response.getTotal()).isEqualTo(7);

        assertThat(response.getItems().get(0).getName()).isEqualTo("Asset A");
        assertThat(response.getItems().get(0).getStatus()).isEqualTo(AssetStatusType.OPERATING);
        assertThat(response.getItems().get(0).getDepreciation().stream().filter(d -> d.getDate().equals(DATE_FROM)).findFirst().orElseThrow())
                .isEqualTo(new Depreciation(DATE_FROM)
                        .setBeginning(GamaMoney.parse("EUR 20000.00"))
                        .setDtValue(GamaMoney.parse("EUR 1500.00"))
                        .setDtExpense(GamaMoney.parse("EUR 1000.00"))
                        .setExpense(GamaMoney.parse("EUR 569.60"))
                        .setEnding(GamaMoney.parse("EUR 19930.40"))
                        .setDepreciation(GamaMoney.parse("EUR 3569.60")));
        assertThat(response.getItems().get(0).getDepreciation().stream().filter(d -> d.getDate().equals(DATE_TO)).findFirst().orElseThrow())
                .isEqualTo(new Depreciation(DATE_TO)
                        .setBeginning(GamaMoney.parse("EUR 18791.52"))
                        .setExpense(GamaMoney.parse("EUR 569.44"))
                        .setEnding(GamaMoney.parse("EUR 18222.08"))
                        .setDepreciation(GamaMoney.parse("EUR 5277.92")));
        assertThat(response.getItems().get(0).getPerPeriod().getDateFrom()).isEqualTo(DATE_FROM);
        assertThat(response.getItems().get(0).getPerPeriod().getDateTo()).isEqualTo(DATE_TO.with(lastDayOfMonth()));
        assertThat(response.getItems().get(0).getPerPeriod().getStatus()).isEqualTo(AssetStatusType.OPERATING);
        assertThat(response.getItems().get(0).getPerPeriod().getAcquired()).isEqualTo(null);
        assertThat(response.getItems().get(0).getPerPeriod().getIncoming()).isEqualTo(GamaMoney.parse("EUR 20000.00"));
        assertThat(response.getItems().get(0).getPerPeriod().getBeginning()).isEqualTo(null);
        assertThat(response.getItems().get(0).getPerPeriod().getDtValue()).isEqualTo(GamaMoney.parse("EUR 1500.00"));
        assertThat(response.getItems().get(0).getPerPeriod().getDtExpense()).isEqualTo(GamaMoney.parse("EUR 1000.00"));
        assertThat(response.getItems().get(0).getPerPeriod().getExpense()).isEqualTo(GamaMoney.parse("EUR 2277.92"));
        assertThat(response.getItems().get(0).getPerPeriod().getEnding()).isEqualTo(GamaMoney.parse("EUR 18222.08"));
        assertThat(response.getItems().get(0).getPerPeriod().getDepreciation()).isEqualTo(GamaMoney.parse("EUR 5277.92"));
        checkValue(response.getItems().get(0).getPerPeriod());

        assertThat(response.getItems().get(1).getName()).isEqualTo("Asset B");
        assertThat(response.getItems().get(1).getStatus()).isEqualTo(AssetStatusType.OPERATING);
        assertThat(response.getItems().get(1).getDepreciation().stream().filter(d -> d.getDate().equals(DATE_FROM)).findFirst().orElseThrow())
                .isEqualTo(new Depreciation(DATE_FROM)
                        .setBeginning(GamaMoney.parse("EUR 19332.33"))
                        .setExpense(GamaMoney.parse("EUR 666.63"))
                        .setEnding(GamaMoney.parse("EUR 18665.70"))
                        .setDepreciation(GamaMoney.parse("EUR 3434.30")));
        assertThat(response.getItems().get(1).getDepreciation().stream().filter(d -> d.getDate().equals(DATE_TO)).findFirst().orElseThrow())
                .isEqualTo(new Depreciation(DATE_TO)
                        .setBeginning(GamaMoney.parse("EUR 17332.44"))
                        .setExpense(GamaMoney.parse("EUR 666.63"))
                        .setEnding(GamaMoney.parse("EUR 16665.81"))
                        .setDepreciation(GamaMoney.parse("EUR 5434.19")));
        assertThat(response.getItems().get(1).getPerPeriod().getDateFrom()).isEqualTo(DATE_FROM);
        assertThat(response.getItems().get(1).getPerPeriod().getDateTo()).isEqualTo(DATE_TO.with(lastDayOfMonth()));
        assertThat(response.getItems().get(1).getPerPeriod().getStatus()).isEqualTo(AssetStatusType.OPERATING);
        assertThat(response.getItems().get(1).getPerPeriod().getAcquired()).isEqualTo(null);
        assertThat(response.getItems().get(1).getPerPeriod().getIncoming()).isEqualTo(null);
        assertThat(response.getItems().get(1).getPerPeriod().getBeginning()).isEqualTo(GamaMoney.parse("EUR 19332.33"));
        assertThat(response.getItems().get(1).getPerPeriod().getDtValue()).isEqualTo(null);
        assertThat(response.getItems().get(1).getPerPeriod().getDtExpense()).isEqualTo(null);
        assertThat(response.getItems().get(1).getPerPeriod().getExpense()).isEqualTo(GamaMoney.parse("EUR 2666.52"));
        assertThat(response.getItems().get(1).getPerPeriod().getEnding()).isEqualTo(GamaMoney.parse("EUR 16665.81"));
        assertThat(response.getItems().get(1).getPerPeriod().getDepreciation()).isEqualTo(GamaMoney.parse("EUR 5434.19"));
        checkValue(response.getItems().get(1).getPerPeriod());

        assertThat(response.getItems().get(2).getName()).isEqualTo("Asset C");
        assertThat(response.getItems().get(2).getStatus()).isEqualTo(AssetStatusType.OPERATING);
        assertThat(response.getItems().get(2).getDepreciation().stream().filter(d -> d.getDate().equals(DATE_FROM)).findFirst()).isEqualTo(Optional.empty());
        assertThat(response.getItems().get(2).getDepreciation().stream().filter(d -> d.getDate().equals(DATE_TO)).findFirst().orElseThrow())
                .isEqualTo(new Depreciation(DATE_TO)
                .setBeginning(GamaMoney.parse("EUR 19110.12"))
                .setExpense(GamaMoney.parse("EUR 888.84"))
                .setEnding(GamaMoney.parse("EUR 18221.28"))
                .setDepreciation(GamaMoney.parse("EUR 3878.72")));
        assertThat(response.getItems().get(2).getPerPeriod().getDateFrom()).isEqualTo(DATE_FROM);
        assertThat(response.getItems().get(2).getPerPeriod().getDateTo()).isEqualTo(DATE_TO.with(lastDayOfMonth()));
        assertThat(response.getItems().get(2).getPerPeriod().getStatus()).isEqualTo(AssetStatusType.OPERATING);
        assertThat(response.getItems().get(2).getPerPeriod().getAcquired()).isEqualTo(null);
        assertThat(response.getItems().get(2).getPerPeriod().getIncoming()).isEqualTo(GamaMoney.parse("EUR 20099.00"));
        assertThat(response.getItems().get(2).getPerPeriod().getBeginning()).isEqualTo(null);
        assertThat(response.getItems().get(2).getPerPeriod().getDtValue()).isEqualTo(GamaMoney.parse("EUR 100.00"));
        assertThat(response.getItems().get(2).getPerPeriod().getDtExpense()).isEqualTo(GamaMoney.parse("EUR 200.00"));
        assertThat(response.getItems().get(2).getPerPeriod().getExpense()).isEqualTo(GamaMoney.parse("EUR 1777.72"));
        assertThat(response.getItems().get(2).getPerPeriod().getEnding()).isEqualTo(GamaMoney.parse("EUR 18221.28"));
        assertThat(response.getItems().get(2).getPerPeriod().getDepreciation()).isEqualTo(GamaMoney.parse("EUR 3878.72"));
        checkValue(response.getItems().get(2).getPerPeriod());

        assertThat(response.getItems().get(3).getName()).isEqualTo("Asset D abc");
        assertThat(response.getItems().get(3).getStatus()).isEqualTo(AssetStatusType.WRITTEN_OFF);
        assertThat(response.getItems().get(3).getDepreciation().stream().filter(d -> d.getDate().equals(DATE_FROM)).findFirst().orElseThrow())
                .isEqualTo(new Depreciation(DATE_FROM)
                        .setBeginning(GamaMoney.parse("EUR 20099.00"))
                        .setDtValue(GamaMoney.parse("EUR 100.00"))
                        .setDtExpense(GamaMoney.parse("EUR 200.00"))
                        .setExpense(GamaMoney.parse("EUR 727.20"))
                        .setEnding(GamaMoney.parse("EUR 19271.80"))
                        .setDepreciation(GamaMoney.parse("EUR 2828.20")));
        assertThat(response.getItems().get(3).getDepreciation().stream().filter(d -> d.getDate().equals(DATE_TO)).findFirst().orElseThrow())
                .isEqualTo(new Depreciation(DATE_TO)
                        .setBeginning(GamaMoney.parse("EUR 17817.32"))
                        .setExpense(GamaMoney.parse("EUR 727.24"))
                        .setEnding(GamaMoney.parse("EUR 17090.08"))
                        .setDepreciation(GamaMoney.parse("EUR 5009.92")));
        assertThat(response.getItems().get(3).getPerPeriod().getDateFrom()).isEqualTo(DATE_FROM);
        assertThat(response.getItems().get(3).getPerPeriod().getDateTo()).isEqualTo(DATE_TO.with(lastDayOfMonth()));
        assertThat(response.getItems().get(3).getPerPeriod().getStatus()).isEqualTo(AssetStatusType.OPERATING);
        assertThat(response.getItems().get(3).getPerPeriod().getAcquired()).isEqualTo(null);
        assertThat(response.getItems().get(3).getPerPeriod().getIncoming()).isEqualTo(GamaMoney.parse("EUR 20099.00"));
        assertThat(response.getItems().get(3).getPerPeriod().getBeginning()).isEqualTo(null);
        assertThat(response.getItems().get(3).getPerPeriod().getDtValue()).isEqualTo(GamaMoney.parse("EUR 100.00"));
        assertThat(response.getItems().get(3).getPerPeriod().getDtExpense()).isEqualTo(GamaMoney.parse("EUR 200.00"));
        assertThat(response.getItems().get(3).getPerPeriod().getExpense()).isEqualTo(GamaMoney.parse("EUR 2908.92"));
        assertThat(response.getItems().get(3).getPerPeriod().getEnding()).isEqualTo(GamaMoney.parse("EUR 17090.08"));
        assertThat(response.getItems().get(3).getPerPeriod().getDepreciation()).isEqualTo(GamaMoney.parse("EUR 5009.92"));
        checkValue(response.getItems().get(3).getPerPeriod());

        assertThat(response.getItems().get(4).getName()).isEqualTo("Asset E");
        assertThat(response.getItems().get(4).getStatus()).isEqualTo(AssetStatusType.CONSERVED);
        assertThat(response.getItems().get(4).getDepreciation().stream().filter(d -> d.getDate().equals(DATE_FROM)).findFirst().orElseThrow())
                .isEqualTo(new Depreciation(DATE_FROM)
                        .setBeginning(GamaMoney.parse("EUR 20099.00"))
                        .setDtValue(GamaMoney.parse("EUR 100.00"))
                        .setDtExpense(GamaMoney.parse("EUR 200.00"))
                        .setExpense(GamaMoney.parse("EUR 727.20"))
                        .setEnding(GamaMoney.parse("EUR 19271.80"))
                        .setDepreciation(GamaMoney.parse("EUR 2828.20")));
        assertThat(response.getItems().get(4).getDepreciation().stream().filter(d -> d.getDate().equals(LocalDate.parse("2015-04-01"))).findFirst().orElseThrow())
                .isEqualTo(new Depreciation(LocalDate.parse("2015-04-01"))
                        .setBeginning(GamaMoney.parse("EUR 18544.56"))
                        .setEnding(GamaMoney.parse("EUR 18544.56"))
                        .setDepreciation(GamaMoney.parse("EUR 3555.44")));
        assertThat(response.getItems().get(4).getDepreciation().stream().filter(d -> d.getDate().equals(DATE_TO)).findFirst()).isEqualTo(Optional.empty());
        assertThat(response.getItems().get(4).getPerPeriod().getDateFrom()).isEqualTo(DATE_FROM);
        assertThat(response.getItems().get(4).getPerPeriod().getDateTo()).isEqualTo(DATE_TO.with(lastDayOfMonth()));
        assertThat(response.getItems().get(4).getPerPeriod().getStatus()).isEqualTo(AssetStatusType.CONSERVED);
        assertThat(response.getItems().get(4).getPerPeriod().getAcquired()).isEqualTo(null);
        assertThat(response.getItems().get(4).getPerPeriod().getIncoming()).isEqualTo(GamaMoney.parse("EUR 20099.00"));
        assertThat(response.getItems().get(4).getPerPeriod().getBeginning()).isEqualTo(null);
        assertThat(response.getItems().get(4).getPerPeriod().getDtValue()).isEqualTo(GamaMoney.parse("EUR 100.00"));
        assertThat(response.getItems().get(4).getPerPeriod().getDtExpense()).isEqualTo(GamaMoney.parse("EUR 200.00"));
        assertThat(response.getItems().get(4).getPerPeriod().getExpense()).isEqualTo(GamaMoney.parse("EUR 1454.44"));
        assertThat(response.getItems().get(4).getPerPeriod().getEnding()).isEqualTo(GamaMoney.parse("EUR 18544.56"));
        assertThat(response.getItems().get(4).getPerPeriod().getDepreciation()).isEqualTo(GamaMoney.parse("EUR 3555.44"));
        checkValue(response.getItems().get(4).getPerPeriod());

        assertThat(response.getItems().get(5).getName()).isEqualTo("Asset F");
        assertThat(response.getItems().get(5).getStatus()).isEqualTo(AssetStatusType.OPERATING);
        assertThat(response.getItems().get(5).getDepreciation().size()).isEqualTo(0);
        assertThat(response.getItems().get(5).getPerPeriod().getDateFrom()).isEqualTo(DATE_FROM);
        assertThat(response.getItems().get(5).getPerPeriod().getDateTo()).isEqualTo(DATE_TO.with(lastDayOfMonth()));
        assertThat(response.getItems().get(5).getPerPeriod().getStatus()).isEqualTo(AssetStatusType.OPERATING);
        assertThat(response.getItems().get(5).getPerPeriod().getAcquired()).isEqualTo(null);
        assertThat(response.getItems().get(5).getPerPeriod().getIncoming()).isEqualTo(null);
        assertThat(response.getItems().get(5).getPerPeriod().getBeginning()).isEqualTo(GamaMoney.parse("EUR 1.00"));
        assertThat(response.getItems().get(5).getPerPeriod().getDtValue()).isEqualTo(null);
        assertThat(response.getItems().get(5).getPerPeriod().getDtExpense()).isEqualTo(null);
        assertThat(response.getItems().get(5).getPerPeriod().getExpense()).isEqualTo(null);
        assertThat(response.getItems().get(5).getPerPeriod().getEnding()).isEqualTo(GamaMoney.parse("EUR 1.00"));
        assertThat(response.getItems().get(5).getPerPeriod().getDepreciation()).isEqualTo(GamaMoney.parse("EUR 19999.00"));
        checkValue(response.getItems().get(5).getPerPeriod());

        assertThat(response.getItems().get(6).getName()).isEqualTo("Asset G");
        assertThat(response.getItems().get(6).getStatus()).isEqualTo(AssetStatusType.CONSERVED);
        assertThat(response.getItems().get(6).getDepreciation().stream().filter(d -> d.getDate().equals(DATE_TO)).findFirst()).isEqualTo(Optional.empty());
        assertThat(response.getItems().get(6).getDepreciation().stream().filter(d -> d.getDate().equals(LocalDate.parse("2014-02-01"))).findFirst().orElseThrow())
                .isEqualTo(new Depreciation(LocalDate.parse("2014-02-01"))
                        .setBeginning(GamaMoney.parse("EUR 1000.00"))
                        .setEnding(GamaMoney.parse("EUR 1000.00"))
                        .setDepreciation(GamaMoney.parse("EUR 19000.00")));
        assertThat(response.getItems().get(6).getPerPeriod().getDateFrom()).isEqualTo(DATE_FROM);
        assertThat(response.getItems().get(6).getPerPeriod().getDateTo()).isEqualTo(DATE_TO.with(lastDayOfMonth()));
        assertThat(response.getItems().get(6).getPerPeriod().getStatus()).isEqualTo(AssetStatusType.CONSERVED);
        assertThat(response.getItems().get(6).getPerPeriod().getAcquired()).isEqualTo(null);
        assertThat(response.getItems().get(6).getPerPeriod().getIncoming()).isEqualTo(null);
        assertThat(response.getItems().get(6).getPerPeriod().getBeginning()).isEqualTo(GamaMoney.parse("EUR 1000.00"));
        assertThat(response.getItems().get(6).getPerPeriod().getDtValue()).isEqualTo(null);
        assertThat(response.getItems().get(6).getPerPeriod().getDtExpense()).isEqualTo(null);
        assertThat(response.getItems().get(6).getPerPeriod().getExpense()).isEqualTo(null);
        assertThat(response.getItems().get(6).getPerPeriod().getEnding()).isEqualTo(GamaMoney.parse("EUR 1000.00"));
        assertThat(response.getItems().get(6).getPerPeriod().getDepreciation()).isEqualTo(GamaMoney.parse("EUR 19000.00"));
        checkValue(response.getItems().get(6).getPerPeriod());

        assertThat(response.getAttachment().getDateFrom()).isEqualTo(DATE_FROM);
        assertThat(response.getAttachment().getDateTo()).isEqualTo(DATE_TO.with(lastDayOfMonth()));
        assertThat(response.getAttachment().getAcquired()).isEqualTo(GamaMoneyUtils.total(
                response.getItems().get(0).getPerPeriod().getAcquired(),
                response.getItems().get(1).getPerPeriod().getAcquired(),
                response.getItems().get(2).getPerPeriod().getAcquired(),
                response.getItems().get(3).getPerPeriod().getAcquired(),
                response.getItems().get(4).getPerPeriod().getAcquired(),
                response.getItems().get(5).getPerPeriod().getAcquired(),
                response.getItems().get(6).getPerPeriod().getAcquired()));
        assertThat(response.getAttachment().getIncoming()).isEqualTo(GamaMoneyUtils.total(
                response.getItems().get(0).getPerPeriod().getIncoming(),
                response.getItems().get(1).getPerPeriod().getIncoming(),
                response.getItems().get(2).getPerPeriod().getIncoming(),
                response.getItems().get(3).getPerPeriod().getIncoming(),
                response.getItems().get(4).getPerPeriod().getIncoming(),
                response.getItems().get(5).getPerPeriod().getIncoming(),
                response.getItems().get(6).getPerPeriod().getIncoming()));
        assertThat(response.getAttachment().getBeginning()).isEqualTo(GamaMoneyUtils.total(
                response.getItems().get(0).getPerPeriod().getBeginning(),
                response.getItems().get(1).getPerPeriod().getBeginning(),
                response.getItems().get(2).getPerPeriod().getBeginning(),
                response.getItems().get(3).getPerPeriod().getBeginning(),
                response.getItems().get(4).getPerPeriod().getBeginning(),
                response.getItems().get(5).getPerPeriod().getBeginning(),
                response.getItems().get(6).getPerPeriod().getBeginning()));
        assertThat(response.getAttachment().getDtValue()).isEqualTo(GamaMoneyUtils.total(
                response.getItems().get(0).getPerPeriod().getDtValue(),
                response.getItems().get(1).getPerPeriod().getDtValue(),
                response.getItems().get(2).getPerPeriod().getDtValue(),
                response.getItems().get(3).getPerPeriod().getDtValue(),
                response.getItems().get(4).getPerPeriod().getDtValue(),
                response.getItems().get(5).getPerPeriod().getDtValue(),
                response.getItems().get(6).getPerPeriod().getDtValue()));
        assertThat(response.getAttachment().getDtExpense()).isEqualTo(GamaMoneyUtils.total(
                response.getItems().get(0).getPerPeriod().getDtExpense(),
                response.getItems().get(1).getPerPeriod().getDtExpense(),
                response.getItems().get(2).getPerPeriod().getDtExpense(),
                response.getItems().get(3).getPerPeriod().getDtExpense(),
                response.getItems().get(4).getPerPeriod().getDtExpense(),
                response.getItems().get(5).getPerPeriod().getDtExpense(),
                response.getItems().get(6).getPerPeriod().getDtExpense()));
        assertThat(response.getAttachment().getExpense()).isEqualTo(GamaMoneyUtils.total(
                response.getItems().get(0).getPerPeriod().getExpense(),
                response.getItems().get(1).getPerPeriod().getExpense(),
                response.getItems().get(2).getPerPeriod().getExpense(),
                response.getItems().get(3).getPerPeriod().getExpense(),
                response.getItems().get(4).getPerPeriod().getExpense(),
                response.getItems().get(5).getPerPeriod().getExpense(),
                response.getItems().get(6).getPerPeriod().getExpense()));
        assertThat(response.getAttachment().getEnding()).isEqualTo(GamaMoneyUtils.total(
                response.getItems().get(0).getPerPeriod().getEnding(),
                response.getItems().get(1).getPerPeriod().getEnding(),
                response.getItems().get(2).getPerPeriod().getEnding(),
                response.getItems().get(3).getPerPeriod().getEnding(),
                response.getItems().get(4).getPerPeriod().getEnding(),
                response.getItems().get(5).getPerPeriod().getEnding(),
                response.getItems().get(6).getPerPeriod().getEnding()));
        assertThat(response.getAttachment().getDepreciation()).isEqualTo(GamaMoneyUtils.total(
                response.getItems().get(0).getPerPeriod().getDepreciation(),
                response.getItems().get(1).getPerPeriod().getDepreciation(),
                response.getItems().get(2).getPerPeriod().getDepreciation(),
                response.getItems().get(3).getPerPeriod().getDepreciation(),
                response.getItems().get(4).getPerPeriod().getDepreciation(),
                response.getItems().get(5).getPerPeriod().getDepreciation(),
                response.getItems().get(6).getPerPeriod().getDepreciation()));
        checkValue(response.getAttachment());
    }

    @Test
    void testListApiByDateAndStatusOperating() throws GamaApiException {
        {
            PageRequest request = new PageRequest();
            request.setConditions(new ArrayList<>());
            request.getConditions().add(new PageRequestCondition(CustomSearchType.DATE, LocalDate.of(2015, 4, 1)));
            request.getConditions().add(new PageRequestCondition(CustomSearchType.ASSET_STATUS, AssetStatusType.OPERATING));

            APIResult<PageResponse<AssetDto, AssetTotal>> result = assetApi.listAsset(request);
            assertThat(result.getError()).isNull();

            PageResponse<AssetDto, AssetTotal> response = result.getData();
            assertThat(response.getTotal()).isEqualTo(5);
            assertThat(response.getItems().get(0).getName()).isEqualTo("Asset A");
            assertThat(response.getItems().get(0).getStatus()).isEqualTo(AssetStatusType.OPERATING);
            assertThat(response.getItems().get(1).getName()).isEqualTo("Asset B");
            assertThat(response.getItems().get(1).getStatus()).isEqualTo(AssetStatusType.OPERATING);
            assertThat(response.getItems().get(2).getName()).isEqualTo("Asset C");
            assertThat(response.getItems().get(2).getStatus()).isEqualTo(AssetStatusType.OPERATING);
            assertThat(response.getItems().get(3).getName()).isEqualTo("Asset D abc");
            assertThat(response.getItems().get(3).getStatus()).isEqualTo(AssetStatusType.WRITTEN_OFF);
            assertThat(response.getItems().get(4).getName()).isEqualTo("Asset F");
            assertThat(response.getItems().get(4).getStatus()).isEqualTo(AssetStatusType.OPERATING);
        }
        {
            PageRequest request = new PageRequest();
            request.setConditions(new ArrayList<>());
            request.getConditions().add(new PageRequestCondition(CustomSearchType.DATE, LocalDate.of(2014, 10, 1)));
            request.getConditions().add(new PageRequestCondition(CustomSearchType.ASSET_STATUS, AssetStatusType.OPERATING));

            APIResult<PageResponse<AssetDto, AssetTotal>> result = assetApi.listAsset(request);
            assertThat(result.getError()).isNull();

            PageResponse<AssetDto, AssetTotal> response = result.getData();
            assertThat(response.getTotal()).isEqualTo(3);
            assertThat(response.getItems().get(0).getName()).isEqualTo("Asset A");
            assertThat(response.getItems().get(0).getStatus()).isEqualTo(AssetStatusType.OPERATING);
            assertThat(response.getItems().get(1).getName()).isEqualTo("Asset B");
            assertThat(response.getItems().get(1).getStatus()).isEqualTo(AssetStatusType.OPERATING);
            assertThat(response.getItems().get(2).getName()).isEqualTo("Asset F");
            assertThat(response.getItems().get(2).getStatus()).isEqualTo(AssetStatusType.OPERATING);
        }
    }

    @Test
    void testListApiByDateAndStatusOperatingAfter() throws GamaApiException {
        PageRequest request = new PageRequest();
        request.setConditions(new ArrayList<>());
        request.getConditions().add(new PageRequestCondition(CustomSearchType.DATE, LocalDate.of(2019, 12, 1)));
        request.getConditions().add(new PageRequestCondition(CustomSearchType.ASSET_STATUS, AssetStatusType.OPERATING));

        APIResult<PageResponse<AssetDto, AssetTotal>> result = assetApi.listAsset(request);
        assertThat(result.getError()).isNull();

        PageResponse<AssetDto, AssetTotal> response = result.getData();
        assertThat(response.getTotal()).isEqualTo(4);
        assertThat(response.getItems().get(0).getName()).isEqualTo("Asset A");
        assertThat(response.getItems().get(1).getName()).isEqualTo("Asset B");
        assertThat(response.getItems().get(2).getName()).isEqualTo("Asset C");
        assertThat(response.getItems().get(3).getName()).isEqualTo("Asset F");
    }

    @Test
    void testListApiByDateAndLastStatusConserved() throws GamaApiException {
        PageRequest request = new PageRequest();
        request.setConditions(new ArrayList<>());
        request.getConditions().add(new PageRequestCondition(CustomSearchType.DATE, LocalDate.of(2015, 4, 1)));
        request.getConditions().add(new PageRequestCondition(CustomSearchType.ASSET_STATUS, AssetStatusType.CONSERVED));

        APIResult<PageResponse<AssetDto, AssetTotal>> result = assetApi.listAsset(request);
        assertThat(result.getError()).isNull();

        PageResponse<AssetDto, AssetTotal> response = result.getData();
        assertThat(response.getTotal()).isEqualTo(2);
        assertThat(response.getItems().get(0).getName()).isEqualTo("Asset E");
        assertThat(response.getItems().get(1).getName()).isEqualTo("Asset G");
    }

    @Test
    void testListApiByDateAndLastStatusConservedAfter() throws GamaApiException {
        PageRequest request = new PageRequest();
        request.setConditions(new ArrayList<>());
        request.getConditions().add(new PageRequestCondition(CustomSearchType.DATE, LocalDate.of(2015, 12, 1)));
        request.getConditions().add(new PageRequestCondition(CustomSearchType.ASSET_STATUS, AssetStatusType.CONSERVED));

        APIResult<PageResponse<AssetDto, AssetTotal>> result = assetApi.listAsset(request);
        assertThat(result.getError()).isNull();

        PageResponse<AssetDto, AssetTotal> response = result.getData();
        assertThat(response.getTotal()).isEqualTo(2);
        assertThat(response.getItems().get(0).getName()).isEqualTo("Asset E");
        assertThat(response.getItems().get(1).getName()).isEqualTo("Asset G");
    }

    @Test
    void testListApiFilter() throws GamaApiException {
        PageRequest request = new PageRequest();
        request.setConditions(new ArrayList<>());
        request.getConditions().add(new PageRequestCondition(CustomSearchType.DATE, LocalDate.of(2015, 4, 1)));
        request.setFilter("aBC");

        APIResult<PageResponse<AssetDto, AssetTotal>> result = assetApi.listAsset(request);
        assertThat(result.getError()).isNull();

        PageResponse<AssetDto, AssetTotal> response = result.getData();
        assertThat(response.getTotal()).isEqualTo(4);
        assertThat(response.getItems().get(0).getName()).isEqualTo("Asset B");
        assertThat(response.getItems().get(0).getCode()).isEqualTo("B AbC 123");
        assertThat(response.getItems().get(0).getNote()).isEqualTo(null);
        assertThat(response.getItems().get(0).getCipher()).isEqualTo(null);

        assertThat(response.getItems().get(1).getName()).isEqualTo("Asset C");
        assertThat(response.getItems().get(1).getCode()).isEqualTo("C");
        assertThat(response.getItems().get(1).getNote()).isEqualTo("Note ABCDEF 123");
        assertThat(response.getItems().get(1).getCipher()).isEqualTo(null);

        assertThat(response.getItems().get(2).getName()).isEqualTo("Asset D abc");
        assertThat(response.getItems().get(2).getCode()).isEqualTo("D");
        assertThat(response.getItems().get(2).getNote()).isEqualTo(null);
        assertThat(response.getItems().get(2).getCipher()).isEqualTo(null);

        assertThat(response.getItems().get(3).getName()).isEqualTo("Asset E");
        assertThat(response.getItems().get(3).getCode()).isEqualTo("E");
        assertThat(response.getItems().get(3).getNote()).isEqualTo(null);
        assertThat(response.getItems().get(3).getCipher()).isEqualTo("aAbcd");
    }

    @Test
    void testListApiFilterLabel() throws GamaApiException {
        PageRequest request = new PageRequest();
        request.setConditions(new ArrayList<>());
        request.getConditions().add(new PageRequestCondition(CustomSearchType.DATE, LocalDate.of(2015, 4, 1)));
        request.setLabel("LabelA");

        APIResult<PageResponse<AssetDto, AssetTotal>> result = assetApi.listAsset(request);
        assertThat(result.getError()).isNull();

        PageResponse<AssetDto, AssetTotal> response = result.getData();
        assertThat(response.getTotal()).isEqualTo(2);
        assertThat(response.getItems().get(0).getName()).isEqualTo("Asset A");
        assertThat(response.getItems().get(1).getName()).isEqualTo("Asset B");
    }
}

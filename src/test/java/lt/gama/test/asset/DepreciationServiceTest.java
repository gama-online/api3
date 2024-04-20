package lt.gama.test.asset;

import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.helpers.DateUtils;
import lt.gama.model.dto.entities.AssetDto;
import lt.gama.model.dto.entities.EmployeeDto;
import lt.gama.model.dto.type.AssetTotal;
import lt.gama.model.sql.entities.AssetSql;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.Location;
import lt.gama.model.type.asset.AssetHistory;
import lt.gama.model.type.asset.Depreciation;
import lt.gama.model.type.doc.DocEmployee;
import lt.gama.model.type.enums.AssetStatusType;
import lt.gama.model.type.enums.DepreciationType;
import lt.gama.service.ex.rt.GamaException;
import lt.gama.test.base.BaseDBTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * gama-online
 * Created by valdas on 2015-10-28.
 */
class DepreciationServiceTest extends BaseDBTest {

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        // set system time and time zone
        DateUtils.mockClock = Clock.fixed(
                LocalDateTime.of(2018, 4, 15, 0, 0).toInstant(ZoneOffset.UTC),
                ZoneId.systemDefault());
        login();
    }

    @Test
    void testWrittenOff() throws GamaApiException {
        EmployeeDto employee = createEmployee("Jonas", "1");
        Location location = new Location("Location", "a1", "a2", "a3", "zip", "city", "municipality", "country");

        AssetSql asset = new AssetSql();
        asset.setCode("A");
        asset.setName("Asset");

        asset.setAcquisitionDate(LocalDate.of(2014, 9, 30));
        asset.setCost(GamaMoney.parse("EUR 22000.00"));

        asset.setDate(LocalDate.of(2015, 2, 1));
        asset.setValue(GamaMoney.parse("EUR 20000.00"));
        asset.setExpenses(GamaMoney.parse("EUR 2000.00"));
        asset.setHistory(new ArrayList<>());

        AssetHistory change = new AssetHistory();
        change.setDate(asset.getDate());
        change.setBeginning(asset.getValue());
        change.setStatus(AssetStatusType.WRITTEN_OFF);
        change.setResponsible(new DocEmployee(employee));
        change.setLocation(location);
        asset.getHistory().add(change);

        depreciationService.reset(asset);

        assertThat(asset.getResponsible().getId()).isEqualTo(employee.getId());
        assertThat(asset.getLocation()).isEqualTo(location);
        assertThat(asset.getStatus()).isEqualTo(AssetStatusType.WRITTEN_OFF);
        assertThat(asset.getWrittenOff()).isEqualTo(GamaMoney.parse("EUR 20000.00"));
        assertThat(asset.getDepreciation().size()).isEqualTo(0);
    }

    @Test
    void testWrittenOffBeforeEnd() {
        AssetSql asset = new AssetSql();
        asset.setCode("A");
        asset.setName("Asset");

        asset.setAcquisitionDate(LocalDate.of(2014, 9, 30));
        asset.setCost(GamaMoney.parse("EUR 22000.00"));

        asset.setDate(LocalDate.of(2015, 2, 1));
        asset.setValue(GamaMoney.parse("EUR 20000.00"));
        asset.setExpenses(GamaMoney.parse("EUR 2000.00"));
        asset.setHistory(new ArrayList<>());

        AssetHistory change = new AssetHistory();
        change.setDate(LocalDate.of(2015, 2, 1));
        change.setFinalDate(LocalDate.of(2018, 2, 1));
        change.setType(DepreciationType.LINE);
        change.setStatus(AssetStatusType.OPERATING);
        asset.getHistory().add(change);

        AssetHistory change2 = new AssetHistory();
        change2.setDate(LocalDate.of(2015, 12, 1));
        change2.setStatus(AssetStatusType.WRITTEN_OFF);
        asset.getHistory().add(change2);

        depreciationService.reset(asset);

        assertThat(asset.getStatus()).isEqualTo(AssetStatusType.WRITTEN_OFF);
        assertThat(asset.getWrittenOff()).isEqualTo(GamaMoney.parse("EUR 14444.56"));
        assertThat(asset.getDepreciation().size()).isEqualTo(10);
        asset.getDepreciation().stream()
                .filter(e -> e.getDate().equals(LocalDate.of(2015, 11, 1)))
                .forEach(d -> assertThat(d.getEnding()).isEqualTo(GamaMoney.parse("EUR 14444.56")));
    }

    @Test
    void testWrittenOffAfterEnd() {
        AssetSql asset = new AssetSql();
        asset.setCode("A");
        asset.setName("Asset");

        asset.setAcquisitionDate(LocalDate.of(2014, 9, 30));
        asset.setCost(GamaMoney.parse("EUR 1100.00"));

        asset.setDate(LocalDate.of(2015, 2, 1));
        asset.setValue(GamaMoney.parse("EUR 1000.00"));
        asset.setExpenses(GamaMoney.parse("EUR 100.00"));
        asset.setHistory(new ArrayList<>());

        AssetHistory change = new AssetHistory();
        change.setDate(LocalDate.of(2015, 2, 1));
        change.setFinalDate(LocalDate.of(2018, 2, 1));
        change.setType(DepreciationType.LINE);
        change.setStatus(AssetStatusType.OPERATING);
        change.setEnding(GamaMoney.parse("EUR 1.00"));
        asset.getHistory().add(change);

        AssetHistory change2 = new AssetHistory();
        change2.setDate(LocalDate.of(2018, 5, 1));
        change2.setStatus(AssetStatusType.WRITTEN_OFF);
        asset.getHistory().add(change2);

        depreciationService.reset(asset);

        assertThat(asset.getHistory().get(0).getDate()).isEqualTo(LocalDate.of(2015, 2, 1));
        assertThat(asset.getHistory().get(0).getBeginning()).isEqualTo(GamaMoney.parse("EUR 1000.00"));
        assertThat(asset.getHistory().get(0).getFinalDate()).isEqualTo(LocalDate.of(2018, 2, 1));
        assertThat(asset.getHistory().get(0).getEnding()).isEqualTo(GamaMoney.parse("EUR 1.00"));

        assertThat(asset.getHistory().get(1).getDate()).isEqualTo(LocalDate.of(2018, 5, 1));
        assertThat(asset.getHistory().get(1).getBeginning()).isEqualTo(GamaMoney.parse("EUR 1.00"));
        assertThat(asset.getHistory().get(1).getFinalDate()).isNull();
        assertThat(asset.getHistory().get(1).getEnding()).isEqualTo(GamaMoney.parse("EUR 1.00"));

        assertThat(asset.getStatus()).isEqualTo(AssetStatusType.WRITTEN_OFF);
        assertThat(asset.getWrittenOff()).isEqualTo(GamaMoney.parse("EUR 1.00"));
        assertThat(asset.getDepreciation().size()).isEqualTo(36);
        asset.getDepreciation().stream()
                .filter(e -> e.getDate().equals(LocalDate.of(2018, 11, 1)))
                .forEach(d -> assertThat(d.getEnding()).isEqualTo(GamaMoney.parse("EUR 1.00")));
    }


    @Test
    void testConservation() throws GamaApiException {
        EmployeeDto employee = createEmployee("Jonas", "1");
        Location location = new Location("Location", "a1", "a2", "a3", "zip", "city", "municipality", "country");

        AssetSql asset = new AssetSql();
        asset.setCode("A");
        asset.setName("Asset");

        asset.setAcquisitionDate(LocalDate.of(2014, 9, 30));
        asset.setCost(GamaMoney.parse("EUR 22000.00"));

        asset.setDate(LocalDate.of(2015, 2, 1));
        asset.setValue(GamaMoney.parse("EUR 20000.00"));
        asset.setExpenses(GamaMoney.parse("EUR 2000.00"));
        asset.setHistory(new ArrayList<>());

        AssetHistory change = new AssetHistory();
        change.setDate(asset.getDate());
        change.setBeginning(asset.getValue());
        change.setStatus(AssetStatusType.CONSERVED);
        change.setResponsible(new DocEmployee(employee));
        change.setLocation(location);
        asset.getHistory().add(change);

        // test without final date
        depreciationService.reset(asset);

        assertThat(asset.getResponsible().getId()).isEqualTo(employee.getId());
        assertThat(asset.getLocation()).isEqualTo(location);
        assertThat(asset.getStatus()).isEqualTo(AssetStatusType.CONSERVED);

        assertThat(asset.getDepreciation().size()).isEqualTo(1);
        Depreciation d = asset.getDepreciation().stream()
                .filter(e -> e.getDate().equals(LocalDate.of(2015, 2, 1))).findAny().orElse(null);
        assertThat(d).isNotNull();
        assertThat(d.getBeginning()).isEqualTo(GamaMoney.parse("EUR 20000.00"));
        assertThat(d.getEnding()).isEqualTo(GamaMoney.parse("EUR 20000.00"));
        assertThat(d.getDepreciation()).isEqualTo(GamaMoney.parse("EUR 2000.00"));
        

        // test with final date - must be the same as without
        change.setFinalDate(LocalDate.of(2015, 4, 1));
        depreciationService.reset(asset);

        assertThat(asset.getDepreciation().size()).isEqualTo(1);
        d = asset.getDepreciation().stream()
                .filter(e -> e.getDate().equals(LocalDate.of(2015, 2, 1))).findAny().orElse(null);
        assertThat(d).isNotNull();
        assertThat(d.getBeginning()).isEqualTo(GamaMoney.parse("EUR 20000.00"));
        assertThat(d.getEnding()).isEqualTo(GamaMoney.parse("EUR 20000.00"));
        assertThat(d.getDepreciation()).isEqualTo(GamaMoney.parse("EUR 2000.00"));
    }

    @Test
    void testResetLineMethod() {
        AssetSql asset = new AssetSql();
        asset.setCode("A");
        asset.setName("Asset");

        asset.setAcquisitionDate(LocalDate.of(2014, 9, 30));
        asset.setCost(GamaMoney.parse("EUR 22000.00"));

        asset.setDate(LocalDate.of(2015, 2, 1));
        asset.setValue(GamaMoney.parse("EUR 20000.00"));
        asset.setExpenses(GamaMoney.parse("EUR 2000.00"));
        asset.setHistory(new ArrayList<>());

        AssetHistory change = new AssetHistory();
        change.setDate(asset.getDate());
        change.setFinalDate(LocalDate.of(2018, 2, 1));

        asset.getHistory().add(change);

        // check if no Depreciation Type
        assertThrows(NullPointerException.class, () -> depreciationService.reset(asset));

        change.setType(DepreciationType.LINE);
        // check if no Status
        assertThrows(NullPointerException.class, () -> depreciationService.reset(asset));

        change.setStatus(AssetStatusType.OPERATING);

        //Beginning value will be set automatically
        depreciationService.reset(asset);

        assertThat(asset.getStatus()).isEqualTo(AssetStatusType.OPERATING);
        assertThat(asset.getDepreciation().size()).isEqualTo(36);

        Depreciation d = asset.getDepreciation().stream()
                .filter(e -> e.getDate().equals(LocalDate.of(2015, 2, 1))).findAny().orElse(null);
        assertThat(d).isNotNull();
        assertThat(d.getBeginning()).isEqualTo(GamaMoney.parse("EUR 20000.00"));
        assertThat(d.getExpense()).isEqualTo(GamaMoney.parse("EUR 555.40"));
        assertThat(d.getEnding()).isEqualTo(GamaMoney.parse("EUR 19444.60"));
        assertThat(d.getDepreciation()).isEqualTo(GamaMoney.parse("EUR 2555.40"));

        d = asset.getDepreciation().stream()
                .filter(e -> e.getDate().equals(LocalDate.of(2015, 3, 1))).findAny().orElse(null);
        assertThat(d).isNotNull();
        assertThat(d.getBeginning()).isEqualTo(GamaMoney.parse("EUR 19444.60"));
        assertThat(d.getExpense()).isEqualTo(GamaMoney.parse("EUR 555.56"));
        assertThat(d.getEnding()).isEqualTo(GamaMoney.parse("EUR 18889.04"));
        assertThat(d.getDepreciation()).isEqualTo(GamaMoney.parse("EUR 3110.96"));

        d = asset.getDepreciation().stream()
                .filter(e -> e.getDate().equals(LocalDate.of(2018, 1, 1))).findAny().orElse(null);
        assertThat(d).isNotNull();
        assertThat(d.getBeginning()).isEqualTo(GamaMoney.parse("EUR 555.56"));
        assertThat(d.getExpense()).isEqualTo(GamaMoney.parse("EUR 555.56"));
        assertThat(d.getEnding()).isEqualTo(GamaMoney.parse("EUR 0.00"));
        assertThat(d.getDepreciation()).isEqualTo(GamaMoney.parse("EUR 22000.00"));

        AssetHistory change2 = new AssetHistory();
        change2.setDate(LocalDate.of(2015, 4, 1));
        change2.setFinalDate(LocalDate.of(2018, 2, 1));
        change2.setType(DepreciationType.LINE);
        change2.setStatus(AssetStatusType.OPERATING);
        change2.setDtValue(GamaMoney.parse("EUR 100.00"));
        change2.setDtExpense(GamaMoney.parse("EUR 200.00"));
        change2.setEnding(GamaMoney.parse("EUR 1.00"));
        asset.getHistory().add(change2);

        depreciationService.reset(asset);

        assertThat(asset.getDepreciation().size()).isEqualTo(36);

        d = asset.getDepreciation().stream()
                .filter(e -> e.getDate().equals(LocalDate.of(2015, 2, 1))).findAny().orElse(null);
        assertThat(d).isNotNull();
        assertThat(d.getBeginning()).isEqualTo(GamaMoney.parse("EUR 20000.00"));
        assertThat(d.getExpense()).isEqualTo(GamaMoney.parse("EUR 555.40"));
        assertThat(d.getEnding()).isEqualTo(GamaMoney.parse("EUR 19444.60"));
        assertThat(d.getDepreciation()).isEqualTo(GamaMoney.parse("EUR 2555.40"));

        d = asset.getDepreciation().stream()
                .filter(e -> e.getDate().equals(LocalDate.of(2015, 3, 1))).findAny().orElse(null);
        assertThat(d).isNotNull();
        assertThat(d.getBeginning()).isEqualTo(GamaMoney.parse("EUR 19444.60"));
        assertThat(d.getExpense()).isEqualTo(GamaMoney.parse("EUR 555.56"));
        assertThat(d.getEnding()).isEqualTo(GamaMoney.parse("EUR 18889.04"));
        assertThat(d.getDepreciation()).isEqualTo(GamaMoney.parse("EUR 3110.96"));

        d = asset.getDepreciation().stream()
                .filter(e -> e.getDate().equals(LocalDate.of(2015, 4, 1))).findAny().orElse(null);
        assertThat(d).isNotNull();
        assertThat(d.getBeginning()).isEqualTo(GamaMoney.parse("EUR 18889.04"));
        assertThat(d.getExpense()).isEqualTo(GamaMoney.parse("EUR 552.57"));
        assertThat(d.getDtValue()).isEqualTo(GamaMoney.parse("EUR 100.00"));
        assertThat(d.getDtExpense()).isEqualTo(GamaMoney.parse("EUR 200.00"));
        assertThat(d.getEnding()).isEqualTo(GamaMoney.parse("EUR 18236.47"));
        assertThat(d.getDepreciation()).isEqualTo(GamaMoney.parse("EUR 3863.53"));

        d = asset.getDepreciation().stream()
                .filter(e -> e.getDate().equals(LocalDate.of(2015, 5, 1))).findAny().orElse(null);
        assertThat(d).isNotNull();
        assertThat(d.getBeginning()).isEqualTo(GamaMoney.parse("EUR 18236.47"));
        assertThat(d.getExpense()).isEqualTo(GamaMoney.parse("EUR 552.59"));
        assertThat(d.getEnding()).isEqualTo(GamaMoney.parse("EUR 17683.88"));
        assertThat(d.getDepreciation()).isEqualTo(GamaMoney.parse("EUR 4416.12"));

        d = asset.getDepreciation().stream()
                .filter(e -> e.getDate().equals(LocalDate.of(2018, 1, 1))).findAny().orElse(null);
        assertThat(d).isNotNull();
        assertThat(d.getBeginning()).isEqualTo(GamaMoney.parse("EUR 553.59"));
        assertThat(d.getExpense()).isEqualTo(GamaMoney.parse("EUR 552.59"));
        assertThat(d.getEnding()).isEqualTo(GamaMoney.parse("EUR 1.00"));
        assertThat(d.getDepreciation()).isEqualTo(GamaMoney.parse("EUR 22099.00"));
    }

    @Test
    void testResetLineMethodWithWindow() {
        AssetSql asset = new AssetSql();
        asset.setCode("A");
        asset.setName("Asset");

        asset.setAcquisitionDate(LocalDate.of(2014, 9, 30));
        asset.setCost(GamaMoney.parse("EUR 1100.00"));

        asset.setDate(LocalDate.of(2015, 2, 1));
        asset.setValue(GamaMoney.parse("EUR 1000.00"));
        asset.setExpenses(GamaMoney.parse("EUR 100.00"));
        asset.setHistory(new ArrayList<>());

        {
            AssetHistory change = new AssetHistory();
            change.setDate(LocalDate.of(2015, 2, 1));
            change.setFinalDate(LocalDate.of(2017, 2, 1));
            change.setType(DepreciationType.LINE);
            change.setStatus(AssetStatusType.OPERATING);
            change.setEnding(GamaMoney.parse("EUR 1.00"));
            asset.getHistory().add(change);
        }
        {
            AssetHistory change = new AssetHistory();
            change.setDate(LocalDate.of(2018, 2, 1));
            change.setFinalDate(LocalDate.of(2019, 2, 1));
            change.setType(DepreciationType.LINE);
            change.setStatus(AssetStatusType.OPERATING);
            change.setDtValue(GamaMoney.parse("EUR 1200.00"));
            change.setEnding(GamaMoney.parse("EUR 1.00"));
            asset.getHistory().add(change);
        }

        depreciationService.reset(asset);

        assertThat(asset.getHistory().get(0).getDate()).isEqualTo(LocalDate.of(2015, 2, 1));
        assertThat(asset.getHistory().get(0).getBeginning()).isEqualTo(GamaMoney.parse("EUR 1000.00"));
        assertThat(asset.getHistory().get(0).getFinalDate()).isEqualTo(LocalDate.of(2017, 2, 1));
        assertThat(asset.getHistory().get(0).getEnding()).isEqualTo(GamaMoney.parse("EUR 1.00"));

        assertThat(asset.getHistory().get(1).getDate()).isEqualTo(LocalDate.of(2018, 2, 1));
        assertThat(asset.getHistory().get(1).getBeginning()).isEqualTo(GamaMoney.parse("EUR 1.00"));
        assertThat(asset.getHistory().get(1).getFinalDate()).isEqualTo(LocalDate.of(2019, 2, 1));
        assertThat(asset.getHistory().get(1).getDtValue()).isEqualTo(GamaMoney.parse("EUR 1200.00"));
        assertThat(asset.getHistory().get(1).getEnding()).isEqualTo(GamaMoney.parse("EUR 1.00"));

        assertThat(asset.getDepreciation().size()).isEqualTo(36);

        Depreciation d = asset.getDepreciation().stream()
                .filter(e -> e.getDate().equals(LocalDate.of(2015, 2, 1))).findAny().orElse(null);
        assertThat(d).isNotNull();
        assertThat(d.getBeginning()).isEqualTo(GamaMoney.parse("EUR 1000.00"));
        assertThat(d.getExpense()).isEqualTo(GamaMoney.parse("EUR 41.51"));
        assertThat(d.getEnding()).isEqualTo(GamaMoney.parse("EUR 958.49"));
        assertThat(d.getDepreciation()).isEqualTo(GamaMoney.parse("EUR 141.51"));

        d = asset.getDepreciation().stream()
                .filter(e -> e.getDate().equals(LocalDate.of(2017, 1, 1))).findAny().orElse(null);
        assertThat(d).isNotNull();
        assertThat(d.getBeginning()).isEqualTo(GamaMoney.parse("EUR 42.63"));
        assertThat(d.getExpense()).isEqualTo(GamaMoney.parse("EUR 41.63"));
        assertThat(d.getEnding()).isEqualTo(GamaMoney.parse("EUR 1.00"));
        assertThat(d.getDepreciation()).isEqualTo(GamaMoney.parse("EUR 1099.00"));

        assertThat(asset.getDepreciation().stream().noneMatch(e -> e.getDate().equals(LocalDate.of(2017, 2, 1)))).isTrue();

        d = asset.getDepreciation().stream()
                .filter(e -> e.getDate().equals(LocalDate.of(2018, 2, 1))).findAny().orElse(null);
        assertThat(d).isNotNull();
        assertThat(d.getBeginning()).isEqualTo(GamaMoney.parse("EUR 1.00"));
        assertThat(d.getDtValue()).isEqualTo(GamaMoney.parse("EUR 1200.00"));
        assertThat(d.getExpense()).isEqualTo(GamaMoney.parse("EUR 100.00"));
        assertThat(d.getEnding()).isEqualTo(GamaMoney.parse("EUR 1101.00"));
        assertThat(d.getDepreciation()).isEqualTo(GamaMoney.parse("EUR 1199.00"));

        d = asset.getDepreciation().stream()
                .filter(e -> e.getDate().equals(LocalDate.of(2019, 1, 1))).findAny().orElse(null);
        assertThat(d).isNotNull();
        assertThat(d.getBeginning()).isEqualTo(GamaMoney.parse("EUR 101.00"));
        assertThat(d.getExpense()).isEqualTo(GamaMoney.parse("EUR 100.00"));
        assertThat(d.getEnding()).isEqualTo(GamaMoney.parse("EUR 1.00"));
        assertThat(d.getDepreciation()).isEqualTo(GamaMoney.parse("EUR 2299.00"));

        assertThat(asset.getDepreciation().stream().noneMatch(e -> e.getDate().equals(LocalDate.of(2019, 2, 1)))).isTrue();
    }

    @Test
    void testDoubleMethod() {
        AssetSql asset = new AssetSql();
        asset.setCode("A");
        asset.setName("Asset");

        asset.setAcquisitionDate(LocalDate.of(2014, 9, 30));
        asset.setCost(GamaMoney.parse("EUR 22000.00"));

        asset.setDate(LocalDate.of(2015, 2, 1));
        asset.setValue(GamaMoney.parse("EUR 20099.00"));
        asset.setExpenses(GamaMoney.parse("EUR 1901.00"));
        asset.setHistory(new ArrayList<>());

        AssetHistory change = new AssetHistory();
        change.setDate(asset.getDate());
        change.setType(DepreciationType.DOUBLE);
        change.setStatus(AssetStatusType.OPERATING);
        change.setFinalDate(LocalDate.of(2020, 1, 1));
        change.setDtValue(GamaMoney.parse("EUR 100.00"));
        change.setDtExpense(GamaMoney.parse("EUR 200.00"));
        change.setEnding(GamaMoney.parse("EUR 1.00"));
        asset.getHistory().add(change);

        // check if full year period
        assertThrows(IllegalArgumentException.class, () -> depreciationService.reset(asset));

        change.setFinalDate(LocalDate.of(2020, 2, 1));

        depreciationService.reset(asset);

        assertThat(asset.getDepreciation().size()).isEqualTo(60);

        Depreciation d = asset.getDepreciation().stream()
                .filter(e -> e.getDate().equals(LocalDate.of(2015, 2, 1))).findAny().orElse(null);
        assertThat(d).isNotNull();
        assertThat(d.getBeginning()).isEqualTo(GamaMoney.parse("EUR 20099.00"));
        assertThat(d.getDtValue()).isEqualTo(GamaMoney.parse("EUR 100.00"));
        assertThat(d.getDtExpense()).isEqualTo(GamaMoney.parse("EUR 200.00"));
        assertThat(d.getExpense()).isEqualTo(GamaMoney.parse("EUR 666.67"));
        assertThat(d.getEnding()).isEqualTo(GamaMoney.parse("EUR 19332.33"));
        assertThat(d.getDepreciation()).isEqualTo(GamaMoney.parse("EUR 2767.67"));

        d = asset.getDepreciation().stream().filter(e -> e.getDate().equals(LocalDate.of(2015, 3, 1))).findAny().orElse(null);
        assertThat(d).isNotNull();
        assertThat(d.getExpense()).isEqualTo(GamaMoney.parse("EUR 666.63"));

        d = asset.getDepreciation().stream().filter(e -> e.getDate().equals(LocalDate.of(2016, 2, 1))).findAny().orElse(null);
        assertThat(d).isNotNull();
        assertThat(d.getExpense()).isEqualTo(GamaMoney.parse("EUR 399.98"));

        d = asset.getDepreciation().stream().filter(e -> e.getDate().equals(LocalDate.of(2016, 3, 1))).findAny().orElse(null);
        assertThat(d).isNotNull();
        assertThat(d.getExpense()).isEqualTo(GamaMoney.parse("EUR 399.98"));

        d = asset.getDepreciation().stream().filter(e -> e.getDate().equals(LocalDate.of(2017, 2, 1))).findAny().orElse(null);
        assertThat(d).isNotNull();
        assertThat(d.getExpense()).isEqualTo(GamaMoney.parse("EUR 239.97"));

        d = asset.getDepreciation().stream().filter(e -> e.getDate().equals(LocalDate.of(2017, 3, 1))).findAny().orElse(null);
        assertThat(d).isNotNull();
        assertThat(d.getExpense()).isEqualTo(GamaMoney.parse("EUR 239.99"));

        d = asset.getDepreciation().stream().filter(e -> e.getDate().equals(LocalDate.of(2018, 2, 1))).findAny().orElse(null);
        assertThat(d).isNotNull();
        assertThat(d.getExpense()).isEqualTo(GamaMoney.parse("EUR 144.02"));

        d = asset.getDepreciation().stream().filter(e -> e.getDate().equals(LocalDate.of(2018, 3, 1))).findAny().orElse(null);
        assertThat(d).isNotNull();
        assertThat(d.getExpense()).isEqualTo(GamaMoney.parse("EUR 143.99"));

        d = asset.getDepreciation().stream().filter(e -> e.getDate().equals(LocalDate.of(2019, 2, 1))).findAny().orElse(null);
        assertThat(d).isNotNull();
        assertThat(d.getExpense()).isEqualTo(GamaMoney.parse("EUR 215.86"));

        d = asset.getDepreciation().stream().filter(e -> e.getDate().equals(LocalDate.of(2019, 3, 1))).findAny().orElse(null);
        assertThat(d).isNotNull();
        assertThat(d.getExpense()).isEqualTo(GamaMoney.parse("EUR 215.91"));

        d = asset.getDepreciation().stream().filter(e -> e.getDate().equals(LocalDate.of(2020, 1, 1))).findAny().orElse(null);
        assertThat(d).isNotNull();
        assertThat(d.getBeginning()).isEqualTo(GamaMoney.parse("EUR 216.91"));
        assertThat(d.getExpense()).isEqualTo(GamaMoney.parse("EUR 215.91"));
        assertThat(d.getEnding()).isEqualTo(GamaMoney.parse("EUR 1.00"));
        assertThat(d.getDepreciation()).isEqualTo(GamaMoney.parse("EUR 22099.00"));
    }

    @Test
    void testOtherMethod() {
        AssetSql asset = new AssetSql();
        asset.setCode("A");
        asset.setName("Asset");

        asset.setAcquisitionDate(LocalDate.of(2014, 9, 30));
        asset.setCost(GamaMoney.parse("EUR 22000.00"));

        asset.setDate(LocalDate.of(2015, 2, 1));
        asset.setValue(GamaMoney.parse("EUR 20099.00"));
        asset.setExpenses(GamaMoney.parse("EUR 1901.00"));
        asset.setHistory(new ArrayList<>());

        AssetHistory change = new AssetHistory();
        change.setDate(asset.getDate());
        change.setType(DepreciationType.OTHER);
        change.setStatus(AssetStatusType.OPERATING);
        change.setFinalDate(LocalDate.of(2016, 1, 1));
        change.setDtValue(GamaMoney.parse("EUR 100.00"));
        change.setDtExpense(GamaMoney.parse("EUR 200.00"));
        change.setEnding(GamaMoney.parse("EUR 1.00"));
        asset.getHistory().add(change);

        // check if rate or amount is not null
        assertThrows(IllegalArgumentException.class, () -> depreciationService.reset(asset));

        change.setRate(40.0);

        depreciationService.reset(asset);

        assertThat(asset.getDepreciation().size()).isEqualTo(11);

        Depreciation d = asset.getDepreciation().stream()
                .filter(e -> e.getDate().equals(LocalDate.of(2015, 2, 1))).findAny().orElse(null);
        assertThat(d).isNotNull();
        assertThat(d.getBeginning()).isEqualTo(GamaMoney.parse("EUR 20099.00"));
        assertThat(d.getDtValue()).isEqualTo(GamaMoney.parse("EUR 100.00"));
        assertThat(d.getDtExpense()).isEqualTo(GamaMoney.parse("EUR 200.00"));
        assertThat(d.getExpense()).isEqualTo(GamaMoney.parse("EUR 727.20"));
        assertThat(d.getEnding()).isEqualTo(GamaMoney.parse("EUR 19271.80"));
        assertThat(d.getDepreciation()).isEqualTo(GamaMoney.parse("EUR 2828.20"));

        d = asset.getDepreciation().stream()
                .filter(e -> e.getDate().equals(LocalDate.of(2015, 12, 1))).findAny().orElse(null);
        assertThat(d).isNotNull();
        assertThat(d.getBeginning()).isEqualTo(GamaMoney.parse("EUR 12726.64"));
        assertThat(d.getExpense()).isEqualTo(GamaMoney.parse("EUR 727.24"));
        assertThat(d.getEnding()).isEqualTo(GamaMoney.parse("EUR 11999.40"));
        assertThat(d.getDepreciation()).isEqualTo(GamaMoney.parse("EUR 10100.60"));

        change.setRate(null);
        change.setAmount(GamaMoney.parse("EUR 10000.00"));

        depreciationService.reset(asset);

        assertThat(asset.getDepreciation().size()).isEqualTo(11);

        d = asset.getDepreciation().stream()
                .filter(e -> e.getDate().equals(LocalDate.of(2015, 2, 1))).findAny().orElse(null);
        assertThat(d).isNotNull();
        assertThat(d.getBeginning()).isEqualTo(GamaMoney.parse("EUR 20099.00"));
        assertThat(d.getDtValue()).isEqualTo(GamaMoney.parse("EUR 100.00"));
        assertThat(d.getDtExpense()).isEqualTo(GamaMoney.parse("EUR 200.00"));
        assertThat(d.getExpense()).isEqualTo(GamaMoney.parse("EUR 909.10"));
        assertThat(d.getEnding()).isEqualTo(GamaMoney.parse("EUR 19089.90"));
        assertThat(d.getDepreciation()).isEqualTo(GamaMoney.parse("EUR 3010.10"));

        d = asset.getDepreciation().stream()
                .filter(e -> e.getDate().equals(LocalDate.of(2015, 12, 1))).findAny().orElse(null);
        assertThat(d).isNotNull();
        assertThat(d.getBeginning()).isEqualTo(GamaMoney.parse("EUR 10908.09"));
        assertThat(d.getExpense()).isEqualTo(GamaMoney.parse("EUR 909.09"));
        assertThat(d.getEnding()).isEqualTo(GamaMoney.parse("EUR 9999.00"));
        assertThat(d.getDepreciation()).isEqualTo(GamaMoney.parse("EUR 12101.00"));
    }

    @Test
    void testAssetsPastDate() {
        AssetSql asset = new AssetSql();
        asset.setCode("A");
        asset.setName("Asset");

        asset.setAcquisitionDate(LocalDate.of(2014, 12, 10));
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
        asset.getHistory().add(change);

        depreciationService.reset(asset);

        dbServiceSQL.saveEntityInCompany(asset);
        clearCaches();

        GamaException e = assertThrows(GamaException.class, () -> depreciationService.generateDE(LocalDate.of(2014, 12, 1)));
        assertThat(e.getMessage()).isEqualTo("No data for double-entry of 2014-12-01");
    }

    @Test
    void testFixAssetUpdatedUntil() {
        AssetSql asset = new AssetSql();
        asset.setCode("A");
        asset.setName("Asset");

        asset.setAcquisitionDate(LocalDate.of(2014, 9, 30));
        asset.setCost(GamaMoney.parse("EUR 22000.00"));

        asset.setDate(LocalDate.of(2015, 2, 1));
        asset.setValue(GamaMoney.parse("EUR 20000.00"));
        asset.setExpenses(GamaMoney.parse("EUR 2000.00"));
        asset.setHistory(new ArrayList<>());

        AssetHistory change = new AssetHistory();
        change.setDate(LocalDate.of(2015, 2, 1));
        change.setFinalDate(LocalDate.of(2018, 2, 1));
        change.setType(DepreciationType.LINE);
        change.setStatus(AssetStatusType.OPERATING);
        asset.getHistory().add(change);

        depreciationService.reset(asset);
        dbServiceSQL.saveEntityInCompany(asset);
        clearCaches();
    }

    @Test
    void testUpdateAsset() throws GamaApiException {
        AssetSql asset = new AssetSql();
        asset.setCode("A");
        asset.setName("Asset");

        asset.setAcquisitionDate(LocalDate.of(2014, 9, 30));
        asset.setCost(GamaMoney.parse("EUR 22000.00"));

        asset.setDate(LocalDate.of(2015, 2, 1));
        asset.setValue(GamaMoney.parse("EUR 20000.00"));
        asset.setExpenses(GamaMoney.parse("EUR 2000.00"));
        asset.setHistory(new ArrayList<>());

        AssetHistory change = new AssetHistory();
        change.setDate(LocalDate.of(2015, 2, 1));
        change.setFinalDate(LocalDate.of(2018, 2, 1));
        change.setType(DepreciationType.LINE);
        change.setStatus(AssetStatusType.OPERATING);
        asset.getHistory().add(change);

        APIResult<AssetDto> result = assetApi.saveAsset(assetSqlMapper.toDto(asset));
        assertThat(result.getError()).isNull();
        clearCaches();
    }

    @Test
    void testCalcAssetTotalOnAndAfterFinal() {
        AssetSql asset = new AssetSql();
        asset.setCode("A");
        asset.setName("Asset");
        asset.setAcquisitionDate(LocalDate.of(2014, 9, 30));
        asset.setCost(GamaMoney.parse("EUR 22000.00"));

        asset.setDate(LocalDate.of(2015, 2, 1));
        asset.setValue(GamaMoney.parse("EUR 20000.00"));
        asset.setExpenses(GamaMoney.parse("EUR 2000.00"));
        asset.setHistory(new ArrayList<>());

        AssetHistory change = new AssetHistory();
        change.setDate(LocalDate.of(2015, 2, 1));
        change.setFinalDate(LocalDate.of(2018, 2, 1));
        change.setEnding(GamaMoney.parse("EUR 1.00"));
        change.setType(DepreciationType.LINE);
        change.setStatus(AssetStatusType.OPERATING);
        asset.getHistory().add(change);

        APIResult<AssetDto> result = assetApi.saveAsset(assetSqlMapper.toDto(asset));
        assertThat(result.getError()).isNull();
        clearCaches();

        AssetDto assetDto = result.getData();

        AssetTotal assetTotal = depreciationService.calcAssetTotal(List.of(assetDto), LocalDate.of(2018, 1, 1));
        assertThat(assetTotal.getIncoming()).isEqualTo(null);
        assertThat(assetTotal.getBeginning()).isEqualTo(GamaMoney.parse("EUR 556.53"));
        assertThat(assetTotal.getExpense()).isEqualTo(GamaMoney.parse("EUR 555.53"));
        assertThat(assetTotal.getEnding()).isEqualTo(GamaMoney.parse("EUR 1.00"));
        assertThat(assetTotal.getDepreciation()).isEqualTo(GamaMoney.parse("EUR 21999.00"));

        assertThat(assetDto.getPerPeriod().getIncoming()).isEqualTo(assetTotal.getIncoming());
        assertThat(assetDto.getPerPeriod().getBeginning()).isEqualTo(assetTotal.getBeginning());
        assertThat(assetDto.getPerPeriod().getExpense()).isEqualTo(assetTotal.getExpense());
        assertThat(assetDto.getPerPeriod().getEnding()).isEqualTo(assetTotal.getEnding());
        assertThat(assetDto.getPerPeriod().getDepreciation()).isEqualTo(assetTotal.getDepreciation());

        assetTotal = depreciationService.calcAssetTotal(List.of(assetDto), LocalDate.of(2018, 2, 1));
        assertThat(assetTotal.getIncoming()).isEqualTo(null);
        assertThat(assetTotal.getBeginning()).isEqualTo(GamaMoney.parse("EUR 1.00"));
        assertThat(assetTotal.getExpense()).isEqualTo(null);
        assertThat(assetTotal.getEnding()).isEqualTo(GamaMoney.parse("EUR 1.00"));
        assertThat(assetTotal.getDepreciation()).isEqualTo(GamaMoney.parse("EUR 21999.00"));

        assertThat(assetDto.getPerPeriod().getIncoming()).isEqualTo(assetTotal.getIncoming());
        assertThat(assetDto.getPerPeriod().getBeginning()).isEqualTo(assetTotal.getBeginning());
        assertThat(assetDto.getPerPeriod().getExpense()).isEqualTo(assetTotal.getExpense());
        assertThat(assetDto.getPerPeriod().getEnding()).isEqualTo(assetTotal.getEnding());
        assertThat(assetDto.getPerPeriod().getDepreciation()).isEqualTo(assetTotal.getDepreciation());
    }

    @Test
    void testCalcAssetTotalWithConservation() {
        AssetSql asset = new AssetSql();
        asset.setCode("A");
        asset.setName("Asset");
        asset.setAcquisitionDate(LocalDate.of(2014, 9, 30));
        asset.setCost(GamaMoney.parse("EUR 22000.00"));

        asset.setDate(LocalDate.of(2015, 2, 1));
        asset.setValue(GamaMoney.parse("EUR 20000.00"));
        asset.setExpenses(GamaMoney.parse("EUR 2000.00"));
        asset.setHistory(new ArrayList<>());

        AssetHistory change = new AssetHistory();
        change.setDate(LocalDate.of(2015, 2, 1));
        change.setFinalDate(LocalDate.of(2018, 2, 1));
        change.setEnding(GamaMoney.parse("EUR 1.00"));
        change.setType(DepreciationType.LINE);
        change.setStatus(AssetStatusType.OPERATING);
        asset.getHistory().add(change);

        change = new AssetHistory();
        change.setDate(LocalDate.of(2017, 5, 1));
        change.setStatus(AssetStatusType.CONSERVED);
        asset.getHistory().add(change);

        change = new AssetHistory();
        change.setDate(LocalDate.of(2018, 2, 1));
        change.setFinalDate(LocalDate.of(2019, 2, 1));
        change.setEnding(GamaMoney.parse("EUR 1.00"));
        change.setType(DepreciationType.LINE);
        change.setStatus(AssetStatusType.OPERATING);
        asset.getHistory().add(change);

        APIResult<AssetDto> result = assetApi.saveAsset(assetSqlMapper.toDto(asset));
        assertThat(result.getError()).isNull();
        clearCaches();

        AssetDto assetDto = result.getData();

        // operation begins
        AssetTotal assetTotal = depreciationService.calcAssetTotal(List.of(assetDto), LocalDate.of(2015, 2, 1));
        assertThat(assetTotal.getIncoming()).isEqualTo(GamaMoney.parse("EUR 20000.00"));
        assertThat(assetTotal.getBeginning()).isEqualTo(null);
        assertThat(assetTotal.getExpense()).isEqualTo(GamaMoney.parse("EUR 555.45"));
        assertThat(assetTotal.getEnding()).isEqualTo(GamaMoney.parse("EUR 19444.55"));
        assertThat(assetTotal.getDepreciation()).isEqualTo(GamaMoney.parse("EUR 2555.45")); // 2000.00 + 555.45

        assertThat(assetDto.getPerPeriod().getIncoming()).isEqualTo(assetTotal.getIncoming());
        assertThat(assetDto.getPerPeriod().getBeginning()).isEqualTo(assetTotal.getBeginning());
        assertThat(assetDto.getPerPeriod().getExpense()).isEqualTo(assetTotal.getExpense());
        assertThat(assetDto.getPerPeriod().getEnding()).isEqualTo(assetTotal.getEnding());
        assertThat(assetDto.getPerPeriod().getDepreciation()).isEqualTo(assetTotal.getDepreciation());

        assetTotal = depreciationService.calcAssetTotal(List.of(assetDto), LocalDate.of(2017, 4, 1));
        assertThat(assetTotal.getIncoming()).isEqualTo(null);
        assertThat(assetTotal.getBeginning()).isEqualTo(GamaMoney.parse("EUR 5556.30"));
        assertThat(assetTotal.getExpense()).isEqualTo(GamaMoney.parse("EUR 555.53"));
        assertThat(assetTotal.getEnding()).isEqualTo(GamaMoney.parse("EUR 5000.77"));
        assertThat(assetTotal.getDepreciation()).isEqualTo(GamaMoney.parse("EUR 16999.23"));

        assertThat(assetDto.getPerPeriod().getIncoming()).isEqualTo(assetTotal.getIncoming());
        assertThat(assetDto.getPerPeriod().getBeginning()).isEqualTo(assetTotal.getBeginning());
        assertThat(assetDto.getPerPeriod().getExpense()).isEqualTo(assetTotal.getExpense());
        assertThat(assetDto.getPerPeriod().getEnding()).isEqualTo(assetTotal.getEnding());
        assertThat(assetDto.getPerPeriod().getDepreciation()).isEqualTo(assetTotal.getDepreciation());

        // conservation begins
        assetTotal = depreciationService.calcAssetTotal(List.of(assetDto), LocalDate.of(2017, 5, 1));
        assertThat(assetTotal.getIncoming()).isEqualTo(null);
        assertThat(assetTotal.getBeginning()).isEqualTo(GamaMoney.parse("EUR 5000.77"));
        assertThat(assetTotal.getExpense()).isEqualTo(null);
        assertThat(assetTotal.getEnding()).isEqualTo(GamaMoney.parse("EUR 5000.77"));
        assertThat(assetTotal.getDepreciation()).isEqualTo(GamaMoney.parse("EUR 16999.23"));

        assertThat(assetDto.getPerPeriod().getIncoming()).isEqualTo(assetTotal.getIncoming());
        assertThat(assetDto.getPerPeriod().getBeginning()).isEqualTo(assetTotal.getBeginning());
        assertThat(assetDto.getPerPeriod().getExpense()).isEqualTo(assetTotal.getExpense());
        assertThat(assetDto.getPerPeriod().getEnding()).isEqualTo(assetTotal.getEnding());
        assertThat(assetDto.getPerPeriod().getDepreciation()).isEqualTo(assetTotal.getDepreciation());

        assetTotal = depreciationService.calcAssetTotal(List.of(assetDto), LocalDate.of(2017, 12, 1));
        assertThat(assetTotal.getIncoming()).isEqualTo(null);
        assertThat(assetTotal.getBeginning()).isEqualTo(GamaMoney.parse("EUR 5000.77"));
        assertThat(assetTotal.getExpense()).isEqualTo(null);
        assertThat(assetTotal.getEnding()).isEqualTo(GamaMoney.parse("EUR 5000.77"));
        assertThat(assetTotal.getDepreciation()).isEqualTo(GamaMoney.parse("EUR 16999.23"));

        assertThat(assetDto.getPerPeriod().getIncoming()).isEqualTo(assetTotal.getIncoming());
        assertThat(assetDto.getPerPeriod().getBeginning()).isEqualTo(assetTotal.getBeginning());
        assertThat(assetDto.getPerPeriod().getExpense()).isEqualTo(assetTotal.getExpense());
        assertThat(assetDto.getPerPeriod().getEnding()).isEqualTo(assetTotal.getEnding());
        assertThat(assetDto.getPerPeriod().getDepreciation()).isEqualTo(assetTotal.getDepreciation());

        // conservation ends and starts operation
        assetTotal = depreciationService.calcAssetTotal(List.of(assetDto), LocalDate.of(2018, 2, 1));
        assertThat(assetTotal.getIncoming()).isEqualTo(null);
        assertThat(assetTotal.getBeginning()).isEqualTo(GamaMoney.parse("EUR 5000.77"));
        assertThat(assetTotal.getExpense()).isEqualTo(GamaMoney.parse("EUR 416.62"));
        assertThat(assetTotal.getEnding()).isEqualTo(GamaMoney.parse("EUR 4584.15"));
        assertThat(assetTotal.getDepreciation()).isEqualTo(GamaMoney.parse("EUR 17415.85"));

        assertThat(assetDto.getPerPeriod().getIncoming()).isEqualTo(assetTotal.getIncoming());
        assertThat(assetDto.getPerPeriod().getBeginning()).isEqualTo(assetTotal.getBeginning());
        assertThat(assetDto.getPerPeriod().getExpense()).isEqualTo(assetTotal.getExpense());
        assertThat(assetDto.getPerPeriod().getEnding()).isEqualTo(assetTotal.getEnding());
        assertThat(assetDto.getPerPeriod().getDepreciation()).isEqualTo(assetTotal.getDepreciation());

        // operation ends
        assetTotal = depreciationService.calcAssetTotal(List.of(assetDto), LocalDate.of(2019, 1, 1));
        assertThat(assetTotal.getIncoming()).isEqualTo(null);
        assertThat(assetTotal.getBeginning()).isEqualTo(GamaMoney.parse("EUR 417.65"));
        assertThat(assetTotal.getExpense()).isEqualTo(GamaMoney.parse("EUR 416.65"));
        assertThat(assetTotal.getEnding()).isEqualTo(GamaMoney.parse("EUR 1.00"));
        assertThat(assetTotal.getDepreciation()).isEqualTo(GamaMoney.parse("EUR 21999.00"));

        assertThat(assetDto.getPerPeriod().getIncoming()).isEqualTo(assetTotal.getIncoming());
        assertThat(assetDto.getPerPeriod().getBeginning()).isEqualTo(assetTotal.getBeginning());
        assertThat(assetDto.getPerPeriod().getExpense()).isEqualTo(assetTotal.getExpense());
        assertThat(assetDto.getPerPeriod().getEnding()).isEqualTo(assetTotal.getEnding());
        assertThat(assetDto.getPerPeriod().getDepreciation()).isEqualTo(assetTotal.getDepreciation());

        assetTotal = depreciationService.calcAssetTotal(List.of(assetDto), LocalDate.of(2020, 1, 1));
        assertThat(assetTotal.getIncoming()).isEqualTo(null);
        assertThat(assetTotal.getBeginning()).isEqualTo(GamaMoney.parse("EUR 1.00"));
        assertThat(assetTotal.getExpense()).isEqualTo(null);
        assertThat(assetTotal.getEnding()).isEqualTo(GamaMoney.parse("EUR 1.00"));
        assertThat(assetTotal.getDepreciation()).isEqualTo(GamaMoney.parse("EUR 21999.00"));

        assertThat(assetDto.getPerPeriod().getIncoming()).isEqualTo(assetTotal.getIncoming());
        assertThat(assetDto.getPerPeriod().getBeginning()).isEqualTo(assetTotal.getBeginning());
        assertThat(assetDto.getPerPeriod().getExpense()).isEqualTo(assetTotal.getExpense());
        assertThat(assetDto.getPerPeriod().getEnding()).isEqualTo(assetTotal.getEnding());
        assertThat(assetDto.getPerPeriod().getDepreciation()).isEqualTo(assetTotal.getDepreciation());
    }
}

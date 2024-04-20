package lt.gama.test.asset;

import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.helpers.DateUtils;
import lt.gama.model.dto.documents.DoubleEntryDto;
import lt.gama.model.dto.entities.AssetDto;
import lt.gama.model.dto.entities.GLOperationDto;
import lt.gama.model.dto.entities.ResponsibilityCenterDto;
import lt.gama.model.sql.documents.DoubleEntrySql;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.asset.AssetHistory;
import lt.gama.model.type.doc.DocRC;
import lt.gama.model.type.enums.AssetStatusType;
import lt.gama.model.type.enums.DepreciationType;
import lt.gama.model.type.gl.GLOperationAccount;
import lt.gama.service.ex.rt.GamaException;
import lt.gama.test.base.BaseDBTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AssetsDoubleEntryGenerationTest extends BaseDBTest {

    private ResponsibilityCenterDto rc;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        // set system time and time zone
        DateUtils.mockClock = Clock.fixed(
                LocalDateTime.of(2018, 4, 15, 0, 0).toInstant(ZoneOffset.UTC),
                ZoneId.systemDefault());
        login();

        rc = responsibilityCenterSqlMapper.toDto(createRC("RC1"));
    }

    @Test
    void testDoubleEntryGeneration() throws GamaApiException {
        {
            AssetDto asset = new AssetDto();
            asset.setCode("A");
            asset.setName("Asset A");

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
            asset.getHistory().add(change);

            APIResult<AssetDto> apiResult = assetApi.saveAsset(asset);
            assertThat(apiResult.getError()).isNull();

            AssetDto assetDto = apiResult.getData();

            assertThat(assetDto.getStatus()).isEqualTo(AssetStatusType.OPERATING);
            assertThat(assetDto.getDepreciation().size()).isEqualTo(36);
            assetDto.getDepreciation().stream()
                    .filter(d -> d.getDate().equals(LocalDate.of(2015, 2, 1)))
                    .forEach(d -> {
                        assertThat(d.getBeginning()).isEqualTo(GamaMoney.parse("EUR 20000.00"));
                        assertThat(d.getExpense()).isEqualTo(GamaMoney.parse("EUR 555.40"));
                        assertThat(d.getEnding()).isEqualTo(GamaMoney.parse("EUR 19444.60"));
                        assertThat(d.getDepreciation()).isEqualTo(GamaMoney.parse("EUR 2555.40"));
                    });
        }
        {
            AssetDto asset = new AssetDto();
            asset.setCode("B");
            asset.setName("Asset B");

            asset.setAccountCost(new GLOperationAccount("11", "A1"));
            asset.setAccountDepreciation(new GLOperationAccount("21", "B1"));
            asset.setAccountRevaluation(new GLOperationAccount("31", "C1"));
            asset.setAccountExpense(new GLOperationAccount("41", "D1"));
            asset.setRcExpense(Collections.singletonList(new DocRC(rc)));

            asset.setAcquisitionDate(LocalDate.of(2014, 9, 30));
            asset.setCost(GamaMoney.parse("EUR 22000.00"));

            asset.setDate(LocalDate.of(2015, 2, 1));
            asset.setValue(GamaMoney.parse("EUR 20099.00"));
            asset.setExpenses(GamaMoney.parse("EUR 1901.00"));
            asset.setHistory(new ArrayList<>());

            AssetHistory change = new AssetHistory();
            change.setDate(asset.getDate());
            change.setFinalDate(LocalDate.of(2020, 2, 1));
            change.setType(DepreciationType.DOUBLE);
            change.setStatus(AssetStatusType.OPERATING);
            change.setDtValue(GamaMoney.parse("EUR 100.00"));
            change.setDtExpense(GamaMoney.parse("EUR 200.00"));
            change.setEnding(GamaMoney.parse("EUR 1.00"));
            asset.getHistory().add(change);

            APIResult<AssetDto> apiResult = assetApi.saveAsset(asset);
            assertThat(apiResult.getError()).isNull();

            AssetDto assetDto = apiResult.getData();
            assetDto.getDepreciation().stream()
                    .filter(d -> d.getDate().equals(LocalDate.of(2015, 2, 1)))
                    .forEach(d -> {
                        assertThat(d.getBeginning()).isEqualTo(GamaMoney.parse("EUR 20099.00"));
                        assertThat(d.getDtValue()).isEqualTo(GamaMoney.parse("EUR 100.00"));
                        assertThat(d.getDtExpense()).isEqualTo(GamaMoney.parse("EUR 200.00"));
                        assertThat(d.getExpense()).isEqualTo(GamaMoney.parse("EUR 666.67"));
                        assertThat(d.getEnding()).isEqualTo(GamaMoney.parse("EUR 19332.33"));
                        assertThat(d.getDepreciation()).isEqualTo(GamaMoney.parse("EUR 2767.67"));
                    });
        }
        {
            AssetDto asset = new AssetDto();
            asset.setCode("C");
            asset.setName("Asset C");

            asset.setAccountCost(new GLOperationAccount("11", "A1"));
            asset.setAccountDepreciation(new GLOperationAccount("21", "B1"));
            asset.setAccountRevaluation(new GLOperationAccount("31", "C1"));
            asset.setAccountExpense(new GLOperationAccount("41", "D1"));
            asset.setRcExpense(Collections.singletonList(new DocRC(rc)));

            asset.setAcquisitionDate(LocalDate.of(2014, 9, 30));
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

            APIResult<AssetDto> apiResult = assetApi.saveAsset(asset);
            assertThat(apiResult.getError()).isNull();

            AssetDto assetDto = apiResult.getData();

            assertThat(assetDto.getDepreciation().size()).isEqualTo(11);
            assetDto.getDepreciation().stream()
                    .filter(d -> d.getDate().equals(LocalDate.of(2015, 2, 1)))
                    .forEach(d -> {
                        assertThat(d.getBeginning()).isEqualTo(GamaMoney.parse("EUR 20099.00"));
                        assertThat(d.getDtValue()).isEqualTo(GamaMoney.parse("EUR 100.00"));
                        assertThat(d.getDtExpense()).isEqualTo(GamaMoney.parse("EUR 200.00"));
                        assertThat(d.getExpense()).isEqualTo(GamaMoney.parse("EUR 727.20"));
                        assertThat(d.getEnding()).isEqualTo(GamaMoney.parse("EUR 19271.80"));
                        assertThat(d.getDepreciation()).isEqualTo(GamaMoney.parse("EUR 2828.20"));
                    });
        }
        {
            AssetDto asset = new AssetDto();
            asset.setCode("D");
            asset.setName("Asset D");

            asset.setAccountCost(new GLOperationAccount("11", "A1"));
            asset.setAccountDepreciation(new GLOperationAccount("21", "B1"));
            asset.setAccountRevaluation(new GLOperationAccount("31", "C1"));
            asset.setAccountExpense(new GLOperationAccount("42", "D2"));
            asset.setRcExpense(Collections.singletonList(new DocRC(rc)));

            asset.setAcquisitionDate(LocalDate.of(2014, 9, 30));
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

            APIResult<AssetDto> apiResult = assetApi.saveAsset(asset);
            assertThat(apiResult.getError()).isNull();

            AssetDto assetDto = apiResult.getData();
            assertThat(assetDto.getDepreciation().size()).isEqualTo(11);
            assetDto.getDepreciation().stream()
                    .filter(d -> d.getDate().equals(LocalDate.of(2015, 2, 1)))
                    .forEach(d -> {
                        assertThat(d.getBeginning()).isEqualTo(GamaMoney.parse("EUR 20099.00"));
                        assertThat(d.getDtValue()).isEqualTo(GamaMoney.parse("EUR 100.00"));
                        assertThat(d.getDtExpense()).isEqualTo(GamaMoney.parse("EUR 200.00"));
                        assertThat(d.getExpense()).isEqualTo(GamaMoney.parse("EUR 727.20"));
                        assertThat(d.getEnding()).isEqualTo(GamaMoney.parse("EUR 19271.80"));
                        assertThat(d.getDepreciation()).isEqualTo(GamaMoney.parse("EUR 2828.20"));
                    });
        }

        clearCaches();

        {
            GamaException e = assertThrows(GamaException.class, () -> depreciationService.generateDE(LocalDate.of(2014, 2, 1)));
            assertThat(e.getMessage()).isEqualTo("No data for double-entry of 2014-02-01");
        }

        DoubleEntryDto de = depreciationService.generateDE(LocalDate.of(2015, 2, 1));
        checkDoubleEntry(de);

        clearCaches();

        List<DoubleEntrySql> list = dbServiceSQL.makeQueryInCompany(DoubleEntrySql.class, DoubleEntrySql.GRAPH_ALL).getResultList();
        assertThat(list.size()).isEqualTo(1);
        checkDoubleEntry(doubleEntrySqlMapper.toDto(list.get(0)));
    }

    private void checkDoubleEntry(DoubleEntryDto doubleEntry) {
        assertThat(doubleEntry.getId()).isNotNull();
        assertThat(doubleEntry.getDate()).isEqualTo(LocalDate.of(2015, 2, 28));
        assertThat(doubleEntry.getOperations().size()).isEqualTo(3);
        doubleEntry.getOperations().sort(Comparator.comparing(GLOperationDto::getAmount));
        assertThat(doubleEntry.getOperations().get(0).getDebit().getNumber()).isEqualTo("41");
        assertThat(doubleEntry.getOperations().get(0).getCredit().getNumber()).isEqualTo("21");
        assertThat(doubleEntry.getOperations().get(0).getAmount()).isEqualTo(GamaMoney.parse("EUR 555.40"));
        assertThat(doubleEntry.getOperations().get(0).getDebitRC()).isNullOrEmpty();
        assertThat(doubleEntry.getOperations().get(0).getCreditRC()).isNullOrEmpty();
        assertThat(doubleEntry.getOperations().get(1).getDebit().getNumber()).isEqualTo("42");
        assertThat(doubleEntry.getOperations().get(1).getCredit().getNumber()).isEqualTo("21");
        assertThat(doubleEntry.getOperations().get(1).getAmount()).isEqualTo(GamaMoney.parse("EUR 927.20"));
        assertThat(doubleEntry.getOperations().get(1).getDebitRC()).isEqualTo(Collections.singletonList(new DocRC(rc)));
        assertThat(doubleEntry.getOperations().get(1).getCreditRC()).isNullOrEmpty();
        assertThat(doubleEntry.getOperations().get(2).getDebit().getNumber()).isEqualTo("41");
        assertThat(doubleEntry.getOperations().get(2).getCredit().getNumber()).isEqualTo("21");
        assertThat(doubleEntry.getOperations().get(2).getAmount()).isEqualTo(GamaMoney.parse("EUR 1793.87"));
        assertThat(doubleEntry.getOperations().get(2).getDebitRC()).isEqualTo(Collections.singletonList(new DocRC(rc)));
        assertThat(doubleEntry.getOperations().get(2).getCreditRC()).isNullOrEmpty();
    }
}

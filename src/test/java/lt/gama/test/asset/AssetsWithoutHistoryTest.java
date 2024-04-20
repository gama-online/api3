package lt.gama.test.asset;

import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.PageRequest;
import lt.gama.api.request.PageRequestCondition;
import lt.gama.api.response.PageResponse;
import lt.gama.model.dto.entities.AssetDto;
import lt.gama.model.dto.type.AssetTotal;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.enums.CustomSearchType;
import lt.gama.model.type.gl.GLOperationAccount;
import lt.gama.test.base.BaseDBTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

public class AssetsWithoutHistoryTest extends BaseDBTest {

    @Test
    void testListAssetWithoutHistory() throws GamaApiException {
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

        APIResult<AssetDto> apiResult = assetApi.saveAsset(asset);
        assertThat(apiResult.getError()).isNull();

        PageRequest request = new PageRequest();
        request.setConditions(new ArrayList<>());
        request.getConditions().add(new PageRequestCondition(CustomSearchType.DATE, LocalDate.of(2015, 4, 1)));

        APIResult<PageResponse<AssetDto, AssetTotal>> result = assetApi.listAsset(request);
        assertThat(result.getError()).isNull();

        PageResponse<AssetDto, AssetTotal> response = result.getData();
        assertThat(response.getTotal()).isEqualTo(1);
        assertThat(response.getItems().get(0).getName()).isEqualTo("Asset A");
    }
}

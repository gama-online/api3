package lt.gama.api.service;

import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.IdRequest;
import lt.gama.api.request.InventoryBalanceRequest;
import lt.gama.api.request.PageRequest;
import lt.gama.api.response.PageResponse;
import lt.gama.auth.annotation.RequiresPermissions;
import lt.gama.model.dto.entities.ManufacturerDto;
import lt.gama.model.dto.entities.PartDto;
import lt.gama.model.dto.entities.RecipeDto;
import lt.gama.model.sql.system.CountryVatRateSql;
import lt.gama.model.type.enums.Permission;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import static lt.gama.api.service.Api.APP_API_3_PATH;

@RequestMapping(APP_API_3_PATH + "part")
@RequiresPermissions
public interface PartApi extends Api {

    @PostMapping("/listPart")
    @RequiresPermissions({Permission.PART_R, Permission.PART_M, Permission.GL})
    APIResult<PageResponse<PartDto, Void>> listPart(PageRequest request) throws GamaApiException;

    @PostMapping("/savePart")
    @RequiresPermissions({Permission.PART_M, Permission.GL})
    APIResult<PartDto> savePart(PartDto request) throws GamaApiException;

    @PostMapping("/getPart")
    @RequiresPermissions({Permission.PART_R, Permission.PART_M, Permission.GL})
    APIResult<PartDto> getPart(IdRequest request) throws GamaApiException;

    @PostMapping("/deletePart")
    @RequiresPermissions({Permission.PART_M, Permission.GL})
    APIResult<Void> deletePart(IdRequest request) throws GamaApiException;

    @PostMapping("/undeletePart")
    @RequiresPermissions({Permission.PART_M, Permission.GL})
    APIResult<PartDto> undeletePart(IdRequest request) throws GamaApiException;


    @PostMapping("/getPartRemainder")
    @RequiresPermissions({Permission.PART_R, Permission.PART_M, Permission.GL})
    APIResult<PartDto> getPartRemainder(InventoryBalanceRequest request) throws GamaApiException;


    @PostMapping("/listRecipe")
    @RequiresPermissions({Permission.PART_R, Permission.PART_M, Permission.GL})
    APIResult<PageResponse<RecipeDto, Void>> listRecipe(PageRequest request) throws GamaApiException;

    @PostMapping("/saveRecipe")
    @RequiresPermissions({Permission.PART_M, Permission.GL})
    APIResult<RecipeDto> saveRecipe(RecipeDto request) throws GamaApiException;

    @PostMapping("/getRecipe")
    @RequiresPermissions({Permission.PART_R, Permission.PART_M, Permission.GL})
    APIResult<RecipeDto> getRecipe(IdRequest request) throws GamaApiException;

    @PostMapping("/deleteRecipe")
    @RequiresPermissions({Permission.PART_M, Permission.GL})
    APIResult<Void> deleteRecipe(IdRequest request) throws GamaApiException;

    /*
     * VAT rates
     */

    @PostMapping("/getVatRate")
    APIResult<CountryVatRateSql> getVatRate() throws GamaApiException;

    /*
     * Manufacturer
     */

    @PostMapping("/listManufacturer")
    @RequiresPermissions({Permission.PART_R, Permission.PART_M, Permission.GL})
    APIResult<PageResponse<ManufacturerDto, Void>> listManufacturer(PageRequest request) throws GamaApiException;

    @PostMapping("/saveManufacturer")
    @RequiresPermissions({Permission.PART_M, Permission.GL})
    APIResult<ManufacturerDto> saveManufacturer(ManufacturerDto request) throws GamaApiException;

    @PostMapping("/getManufacturer")
    @RequiresPermissions({Permission.PART_R, Permission.PART_M, Permission.GL})
    APIResult<ManufacturerDto> getManufacturer(IdRequest request) throws GamaApiException;

    @PostMapping("/deleteManufacturer")
    @RequiresPermissions({Permission.PART_M, Permission.GL})
    APIResult<Void> deleteManufacturer(IdRequest request) throws GamaApiException;

//    /*
//     * price-list
//     */
//
//    @PostMapping("/listPricelist")
//    @RequiresPermissions({Permission.PART_R, Permission.PART_M, Permission.GL})
//    APIResult<PageResponse<Pl, Void>> listPricelist(PageRequest request) throws GamaApiException;
//
//    @PostMapping("/savePricelist")
//    @RequiresPermissions({Permission.PART_M, Permission.GL})
//    APIResult<Pl> savePricelist(Pl request) throws GamaApiException;
//
//    @PostMapping("/getPricelist")
//    @RequiresPermissions({Permission.PART_R, Permission.PART_M, Permission.GL})
//    APIResult<Pl> getPricelist(IdRequest request) throws GamaApiException;
//
//    @PostMapping("/deletePricelist")
//    @RequiresPermissions({Permission.PART_M, Permission.GL})
//    APIResult<Void> deletePricelist(IdRequest request) throws GamaApiException;
//
//    /*
//     * Pricelist part methods:
//     */
//
//    @PostMapping("/listPricelistPart")
//    @RequiresPermissions({Permission.PART_R, Permission.PART_M, Permission.GL})
//    APIResult<PageResponse<PlPart, Void>> listPricelistPart(PageRequest request) throws GamaApiException;
//
//    @PostMapping("/savePricelistPart")
//    @RequiresPermissions({Permission.PART_M, Permission.GL})
//    APIResult<PlPart> savePricelistPart(PlPart request) throws GamaApiException;
//
//    @PostMapping("/getPricelistPart")
//    @RequiresPermissions({Permission.PART_R, Permission.PART_M, Permission.GL})
//    APIResult<PlPart> getPricelistPart(IdRequest request) throws GamaApiException;
//
//    @PostMapping("/deletePricelistPart")
//    @RequiresPermissions({Permission.PART_M, Permission.GL})
//    APIResult<Void> deletePricelistPart(IdRequest request) throws GamaApiException;
//
//    /*
//     * Pricelist discount methods:
//     */
//
//    @PostMapping("/listPricelistDiscount")
//    @RequiresPermissions({Permission.PART_R, Permission.PART_M, Permission.GL})
//    APIResult<PageResponse<PlDiscount, Void>> listPricelistDiscount(PageRequest request) throws GamaApiException;
//
//    @PostMapping("/savePricelistDiscount")
//    @RequiresPermissions({Permission.PART_M, Permission.GL})
//    APIResult<PlDiscount> savePricelistDiscount(PlDiscount request) throws GamaApiException;
//
//    @PostMapping("/getPricelistDiscount")
//    @RequiresPermissions({Permission.PART_R, Permission.PART_M, Permission.GL})
//    APIResult<PlDiscount> getPricelistDiscount(IdRequest request) throws GamaApiException;
//
//    @PostMapping("/deletePricelistDiscount")
//    @RequiresPermissions({Permission.PART_M, Permission.GL})
//    APIResult<String> deletePricelistDiscount(IdRequest request) throws GamaApiException;
//
//    /*
//     * Others
//     */
//
//    @PostMapping("/listPartPricelist")
//    @RequiresPermissions({Permission.PART_R, Permission.PART_M, Permission.GL})
//    APIResult<PageResponse<PlPart, Void>> listPartPricelist(PageRequest request) throws GamaApiException;
//
//    @PostMapping("/actualPrice")
//    @RequiresPermissions({Permission.PART_R, Permission.PART_M, Permission.GL})
//    APIResult<List<PlPrice>> actualPrice(IdRequest request) throws GamaApiException;
//
//    @PostMapping("/listPricelistActual")
//    @RequiresPermissions({Permission.PART_R, Permission.PART_M, Permission.GL})
//    APIResult<PageResponse<PlActual, Void>> listPricelistActual(PageRequest request) throws GamaApiException;
//
//    @PostMapping("/getPricelistActual")
//    @RequiresPermissions({Permission.PART_R, Permission.PART_M, Permission.GL})
//    APIResult<PlActual> getPricelistActual(IdRequest request) throws GamaApiException;

    @PostMapping("/syncPartTask")
    @RequiresPermissions({Permission.PART_M, Permission.GL})
    APIResult<String> syncPartTask() throws GamaApiException;

    @PostMapping("/syncWarehousePartsTask")
    @RequiresPermissions({Permission.PART_M, Permission.GL})
    APIResult<String> syncWarehousePartsTask() throws GamaApiException;
}

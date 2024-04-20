//package lt.gama.impexp;
//
//import com.google.inject.Injector;
//import lt.gama.impexp.link.*;
//import lt.gama.model.dto.documents.*;
//import lt.gama.model.i.ICompany;
//import lt.gama.model.sql.documents.DoubleEntrySql;
//import lt.gama.model.sql.documents.EstimateSql;
//import lt.gama.model.sql.documents.GLOpeningBalanceSql;
//import lt.gama.model.sql.documents.InventorySql;
//import lt.gama.model.sql.entities.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Service;
//
//import javax.inject.Inject;
//import java.lang.invoke.MethodHandles;
//import java.util.HashMap;
//import java.util.Map;
//
//import static java.util.Map.entry;
//
///**
// * Gama
// * Created by valdas on 15-06-10.
// */
//@Service
//public class MapLink {
//
//    private static final Logger log = LoggerFactory.getLogger(MapLink.class);
//
//
//    /**
//     * Add here all entities maps
//     */
//    static private final Map<Class<? extends ICompany>, Class<? extends LinkBase>> mapTypes = Map.ofEntries(
//            entry(AssetSql.class, LinkAsset.class),
//            entry(EmployeeCardSql.class, LinkEmployeeCardSql.class),
//            entry(PartSql.class, LinkPartSql.class),
//            entry(PositionSql.class, LinkPosition.class),
//
//            entry(EmployeeOperationDto.class, LinkEmployeeOperationDto.class),
//            entry(BankOperationDto.class, LinkBankOperationDto.class),
//            entry(CashOperationDto.class, LinkCashOperationDto.class),
//            entry(DebtCorrectionDto.class, LinkDebtCorrectionDto.class),
//
//            entry(CounterpartySql.class, LinkCounterpartySql.class),
//            entry(DoubleEntrySql.class, LinkDoubleEntry.class),
//            entry(InventoryDto.class, LinkInventoryDto.class),
//            entry(InvoiceDto.class, LinkInvoiceDto.class),
//            entry(PurchaseDto.class, LinkPurchaseDto.class),
//            entry(TransProdDto.class, LinkTransProdDto.class),
//            entry(EstimateDto.class, LinkEstimateDto.class),
//
//            entry(InventorySql.class, LinkInventoryDto.class),
//            entry(EstimateSql.class, LinkEstimateDto.class),
//
//            entry(EmployeeOpeningBalanceDto.class, LinkEmployeeOpeningBalanceDto.class),
//            entry(BankOpeningBalanceDto.class, LinkBankOpeningBalanceDto.class),
//            entry(CashOpeningBalanceDto.class, LinkCashOpeningBalanceDto.class),
//            entry(DebtOpeningBalanceDto.class, LinkDebtOpeningBalanceDto.class),
//
//            entry(GLOpeningBalanceSql.class, LinkGLOpeningBalance.class),
//            entry(InventoryOpeningBalanceDto.class, LinkInventoryOpeningBalanceDto.class),
//
//            entry(EmployeeRateInfluenceDto.class, LinkEmployeeRateInfluenceDto.class),
//            entry(BankRateInfluenceDto.class, LinkBankRateInfluenceDto.class),
//            entry(CashRateInfluenceDto.class, LinkCashRateInfluenceDto.class),
//
//            entry(DebtRateInfluenceDto.class, LinkDebtRateInfluenceDto.class));
//
//    private Map<Class<?>, LinkBase<?>> mapMaps = null;
//
//    public MapLink(Injector injector) {
//        this.injector = injector;
//    }
//
//    @Override
//    public LinkBase getMap(Class type) {
//        if (mapMaps == null) mapMaps = new HashMap<>();
//
//        LinkBase<?> map = mapMaps.get(type);
//        if (map == null) {
//            Class<?> classType = mapTypes.get(type);
//            if (classType != null) {
//                try {
//                    map = (LinkBase<?>) classType.newInstance();
//                    injector.injectMembers(map);
//                    mapMaps.put(type, map);
//
//                } catch (InstantiationException | IllegalAccessException e) {
//                    log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
//                }
//            }
//        }
//        return map;
//    }
//}

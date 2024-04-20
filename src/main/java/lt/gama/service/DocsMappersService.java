package lt.gama.service;

import com.openhtmltopdf.outputdevice.helper.BaseDocument;
import lt.gama.helpers.EntityUtils;
import lt.gama.model.dto.base.BaseDocumentDto;
import lt.gama.model.i.base.IBaseDocument;
import lt.gama.model.mappers.*;
import lt.gama.model.sql.base.BaseDocumentSql;
import lt.gama.model.sql.documents.*;
import lt.gama.model.type.enums.DBType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

import static java.util.Map.entry;

@Service
public class DocsMappersService {

    // SQL mappers
    @Autowired private DebtCorrectionSqlMapper debtCorrectionSqlMapper;
    @Autowired private DebtOpeningBalanceSqlMapper debtOpeningBalanceSqlMapper;
    @Autowired private DebtRateInfluenceSqlMapper debtRateInfluenceSqlMapper;

    @Autowired private EmployeeOpeningBalanceSqlMapper employeeOpeningBalanceSqlMapper;
    @Autowired private EmployeeOperationSqlMapper employeeOperationSqlMapper;
    @Autowired private EmployeeRateInfluenceSqlMapper employeeRateInfluenceSqlMapper;

    @Autowired private BankOpeningBalanceSqlMapper bankOpeningBalanceSqlMapper;
    @Autowired private BankOperationSqlMapper bankOperationSqlMapper;
    @Autowired private BankRateInfluenceSqlMapper bankRateInfluenceSqlMapper;

    @Autowired private CashOpeningBalanceSqlMapper cashOpeningBalanceSqlMapper;
    @Autowired private CashOperationSqlMapper cashOperationSqlMapper;
    @Autowired private CashRateInfluenceSqlMapper cashRateInfluenceSqlMapper;

    @Autowired private SalarySqlMapper salarySqlMapper;

    @Autowired private EstimateSqlMapper estimateSqlMapper;
    @Autowired private InventorySqlMapper inventorySqlMapper;
    @Autowired private InventoryOpeningBalanceSqlMapper inventoryOpeningBalanceSqlMapper;
    @Autowired private InvoiceSqlMapper invoiceSqlMapper;
    @Autowired private OrderSqlMapper orderSqlMapper;
    @Autowired private PurchaseSqlMapper purchaseSqlMapper;
    @Autowired private TransProdSqlMapper transportationSqlMapper;

    private volatile Map<Class<? extends IBaseDocument>, IBaseMapper<? extends BaseDocumentDto, ? extends BaseDocumentSql>> MAPPERS = null;
    private final DummyMapper<? extends IBaseDocument> dummyMapper = new DummyMapper<>();

    private volatile Map<Class<? extends IBaseDocument>, String> GRAPHS = null;
    private volatile Map<String, Class<? extends BaseDocumentSql>> DOC_TYPES = null;

    private final Object MAPPERS_KEY = new Object();
    private final Object GRAPHS_KEY = new Object();
    private final Object DOC_TYPES_KEY = new Object();

    private Map<Class<? extends IBaseDocument>, IBaseMapper<? extends BaseDocumentDto, ? extends BaseDocumentSql>> getMappers() {
        var localRef = MAPPERS;
        if (localRef == null) {
            synchronized (MAPPERS_KEY) {
                localRef = MAPPERS;
                if (localRef == null) {
                    MAPPERS = localRef = Map.ofEntries(
                            // SQL mappers
                            entry(DebtOpeningBalanceSql.class, debtOpeningBalanceSqlMapper),
                            entry(DebtCorrectionSql.class, debtCorrectionSqlMapper),
                            entry(DebtRateInfluenceSql.class, debtRateInfluenceSqlMapper),

                            entry(EmployeeOpeningBalanceSql.class, employeeOpeningBalanceSqlMapper),
                            entry(EmployeeOperationSql.class, employeeOperationSqlMapper),
                            entry(EmployeeRateInfluenceSql.class, employeeRateInfluenceSqlMapper),

                            entry(BankOpeningBalanceSql.class, bankOpeningBalanceSqlMapper),
                            entry(BankOperationSql.class, bankOperationSqlMapper),
                            entry(BankRateInfluenceSql.class, bankRateInfluenceSqlMapper),

                            entry(CashOpeningBalanceSql.class, cashOpeningBalanceSqlMapper),
                            entry(CashOperationSql.class, cashOperationSqlMapper),
                            entry(CashRateInfluenceSql.class, cashRateInfluenceSqlMapper),

                            entry(SalarySql.class, salarySqlMapper),

                            entry(EstimateSql.class, estimateSqlMapper),
                            entry(InventorySql.class, inventorySqlMapper),
                            entry(InventoryOpeningBalanceSql.class, inventoryOpeningBalanceSqlMapper),
                            entry(InvoiceSql.class, invoiceSqlMapper),
                            entry(OrderSql.class, orderSqlMapper),
                            entry(PurchaseSql.class, purchaseSqlMapper),
                            entry(TransProdSql.class, transportationSqlMapper));
                }
            }
        }
        return localRef;
    }

    private Map<Class<? extends IBaseDocument>, String> getGraphs() {
        var localRef = GRAPHS;
        if (localRef == null) {
            synchronized (GRAPHS_KEY) {
                localRef = GRAPHS;
                if (localRef == null) {
                    GRAPHS = localRef = Map.ofEntries(
                            entry(DebtOpeningBalanceSql.class, DebtOpeningBalanceSql.GRAPH_ALL),
                            entry(DebtCorrectionSql.class, DebtCorrectionSql.GRAPH_ALL),
                            entry(DebtRateInfluenceSql.class, DebtRateInfluenceSql.GRAPH_ALL),

                            entry(EmployeeOpeningBalanceSql.class, EmployeeOpeningBalanceSql.GRAPH_ALL),
                            entry(EmployeeOperationSql.class, EmployeeOperationSql.GRAPH_ALL),
                            entry(EmployeeRateInfluenceSql.class, EmployeeRateInfluenceSql.GRAPH_ALL),

                            entry(BankOpeningBalanceSql.class, BankOpeningBalanceSql.GRAPH_ALL),
                            entry(BankOperationSql.class, BankOperationSql.GRAPH_ALL),
                            entry(BankRateInfluenceSql.class, BankRateInfluenceSql.GRAPH_ALL),

                            entry(CashOpeningBalanceSql.class, CashOpeningBalanceSql.GRAPH_ALL),
                            entry(CashOperationSql.class, CashOperationSql.GRAPH_ALL),
                            entry(CashRateInfluenceSql.class, CashRateInfluenceSql.GRAPH_ALL),

                            entry(EstimateSql.class, EstimateSql.GRAPH_ALL),
                            entry(InventorySql.class, InventorySql.GRAPH_ALL),
                            entry(InventoryOpeningBalanceSql.class, InventoryOpeningBalanceSql.GRAPH_ALL),
                            entry(InvoiceSql.class, InvoiceSql.GRAPH_ALL),
                            entry(OrderSql.class, OrderSql.GRAPH_ALL),
                            entry(PurchaseSql.class, PurchaseSql.GRAPH_ALL),
                            entry(TransProdSql.class, TransProdSql.GRAPH_ALL));
                }
            }
        }
        return localRef;
    }

    private Map<String, Class<? extends BaseDocumentSql>> getDocumentTypes() {
        var localRef = DOC_TYPES;
        if (localRef == null) {
            synchronized (DOC_TYPES_KEY) {
                localRef = DOC_TYPES;
                if (localRef == null) {
                    DOC_TYPES = localRef = Map.ofEntries(
                            entry(EntityUtils.normalizeEntityClassName(EmployeeOpeningBalanceSql.class), EmployeeOpeningBalanceSql.class),
                            entry(EntityUtils.normalizeEntityClassName(EmployeeOperationSql.class), EmployeeOperationSql.class),
                            entry(EntityUtils.normalizeEntityClassName(EmployeeRateInfluenceSql.class), EmployeeRateInfluenceSql.class),

                            entry(EntityUtils.normalizeEntityClassName(BankOpeningBalanceSql.class), BankOpeningBalanceSql.class),
                            entry(EntityUtils.normalizeEntityClassName(BankOperationSql.class), BankOperationSql.class),
                            entry(EntityUtils.normalizeEntityClassName(BankRateInfluenceSql.class), BankRateInfluenceSql.class),

                            entry(EntityUtils.normalizeEntityClassName(CashOpeningBalanceSql.class), CashOpeningBalanceSql.class),
                            entry(EntityUtils.normalizeEntityClassName(CashOperationSql.class), CashOperationSql.class),
                            entry(EntityUtils.normalizeEntityClassName(CashRateInfluenceSql.class), CashRateInfluenceSql.class),

                            entry(EntityUtils.normalizeEntityClassName(DebtCorrectionSql.class), DebtCorrectionSql.class),
                            entry(EntityUtils.normalizeEntityClassName(DebtOpeningBalanceSql.class), DebtOpeningBalanceSql.class),
                            entry(EntityUtils.normalizeEntityClassName(DebtRateInfluenceSql.class), DebtRateInfluenceSql.class),

                            entry(EntityUtils.normalizeEntityClassName(SalarySql.class), SalarySql.class),

                            entry(EntityUtils.normalizeEntityClassName(EstimateSql.class), EstimateSql.class),
                            entry(EntityUtils.normalizeEntityClassName(InventorySql.class), InventorySql.class),
                            entry(EntityUtils.normalizeEntityClassName(InventoryOpeningBalanceSql.class), InventoryOpeningBalanceSql.class),
                            entry(EntityUtils.normalizeEntityClassName(InvoiceSql.class), InvoiceSql.class),
                            entry(EntityUtils.normalizeEntityClassName(OrderSql.class), OrderSql.class),
                            entry(EntityUtils.normalizeEntityClassName(PurchaseSql.class), PurchaseSql.class),
                            entry(EntityUtils.normalizeEntityClassName(TransProdSql.class), TransProdSql.class));
                }
            }
        }
        return localRef;
    }

    public <E extends BaseDocumentSql> IBaseMapper<? extends BaseDocumentDto, E> getOrNull(Class<E> clazz) {
        //noinspection unchecked
        return (IBaseMapper<BaseDocumentDto, E>) getMappers().get(clazz);
    }

    public String getGraphName(Class<? extends BaseDocumentSql> clazz) {
        return clazz == null ? null : getGraphs().get(clazz);
    }

    public Class<? extends BaseDocumentSql> getDocumentClass(DBType dbType, String documentType) {
        return getDocumentTypes().get(documentType);
    }
}


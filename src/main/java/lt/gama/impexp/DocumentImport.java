package lt.gama.impexp;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lt.gama.model.dto.base.BaseCompanyDto;
import lt.gama.model.dto.documents.*;
import lt.gama.model.i.base.IBaseDocument;
import lt.gama.model.type.enums.DBType;

/**
 * Gama
 * Created by valdas on 15-06-08.
 */
public class DocumentImport extends BaseCompanyDto {

    private int type;

    private Boolean finished;

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@class")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = EmployeeOperationDto.class, name = "AdvanceOperation"),
            @JsonSubTypes.Type(value = EmployeeOpeningBalanceDto.class, name = "AdvanceOpening"),
            @JsonSubTypes.Type(value = BankOperationDto.class, name = "BankOperation"),
            @JsonSubTypes.Type(value = BankOpeningBalanceDto.class, name = "BankOpening"),
            @JsonSubTypes.Type(value = CashOperationDto.class, name = "CashOrder"),
            @JsonSubTypes.Type(value = CashOpeningBalanceDto.class, name = "CashOpening"),
            @JsonSubTypes.Type(value = DebtCorrectionDto.class, name = "DebtCorrection"),
            @JsonSubTypes.Type(value = DebtOpeningBalanceDto.class, name = "DebtOpeningBalance"),

            @JsonSubTypes.Type(value = EstimateDto.class, name = "Estimate"),
            @JsonSubTypes.Type(value = InventoryDto.class, name = "Inventory"),
            @JsonSubTypes.Type(value = InventoryOpeningBalanceDto.class, name = "InventoryOpening"),
            @JsonSubTypes.Type(value = InvoiceDto.class, name = "Invoice"),
            @JsonSubTypes.Type(value = OrderDto.class, name = "Order"),
            @JsonSubTypes.Type(value = PurchaseDto.class, name = "Purchase"),
            @JsonSubTypes.Type(value = TransProdDto.class, name = "TransProd"),

            @JsonSubTypes.Type(value = EmployeeRateInfluenceDto.class, name = "AdvanceRateInfluence"),
            @JsonSubTypes.Type(value = BankRateInfluenceDto.class, name = "BankRateInfluence"),
            @JsonSubTypes.Type(value = CashRateInfluenceDto.class, name = "CashRateInfluence"),
            @JsonSubTypes.Type(value = DebtRateInfluenceDto.class, name = "DebtRateInfluence"),
    })
    private IBaseDocument doc;

    private DoubleEntryDto gl;

    public DocumentImport() {
    }

    public DocumentImport(long companyId, Long id, int type, Boolean finished, IBaseDocument doc, DoubleEntryDto gl) {
        super(companyId, id, DBType.POSTGRESQL);
        this.type = type;
        this.finished = finished;
        this.doc = doc;
        this.gl = gl;
    }

    // generated

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Boolean getFinished() {
        return finished;
    }

    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    public IBaseDocument getDoc() {
        return doc;
    }

    public void setDoc(IBaseDocument doc) {
        this.doc = doc;
    }

    public DoubleEntryDto getGl() {
        return gl;
    }

    public void setGl(DoubleEntryDto gl) {
        this.gl = gl;
    }

    @Override
    public String toString() {
        return "DocumentImport{" +
                "type=" + type +
                ", finished=" + finished +
                ", doc=" + doc +
                ", gl=" + gl +
                "} " + super.toString();
    }
}

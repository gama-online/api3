package lt.gama.integrations.vmi;

import lt.gama.integrations.vmi.types.TaxFreeState;
import lt.gama.model.type.enums.DBType;
import lt.gama.model.type.inventory.TaxFree;

import javax.net.ssl.SSLContext;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public interface ITaxRefundService {

    SSLContext getSSLContext();

    String makeDocId();

    TaxFree generateInvoiceTaxFreeSQL(long id, DBType db);
//TODO remove comments
//    InvoiceDto saveInvoiceTaxFreeSQL(long id, TaxFree taxFree, DBType db);
//    InvoiceDto submitInvoiceTaxFreeSQL(long id, DBType db);
//    InvoiceDto cancelInvoiceTaxFreeSQL(long id, DBType db);
//    InvoiceDto submitInvoiceTaxFreePaymentInfoSQL(long id, DBType db);

    QueryDeclarationsResponse queryTaxFreeDeclarations(LocalDateTime timestampFrom, LocalDateTime timestampTo);
    DeclarationInfoResponse getTaxFreeInfoOnExportedGoods(String docId);

    String testCert();

    String testVMISubmitDeclaration(String docId);
    String testVMICancelDeclaration(String docId);
//TODO remove comments
//    String testVMIQueryDeclarations(String docId, LocalDateTime timestampFrom, LocalDateTime timestampTo, DeclStateForQueryType state);
    String testVMIGetInfoOnExportedGoods(String docId);
    String testVMISubmitPaymentInfo(String docId);


    abstract class DeclarationBaseResponse {
        protected boolean success;
        protected TaxFreeState state;
        protected List<DeclarationError> errors;
        protected LocalDateTime resultDate;

        protected static <T extends DeclarationBaseResponse> T ok(T response, LocalDateTime resultDate) {
            response.success = true;
            response.resultDate = resultDate;
            return response;
        }

        protected static <T extends DeclarationBaseResponse> T ok(T response, LocalDateTime resultDate, TaxFreeState state) {
            ok(response, resultDate);
            response.state = state;
            return response;
        }

        protected static <T extends DeclarationBaseResponse> T error(T response, LocalDateTime resultDate) {
            response.success = true;
            response.resultDate = resultDate;
            return response;
        }

        public static <T extends DeclarationBaseResponse> T error(T response, LocalDateTime resultDate, List<DeclarationError> errors) {
            error(response, resultDate);
            response.errors = errors;
            return response;
        }

        public static <T extends DeclarationBaseResponse> T error(T response, LocalDateTime resultDate, String error, String errorDetails) {
            error(response, resultDate);
            response.errors = Collections.singletonList(new DeclarationError(0, error, errorDetails));
            return response;
        }

        // generated

        public boolean isSuccess() {
            return success;
        }

        public TaxFreeState getState() {
            return state;
        }

        public List<DeclarationError> getErrors() {
            return errors;
        }

        public LocalDateTime getResultDate() {
            return resultDate;
        }
    }

    class DeclarationResponse extends DeclarationBaseResponse {
        public static DeclarationResponse ok(LocalDateTime resultDate, TaxFreeState state) {
            return DeclarationBaseResponse.ok(new DeclarationResponse(), resultDate, state);
        }

        public static DeclarationResponse error(LocalDateTime resultDate, List<DeclarationError> errors) {
            return DeclarationBaseResponse.error(new DeclarationResponse(), resultDate, errors);
        }

        public static DeclarationResponse error(LocalDateTime resultDate, String error, String errorDetails) {
            return DeclarationBaseResponse.error(new DeclarationResponse(), resultDate, error, errorDetails);
        }
    }

    class DeclarationError {
        protected int code;
        protected String description;
        protected String details;

        public DeclarationError() {
        }

        public DeclarationError(int code, String description, String details) {
            this.code = code;
            this.description = description;
            this.details = details;
        }

        // generated

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getDetails() {
            return details;
        }

        public void setDetails(String details) {
            this.details = details;
        }
    }

    class QueryDeclarationsResponse extends DeclarationResponse {
        protected List<DeclarationItem> declarations;

        public QueryDeclarationsResponse() {
        }

        public static QueryDeclarationsResponse ok(LocalDateTime resultDate, List<DeclarationItem> declarations) {
            QueryDeclarationsResponse response = DeclarationBaseResponse.ok(new QueryDeclarationsResponse(), resultDate);
            response.declarations = declarations;
            return response;
        }

        public static QueryDeclarationsResponse error(LocalDateTime resultDate, List<DeclarationError> errors) {
            QueryDeclarationsResponse response = DeclarationBaseResponse.ok(new QueryDeclarationsResponse(), resultDate);
            response.errors = errors;
            return response;
        }

        // generated

        public List<DeclarationItem> getDeclarations() {
            return declarations;
        }
    }

    class DeclarationItem {
        protected String docId;
        protected int docCorrNoLast;
        protected Integer docCorrNoCustoms;
        protected TaxFreeState state;
        protected LocalDateTime stateDate;

        public DeclarationItem() {
        }

        public DeclarationItem(String docId, int docCorrNoLast, Integer docCorrNoCustoms, TaxFreeState state, LocalDateTime stateDate) {
            this.docId = docId;
            this.docCorrNoLast = docCorrNoLast;
            this.docCorrNoCustoms = docCorrNoCustoms;
            this.state = state;
            this.stateDate = stateDate;
        }

        // generated

        public String getDocId() {
            return docId;
        }

        public int getDocCorrNoLast() {
            return docCorrNoLast;
        }

        public Integer getDocCorrNoCustoms() {
            return docCorrNoCustoms;
        }

        public TaxFreeState getState() {
            return state;
        }

        public LocalDateTime getStateDate() {
            return stateDate;
        }
    }

    class DeclarationInfoResponse extends DeclarationResponse {
        protected Assessment assessment;
        protected CustomsVerification customsVerification;

        public DeclarationInfoResponse() {
        }

        public static DeclarationInfoResponse ok(LocalDateTime resultDate, Assessment assessment, CustomsVerification customsVerification) {
            DeclarationInfoResponse response =  DeclarationBaseResponse.ok(new DeclarationInfoResponse(), resultDate);
            response.assessment = assessment;
            response.customsVerification = customsVerification;
            return response;
        }

        public static DeclarationInfoResponse error(LocalDateTime resultDate, List<DeclarationError> errors) {
            return DeclarationBaseResponse.error(new DeclarationInfoResponse(), resultDate, errors);
        }

        // generated

        public Assessment getAssessment() {
            return assessment;
        }

        public CustomsVerification getCustomsVerification() {
            return customsVerification;
        }
    }

    class Assessment {
        protected LocalDateTime date;
        protected List<AssessmentCondition> conditions;

        public Assessment() {
        }

        public Assessment(LocalDateTime date, List<AssessmentCondition> conditions) {
            this.date = date;
            this.conditions = conditions;
        }

        // generated

        public LocalDateTime getDate() {
            return date;
        }

        public List<AssessmentCondition> getConditions() {
            return conditions;
        }
    }

    class AssessmentCondition {
        protected String code;
        protected String description;
        protected boolean result;

        public AssessmentCondition() {
        }

        public AssessmentCondition(String code, String description, boolean result) {
            this.code = code;
            this.description = description;
            this.result = result;
        }

        // generated

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public boolean isResult() {
            return result;
        }
    }

    class CustomsVerification {
        protected String docId;
        protected int docCorrNo;
        protected LocalDateTime verificationDate;
        protected LocalDateTime correctionDate;
        protected boolean verificationResult;    // true - everything ok, false - there are problems with some goods
        protected List<GoodVerification> verifiedGoods;

        public CustomsVerification() {
        }

        public CustomsVerification(String docId, int docCorrNo, LocalDateTime verificationDate, LocalDateTime correctionDate, boolean verificationResult, List<GoodVerification> verifiedGoods) {
            this.docId = docId;
            this.docCorrNo = docCorrNo;
            this.verificationDate = verificationDate;
            this.correctionDate = correctionDate;
            this.verificationResult = verificationResult;
            this.verifiedGoods = verifiedGoods;
        }

        // generated

        public String getDocId() {
            return docId;
        }

        public int getDocCorrNo() {
            return docCorrNo;
        }

        public LocalDateTime getVerificationDate() {
            return verificationDate;
        }

        public LocalDateTime getCorrectionDate() {
            return correctionDate;
        }

        public boolean isVerificationResult() {
            return verificationResult;
        }

        public List<GoodVerification> getVerifiedGoods() {
            return verifiedGoods;
        }
    }

    class GoodVerification {
        protected int sequenceNo;
        protected BigDecimal totalAmount;
        protected BigDecimal quantity;
        protected String unitOfMeasureCode;
        protected String unitOfMeasureOther;
        protected BigDecimal quantityVerified;

        public GoodVerification() {
        }

        public GoodVerification(int sequenceNo, BigDecimal totalAmount, BigDecimal quantity, String unitOfMeasureCode, String unitOfMeasureOther, BigDecimal quantityVerified) {
            this.sequenceNo = sequenceNo;
            this.totalAmount = totalAmount;
            this.quantity = quantity;
            this.unitOfMeasureCode = unitOfMeasureCode;
            this.unitOfMeasureOther = unitOfMeasureOther;
            this.quantityVerified = quantityVerified;
        }

        // generated

        public int getSequenceNo() {
            return sequenceNo;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public BigDecimal getQuantity() {
            return quantity;
        }

        public String getUnitOfMeasureCode() {
            return unitOfMeasureCode;
        }

        public String getUnitOfMeasureOther() {
            return unitOfMeasureOther;
        }

        public BigDecimal getQuantityVerified() {
            return quantityVerified;
        }
    }
}

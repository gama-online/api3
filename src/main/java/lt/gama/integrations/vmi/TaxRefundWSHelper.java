package lt.gama.integrations.vmi;

import lt.gama.helpers.*;
import lt.gama.integrations.WSHelper;
import lt.gama.integrations.vmi.types.IdDocType;
import lt.gama.integrations.vmi.types.PaymentType;
import lt.gama.integrations.vmi.ws.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class TaxRefundWSHelper {

    static public final ZoneId API_ZONE_ID = ZoneId.of("Europe/Vilnius");

    static public SubmitDeclarationRequest createSubmitDeclarationRequest(String senderIn,
                                                                          DocHeaderType header,
                                                                          SalesManType salesMan,
                                                                          CustomerType customer,
                                                                          SalesDocumentType document) {
        Validators.checkArgument(StringHelper.hasValue(senderIn), "No senderIn");
        SubmitDeclarationRequest request = new SubmitDeclarationRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setTimeStamp(WSHelper.xmlFromLocalDateTime(DateUtils.now(API_ZONE_ID)));
        request.setSenderIn(senderIn);
        request.setSituation(1); // Situacija, kada teikiama deklaracija: 1 - „Rengiant deklaraciją“ - visada!
        TFDeclarationType declaration = new TFDeclarationType();
        declaration.setDocHeader(header);
        declaration.setSalesman(salesMan);
        declaration.setCustomer(customer);
        declaration.getSalesDocument().add(document);
        request.setDeclaration(declaration);
        return request;
    }

    /**
     *
     * @param docId Deklaracijos unikalus numeris (Nuo 7 iki 34 ženklų ilgio sudaryta iš skaičių, didžiųjų ir
     *              mažųjų lotyniškų raidžių, taškų („.“), brūkšnelių („-“) bei dešinėn pasvirusių brūkšnių („/“).
     *              RegExp: '[0-9A-Za-z.\-/]{7,34}')
     * @param docCorrNo Deklaracijos su tuo pačiu unikaliu numeriu DocId korekcijos eilės numeris (1, 2, 3,...).
     * @param date Deklaracijos pildymo data. Jei teikiama ta pati pakoreguota deklaracija, tai jos koregavimo data.
     */
    static public DocHeaderType createHeader(String docId, int docCorrNo, LocalDate date) {
        Validators.checkArgument(StringHelper.hasValue(docId) && (docId.length() <= 34), "No or wrong docId");
        DocHeaderType header = new DocHeaderType();
        header.setDocId(docId);
        header.setDocCorrNo(docCorrNo);
        header.setCompletionDate(WSHelper.xmlFromLocalDate(date));
        header.setAffirmation("Y"); // Patvirtiname, kad įsitikinome, jog šis pirkėjas turi teisę naudotis TaxFree schema - visada "Y"
        return header;
    }

    /**
     *
     * @param name Pardavėjo pavadinimas
     * @param vatPayerCode Pardavėjo PVM mokėtojo kodas, suteiktas Lietuvoje
     */
    static public SalesManType createSalesMan(String name, String vatPayerCode) {
        Validators.checkArgument(StringHelper.hasValue(name), "No name");
        Validators.checkArgument(StringHelper.hasValue(vatPayerCode) && (vatPayerCode.length() == 11 || vatPayerCode.length() == 14), "Wrong or no vatPayerCode");
        SalesManType salesMan = new SalesManType();
        salesMan.setName(name);
        salesMan.setVatPayerCode(new LtVatPayerCodeType());
        salesMan.getVatPayerCode().setIssuedBy(IsoCountryCodeType.LT);
        salesMan.getVatPayerCode().setValue(vatPayerCode.substring(2));
        return salesMan;
    }

    /**
     *
     * @param firstName Pirkėjo vardas
     * @param lastName Pirkėjo pavardė
     * @param birthDate Pirkėjo gimimo data.
     * @param idDocType Asmens tapatybę patvirtinančio dokumento tipas
     * @param idDocNo Asmens tapatybę patvirtinančio dokumento numeris
     * @param issuedBy Valstybės, kurioje išduotas asmens tapatybę patvirtinantis dokumentas, kodas
     * @param resCountryCode Europos Sąjungai nepriklausančios valstybės, kurioje yra pirkėjo nuolatinė gyvenamoji vieta, kodas
     */
    static public CustomerType createCustomer(String firstName, String lastName, LocalDate birthDate,
                                              IdDocType idDocType, String idDocNo, IsoCountryCodeType issuedBy,
                                              NonEuCountryCodeType resCountryCode,
                                              String otherDocType, String otherDocNo, IsoCountryCodeType otherDocIssuedBy) {
        CustomerType customer = new CustomerType();
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setBirthDate(WSHelper.xmlFromLocalDate(birthDate));
        IdentityDocumentType identityDocument = new IdentityDocumentType();
        identityDocument.setDocType(idDocType.getValue());
        identityDocument.setDocNo(new IdDocNoType());
        identityDocument.getDocNo().setValue(idDocNo);
        identityDocument.getDocNo().setIssuedBy(issuedBy);
        customer.setIdentityDocument(identityDocument);
        customer.setResCountryCode(resCountryCode);

        if (StringHelper.hasValue(otherDocType)) {
            OtherDocumentType otherDocumentType = new OtherDocumentType();
            otherDocumentType.setDocType(otherDocType);
            OtherDocNoType otherDocNoType = new OtherDocNoType();
            otherDocNoType.setValue(otherDocNo);
            otherDocNoType.setIssuedBy(otherDocIssuedBy);
            otherDocumentType.setDocNo(otherDocNoType);
            customer.getOtherDocument().add(otherDocumentType);
        }
        return customer;
    }

    static public SalesDocumentType createSalesDocument(LocalDate date, String invoiceNo, List<GoodsItemType> goods) {
        Validators.checkArgument(date != null, "No date in sale document");
        Validators.checkArgument(StringHelper.hasValue(invoiceNo), "No invoiceNo of sale document");
        Validators.checkArgument(CollectionsHelper.hasValue(goods), "No goods in sale document");
        SalesDocumentType document = new SalesDocumentType();
        document.setSalesDate(WSHelper.xmlFromLocalDate(date));
        document.setInvoiceNo(invoiceNo);
        AtomicInteger seq = new AtomicInteger();
        goods.forEach(item -> item.setSequenceNo(seq.incrementAndGet()));
        document.getGoods().addAll(goods);
        return document;
    }

    /**
     *
     * @param description Prekės pavadinimas (aprašymas). Pavadinimas turi būti toks, kad galima būtų tiksliai identifikuoti prekę.
     * @param quantity kiekis
     * @param unitOfMeasureCode Matavimo vienetas turi nurodyti matavimo vieneto rūšį: pvz., kg, vienetas ir pan.
     *                          Nurodomas kodas iš matavimo vienetų klasifikatoriaus, kuris pateiktas 7.2.1 skyriuje.
     * @param unitOfMeasureOther Kitokio matavimo vieneto pavadinimas. Nurodomas, kai klasifikatoriuje nėra tinkamos reikšmės
     * @param taxableAmount Kaina be PVM (Eur)
     * @param vatRate PVM tarifas (procentais)
     * @param vatAmount PVM suma (Eur)
     * @param totalAmount Kaina su PVM (Eur)
     */
    static public GoodsItemType createSalesGoodItem(String description, BigDecimal quantity,
                                                    String unitOfMeasureCode, String unitOfMeasureOther,
                                                    BigDecimal taxableAmount, BigDecimal vatRate, BigDecimal vatAmount, BigDecimal totalAmount) {

        Validators.checkArgument(StringHelper.hasValue(description), "No good's description");
        Validators.checkArgument(BigDecimalUtils.isPositive(quantity), "No good's quantity");
        Validators.checkArgument(StringHelper.hasValue(unitOfMeasureCode) || StringHelper.hasValue(unitOfMeasureOther), "No good's unit Of Measure");
        Validators.checkArgument(BigDecimalUtils.isPositive(taxableAmount), "No good's taxable amount");
        Validators.checkArgument(BigDecimalUtils.isPositive(vatRate), "No good's vat rate");
        Validators.checkArgument(BigDecimalUtils.isPositive(vatAmount), "No good's vat amount");
        Validators.checkArgument(BigDecimalUtils.isPositive(totalAmount), "No good's total amount");

        GoodsItemType item = new GoodsItemType();
        item.setDescription(description);
        item.setQuantity(quantity);
        if (StringHelper.hasValue(unitOfMeasureCode)) item.setUnitOfMeasureCode(unitOfMeasureCode);
        if (StringHelper.hasValue(unitOfMeasureOther)) item.setUnitOfMeasureOther(unitOfMeasureOther);
        item.setTaxableAmount(taxableAmount);
        item.setVatRate(vatRate);
        item.setVatAmount(vatAmount);
        item.setTotalAmount(totalAmount);
        return item;
    }

    static public CancelDeclarationRequest createCancelDeclarationRequest(String senderIn, String docId) {
        CancelDeclarationRequest request = new CancelDeclarationRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setTimeStamp(WSHelper.xmlFromLocalDateTime(DateUtils.now(API_ZONE_ID)));
        request.setSenderIn(senderIn);
        request.setDocId(docId);
        return request;
    }

    static public QueryDeclarationsRequest createQueryDeclarationsRequest(String senderIn, lt.gama.integrations.vmi.ws.QueryDeclarationsRequest.Query query) {
        QueryDeclarationsRequest request = new QueryDeclarationsRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setTimeStamp(WSHelper.xmlFromLocalDateTime(DateUtils.now(API_ZONE_ID)));
        request.setSenderIn(senderIn);
        request.setQuery(query);
        return request;
    }


    static public lt.gama.integrations.vmi.ws.QueryDeclarationsRequest.Query createQuery(String docId) {
        lt.gama.integrations.vmi.ws.QueryDeclarationsRequest.Query query = new lt.gama.integrations.vmi.ws.QueryDeclarationsRequest.Query();
        query.setDocId(docId);
        return query;
    }

    static public lt.gama.integrations.vmi.ws.QueryDeclarationsRequest.Query createQuery(LocalDateTime stateDateFrom, LocalDateTime stateDateTo) {
        lt.gama.integrations.vmi.ws.QueryDeclarationsRequest.Query query = new lt.gama.integrations.vmi.ws.QueryDeclarationsRequest.Query();
        query.setStateDateFrom(WSHelper.xmlFromLocalDateTime(DateUtils.adjust(stateDateFrom, API_ZONE_ID)));
        query.setStateDateTo(WSHelper.xmlFromLocalDateTime(DateUtils.adjust(stateDateTo, API_ZONE_ID)));
        return query;
    }

    static public lt.gama.integrations.vmi.ws.QueryDeclarationsRequest.Query createQuery(LocalDateTime stateDateFrom, LocalDateTime stateDateTo, DeclStateForQueryType state) {
        lt.gama.integrations.vmi.ws.QueryDeclarationsRequest.Query query = createQuery(stateDateFrom, stateDateTo);
        query.setDeclState(state);
        return query;
    }

    static public GetInfoOnExportedGoodsRequest createGetInfoOnExportedGoodsRequest(String senderIn, String docId) {
        GetInfoOnExportedGoodsRequest request = new GetInfoOnExportedGoodsRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setTimeStamp(WSHelper.xmlFromLocalDateTime(DateUtils.now(API_ZONE_ID)));
        request.setSenderIn(senderIn);
        request.setDocId(docId);
        return request;
    }

    static public SubmitPaymentInfoRequest createSubmitPaymentInfoRequest(String senderIn, String docId, List<PaymentInfoType.Payment> payments) {
        SubmitPaymentInfoRequest request = new SubmitPaymentInfoRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setTimeStamp(WSHelper.xmlFromLocalDateTime(DateUtils.now(API_ZONE_ID)));
        request.setSenderIn(senderIn);
        request.setDocId(docId);
        request.setPaymentInfo(new PaymentInfoType());
        request.getPaymentInfo().getPayment().addAll(payments);
        return request;
    }

    static public PaymentInfoType.Payment createPayment(PaymentType type, BigDecimal amount, LocalDate date) {
        PaymentInfoType.Payment payment = new PaymentInfoType.Payment();
        payment.setType(type.getValue());
        payment.setAmount(amount);
        payment.setDate(WSHelper.xmlFromLocalDate(date));
        return payment;
    }

}

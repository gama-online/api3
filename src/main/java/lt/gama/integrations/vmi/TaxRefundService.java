package lt.gama.integrations.vmi;

import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.handler.Handler;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.*;
import lt.gama.integrations.SOAPLogHandler;
import lt.gama.integrations.WSHelper;
import lt.gama.integrations.vmi.types.IdDocType;
import lt.gama.integrations.vmi.types.PaymentType;
import lt.gama.integrations.vmi.types.TaxFreeState;
import lt.gama.integrations.vmi.ws.*;
import lt.gama.model.dto.documents.InvoiceDto;
import lt.gama.model.mappers.InvoiceSqlMapper;
import lt.gama.model.sql.documents.InvoiceSql;
import lt.gama.model.sql.documents.items.InvoicePartSql;
import lt.gama.model.sql.system.CompanySql;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.model.type.enums.DBType;
import lt.gama.model.type.inventory.TaxFree;
import lt.gama.model.type.inventory.taxfree.Customer;
import lt.gama.model.type.inventory.taxfree.DocHeader;
import lt.gama.model.type.inventory.taxfree.Good;
import lt.gama.model.type.inventory.taxfree.SalesDoc;
import lt.gama.service.AppPropService;
import lt.gama.service.DBServiceSQL;
import lt.gama.service.DocumentService;
import lt.gama.service.TranslationService;
import lt.gama.service.ex.rt.GamaException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


@Service
public class TaxRefundService implements ITaxRefundService {

    private static final String KEYSTORE_PATH = "integrations" + File.separator +
            "vmi" + File.separator +
            "certificates" + File.separator +
            "keystore.pkcs12";
    private static final String KEYSTORE_PASSWORD = "123456";

    /**
     * Supported types in Java8: jceks / jks / dks / pkcs11 / pkcs12
     * @see <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#KeyStore">KeyStore Types</a>
     */
    private static final String KEYSTORE_TYPE = "pkcs12";

    /**
     * Supported protocols in Java8: SSL / SSLv2 / SSLv3 / TLS / TLSv1 / TLSv1.1 / TLSv1.2
     * @see <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#SSLContext">SSLContext Algorithms</a>
     */
    private static final String PROTOCOL = "SSL";

    private static final ThreadLocal<SSLContext> sslContexts = new ThreadLocal<>();


    private final Auth auth;
    private final DBServiceSQL dbServiceSQL;
    private final DocumentService documentService;
    private final InvoiceSqlMapper invoiceSqlMapper;
    private final AppPropService appPropService;

    public TaxRefundService(Auth auth,
                            DBServiceSQL dbServiceSQL,
                            DocumentService documentService,
                            InvoiceSqlMapper invoiceSqlMapper, AppPropService appPropService) {
        this.auth = auth;
        this.dbServiceSQL = dbServiceSQL;
        this.documentService = documentService;
        this.invoiceSqlMapper = invoiceSqlMapper;
        this.appPropService = appPropService;
    }

    @Override
    public SSLContext getSSLContext() {
        SSLContext sslContext = sslContexts.get();
        if (sslContext == null) {
            try (InputStream keyStoreStream = this.getClass().getClassLoader().getResourceAsStream(KEYSTORE_PATH)) {
                KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
                keyStore.load(keyStoreStream, KEYSTORE_PASSWORD.toCharArray());
                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                keyManagerFactory.init(keyStore, KEYSTORE_PASSWORD.toCharArray());

                sslContext = SSLContext.getInstance(PROTOCOL);
                sslContext.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom());
                sslContexts.set(sslContext);
            } catch (Exception e) {
                throw new GamaException(e.getMessage(), e);
            }
        }
        return sslContext;
    }

    /**
     * generate unique docId
     * @return docId, i.e. according to regexp: [0-9A-Za-z.\-/]{7,34}
     *
     */
    @Override
    public String makeDocId() {
        CompanySql company = getCompany();
        long ms = DateUtils.instant().toEpochMilli();
        int random = (int) (Math.random() * (1000));
        return company.getCode() + "." + Long.toHexString(ms).toUpperCase() + "-" + random;
    }

    @Override
    public TaxFree generateInvoiceTaxFreeSQL(long id, DBType db) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        Validators.checkArgument("LT".equals(companySettings.getCountry()) && BooleanUtils.isTrue(companySettings.getEnableTaxFree()), "Tax Free not enabled");
        InvoiceSql entity = dbServiceSQL.getAndCheck(InvoiceSql.class, id);
        Validators.checkDocumentDate(companySettings, entity, auth.getLanguage());
        if (entity.isZeroVAT() || GamaMoneyUtils.isZero(entity.getBaseTaxTotal())) {
            throw new GamaException("no VAT");
        }
        TaxFree taxFree = new TaxFree();
        taxFree.setDocHeader(new DocHeader());
        taxFree.getDocHeader().setDocCorrNo(0);
        taxFree.getDocHeader().setDocId(makeDocId());
        taxFree.setCustomer(new Customer());
        if (entity.getCounterparty() != null) {
            String[] names = StringUtils.splitByWholeSeparator(entity.getCounterparty().getName(), null);
            if (names.length > 0) taxFree.getCustomer().setFirstName(names[0]);
            if (names.length > 1) taxFree.getCustomer().setLastName(names[1]);
        }
        taxFree.setSalesDoc(new SalesDoc());
        taxFree.getSalesDoc().setDate(entity.getDate());
        taxFree.getSalesDoc().setInvoiceNo(entity.getNumber());
        if (CollectionsHelper.hasValue(entity.getParts())) {
            AtomicInteger seqNo = new AtomicInteger(1);
            taxFree.getSalesDoc().setGoods(entity.getParts().stream()
                    .filter(p -> p instanceof InvoicePartSql)
                    .map(InvoicePartSql.class::cast)
                    .filter(InvoicePartSql::isTaxable)
                    .map(part -> {
                        Good good = new Good();
                        good.setSequenceNo(seqNo.getAndIncrement());
                        good.setDescription((part.getName()));
                        if (StringUtils.containsIgnoreCase(part.getUnit(), "vnt") || StringUtils.containsIgnoreCase(part.getUnit(), "vienetai")) {
                            good.setUnitOfMeasureCode("NAR");
                        } else if (StringUtils.equalsIgnoreCase(part.getUnit(), "kg")) {
                            good.setUnitOfMeasureCode("KGM");
                        } else if (StringUtils.equalsIgnoreCase(part.getUnit(), "g")) {
                            good.setUnitOfMeasureCode("GRM");
                        } else {
                            good.setUnitOfMeasureOther(part.getUnit());
                        }
                        good.setQuantity(part.getQuantity());
                        good.setTaxableAmount(part.getDiscountedTotal().getAmount());
                        good.setVatAmount(BigDecimalUtils.subtract(part.getDiscountedTotalWithVAT().getAmount(), part.getDiscountedTotal().getAmount()));
                        good.setTotalAmount(part.getDiscountedTotalWithVAT().getAmount());
                        good.setVatRate(BigDecimal.valueOf(part.getVat().getRate()));
                        return good;
                    })
                    .collect(Collectors.toList()));
        } else {
            taxFree.getSalesDoc().setGoods(new ArrayList<>());
        }
        return taxFree;
    }

    @Override
    public InvoiceDto saveInvoiceTaxFreeSQL(long id, TaxFree taxFree, DBType db) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        Validators.checkArgument("LT".equals(companySettings.getCountry()) && BooleanUtils.isTrue(companySettings.getEnableTaxFree()), "Tax Free not enabled");

        return invoiceSqlMapper.toDto(dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            InvoiceSql entity = dbServiceSQL.getAndCheck(InvoiceSql.class, id, InvoiceSql.GRAPH_ALL);
            Validators.checkDocumentDate(companySettings, entity, auth.getLanguage());
            if (taxFree != null && taxFree.getDocHeader() != null) {
                if (StringHelper.isEmpty(taxFree.getDocHeader().getDocId())) {
                    taxFree.getDocHeader().setDocId(makeDocId());
                }
                taxFree.getDocHeader().setCompletionDate(DateUtils.date("Europe/Vilnius"));
            }
            entity.setTaxFree(taxFree);
            return entity;
        }));
    }

    @Override
    public InvoiceDto submitInvoiceTaxFreeSQL(long id, DBType db) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        Validators.checkArgument("LT".equals(companySettings.getCountry()) && BooleanUtils.isTrue(companySettings.getEnableTaxFree()), "Tax Free not enabled");

        return invoiceSqlMapper.toDto(dbServiceSQL.executeAndReturnInTransaction(entitymanager -> {
            InvoiceSql entity = dbServiceSQL.getAndCheck(InvoiceSql.class, id, InvoiceSql.GRAPH_ALL);
            Validators.checkDocumentDate(companySettings, entity, auth.getLanguage());
            final TaxFree taxFree = entity.getTaxFree();
            if (taxFree == null || !taxFree.hasValues()) {
                throw new GamaException(TranslationService.getInstance().translate(TranslationService.INVENTORY.NoTaxFreeDeclaration, auth.getLanguage()));
            }

            taxFree.incDocCorrNo();
            taxFree.getDocHeader().setCompletionDate(DateUtils.date("Europe/Vilnius"));
            try {
                DeclarationResponse response = submitDeclaration(taxFree);
                if (response.getState() != null) taxFree.setState(response.getState());
                taxFree.setUpdatedOn(response.getResultDate());
                taxFree.setErrors(response.getErrors());

            } catch (Exception e) {
                taxFree.setUpdatedOn(DateUtils.now());
                taxFree.setErrors(Collections.singletonList(
                        new DeclarationError(-1, "System Error", e.getMessage())));
            }
            entity = dbServiceSQL.saveEntityInCompany(entity);

            if (taxFree.getState() == TaxFreeState.ACCEPTED) {
                entity = dbServiceSQL.getById(InvoiceSql.class, entity.getId(), InvoiceSql.GRAPH_ALL);
                documentService.generatePrintForm(invoiceSqlMapper.toDto(entity), "tf");
            }
            return entity;
        }));
    }

    @Override
    public InvoiceDto cancelInvoiceTaxFreeSQL(long id, DBType db) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        Validators.checkArgument("LT".equals(companySettings.getCountry()) && BooleanUtils.isTrue(companySettings.getEnableTaxFree()), "Tax Free not enabled");

        return invoiceSqlMapper.toDto(dbServiceSQL.executeAndReturnInTransaction(entitymanager -> {
            InvoiceSql entity = dbServiceSQL.getAndCheck(InvoiceSql.class, id, InvoiceSql.GRAPH_ALL);
            Validators.checkDocumentDate(companySettings, entity, auth.getLanguage());
            final TaxFree taxFree = entity.getTaxFree();
            if (taxFree == null || !taxFree.hasValues()) {
                throw new GamaException(TranslationService.getInstance().translate(TranslationService.INVENTORY.NoTaxFreeDeclaration, auth.getLanguage()));
            }
            if (taxFree.getState() == TaxFreeState.ASSESSED || taxFree.getState() == TaxFreeState.REFUNDED) {
                throw new GamaException(TranslationService.getInstance().translate(TranslationService.INVENTORY.TaxFreeDeclarationAlreadyCompleted, auth.getLanguage()));
            }

            try {
                DeclarationResponse response = cancelDeclaration(taxFree.getDocHeader().getDocId());
                if (response.getState() != null) taxFree.setState(response.getState());
                taxFree.setUpdatedOn(response.getResultDate());
                taxFree.setErrors(response.getErrors());

            } catch (Exception e) {
                taxFree.setUpdatedOn(DateUtils.now());
                taxFree.setErrors(Collections.singletonList(
                        new DeclarationError(-1, "System Error", e.getMessage())));
            }
            return entity;
        }));
    }

    @Override
    public InvoiceDto submitInvoiceTaxFreePaymentInfoSQL(long id, DBType db) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        Validators.checkArgument("LT".equals(companySettings.getCountry()) && BooleanUtils.isTrue(companySettings.getEnableTaxFree()), "Tax Free not enabled");

        return invoiceSqlMapper.toDto(dbServiceSQL.executeAndReturnInTransaction(entitymanager -> {
            InvoiceSql entity = dbServiceSQL.getAndCheck(InvoiceSql.class, id, InvoiceSql.GRAPH_ALL);
            Validators.checkDocumentDate(companySettings, entity, auth.getLanguage());
            final TaxFree taxFree = entity.getTaxFree();
            if (taxFree == null || !taxFree.hasValues()) {
                throw new GamaException(TranslationService.getInstance().translate(TranslationService.INVENTORY.NoTaxFreeDeclaration, auth.getLanguage()));
            }
            if (taxFree.getState() != TaxFreeState.ASSESSED) {
                throw new GamaException(TranslationService.getInstance().translate(TranslationService.INVENTORY.TaxFreeDeclarationWrongState, auth.getLanguage()));
            }
            try {
                DeclarationResponse response = submitPaymentInfo(
                        taxFree.getDocHeader().getDocId(),
                        PaymentType.from(taxFree.getPaymentInfo().getPaymentType()),
                        taxFree.getPaymentInfo().getAmount(),
                        taxFree.getPaymentInfo().getDate());
                if (response.getState() != null) taxFree.setState(response.getState());
                taxFree.setUpdatedOn(response.getResultDate());
                taxFree.setErrors(response.getErrors());

            } catch (Exception e) {
                taxFree.setUpdatedOn(DateUtils.now());
                taxFree.setErrors(Collections.singletonList(
                        new DeclarationError(-1, "System Error", e.getMessage())));
            }
            return entity;
        }));
    }

    @Override
    public QueryDeclarationsResponse queryTaxFreeDeclarations(LocalDateTime timestampFrom, LocalDateTime timestampTo) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        Validators.checkArgument("LT".equals(companySettings.getCountry()) && BooleanUtils.isTrue(companySettings.getEnableTaxFree()), "Tax Free not enabled");
        try {
            return queryDeclarations(timestampFrom, timestampTo, DeclStateForQueryType.ASSESSED);

        } catch (Exception e) {
            throw new GamaException(e.getMessage(), e);
        }
    }

    @Override
    public DeclarationInfoResponse getTaxFreeInfoOnExportedGoods(String docId) {
        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        Validators.checkArgument("LT".equals(companySettings.getCountry()) && BooleanUtils.isTrue(companySettings.getEnableTaxFree()), "Tax Free not enabled");
        try {
            return getInfoOnExportedGoodsRequest(docId);

        } catch (Exception e) {
            throw new GamaException(e.getMessage(), e);
        }
    }

    private DeclarationResponse submitDeclaration(TaxFree declaration) {
        CompanySql company = getCompany();
        VATRefundforForeignTravelerTRPort port = getVATRefundTestService();
        SubmitDeclarationRequest request = TaxRefundWSHelper.createSubmitDeclarationRequest(
                company.getCode(),
                TaxRefundWSHelper.createHeader(
                        declaration.getDocHeader().getDocId(),
                        declaration.getDocHeader().getDocCorrNo(),
                        declaration.getDocHeader().getCompletionDate()),
                TaxRefundWSHelper.createSalesMan(company.getBusinessName(), company.getVatCode()),
                TaxRefundWSHelper.createCustomer(
                        declaration.getCustomer().getFirstName(),
                        declaration.getCustomer().getLastName(),
                        declaration.getCustomer().getBirthDate(),
                        IdDocType.from(declaration.getCustomer().getIdDocType()),
                        declaration.getCustomer().getIdDocNo(),
                        IsoCountryCodeType.valueOf(declaration.getCustomer().getIssuedBy()),
                        NonEuCountryCodeType.valueOf(declaration.getCustomer().getResCountryCode()),
                        declaration.getCustomer().getOtherDocType(),
                        declaration.getCustomer().getOtherDocNo(),
                        StringHelper.hasValue(declaration.getCustomer().getOtherIssuedBy())
                                ? IsoCountryCodeType.valueOf(declaration.getCustomer().getOtherIssuedBy())
                                : null),
                TaxRefundWSHelper.createSalesDocument(
                        declaration.getSalesDoc().getDate(),
                        declaration.getSalesDoc().getInvoiceNo(),
                        declaration.getSalesDoc().getGoods().stream()
                                .map(item -> TaxRefundWSHelper.createSalesGoodItem(
                                        item.getDescription(),
                                        item.getQuantity(),
                                        item.getUnitOfMeasureCode(),
                                        item.getUnitOfMeasureOther(),
                                        item.getTaxableAmount(),
                                        item.getVatRate(),
                                        item.getVatAmount(),
                                        item.getTotalAmount()))
                                .collect(Collectors.toList())));
        lt.gama.integrations.vmi.ws.SubmitDeclarationResponse response = port.submitDeclaration(request);
        return response.getResultStatus() == ResultStatusType.SUCCESS
                ? DeclarationResponse.ok(WSHelper.localDateTimeFromXML(response.getResultDate()), TaxFreeState.from(response.getDeclState()))
                : DeclarationResponse.error(
                        WSHelper.localDateTimeFromXML(response.getResultDate()),
                        response.getErrors().getError().stream()
                                .map(err -> new DeclarationError(err.getErrorCode(), err.getDescription(), err.getDetails()))
                                .collect(Collectors.toList()));
    }

    private DeclarationResponse cancelDeclaration(String docId) {
        CompanySql company = getCompany();
        VATRefundforForeignTravelerTRPort port = getVATRefundTestService();
        CancelDeclarationRequest request = TaxRefundWSHelper.createCancelDeclarationRequest(company.getCode(), docId);
        lt.gama.integrations.vmi.ws.CancelDeclarationResponse response = port.cancelDeclaration(request);
        return response.getResultStatus() == ResultStatusType.SUCCESS
                ? DeclarationResponse.ok(WSHelper.localDateTimeFromXML(response.getResultDate()), TaxFreeState.CANCELLED)
                : DeclarationResponse.error(
                        WSHelper.localDateTimeFromXML(response.getResultDate()),
                        response.getErrors().getError().stream()
                                .map(err -> new DeclarationError(err.getErrorCode(), err.getDescription(), err.getDetails()))
                                .collect(Collectors.toList()));
    }

    private DeclarationResponse submitPaymentInfo(String docId, PaymentType paymentType, BigDecimal amount, LocalDate date) {
        CompanySql company = getCompany();
        VATRefundforForeignTravelerTRPort port = getVATRefundTestService();
        SubmitPaymentInfoRequest request = TaxRefundWSHelper.createSubmitPaymentInfoRequest(
                company.getCode(),
                docId,
                Collections.singletonList(TaxRefundWSHelper.createPayment(paymentType, amount, date)));
        lt.gama.integrations.vmi.ws.SubmitPaymentInfoResponse response = port.submitPaymentInfo(request);
        return response.getResultStatus() == ResultStatusType.SUCCESS
                ? DeclarationResponse.ok(WSHelper.localDateTimeFromXML(response.getResultDate()), TaxFreeState.REFUNDED)
                : DeclarationResponse.error(
                        WSHelper.localDateTimeFromXML(response.getResultDate()),
                        response.getErrors().getError().stream()
                                .map(err -> new DeclarationError(err.getErrorCode(), err.getDescription(), err.getDetails()))
                                .collect(Collectors.toList()));
    }

    private QueryDeclarationsResponse queryDeclarations(LocalDateTime timestampFrom, LocalDateTime timestampTo, DeclStateForQueryType state) {
        CompanySql company = getCompany();
        VATRefundforForeignTravelerTRPort port = getVATRefundTestService();
        QueryDeclarationsRequest request = TaxRefundWSHelper.createQueryDeclarationsRequest(
                company.getCode(),
                TaxRefundWSHelper.createQuery(timestampFrom, timestampTo, state));
        lt.gama.integrations.vmi.ws.QueryDeclarationsResponse response = port.queryDeclarations(request);
        return response.getResultStatus() == ResultStatusType.SUCCESS
                ? QueryDeclarationsResponse.ok(
                        WSHelper.localDateTimeFromXML(response.getResultDate()),
                        response.getDeclList() != null && CollectionsHelper.hasValue(response.getDeclList().getDeclListItem())
                                ? response.getDeclList().getDeclListItem().stream()
                                    .map(item -> new DeclarationItem(item.getDocId(),
                                            item.getDocCorrNoLast(), item.getDocCorrNoCostums(),
                                            TaxFreeState.from(item.getDeclState().value()),
                                            WSHelper.localDateTimeFromXML(item.getStateDate())))
                                    .collect(Collectors.toList())
                                : null)
                : QueryDeclarationsResponse.error(
                        WSHelper.localDateTimeFromXML(response.getResultDate()),
                        response.getErrors().getError().stream()
                                .map(err -> new DeclarationError(err.getErrorCode(), err.getDescription(), err.getDetails()))
                                .collect(Collectors.toList()));
    }

    private DeclarationInfoResponse getInfoOnExportedGoodsRequest(String docId) {
        CompanySql company = getCompany();
        VATRefundforForeignTravelerTRPort port = getVATRefundTestService();
        GetInfoOnExportedGoodsRequest request = TaxRefundWSHelper.createGetInfoOnExportedGoodsRequest(company.getCode(), docId);
        lt.gama.integrations.vmi.ws.GetInfoOnExportedGoodsResponse response = port.getInfoOnExportedGoods(request);

        Assessment assessment = null;
        if (response.getInfoOnExportedGoods() != null && response.getInfoOnExportedGoods().getSTIAssessmentResults() != null) {
            assessment = new Assessment(
                    WSHelper.localDateTimeFromXML(response.getInfoOnExportedGoods().getSTIAssessmentResults().getAssessmentDate()),
                    CollectionsHelper.hasValue(response.getInfoOnExportedGoods().getSTIAssessmentResults().getCondition())
                            ? response.getInfoOnExportedGoods().getSTIAssessmentResults().getCondition().stream()
                                .map(item -> new AssessmentCondition(item.getCode(), item.getDescription(), item.isResult()))
                                .collect(Collectors.toList())
                            : null);
        }

        CustomsVerification customsVerification = null;
        if (response.getInfoOnExportedGoods() != null && response.getInfoOnExportedGoods().getCustomsVerificationResults() != null &&
                CollectionsHelper.hasValue(response.getInfoOnExportedGoods().getCustomsVerificationResults().getVerifiedGoods())) {
            customsVerification = new CustomsVerification(
                    response.getInfoOnExportedGoods().getCustomsVerificationResults().getDocId(),
                    response.getInfoOnExportedGoods().getCustomsVerificationResults().getDocCorrNo(),
                    WSHelper.localDateTimeFromXML(response.getInfoOnExportedGoods().getCustomsVerificationResults().getVerificationDate()),
                    WSHelper.localDateTimeFromXML(response.getInfoOnExportedGoods().getCustomsVerificationResults().getCorrectionDate()),
                    "A1".equalsIgnoreCase(response.getInfoOnExportedGoods().getCustomsVerificationResults().getVerificationResult()),
                    response.getInfoOnExportedGoods().getCustomsVerificationResults().getVerifiedGoods().stream()
                        .map(item -> new GoodVerification(
                                item.getSequenceNo(),
                                item.getTotalAmount(),
                                item.getQuantity(),
                                item.getUnitOfMeasureCode(),
                                item.getUnitOfMeasureOther(),
                                item.getQuantityVerified()))
                        .collect(Collectors.toList()));
        }
        return response.getResultStatus() == ResultStatusType.SUCCESS
                ? DeclarationInfoResponse.ok(WSHelper.localDateTimeFromXML(response.getResultDate()), assessment, customsVerification)
                : DeclarationInfoResponse.error(
                    WSHelper.localDateTimeFromXML(response.getResultDate()),
                    response.getErrors().getError().stream()
                            .map(err -> new DeclarationError(err.getErrorCode(), err.getDescription(), err.getDetails()))
                            .collect(Collectors.toList()));
    }

    @Override
    public String testCert() {
        try {
            HttpsURLConnection connection = (HttpsURLConnection) new URL("https://imas-ws.vmi.lt/ivaz-processor/services/sync?wsdl").openConnection();
            connection.setSSLSocketFactory(getSSLContext().getSocketFactory());

            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                reader.lines().forEach(line -> sb.append(line).append('\n'));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new GamaException(e.getMessage(), e);
        }
    }

    @Override
    public String testVMISubmitDeclaration(String docId) {
        CompanySql company = getCompany();
        VATRefundforForeignTravelerTRPort port = getVATRefundTestService();
        SubmitDeclarationRequest request = TaxRefundWSHelper.createSubmitDeclarationRequest(
                company.getCode(),
                TaxRefundWSHelper.createHeader(docId, 2, DateUtils.date("Europe/Vilnius")),
                TaxRefundWSHelper.createSalesMan(company.getBusinessName(), company.getVatCode()),
                TaxRefundWSHelper.createCustomer(
                        "Jonas",
                        "Petraitis",
                        LocalDate.of(2000, 1, 15),
                        IdDocType.PASSPORT,
                        "ABC1234567890",
                        IsoCountryCodeType.GB,
                        NonEuCountryCodeType.GB,
                        "Important paper",
                        "123-456-789",
                        IsoCountryCodeType.GB),
                TaxRefundWSHelper.createSalesDocument(
                        LocalDate.now(),
                        "INV 00012345",
                        Arrays.asList(
                                TaxRefundWSHelper.createSalesGoodItem(
                                        "Bananai",
                                        new BigDecimal("12.5"),
                                        "KGM", null,
                                        new BigDecimal("123.45"),
                                        new BigDecimal("21.00"),
                                        new BigDecimal("25.92"),
                                        new BigDecimal("149.37")),
                                TaxRefundWSHelper.createSalesGoodItem(
                                        "Apelsinai",
                                        BigDecimal.ONE,
                                        null, "Vnt.",
                                        new BigDecimal("1.00"),
                                        new BigDecimal("21.00"),
                                        new BigDecimal("0.21"),
                                        new BigDecimal("1.21")))));
        lt.gama.integrations.vmi.ws.SubmitDeclarationResponse response = port.submitDeclaration(request);
        return ReflectionToStringBuilder.toString(response, toStringStyle());
    }

    @Override
    public String testVMICancelDeclaration(String docId) {
        CompanySql company = getCompany();
        VATRefundforForeignTravelerTRPort port = getVATRefundTestService();
        CancelDeclarationRequest request = TaxRefundWSHelper.createCancelDeclarationRequest(company.getCode(), docId);
        CancelDeclarationResponse response = port.cancelDeclaration(request);
        return ReflectionToStringBuilder.toString(response, toStringStyle());
    }

    @Override
    public String testVMIQueryDeclarations(String docId, LocalDateTime timestampFrom, LocalDateTime timestampTo, DeclStateForQueryType state) {
        CompanySql company = getCompany();
        VATRefundforForeignTravelerTRPort port = getVATRefundTestService();
        QueryDeclarationsRequest request = TaxRefundWSHelper.createQueryDeclarationsRequest(
                company.getCode(),
                StringHelper.hasValue(docId)
                        ? TaxRefundWSHelper.createQuery(docId)
                        : TaxRefundWSHelper.createQuery(timestampFrom, timestampTo, state));
        lt.gama.integrations.vmi.ws.QueryDeclarationsResponse response = port.queryDeclarations(request);
        return ReflectionToStringBuilder.toString(response, toStringStyle());
    }

    @Override
    public String testVMIGetInfoOnExportedGoods(String docId) {
        CompanySql company = getCompany();
        VATRefundforForeignTravelerTRPort port = getVATRefundTestService();
        GetInfoOnExportedGoodsRequest request = TaxRefundWSHelper.createGetInfoOnExportedGoodsRequest(company.getCode(), docId);
        GetInfoOnExportedGoodsResponse response = port.getInfoOnExportedGoods(request);
        return ReflectionToStringBuilder.toString(response, toStringStyle());
    }

    @Override
    public String testVMISubmitPaymentInfo(String docId) {
        CompanySql company = getCompany();
        VATRefundforForeignTravelerTRPort port = getVATRefundTestService();
        SubmitPaymentInfoRequest request = TaxRefundWSHelper.createSubmitPaymentInfoRequest(
                company.getCode(),
                docId,
                Arrays.asList(
                        TaxRefundWSHelper.createPayment(PaymentType.BANK, new BigDecimal("20.00"), LocalDate.now()),
                        TaxRefundWSHelper.createPayment(PaymentType.CASH, new BigDecimal("6.04"), LocalDate.now())));
        SubmitPaymentInfoResponse response = port.submitPaymentInfo(request);
        return ReflectionToStringBuilder.toString(response, toStringStyle());
    }

    private static final ThreadLocal<VATRefundforForeignTravelerTRPort> ports = new ThreadLocal<>();

    private VATRefundforForeignTravelerTRPort getVATRefundTestService() {
        VATRefundforForeignTravelerTRPort port = ports.get();
        if (port == null) {
            VATRefundforForeignTravelerTRPortService service = new VATRefundforForeignTravelerTRPortService();
            port = service.getPort(VATRefundforForeignTravelerTRPort.class);

            final BindingProvider bindingProvider = (BindingProvider) port;
            if (appPropService.isDevelopment()) {
                bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, TaxRefundConst.TEST_URL);
            } else {
                bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, TaxRefundConst.PROD_URL);
            }
            bindingProvider.getRequestContext().put("com.sun.xml.internal.ws.transport.https.client.SSLSocketFactory", getSSLContext().getSocketFactory());

            @SuppressWarnings("rawtypes") List<Handler> chain = bindingProvider.getBinding().getHandlerChain();
            chain.add(new SOAPLogHandler());
            bindingProvider.getBinding().setHandlerChain(chain);

            ports.set(port);
        }
        return port;
    }

    private ToStringStyle toStringStyle() {
        return new RecursiveToStringStyle() {
            {
                setUseShortClassName(true);
                setUseIdentityHashCode(false);
            }

            @Override
            public void appendDetail(StringBuffer buffer, String fieldName, Object value) {
                if (value instanceof XMLGregorianCalendar || value instanceof Number) {
                    buffer.append(value);
                } else {
                    super.appendDetail(buffer, fieldName, value);
                }
            }
        };
    }

    private CompanySql getCompany() {
        return Validators.checkNotNull(dbServiceSQL.getById(CompanySql.class, auth.getCompanyId()), "No company");
    }
}

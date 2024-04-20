package lt.gama.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.openhtmltopdf.objects.zxing.ZXingObjectDrawer;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.render.DefaultObjectDrawerFactory;
import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.auth.impl.Auth;
import lt.gama.freemarker.GCSTemplateLoader;
import lt.gama.freemarker.GamaMoneyFormatter;
import lt.gama.freemarker.JodaObjectWrapper;
import lt.gama.helpers.*;
import lt.gama.model.dto.base.BaseDocumentDto;
import lt.gama.model.dto.documents.*;
import lt.gama.model.dto.documents.items.PartInvoiceDto;
import lt.gama.model.dto.entities.BankAccountDto;
import lt.gama.model.dto.entities.CompanyDto;
import lt.gama.model.i.IMoneyDocument;
import lt.gama.model.mappers.BankAccountSqlMapper;
import lt.gama.model.sql.entities.BankAccountSql;
import lt.gama.model.sql.entities.PartSql;
import lt.gama.model.sql.system.CompanySql;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.model.type.enums.PartType;
import lt.gama.model.type.inventory.Packing;
import lt.gama.model.type.inventory.taxfree.TaxFreeForQRCode;
import lt.gama.service.ex.rt.GamaException;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.Adler32;
import java.util.zip.GZIPOutputStream;

/**
 * Gama
 * Created by valdas on 15-07-10.
 */
@Service
public class TemplateService {

    private static final Logger log = LoggerFactory.getLogger(TemplateService.class);


    private static final Version VERSION = Configuration.VERSION_2_3_30;

    private volatile Configuration fmConfiguration = null;

    @PersistenceContext
    private EntityManager entityManager;

    private final DBServiceSQL dbServiceSQL;
    private final StorageService storageService;
    private final Auth auth;
    private final AuthSettingsCacheService authSettingsCacheService;
    private final BankAccountSqlMapper bankAccountSqlMapper;
    private final DocsMappersService docsMappersService;
    private final ObjectMapper objectMapper;
    
    
    public TemplateService(DBServiceSQL dbServiceSQL, StorageService storageService, Auth auth, AuthSettingsCacheService authSettingsCacheService, BankAccountSqlMapper bankAccountSqlMapper, DocsMappersService docsMappersService, ObjectMapper objectMapper) {
        this.dbServiceSQL = dbServiceSQL;
        this.storageService = storageService;
        this.auth = auth;
        this.authSettingsCacheService = authSettingsCacheService;
        this.bankAccountSqlMapper = bankAccountSqlMapper;
        this.docsMappersService = docsMappersService;
        this.objectMapper = objectMapper;
    }
    
    private Configuration getFmConfiguration() {
        Configuration localRef = fmConfiguration;
        if (localRef == null) {
            synchronized (this) {
                localRef = fmConfiguration;
                if (localRef == null) {
                    Configuration configuration = new Configuration(VERSION);
                    configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
                    configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
                    configuration.setLocalizedLookup(false);
                    MultiTemplateLoader mtl = new MultiTemplateLoader(new TemplateLoader[] {
                            new GCSTemplateLoader(storageService),
                            new ClassTemplateLoader()
                    });
                    configuration.setTemplateLoader(mtl);
                    configuration.setObjectWrapper(new JodaObjectWrapper(VERSION));

                    fmConfiguration = localRef = configuration;
                }
            }
        }
        return localRef;
    }

    private Template getFmTemplate(long companyId, String name, Locale locale) throws IOException {
        Template template = null;
        try {
            template = getFmConfiguration().getTemplate(companyId + "/" + name, locale, Charsets.UTF_8.name());
        } catch (TemplateNotFoundException ignored) {
        }

        if (template == null && companyId != 0) {
            try {
                template = getFmConfiguration().getTemplate(0 + "/" + name, locale, Charsets.UTF_8.name());
            } catch (TemplateNotFoundException ignored) {
            }
        }

        if (template == null) {
            try {
                template = getFmConfiguration().getTemplate(name, locale, Charsets.UTF_8.name());
            } catch (TemplateNotFoundException ignored) {
            }
        }
        return template;
    }

    public String filename(UUID uuid, String subtype, boolean mail, String language) {
        return (uuid != null ? uuid : "") +
                (StringHelper.isEmpty(subtype) ? "" : "_" + subtype) +
                (mail ? "-mail" : "") +
                (StringHelper.isEmpty(language) ? "" : "-" + language);
    }


    public <D extends BaseDocumentDto> void generateDocument(D document, OutputStream out, String subtype) {
        generateDocument(document, out, subtype, false, null, null);
    }

    public <D extends BaseDocumentDto> void generateDocument(D document, OutputStream out, String subtype, boolean mail, String language, String country) {
        try {
            long companyId = document.getCompanyId();
            auth.setCompanyId(companyId);
            auth.setSettings(authSettingsCacheService.get(auth.getCompanyId()));

            Map<String, Object> variables = new HashMap<>();
            variables.put("document", document);

            String templateName = document.getDocumentType();

            if (document instanceof InvoiceDto invoice) {
                CompanySettings settings = auth.getSettings();
                if (settings != null && settings.getSales() != null) {
                    variables.put("printBarcode", BooleanUtils.isNotTrue(settings.getSales().getInvoiceNoBarcode()));
                    variables.put("printSKU", BooleanUtils.isNotTrue(settings.getSales().getInvoiceNoSku()));
                } else {
                    variables.put("printBarcode", true);
                    variables.put("printSKU", true);
                }
                List<BankAccountDto> banks = new ArrayList<>();
                if (invoice.getAccount() != null) {
                    banks.add(invoice.getAccount());
                } else {
                    List<BankAccountDto> bankAccounts = getInvoiceBankAccounts();
                    banks.addAll(bankAccounts);
                }
                variables.put("banks", banks);

                boolean hasDiscounts;
                if (NumberUtils.isZero(invoice.getDiscount(), 1)) {
                    hasDiscounts = CollectionsHelper.streamOf(invoice.getParts())
                            .anyMatch(p -> !NumberUtils.isZero(p.getDiscount(), 1));
                } else {
                    hasDiscounts = true;
                }
                variables.put("hasDiscounts", hasDiscounts);

                // check if packing list - if so, read products weights info and calculate total weights
                if ("pl".equals(subtype)) {
                    invoicePackingListVariablesDto(variables, invoice);
                } else if ("tf".equals(subtype)) {
                    taxFreeDeclarationVariablesDto(variables, invoice);
                }

            } else if (document instanceof EstimateDto estimate) {
                CompanySettings settings = auth.getSettings();
                if (settings != null && settings.getSales() != null) {
                    variables.put("printBarcode", BooleanUtils.isNotTrue(settings.getSales().getInvoiceNoBarcode()));
                    variables.put("printSKU", BooleanUtils.isNotTrue(settings.getSales().getInvoiceNoSku()));
                } else {
                    variables.put("printBarcode", true);
                    variables.put("printSKU", true);
                }
                List<BankAccountDto> banks = new ArrayList<>();
                if (estimate.getAccount() != null) {
                    banks.add(estimate.getAccount());
                } else {
                    List<BankAccountDto> bankAccounts = getInvoiceBankAccounts();
                    banks.addAll(bankAccounts);
                }
                variables.put("banks", banks);

            } else if (document instanceof CashOperationDto) {
                templateName += GamaMoneyUtils.isNegative(((IMoneyDocument) document).getAmount()) ? "-out" : "-in";

            } else if (document instanceof TransProdDto transProd) {
                templateName = CollectionsHelper.hasValue(transProd.getPartsTo()) ? "production" : "transportation";

            } else if (document instanceof InventoryDto inventory) {
                BigDecimal totalQuantity = null;
                GamaMoney totalCostTotal = null;
                if (CollectionsHelper.hasValue(inventory.getParts())) {
                    for (var part : inventory.getParts()) {
                        totalQuantity = NumberUtils.add(totalQuantity, part.getQuantity());
                        totalCostTotal = GamaMoneyUtils.add(totalCostTotal, part.getCostTotal());
                    }
                }
                variables.put("totalQuantity", totalQuantity);
                variables.put("totalCostTotal", totalCostTotal);
            }

            generateReport(companyId, templateName, variables, out, subtype, mail, language, country);
            log.info(this.getClass().getSimpleName() + ": Document generated from template=" + templateName +
                    ", subtype=" + subtype +
                    ", mail=" + mail +
                    ", language=" + language +
                    ", country=" + country +
                    ", uuid=" + document.getUuid() +
                    ", type=" + document.getDocumentType() +
                    ", class=" + document.getClass().getSimpleName());

        } catch (NullPointerException e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    private void invoicePackingListVariablesDto(Map<String, Object> variables, InvoiceDto invoice) {
        String unitsWeight;
        BigDecimal brutto = BigDecimal.ZERO;
        BigDecimal netto = BigDecimal.ZERO;
        BigDecimal partsCount = BigDecimal.ZERO;
        BigDecimal bruttoTotal = BigDecimal.ZERO;

        // find units of weight
        Set<String> units = new HashSet<>();
        Map<String, PartSql> parts = null;

        if (CollectionsHelper.hasValue(invoice.getParts())) {

            Set<Long> partIds = invoice.getParts().stream()
                    .map(PartInvoiceDto::getId)
                    .collect(Collectors.toSet());
            parts = dbServiceSQL.queryByIds(PartSql.class, null, partIds).getResultStream()
                    .collect(Collectors.toMap(it -> String.valueOf(it.getId()), Function.identity()));

            variables.put("partsMap", parts);

            units.addAll(parts.values().stream()
                    .filter(it -> PartType.SERVICE != it.getType())
                    .map(it -> StringHelper.isEmpty(it.getUnitsWeight()) ? "" : it.getUnitsWeight().toLowerCase())
                    .collect(Collectors.toSet()));
        }
        if (CollectionsHelper.hasValue(invoice.getPacking())) {
            units.addAll(invoice.getPacking().stream()
                    .map(it -> StringHelper.isEmpty(it.getUnitsWeight()) ? "" : it.getUnitsWeight().toLowerCase())
                    .collect(Collectors.toSet()));
        }

        // unitsWeight can be "kg" or "g"
        if (units.size() == 1) {
            unitsWeight = units.iterator().next();
            if (!"g".equalsIgnoreCase(unitsWeight)) unitsWeight = "kg";
        } else {
            unitsWeight = "kg";
        }

        if (CollectionsHelper.hasValue(invoice.getParts()) && parts != null) {
            for (PartInvoiceDto partInvoice : invoice.getParts()) {
                PartSql part =  parts.get(String.valueOf(partInvoice.getId()));
                if (part == null || part.getType() == PartType.SERVICE ||
                        BigDecimalUtils.isZero(partInvoice.getQuantity())) continue;

                String partUnitsWeight = StringHelper.isEmpty(part.getUnitsWeight()) ? "" : part.getUnitsWeight().toLowerCase();

                BigDecimal k = "kg".equals(unitsWeight) && "g".equals(partUnitsWeight) ?
                        BigDecimal.valueOf(1, 3) : BigDecimal.ONE;

                brutto = BigDecimalUtils.add(brutto,
                        BigDecimalUtils.multiply(
                                BigDecimalUtils.multiply(part.getBrutto(), partInvoice.getQuantity()), k));

                netto = BigDecimalUtils.add(netto,
                        BigDecimalUtils.multiply(
                                BigDecimalUtils.multiply(part.getNetto(), partInvoice.getQuantity()), k));

                partsCount = BigDecimalUtils.add(partsCount, partInvoice.getQuantity());
            }
        }
        if (CollectionsHelper.hasValue(invoice.getPacking())) {
            bruttoTotal = brutto;
            for (Packing packing : invoice.getPacking()) {
                String packingUnitsWeight = StringHelper.isEmpty(packing.getUnitsWeight()) ? "" : packing.getUnitsWeight().toLowerCase();

                BigDecimal k = "kg".equals(unitsWeight) && "g".equals(packingUnitsWeight) ?
                        BigDecimal.valueOf(1, 3) : BigDecimal.ONE;

                bruttoTotal = BigDecimalUtils.add(bruttoTotal, BigDecimalUtils.multiply(packing.getTotalWeight(), k));
            }
        }

        variables.put("unitsWeight", unitsWeight);
        variables.put("brutto", brutto);
        variables.put("netto", netto);
        variables.put("partsCount", partsCount);
        variables.put("bruttoTotal", bruttoTotal);
    }

    private void taxFreeDeclarationVariablesDto(Map<String, Object> variables, InvoiceDto invoice) {
        variables.put("declaration", invoice.getTaxFree());
        TaxFreeForQRCode taxFree = invoice.getTaxFree().prepareForQRCode();
        try {
            String json = objectMapper.writeValueAsString(taxFree);
            byte[] bytes = compress(json);
            String encoded = Base64.getEncoder().encodeToString(bytes);
            List<String> chunks = Splitter.fixedLength(1024).splitToList(encoded);

            log.info(this.getClass().getSimpleName() + ": json=" + json.length() +
                    ", byte[]=" + bytes.length +
                    ", Base64=" + encoded.length() +
                    ", chunks=" + chunks.size());

            int total = chunks.size();
            String checksum = calculateCheckSum(encoded);
            AtomicInteger chunkNo = new AtomicInteger(0);

            List<String> qrcodes = chunks.stream()
                    .map(x -> Map.of(
                            "data", x,
                            "chunk", chunkNo.incrementAndGet(),
                            "total", total,
                            "checksum", checksum))
                    .map(x -> {
                        try {
                            return objectMapper.writeValueAsString(x);
                        } catch (JsonProcessingException e) {
                            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
                            throw new GamaException(e.getMessage(), e);
                        }
                    })
                    .map(StringEscapeUtils::escapeXml11)
                    .peek(s -> log.info(this.getClass().getSimpleName() + ": chunk.length=" + s.length()))
                    .collect(Collectors.toList());

            variables.put("qrcodes", qrcodes);

        } catch (IOException e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
            throw new GamaException(e.getMessage(), e);
        }
    }

    private byte[] compress(final String text) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final GZIPOutputStream compressor = new GZIPOutputStream(baos);
        compressor.write(text.getBytes(StandardCharsets.UTF_8));
        compressor.finish();
        compressor.close();
        return baos.toByteArray();
    }

    private String calculateCheckSum(String data) {
        final Adler32 adler32 = new Adler32();
        adler32.update(data.getBytes(StandardCharsets.UTF_8));
        return String.format("%08X", adler32.getValue());
    }

    private List<BankAccountDto> getInvoiceBankAccounts() {
        return entityManager.createQuery(
                        "SELECT a FROM " + BankAccountSql.class.getName() + " a" +
                                " WHERE (invoice IS TRUE)" +
                                " AND companyId = :companyId" +
                                " AND (a.archive IS null OR a.archive = false)",
                        BankAccountSql.class)
                .setParameter("companyId", auth.getCompanyId())
                .getResultList()
                .stream()
                .map(bankAccountSqlMapper::toDto)
                .toList();
    }

    public void generateReport(long companyId, String templateName, Map<String, Object> variables, OutputStream out) {
        generateReport(companyId, templateName, variables, out, null, false, null, null);
    }
    public void generateReport(long companyId, String templateName, Map<String, Object> variables, OutputStream out,
                               String language, String country) {
        generateReport(companyId, templateName, variables, out, null, false, language, country);
    }


    public void generateReport(long companyId, String templateName, Map<String, Object> variables, OutputStream out,
                               String subtype, boolean mail, String language, String country) {

        Validators.checkNotNull(out, "No output");

        CompanySql company = dbServiceSQL.executeAndReturnInTransaction(entityManager -> {
            auth.setCompanyId(companyId);
            auth.setSettings(authSettingsCacheService.get(auth.getCompanyId()));

            return dbServiceSQL.getById(CompanySql.class, companyId);
        });
        CompanyDto companyFront = new CompanyDto();
        String name = StringHelper.hasValue(company.getBusinessName()) ? company.getBusinessName() : company.getName();
        companyFront.setBusinessName(name);
        companyFront.setName(name);
        companyFront.setCode(company.getCode());
        companyFront.setVatCode(company.getVatCode());
        companyFront.setBusinessAddress(company.getBusinessAddress());
        companyFront.setRegistrationAddress(company.getRegistrationAddress());
        companyFront.setBanks(company.getBanks());
        companyFront.setLocations(company.getLocations());
        companyFront.setContactsInfo(company.getContactsInfo());
        companyFront.setSettings(company.getSettings());
        companyFront.setLogo(company.getLogo());
        companyFront.setEmail(company.getEmail());

        if (language == null || language.isEmpty()) language = company.getSettings().getLanguage();
        if (country == null || country.isEmpty()) country = company.getSettings().getCountry();

        if ("lt".equals(language)) {
            country = "LT";
        }
        else if ("en".equals(language) &&
                !"US".equals(country) && !"GB".equals(country) && !"CA".equals(country) && !"AU".equals(country)) {
            country = "GB";
        }

        Locale locale = Locale.of(language, country);

        variables.put("company", companyFront);
        variables.put("moneyFormatter", GamaMoneyFormatter.getInstance(locale, company.getSettings().getDecimalPrice()));
        variables.put("translate", TranslationService.getInstance());
        variables.put("moneyZero", null);

        // try to generate report from freemarker template
        generateFmReport(companyId, templateName, variables, out, subtype, mail, language, locale);
    }

    private InputStream loadFont(String name) {
        return Validators.checkNotNull(this.getClass().getClassLoader().getResourceAsStream(
                "templates" + File.separator + "fonts" + File.separator + name), name + " - Font not found");
    }

    private void generateFmReport(long companyId, String templateName, Map<String, Object> variables, OutputStream out,
                                  String subtype, boolean mail, String language, Locale locale) {
        try {
            String templatePath = "templates/" +
                    language + "/" +
                    templateName.toLowerCase() +
                    (StringHelper.hasValue(subtype) ? "_" + subtype : "") +
                    (mail ? "-mail" : "") +
                    ".ftl";
            Template template = getFmTemplate(companyId, templatePath, locale);

            if (template == null && !"en".equals(language)) {
                templatePath = "templates/" +
                        "en" + "/" +
                        templateName.toLowerCase() +
                        (StringHelper.hasValue(subtype) ? "_" + subtype : "") +
                        (mail ? "-mail" : "") +
                        ".ftl";
                template = getFmTemplate(companyId, templatePath, locale);
            }

            Validators.checkNotNull(template, "No template: {0}", templatePath);
            log.info(this.getClass().getSimpleName() + ": templatePath=" + templatePath);

            StringWriter buffer = new StringWriter();
            template.process(variables, buffer);

            // insert @page css style into html template just before </head> tag if not exists
            int position = buffer.getBuffer().indexOf("</head>");
            if (position >= 0) {
                buffer.getBuffer().replace(position, position + "</head>".length(),
                        "<style>" +
                                "@page{size:a4 portrait;margin:1.5cm 1cm 1cm 2cm}" +
                                "</style>" +
                                "</head>");
            }

            try (
                    InputStream fontRegular = loadFont("LiberationSans.ttf");
                    InputStream fontItalic = loadFont("LiberationSansItalic.ttf");
                    InputStream fontBold = loadFont("LiberationSansBold.ttf");
                    InputStream fontBoldItalic = loadFont("LiberationSansBoldItalic.ttf")
            ) {
                org.jsoup.nodes.Document doc = Jsoup.parse(buffer.toString());
                org.w3c.dom.Document w3Dom = new W3CDom().fromJsoup(doc);

                PdfRendererBuilder builder = new PdfRendererBuilder()
                        .useFastMode()
                        .useFont(() -> fontRegular, "Helvetica", 400, BaseRendererBuilder.FontStyle.NORMAL, true)
                        .useFont(() -> fontItalic, "Helvetica", 400, BaseRendererBuilder.FontStyle.ITALIC, true)
                        .useFont(() -> fontBold, "Helvetica", 700, BaseRendererBuilder.FontStyle.NORMAL, true)
                        .useFont(() -> fontBoldItalic, "Helvetica", 700, BaseRendererBuilder.FontStyle.ITALIC, true)
                        .withW3cDocument(w3Dom, null)
                        .toStream(out);

                DefaultObjectDrawerFactory factory = new DefaultObjectDrawerFactory();
                factory.registerDrawer("image/barcode", new ZXingObjectDrawer());
                builder.useObjectDrawerFactory(factory);

                builder.run();
            }

        } catch (IOException | TemplateException | NullPointerException e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    private String getLocalTemplateContent(String folder, String fileName) {
        try {
            return StringHelper.readFromFile(folder + File.separator + fileName);

        } catch (Exception e) {
            log.error(this.getClass().getSimpleName() + ": LocalTemplate=" + folder + File.separator + fileName + " error=" + e.getMessage(), e);
        }
        return null;
    }

    public String getHtmlTemplate(long companyId, String templateName, String language) {
        return getTemplateContent(companyId, templateName, language, "html");
    }

    public String getJsonData(long companyId, String templateName, String language) {
        return getTemplateContent(companyId, templateName, language, "json");
    }

    private String getTemplateContent(long companyId, String templateName, String language, String type) {
        auth.setCompanyId(companyId);
        auth.setSettings(authSettingsCacheService.get(auth.getCompanyId()));

        final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
        if (language == null || language.isEmpty()) language = companySettings.getLanguage();

        String folder = "templates/" + language + "/" + type;
        String templateFileName = templateName.toLowerCase() + "." + type;

        String content = storageService.getContent(folder, templateFileName);
        if (content == null) content = getLocalTemplateContent(folder, templateFileName);

        return content;
    }
}

package lt.gama.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lt.gama.ConstWorkers;
import lt.gama.api.response.UploadResponse;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.*;
import lt.gama.impexp.Csv;
import lt.gama.impexp.EntityType;
import lt.gama.impexp.MapBase;
import lt.gama.impexp.MapMap;
import lt.gama.model.dto.base.BaseDocPartDto;
import lt.gama.model.dto.documents.*;
import lt.gama.model.dto.documents.items.*;
import lt.gama.model.dto.entities.PartDto;
import lt.gama.model.i.ICompany;
import lt.gama.model.i.IDocPartsDto;
import lt.gama.model.mappers.PartSqlMapper;
import lt.gama.model.sql.entities.*;
import lt.gama.model.sql.entities.id.ImportId;
import lt.gama.model.type.GamaBigMoney;
import lt.gama.model.type.GamaMoney;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.model.type.enums.DBType;
import lt.gama.model.type.enums.DataFormatType;
import lt.gama.service.ex.rt.GamaException;
import lt.gama.tasks.ImportTask;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static lt.gama.service.StorageConst.extensionFromMime;


@Service
public class StorageService {

    private static final Logger log = LoggerFactory.getLogger(StorageService.class);


    @PersistenceContext
    private EntityManager entityManager;

    protected final Storage storage;
    protected final String bucketName;
    protected final Auth auth;
    protected final PartSqlMapper partSqlMapper;
    protected final TaskQueueService taskQueueService;
    protected final AppPropService appPropService;
    protected final ObjectMapper objectMapper;
    protected final DBServiceSQL dbServiceSQL;

    
    public StorageService(Auth auth, PartSqlMapper partSqlMapper, TaskQueueService taskQueueService, AppPropService appPropService, ObjectMapper objectMapper, DBServiceSQL dbServiceSQL) {
        this.auth = auth;
        this.partSqlMapper = partSqlMapper;
        this.taskQueueService = taskQueueService;
        this.appPropService = appPropService;
        this.objectMapper = objectMapper;
        this.dbServiceSQL = dbServiceSQL;
        this.storage = StorageOptions.getDefaultInstance().getService();
        this.bucketName = (appPropService.isDevelopment() ? "staging." : "") + "gama-online.appspot.com";
        log.info(this.getClass().getSimpleName() + ": Bucket='" + bucketName + '\'');
    }

    public Bucket defaultBucket() {
        return storage.get(bucketName);
    }

    public Blob getBlob(String filePath) {
        BlobId blobId = BlobId.of(bucketName, filePath);
        return storage.get(blobId);
    }

    protected Blob getBlobContentType(String filePath) {
        BlobId blobId = BlobId.of(bucketName, filePath);
        return storage.get(blobId, Storage.BlobGetOption.fields(Storage.BlobField.CONTENT_TYPE));
    }

    public boolean gcsFileExists(String filePath) {
        try {
            Blob blob = getBlob(filePath);
            return blob != null && blob.exists();
        } catch (Throwable t) {
            log.error(this.getClass().getSimpleName() + ": " + t.getMessage(), t);
            return false;
        }
    }

    public ReadableByteChannel gcsFileReadChannel(String filePath) {
        Blob blob = getBlob(filePath);
        return blob != null ? blob.reader() : null;
    }

    public WriteChannel gcsFileWriteChannel(String filePath, String mimeType, boolean publicAccess, String outputFileName) {
        Validators.checkArgument(StringHelper.hasValue(filePath), "No file name");
        BlobInfo.Builder blobBuilder = BlobInfo.newBuilder(bucketName, filePath)
                .setContentType(mimeType == null ? "text/plain" : mimeType);
        if (publicAccess) blobBuilder.setAcl(Collections.singletonList(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER)));
        if (StringHelper.hasValue(outputFileName)) {
            blobBuilder.setContentDisposition(String.format("filename=\"%s\"", outputFileName));
        }
        Blob blob = storage.create(blobBuilder.build());
        return blob.writer();
    }

    public String getPublicUrl(String filePath) {
        return "https://storage.googleapis.com/" + bucketName + "/" + filePath;
    }

    public UploadResponse getUploadUrlv4(String contentType, String folder, String fileName, boolean isPublic, String sourceFileName) {

        String uploadFilename = getFilePath(auth.getCompanyId(), folder, fileName);

        // Define Resource
        var blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, uploadFilename)).build();

        // Generate Signed URL for POST forms
        var fieldsBuilder = PostPolicyV4.PostFieldsV4.newBuilder();
        if (isPublic) fieldsBuilder.setAcl("public-read");
        if (StringHelper.hasValue(sourceFileName)) fieldsBuilder.setContentDisposition("filename=\"" + sourceFileName + '"');
        var post = storage.generateSignedPostPolicyV4(
                blobInfo,
                5, TimeUnit.MINUTES,
                fieldsBuilder.build()
        );

        UploadResponse upload = new UploadResponse();
        upload.setUpload(post.getUrl());
        upload.setUrl(getPublicUrl(uploadFilename));
        upload.setFilename(uploadFilename);
        upload.setVersion("v4");
        upload.setFields(post.getFields());

        log.info(this.getClass().getSimpleName() + ": upload url='" + upload.getUpload() + '\'');
        log.info(this.getClass().getSimpleName() + ": doc url='" + upload.getUrl() + '\'');

        return upload;
    }

    public String startImport(String fileName, String entityType, boolean delete, DataFormatType format) {

        if (EntityType.PART.toString().equals(entityType)) {
            final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
            if (!companySettings.isDisableGL() && companySettings.getGl() != null) {

                if (!Validators.isValid(companySettings.getGl().getServiceExpense()) ||
                        !Validators.isValid(companySettings.getGl().getServiceIncome()) ||
                        !Validators.isValid(companySettings.getGl().getProductAsset()) ||
                        !Validators.isValid(companySettings.getGl().getProductExpense()) ||
                        !Validators.isValid(companySettings.getGl().getProductIncome())) {

                    throw new GamaException("No default G.L. settings for products/services");
                }
            }
        }

        if (EntityType.COUNTERPARTY.toString().equals(entityType)) {
            final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
            if (!companySettings.isDisableGL() && companySettings.getGl() != null) {

                if (!Validators.isValid(companySettings.getGl().getCounterpartyCustomer()) ||
                        !Validators.isValid(companySettings.getGl().getCounterpartyVendor())) {

                    throw new GamaException("No default G.L. settings for customers/vendors");
                }
            }
        }

        final MapBase<? extends ICompany> map = MapMap.getMap(entityType);
        if (map == null) {
            throw new GamaException("Wrong import entity: '" + entityType + "'");
        }

        Validators.checkNotNull(fileName, "No file");

        // if csv or EntityType.DOCUMENT or EntityType.ASSET or EntityType.EMPLOYEE_CARD
        return taskQueueService.queueTask(new ImportTask(auth.getCompanyId(), fileName, 0, EntityType.from(entityType), delete, format));
    }

    public String upload(String content, String folder, String fileName, String mimeType) {
        if (StringHelper.isEmpty(fileName)) fileName = UUID.randomUUID() + extensionFromMime(mimeType);
        String filePath = getFilePath(auth.getCompanyId(), folder, fileName);
        return upload(content, filePath, mimeType);
    }

    public String upload(String content, String filePath, String mimeType) {
        Validators.checkArgument(StringHelper.hasValue(content));

        try {
            Blob blob = getBlob(filePath);
            if (blob == null || !blob.exists()) {
                BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, filePath)
                        .setContentType(StringHelper.hasValue(mimeType) ? mimeType : "text/plain")
                        .build();
                blob = storage.create(blobInfo);
            }

            try (WriteChannel writeChannel = blob.writer()) {
                writeChannel.write(ByteBuffer.wrap(content.getBytes(StandardCharsets.UTF_8)));
            }

            log.info(this.getClass().getSimpleName() + ": Data uploaded into " + filePath);
            return blob.getName();

        } catch (IOException e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
            throw new GamaException(e.getMessage(), e);
        }
    }

    public String getContent(String folder, String fileName) {
        String filePath = getFilePath(auth.getCompanyId(), folder, fileName);
        return getContent(filePath);
    }

    public String getContent(String filePath) {
        if (!gcsFileExists(filePath)) return null;

        try (BufferedReader reader = new BufferedReader(Channels.newReader(gcsFileReadChannel(filePath), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));

        } catch (StorageException e) {
            if (e.getCode() != 404) {
                log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
            }

        } catch (Exception e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
        return null;
    }

    public String getContentType(String filePath) {
        if (!gcsFileExists(filePath)) return null;
        Blob blob = getBlobContentType(filePath);
        return blob.getContentType();
    }

    public void deleteFile(String folder, String fileName) {
       String filePath = getFilePath(auth.getCompanyId(), folder, fileName);
       deleteFile(filePath);
    }

    public void deleteFile(String filePath) {
        try {
            Blob blob = getBlob(filePath);
            if (blob != null && blob.exists()) {
                blob.delete();
                log.info(this.getClass().getSimpleName() + ": Deleted: " + filePath);
            }
        } catch (Exception e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    protected BufferedReader getBufferedReader(String fileName) {
        Blob blob = getBlob(fileName);
        return new BufferedReader(Channels.newReader(blob.reader(), StandardCharsets.UTF_8)) {
            @Override
            public void close() throws IOException {
                super.close();
                blob.delete();
            }
        };
    }

    public GLOpeningBalanceDto importGLOpeningBalance(GLOpeningBalanceDto entity, String fileName) {
        try (BufferedReader reader = getBufferedReader(fileName)) {
            String line;
            while ((line = reader.readLine()) != null) {
                GLOpeningBalanceOperationDto balance = objectMapper.readValue(line, GLOpeningBalanceOperationDto.class);
                if (entity.getBalances() == null) entity.setBalances(new ArrayList<>());
                entity.getBalances().add(balance);
            }

        } catch (IOException e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
        return entity;
    }

    public BankOpeningBalanceDto importBankOpeningBalance(BankOpeningBalanceDto entity, String fileName) {
        try (BufferedReader reader = getBufferedReader(fileName)) {
            String line;
            while ((line = reader.readLine()) != null) {
                BankAccountBalanceDto balance = objectMapper.readValue(line, BankAccountBalanceDto.class);
                // check export id
                if (balance.getBankAccount() != null && balance.getBankAccount().getExportId() != null) {
                    ImportSql imp = dbServiceSQL.getById(ImportSql.class,
                            new ImportId(entity.getCompanyId(), BankAccountSql.class, balance.getBankAccount().getExportId()));
                    if (imp != null) {
                        balance.getBankAccount().setId(imp.getEntityId());
                    } else {
                        log.error(this.getClass().getSimpleName() + ": " + MessageFormat.format("No Import - companyId={0}, entity={1}, id={2}",
                                entity.getCompanyId(), BankAccountSql.class.getSimpleName(), balance.getBankAccount().getExportId()));
                        return null;
                    }
                }
                if (entity.getBankAccounts() == null) entity.setBankAccounts(new ArrayList<>());
                entity.getBankAccounts().add(balance);
            }

        } catch (IOException e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
        return entity;
    }

    public CashOpeningBalanceDto importCashOpeningBalance(CashOpeningBalanceDto entity, String fileName) {
        try (BufferedReader reader = getBufferedReader(fileName)) {
            String line;
            while ((line = reader.readLine()) != null) {
                CashBalanceDto balance = objectMapper.readValue(line, CashBalanceDto.class);
                // check export id
                if (balance.getCash() != null && balance.getCash().getExportId() != null) {
                    ImportSql imp = dbServiceSQL.getById(ImportSql.class,
                            new ImportId(entity.getCompanyId(), CashSql.class, balance.getCash().getExportId()));
                    if (imp != null) {
                        balance.getCash().setId(imp.getEntityId());
                    } else {
                        log.error(this.getClass().getSimpleName() + ": " + MessageFormat.format("No Import - companyId={0}, entity={1}, id={2}",
                                entity.getCompanyId(), CashSql.class.getSimpleName(), balance.getCash().getExportId()));
                        return null;
                    }
                }
                if (entity.getCashes() == null) entity.setCashes(new ArrayList<>());
                entity.getCashes().add(balance);
            }

        } catch (IOException e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
        return entity;
    }

    public DebtOpeningBalanceDto importDebtOpeningBalance(DebtOpeningBalanceDto entity, String fileName) {
        final long companyId = auth.getCompanyId();
        try (BufferedReader reader = getBufferedReader(fileName)) {
            String line;
            while ((line = reader.readLine()) != null) {
                DebtBalanceDto balance = objectMapper.readValue(line, DebtBalanceDto.class);
                // check export id
                if (balance.getCounterparty() != null && balance.getCounterparty().getExportId() != null) {
                    ImportSql imp = dbServiceSQL.getById(ImportSql.class, new ImportId(companyId, CounterpartySql.class, balance.getCounterparty().getExportId()));
                    if (imp != null) {
                        if (imp.getDb() == DBType.POSTGRESQL) {
                            balance.getCounterparty().setId(imp.getEntityId());
                        } else {
                            long id = dbServiceSQL.getIdByForeignId(CounterpartySql.class, imp.getEntityId());
                            balance.getCounterparty().setId(id);
                            imp.setEntityId(id);
                            imp.setEntityDb(DBType.POSTGRESQL);
                            dbServiceSQL.saveEntity(imp);
                        }
                    } else {
                        log.error(this.getClass().getSimpleName() + ": " + MessageFormat.format("No Import - companyId={0}, entity={1}, id={2}",
                                entity.getCompanyId(), CounterpartySql.class.getSimpleName(), balance.getCounterparty().getExportId()));
                        return null;
                    }
                }
                if (entity.getCounterparties() == null) entity.setCounterparties(new ArrayList<>());
                entity.getCounterparties().add(balance);
            }

        } catch (IOException e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
        return entity;
    }

    public EmployeeOpeningBalanceDto importAdvanceOpeningBalance(EmployeeOpeningBalanceDto entity, String fileName) {
        try (BufferedReader reader = getBufferedReader(fileName)) {
            String line;
            while ((line = reader.readLine()) != null) {
                EmployeeBalanceDto balance = objectMapper.readValue(line, EmployeeBalanceDto.class);
                // check export id
                if (balance.getEmployee() != null && balance.getEmployee().getExportId() != null) {
                    ImportSql imp = dbServiceSQL.getById(ImportSql.class,
                            new ImportId(entity.getCompanyId(), EmployeeSql.class, balance.getEmployee().getExportId()));
                    if (imp != null) {
                        balance.getEmployee().setId(imp.getEntityId());
                    } else {
                        log.error(this.getClass().getSimpleName() + ": " + MessageFormat.format("No Import - companyId={0}, entity={1}, id={2}",
                                entity.getCompanyId(), EmployeeSql.class.getSimpleName(), balance.getEmployee().getExportId()));
                        return null;
                    }
                }
                if (entity.getEmployees() == null) entity.setEmployees(new ArrayList<>());
                entity.getEmployees().add(balance);
            }

        } catch (IOException e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
        return entity;
    }

    private <P extends BaseDocPartDto, E extends ICompany & IDocPartsDto<P>> E importPartsSQL(Class<P> clazz, E entity, String fileName) {
        try (BufferedReader reader = getBufferedReader(fileName)) {
            String line;
            Map<String, PartDto> allUsedSKUs = new HashMap<>();
            Map<Long, PartDto> allUsedIds = new HashMap<>();

            while ((line = reader.readLine()) != null) {
                P balance = objectMapper.readValue(line, clazz);

                // check warehouse export id
                if (clazz.isAssignableFrom(PartInventoryDto.class)) {
                    String exportId = (((PartInventoryDto) balance).getWarehouse().getExportId());
                    if  (exportId != null) {
                        ImportSql imp = dbServiceSQL.getById(ImportSql.class,
                                new ImportId(entity.getCompanyId(), WarehouseSql.class, exportId));
                        if (imp != null) {
                            ((PartInventoryDto) balance).getWarehouse().setId(imp.getEntityId());
                        } else {
                            throw new GamaException(MessageFormat.format("No Import - companyId={0}, entity={1}, id={2}",
                                    entity.getCompanyId(), WarehouseSql.class.getSimpleName(), exportId));
                        }
                    }
                }

                // check part export id
                if (balance.getExportId() != null) {
                    ImportSql imp = dbServiceSQL.getById(ImportSql.class,
                            new ImportId(entity.getCompanyId(), PartSql.class, balance.getExportId()));
                    if (imp != null) {
                        balance.setId(imp.getEntityId());
                        var part = allUsedIds.get(balance.getId());
                        if (part == null) part = this.partSqlMapper.toDto(dbServiceSQL.getById(PartSql.class, balance.getId()));
                        if (part != null) {
                            balance.setSku(part.getSku());
                            balance.setType(part.getType());
                            balance.setBarcode(part.getBarcode());
                            balance.setName(part.getName());
                            balance.setUnit(part.getUnit());
                        }
                    } else {
                        log.error(MessageFormat.format("No Import - companyId={0}, entity={1}, id={2}",
                                entity.getCompanyId(), PartSql.class.getSimpleName(), balance.getExportId()));
                        return null;
                    }
                }
                if (!Validators.isValid(balance)) {
                    // if no id check part SKU
                    if (StringHelper.hasValue(balance.getSku())) {
                        var part = allUsedSKUs.get(balance.getSku());
                        if (part == null) {
                            part = entityManager.createQuery(
                                            "SELECT p" +
                                                    " FROM " + PartSql.class.getName() + " p" +
                                                    " WHERE sku = :sku" +
                                                    " AND companyId = :companyId" +
                                                    " AND (p.archive IS null OR p.archive = false)" +
                                                    " AND (p.hidden IS null OR p.hidden = false)", PartSql.class)
                                    .setParameter("companyId", auth.getCompanyId())
                                    .setParameter("sku", balance.getSku())
                                    .getResultStream()
                                    .limit(1)
                                    .map(this.partSqlMapper::toDto)
                                    .findAny()
                                    .orElse(null);
                            if (part != null) {
                                allUsedSKUs.put(balance.getSku(), part);
                                allUsedIds.put(part.getId(), part);
                            }
                        }
                        if (part != null) {
                            balance.setId(part.getId());
                            balance.setSku(part.getSku());
                            balance.setType(part.getType());
                            balance.setBarcode(part.getBarcode());
                            balance.setName(part.getName());
                            balance.setUnit(part.getUnit());
                        }
                    }
                    if (!Validators.isValid(balance)) throw new GamaException(MessageFormat.format("No part id - companyId={0}",
                            entity.getCompanyId()));
                }
                balance.setCompanyId(entity.getCompanyId());

                if (entity.getParts() == null) entity.setParts(new ArrayList<>());
                entity.getParts().add(balance);
            }

        } catch (IOException e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
        return entity;
    }

    private <P extends BaseDocPartDto> List<P> importPartsJson(Class<P> clazz, String fileName) {
        List<P> parts = new ArrayList<>();
        try (BufferedReader reader = getBufferedReader(fileName)) {
            String line;
            Map<String, PartDto> allUsedSKUs = new HashMap<>();
            while ((line = reader.readLine()) != null) {
                P balance = objectMapper.readValue(line, clazz);

                // check warehouse export id
                if (clazz.isAssignableFrom(PartInventoryDto.class)) {
                    String exportId = (((PartInventoryDto) balance).getWarehouse().getExportId());
                    if  (exportId != null) {
                        ImportSql imp = dbServiceSQL.getById(ImportSql.class,
                                new ImportId(auth.getCompanyId(), WarehouseSql.class, exportId));
                        if (imp != null) {
                            ((PartInventoryDto) balance).getWarehouse().setId(imp.getEntityId());
                        } else {
                            throw new GamaException(MessageFormat.format("No Import - companyId={0}, entity={1}, id={2}",
                                    auth.getCompanyId(), WarehouseSql.class.getSimpleName(), exportId));
                        }
                    }
                }

                // check part export id
                if (balance.getExportId() != null) {
                    ImportSql imp = dbServiceSQL.getById(ImportSql.class,
                            new ImportId(auth.getCompanyId(), PartSql.class, balance.getExportId()));
                    if (imp != null) {
                        balance.setId(imp.getEntityId());
                    } else {
                        log.error(MessageFormat.format("No Import - companyId={0}, entity={1}, id={2}",
                                auth.getCompanyId(), PartSql.class.getSimpleName(), balance.getExportId()));
                        return null;
                    }
                }
                if (!Validators.isValid(balance)) {
                    // if no id check part SKU
                    if (StringHelper.hasValue(balance.getSku())) {
                        var part = allUsedSKUs.get(balance.getSku());
                        if (part == null) {
                            part = entityManager.createQuery(
                                            "SELECT p" +
                                                    " FROM " + PartSql.class.getName() + " p" +
                                                    " WHERE sku = :sku" +
                                                    " AND companyId = :companyId" +
                                                    " AND (p.archive IS null OR p.archive = false)" +
                                                    " AND (p.hidden IS null OR p.hidden = false)", PartSql.class)
                                    .setParameter("companyId", auth.getCompanyId())
                                    .setParameter("sku", balance.getSku())
                                    .getResultStream()
                                    .limit(1)
                                    .map(this.partSqlMapper::toDto)
                                    .findAny()
                                    .orElse(null);
                            if (part != null) allUsedSKUs.put(balance.getSku(), part);
                        }
                        if (part != null) {
                            balance.setId(part.getId());
                            balance.setSku(part.getSku());
                            balance.setType(part.getType());
                            balance.setBarcode(part.getBarcode());
                            balance.setName(part.getName());
                            balance.setUnit(part.getUnit());
                        }
                    }
                    if (!Validators.isValid(balance)) throw new GamaException(MessageFormat.format("No part id - companyId={0}",
                            auth.getCompanyId()));
                }
                balance.setCompanyId(auth.getCompanyId());
                parts.add(balance);
            }

        } catch (IOException e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
        return parts;
    }

    private <P extends BaseDocPartDto> List<P> importPartsCsv(String fileName, DataFormatType format, Function<CSVRecord, P> factory) {
        List<P> parts = new ArrayList<>();
        try (BufferedReader reader = getBufferedReader(fileName);
             CSVParser parser = new CSVParser(reader, format == DataFormatType.CSV ? Csv.getCSVFormat() : Csv.getCSVFormatTab())) {
            Map<String, PartDto> allUsedSKUs = new HashMap<>();
            for (final CSVRecord record : parser) {
                var balance = factory.apply(record);
                // check part export id
                if (balance.getExportId() != null) {
                    ImportSql imp = dbServiceSQL.getById(ImportSql.class,
                            new ImportId(auth.getCompanyId(), PartSql.class, balance.getExportId()));
                    if (imp != null) {
                        balance.setId(imp.getEntityId());
                    } else {
                        log.error(MessageFormat.format("No Import - companyId={0}, entity={1}, id={2}",
                                auth.getCompanyId(), PartSql.class.getSimpleName(), balance.getExportId()));
                        return null;
                    }
                }
                if (!Validators.isValid(balance)) {
                    // if no id check part SKU
                    if (StringHelper.hasValue(balance.getSku())) {
                        var part = allUsedSKUs.get(balance.getSku());
                        if (part == null) {
                            part = entityManager.createQuery(
                                            "SELECT p" +
                                                    " FROM " + PartSql.class.getName() + " p" +
                                                    " WHERE sku = :sku" +
                                                    " AND companyId = :companyId" +
                                                    " AND (p.archive IS null OR p.archive = false)" +
                                                    " AND (p.hidden IS null OR p.hidden = false)", PartSql.class)
                                    .setParameter("companyId", auth.getCompanyId())
                                    .setParameter("sku", balance.getSku())
                                    .getResultStream()
                                    .limit(1)
                                    .map(this.partSqlMapper::toDto)
                                    .findAny()
                                    .orElse(null);
                            if (part != null) allUsedSKUs.put(balance.getSku(), part);
                        }
                        if (part != null) {
                            balance.setId(part.getId());
                            balance.setSku(part.getSku());
                            balance.setType(part.getType());
                            balance.setBarcode(part.getBarcode());
                            balance.setName(part.getName());
                            balance.setUnit(part.getUnit());
                        }
                    }
                    if (!Validators.isValid(balance)) throw new GamaException(MessageFormat.format("No part id - companyId={0}",
                            auth.getCompanyId()));
                }
                balance.setCompanyId(auth.getCompanyId());
                parts.add(balance);
            }

        } catch (IOException e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
        return parts;
    }

    public InventoryOpeningBalanceDto importInventoryOpeningBalanceSQL(InventoryOpeningBalanceDto entity, String fileName) {
        return importPartsSQL(PartOpeningBalanceDto.class, entity, fileName);
    }

    public TransProdDto importTransProdSQL(TransProdDto entity, String fileName) {
        try (BufferedReader reader = getBufferedReader(fileName)) {
            String line;
            while ((line = reader.readLine()) != null) {
                PartFromDto balance = objectMapper.readValue(line, PartFromDto.class);
                // check export id
                if (balance.getExportId() != null) {
                    ImportSql imp = dbServiceSQL.getById(ImportSql.class,
                            new ImportId(entity.getCompanyId(), PartSql.class, balance.getExportId()));
                    if (imp != null) {
                        // TODO set other values from db - name, sku, barcode, unit, type? Make same as in importPartsSQL. Fix tests.
                        balance.setId(imp.getEntityId());
                    } else {
                        log.error(MessageFormat.format("No Import - companyId={0}, entity={1}, id={2}",
                                entity.getCompanyId(), PartSql.class.getSimpleName(), balance.getExportId()));
                        return null;
                    }
                }
                if (!Validators.isValid(balance)) {
                    // if no id check part SKU
                    if (StringHelper.hasValue(balance.getSku())) {
                        List<Long> ids = entityManager.createQuery(
                                        "SELECT id" +
                                                " FROM " + PartSql.class.getName() + " p" +
                                                " WHERE sku = :sku" +
                                                " AND companyId = :companyId" +
                                                " AND (p.archive IS null OR p.archive = false)" +
                                                " AND (p.hidden IS null OR p.hidden = false)", Long.class)
                                .setParameter("companyId", auth.getCompanyId())
                                .setParameter("sku", balance.getSku())
                                .getResultList();
                        if (CollectionsHelper.hasValue(ids)) {
                            balance.setId(ids.get(0));
                        }
                    }
                    if (!Validators.isValid(balance)) throw new GamaException(MessageFormat.format("No id - companyId={0}, entity={1}",
                            entity.getCompanyId(), PartSql.class.getSimpleName()));
                }
                balance.setCompanyId(entity.getCompanyId());

                if (entity.getPartsFrom() == null) entity.setPartsFrom(new ArrayList<>());
                entity.getPartsFrom().add(balance);
            }

        } catch (IOException e) {
            log.error(this.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
        return entity;
    }

    public InventoryDto importInventorySQL(InventoryDto entity, String fileName) {
        return importPartsSQL(PartInventoryDto.class, entity, fileName);
    }

    public List<PartPurchaseDto> importPurchase(String fileName, DataFormatType format, String currency) {
        return switch (format) {
            case JSON -> importPartsJson(PartPurchaseDto.class, fileName);
            case CSV, CSV_TAB -> importPartsCsv(fileName, format, (CSVRecord record) -> {
                var quantity = CSVRecordUtils.getDecimal(record, "quantity");
                var price = CSVRecordUtils.getDecimalMoneyPart(record, "price", currency);
                var total = CSVRecordUtils.getDecimalMoneyPart(record, "total", currency);
                var sku = CSVRecordUtils.getString(record, "sku");
                var part = new PartPurchaseDto();
                part.setQuantity(quantity);
                part.setPrice(GamaBigMoney.of(currency, price));
                if (total != null) {
                    part.setTotal(GamaMoney.of(currency, total));
                } else {
                    part.setTotal(GamaMoneyUtils.multipliedBy(part.getPrice(), part.getQuantity()).toMoney());
                }
                part.setSku(sku);
                return part;
            });
        };
    }

    public String getFilePath(Long companyId, String folder, String fileName) {
        return (appPropService.isDevelopment() ? "_develop/" : "") +
                (companyId != null ? companyId + "/" : "") +
                (folder != null ? folder : ConstWorkers.IMPORT_FOLDER) + "/" +
                (fileName != null ? fileName : java.util.UUID.randomUUID().toString());
    }

}

package lt.gama.api.impl.v4;

import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.PageRequest;
import lt.gama.api.response.PageResponse;
import lt.gama.api.service.v4.PurchaseApi;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.BooleanUtils;
import lt.gama.helpers.EntityUtils;
import lt.gama.helpers.StringHelper;
import lt.gama.helpers.Validators;
import lt.gama.model.dto.documents.PurchaseApiDto;
import lt.gama.model.i.IFinished;
import lt.gama.model.mappers.PurchaseApiSqlMapper;
import lt.gama.model.sql.documents.PurchaseSql;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.model.type.enums.DBType;
import lt.gama.service.*;
import lt.gama.service.ex.rt.GamaNotFoundException;
import org.springframework.web.bind.annotation.RestController;

import java.text.MessageFormat;
import java.time.LocalDate;

@RestController("PurchaseApiImplv4")
public class PurchaseApiImpl implements PurchaseApi {

    private final Auth auth;
    private final PurchaseApiSqlMapper purchaseApiSqlMapper;
    private final DocumentService documentService;
    private final TradeService tradeService;
    private final DBServiceSQL dbServiceSQL;
    private final APIResultService apiResultService;

    public PurchaseApiImpl(Auth auth, PurchaseApiSqlMapper purchaseApiSqlMapper, DocumentService documentService, TradeService tradeService, DBServiceSQL dbServiceSQL, APIResultService apiResultService) {
        this.auth = auth;
        this.purchaseApiSqlMapper = purchaseApiSqlMapper;
        this.documentService = documentService;
        this.tradeService = tradeService;
        this.dbServiceSQL = dbServiceSQL;
        this.apiResultService = apiResultService;
    }


    @Override
    public PageResponse<PurchaseApiDto, Void> list(LocalDate dateFrom, LocalDate dateTo, Integer cursor, int pageSize) throws GamaApiException {
        return apiResultService.execute(() -> {
            Validators.checkArgument(pageSize > 0 && pageSize <= PageRequest.MAX_PAGE_SIZE,
                    MessageFormat.format(
                            TranslationService.getInstance().translate(TranslationService.VALIDATORS.InvalidPageSize, auth.getLanguage()),
                            pageSize, PageRequest.MAX_PAGE_SIZE));

            PageRequest pageRequest = new PageRequest();
            pageRequest.setPageSize(pageSize);
            pageRequest.setCursor(cursor);
            pageRequest.setOrder("mainIndex");
            pageRequest.setDateRange(true);
            pageRequest.setDateFrom(dateFrom);
            pageRequest.setDateTo(dateTo);

            return dbServiceSQL.list(pageRequest, PurchaseSql.class, PurchaseSql.GRAPH_ALL, purchaseApiSqlMapper,
                    (cb, root) -> EntityUtils.whereDoc(pageRequest, cb, root, null, null),
                    (cb, root) -> EntityUtils.orderDoc(pageRequest.getOrder(), cb, root),
                    (cb, root) -> EntityUtils.selectIdDoc(pageRequest.getOrder(), cb, root));
        });
    }


    @Override
    public PurchaseApiDto get(long id) throws GamaApiException {
        return apiResultService.execute(() -> {
            PurchaseApiDto document = purchaseApiSqlMapper.toDto(dbServiceSQL.getAndCheckNullable(PurchaseSql.class, id, PurchaseSql.GRAPH_ALL));
            if (document == null) {
                throw new GamaNotFoundException(MessageFormat.format(
                        TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDocumentWithId, auth.getLanguage()),
                        id));
            }

            return document;
        });
    }

    @Override
    public PurchaseApiDto create(PurchaseApiDto documentDto) throws GamaApiException {
        return apiResultService.execute(() -> {
            final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");

            Validators.checkArgument(documentDto.getId() == null, "id specified");

            Validators.checkDocumentDate(companySettings, documentDto, auth.getLanguage());
            Validators.checkArgument(StringHelper.hasValue(documentDto.getNumber()),
                    TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDocumentNumber, auth.getLanguage()));
            Validators.checkValid(documentDto.getWarehouse(),
                    TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoWarehouse, auth.getLanguage()));

            documentDto.setId(null);

            return purchaseApiSqlMapper.dtoToApiDto(tradeService.savePurchase(purchaseApiSqlMapper.apiDtoToDto(documentDto)));
        });
    }

    @Override
    public PurchaseApiDto update(PurchaseApiDto documentDto) throws GamaApiException {
        return apiResultService.execute(() -> {
            Validators.checkArgument(documentDto.getId() != null && documentDto.getId() != 0,
                    TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDocumentId, auth.getLanguage()));
            Validators.checkValid(documentDto.getWarehouse(),
                    TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoWarehouse, auth.getLanguage()));

            IFinished document = dbServiceSQL.getAndCheckNullable(PurchaseSql.class, documentDto.getId());

            if (document == null) {
                throw new GamaNotFoundException(MessageFormat.format(
                        TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDocumentWithId, auth.getLanguage()),
                        documentDto.getId()));
            }

            Validators.checkArgument(BooleanUtils.isNotTrue(document.getFinished()),
                    TranslationService.getInstance().translate(TranslationService.VALIDATORS.DocumentFinishedAlready, auth.getLanguage()));

            final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");

            Validators.checkDocumentDate(companySettings, documentDto, auth.getLanguage());
            Validators.checkArgument(StringHelper.hasValue(documentDto.getNumber()),
                    TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDocumentNumber, auth.getLanguage()));

            return purchaseApiSqlMapper.dtoToApiDto(tradeService.savePurchase(purchaseApiSqlMapper.apiDtoToDto(documentDto)));
        });
    }

    @Override
    public void delete( long id) throws GamaApiException {
        apiResultService.execute(() -> {
            Validators.checkArgument(id != 0,
                    TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDocumentId, auth.getLanguage()));

            PurchaseApiDto document = purchaseApiSqlMapper.toDto(dbServiceSQL.getAndCheckNullable(PurchaseSql.class, id));
            if (document == null) {
                throw new GamaNotFoundException(
                        MessageFormat.format(
                                TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDocumentWithId, auth.getLanguage()),
                                id));
            }
            Validators.checkArgument(BooleanUtils.isNotTrue(document.getFinished()),
                    TranslationService.getInstance().translate(TranslationService.VALIDATORS.DocumentFinishedAlready, auth.getLanguage()));

            documentService.deleteDocument(id, DBType.POSTGRESQL);
            return null;
        });
    }
}

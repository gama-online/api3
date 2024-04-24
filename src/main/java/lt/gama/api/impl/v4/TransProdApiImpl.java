package lt.gama.api.impl.v4;

import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.PageRequest;
import lt.gama.api.response.PageResponse;
import lt.gama.api.service.v4.TransProdApi;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.BooleanUtils;
import lt.gama.helpers.EntityUtils;
import lt.gama.helpers.StringHelper;
import lt.gama.helpers.Validators;
import lt.gama.model.dto.documents.TransProdApiDto;
import lt.gama.model.i.IFinished;
import lt.gama.model.mappers.TransProdApiSqlMapper;
import lt.gama.model.sql.documents.TransProdSql;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.model.type.enums.DBType;
import lt.gama.service.*;
import lt.gama.service.ex.rt.GamaNotFoundException;
import org.springframework.web.bind.annotation.RestController;

import java.text.MessageFormat;
import java.time.LocalDate;

@RestController("TransProdApiImplv4")
public class TransProdApiImpl implements TransProdApi {

    private final Auth auth;
    private final TransProdApiSqlMapper transProdApiSqlMapper;
    private final DocumentService documentService;
    private final TradeService tradeService;
    private final DBServiceSQL dbServiceSQL;
    private final APIResultService apiResultService;

    public TransProdApiImpl(Auth auth, TransProdApiSqlMapper transProdApiSqlMapper, DocumentService documentService, TradeService tradeService, DBServiceSQL dbServiceSQL, APIResultService apiResultService) {
        this.auth = auth;
        this.transProdApiSqlMapper = transProdApiSqlMapper;
        this.documentService = documentService;
        this.tradeService = tradeService;
        this.dbServiceSQL = dbServiceSQL;
        this.apiResultService = apiResultService;
    }

    @Override
    public PageResponse<TransProdApiDto, Void> list(LocalDate dateFrom, LocalDate dateTo, Integer cursor, int pageSize) throws GamaApiException {
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

            return dbServiceSQL.list(pageRequest, TransProdSql.class, TransProdSql.GRAPH_ALL, transProdApiSqlMapper,
                    (cb, root) -> EntityUtils.whereDoc(pageRequest, cb, root, null, null),
                    (cb, root) -> EntityUtils.orderDoc(pageRequest.getOrder(), cb, root),
                    (cb, root) -> EntityUtils.selectIdDoc(pageRequest.getOrder(), cb, root));
        });
    }

    @Override
    public TransProdApiDto get(long id) throws GamaApiException {
        return apiResultService.execute(() -> {
            TransProdApiDto document = transProdApiSqlMapper.toDto(dbServiceSQL.getAndCheckNullable(TransProdSql.class, id, TransProdSql.GRAPH_ALL));
            if (document == null) {
                throw new GamaNotFoundException(MessageFormat.format(
                        TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDocumentWithId, auth.getLanguage()),
                        id));
            }
            return document;
        });
    }

    @Override
    public TransProdApiDto create(TransProdApiDto documentDto) throws GamaApiException {
        return apiResultService.execute(() -> {
            final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");

            Validators.checkArgument(documentDto.getId() == null, "id specified");

            Validators.checkDocumentDate(companySettings, documentDto, auth.getLanguage());
            Validators.checkArgument(BooleanUtils.isTrue(documentDto.getAutoNumber()) || StringHelper.hasValue(documentDto.getNumber()),
                    TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDocumentNumber, auth.getLanguage()));
            Validators.checkValid(documentDto.getWarehouseFrom(),
                    TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoWarehouse, auth.getLanguage()));

            documentDto.setId(null);

            return transProdApiSqlMapper.dtoToApiDto(tradeService.saveTransProd(transProdApiSqlMapper.apiDtoToDto(documentDto)));
        });
    }

    @Override
    public TransProdApiDto update(TransProdApiDto documentDto) throws GamaApiException {
        return apiResultService.execute(() -> {
            Validators.checkArgument(documentDto.getId() != null && documentDto.getId() != 0,
                    TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDocumentId, auth.getLanguage()));
            Validators.checkValid(documentDto.getWarehouseFrom(),
                    TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoWarehouse, auth.getLanguage()));

            IFinished document = dbServiceSQL.getAndCheckNullable(TransProdSql.class, documentDto.getId());
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

            return transProdApiSqlMapper.dtoToApiDto(tradeService.saveTransProd(transProdApiSqlMapper.apiDtoToDto(documentDto)));
        });
    }

    @Override
    public void delete(long id) throws GamaApiException {
        apiResultService.execute(() -> {
            Validators.checkArgument(id != 0,
                    TranslationService.getInstance().translate(TranslationService.VALIDATORS.NoDocumentId, auth.getLanguage()));

            TransProdApiDto document = transProdApiSqlMapper.toDto(dbServiceSQL.getAndCheckNullable(TransProdSql.class, id));
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

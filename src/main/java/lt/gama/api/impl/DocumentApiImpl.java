package lt.gama.api.impl;

import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.DocumentRequest;
import lt.gama.api.request.HtmlTemplateRequest;
import lt.gama.api.request.IdRequest;
import lt.gama.api.request.TaskStatusRequest;
import lt.gama.api.response.TaskResponse;
import lt.gama.api.service.DocumentApi;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.StringHelper;
import lt.gama.model.dto.base.BaseDocumentDto;
import lt.gama.model.i.base.IBaseDocument;
import lt.gama.model.mappers.DoubleEntrySqlMapper;
import lt.gama.model.mappers.IBaseMapper;
import lt.gama.model.sql.base.BaseDocumentSql;
import lt.gama.model.sql.documents.DoubleEntrySql;
import lt.gama.model.type.enums.DBType;
import lt.gama.service.*;
import lt.gama.service.ex.rt.GamaException;
import lt.gama.service.ex.rt.GamaNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.text.MessageFormat;

/**
 * Gama
 * Created by valdas on 15-07-31.
 */
@RestController
public class DocumentApiImpl implements DocumentApi {

    private final DocumentService documentService;
    private final TemplateService templateService;
    private final DBServiceSQL dbServiceSQL;
    private final DoubleEntrySqlMapper doubleEntrySqlMapper;
    private final Auth auth;
    private final DocsMappersService docsMappersService;
    private final APIResultService apiResultService;

    public DocumentApiImpl(DocumentService documentService, TemplateService templateService, DBServiceSQL dbServiceSQL, DoubleEntrySqlMapper doubleEntrySqlMapper, Auth auth, DocsMappersService docsMappersService, APIResultService apiResultService) {
        this.documentService = documentService;
        this.templateService = templateService;
        this.dbServiceSQL = dbServiceSQL;
        this.doubleEntrySqlMapper = doubleEntrySqlMapper;
        this.auth = auth;
        this.docsMappersService = docsMappersService;
        this.apiResultService = apiResultService;
    }

    @Override
    public APIResult<IBaseDocument> get(DocumentRequest request) throws GamaApiException {
        return apiResultService.result(() -> documentService.getDocument(request.getDocumentType(), request.getId(), request.getDb()));
    }

    @Override
    public APIResult<IBaseDocument> getByDE(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            DoubleEntrySql doubleEntry = dbServiceSQL.getAndCheck(DoubleEntrySql.class, request.getId(), DoubleEntrySql.GRAPH_ALL);
            if (doubleEntry != null && doubleEntry.getParentId() != null) {
                BaseDocumentSql document = dbServiceSQL.getAndCheck(BaseDocumentSql.class, doubleEntry.getParentId(),
                        docsMappersService.getGraphName(docsMappersService.getDocumentClass(DBType.POSTGRESQL, doubleEntry.getParentType())));
                @SuppressWarnings("unchecked")
                var mapper = (IBaseMapper<BaseDocumentDto, BaseDocumentSql>) docsMappersService.getOrNull(document.getClass());
                IBaseDocument doc = mapper.toDto(document);
                doc.setDoubleEntry(doubleEntrySqlMapper.toDto(doubleEntry));
                return doc;
            }
            throw new GamaNotFoundException(MessageFormat.format(
                    TranslationService.getInstance().translate(TranslationService.GL.NoDoubleEntryDocumentWithId, auth.getLanguage()),
                    request.getId()));
        });
    }

    @Override
    public APIResult<TaskResponse<Object>> getTaskStatus(TaskStatusRequest request) throws GamaApiException {
        return apiResultService.execute(() -> documentService.getTaskStatus(request.getId()));
    }

    @Override
    public APIResult<String> getHtmlTemplate(HtmlTemplateRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            if (StringHelper.isEmpty(request.getTemplate())) throw new GamaException("No template");

            return templateService.getHtmlTemplate(auth.getCompanyId(),
                    request.getTemplate(), request.getLanguage());
        });
    }
}

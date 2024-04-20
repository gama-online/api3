package lt.gama.api.impl;

import lt.gama.api.APIResult;
import lt.gama.api.ex.GamaApiException;
import lt.gama.api.request.IdRequest;
import lt.gama.api.service.OCApi;
import lt.gama.auth.impl.Auth;
import lt.gama.helpers.BooleanUtils;
import lt.gama.helpers.GamaMoneyUtils;
import lt.gama.helpers.StringHelper;
import lt.gama.helpers.Validators;
import lt.gama.model.dto.entities.PartDto;
import lt.gama.model.i.ICounterparty;
import lt.gama.model.mappers.PartSqlMapper;
import lt.gama.model.sql.entities.CounterpartySql;
import lt.gama.model.sql.entities.ImportSql;
import lt.gama.model.sql.entities.PartSql;
import lt.gama.model.sql.entities.id.ImportId;
import lt.gama.model.type.Contact;
import lt.gama.model.type.NameContact;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.model.type.enums.DBType;
import lt.gama.model.type.enums.SyncType;
import lt.gama.model.type.sync.SyncSettings;
import lt.gama.service.APIResultService;
import lt.gama.service.AuthSettingsCacheService;
import lt.gama.service.DBServiceSQL;
import lt.gama.service.TaskQueueService;
import lt.gama.service.ex.rt.GamaException;
import lt.gama.service.ex.rt.GamaUnauthorizedException;
import lt.gama.service.sync.SyncHttpService;
import lt.gama.service.sync.i.ISyncOpenCartUtilsService;
import lt.gama.service.sync.openCart.SyncOpenCartUtilsService;
import lt.gama.service.sync.openCart.model.OCIdResponse;
import lt.gama.service.sync.openCart.model.OCImportStep;
import lt.gama.service.sync.openCart.model.OCLogin;
import lt.gama.service.sync.openCart.tasks.OCUploadCustomerTask;
import lt.gama.tasks.sync.SyncCompanyTask;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 2019-03-02.
 */
@RestController
public class OCApiImpl implements OCApi {

    private final SyncOpenCartUtilsService syncOpenCartUtilsService;
    private final Auth auth;
    private final DBServiceSQL dbServiceSQL;
    private final AuthSettingsCacheService authSettingsCacheService;
    private final SyncHttpService syncHttpService;
    private final PartSqlMapper partSqlMapper;
    private final TaskQueueService taskQueueService;
    private final APIResultService apiResultService;

    public OCApiImpl(SyncOpenCartUtilsService syncOpenCartUtilsService, Auth auth, DBServiceSQL dbServiceSQL, AuthSettingsCacheService authSettingsCacheService, SyncHttpService syncHttpService, PartSqlMapper partSqlMapper, TaskQueueService taskQueueService, APIResultService apiResultService) {
        this.syncOpenCartUtilsService = syncOpenCartUtilsService;
        this.auth = auth;
        this.dbServiceSQL = dbServiceSQL;
        this.authSettingsCacheService = authSettingsCacheService;
        this.syncHttpService = syncHttpService;
        this.partSqlMapper = partSqlMapper;
        this.taskQueueService = taskQueueService;
        this.apiResultService = apiResultService;
    }

    @Override
    public APIResult<OCImportStep> adminUploadProduct(AdminUploadProductRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            auth.setCompanyId(request.companyId);
            auth.setSettings(authSettingsCacheService.get(auth.getCompanyId()));

            final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
            SyncSettings syncSettings = Validators.checkNotNull(companySettings.getSync(), "No Sync settings");

            PartDto part = partSqlMapper.toDto(Validators.checkNotNull(dbServiceSQL.getById(PartSql.class, request.partId), "No part with id = " + request.partId));
            Long ocSku = part.getForeignId() != null ? part.getForeignId() : part.getId();

            OCLogin login = Validators.checkNotNull(syncOpenCartUtilsService.login(syncSettings.getUrl(), syncSettings.getKey(), syncSettings.getId()), "Can't login");

            OCIdResponse response = syncHttpService.getRequestData(
                    SyncHttpService.HttpMethod.POST,
                    syncSettings.getUrl(),
                    Map.of("route", "api/customerproduct/addEditProduct", "token", login.getToken()),
                    SyncHttpService.ContentType.FORM,
                    Map.of(
                            "name", part.getName(),
                            "model", part.getSku() != null ? part.getSku() : "",
                            "sku", ocSku.toString(),
                            "meta_title", part.getName(),
                            "price", GamaMoneyUtils.isPositive(part.getPrice()) ? part.getPrice().getAmount().toString() : "0",
                            "keyword", part.getSku(),
                            "quantity", part.getQuantityTotal() != null ? part.getQuantityTotal().toString() : "0",
                            "product_store", "0",
                            "status", "1"),
                    OCIdResponse.class,
                    login.getSession());

            OCImportStep step = new OCImportStep();

            if (response != null && StringHelper.hasValue(response.getId())) {
                ImportSql imp = dbServiceSQL.getById(ImportSql.class, new ImportId(part.getCompanyId(), PartSql.class, response.getId()));
                if (imp != null) {
                    if (!Objects.equals(imp.getEntityId(), part.getId())) {
                        imp.setEntityId(part.getId());
                        dbServiceSQL.saveEntity(imp);
                        step.updated();
                    } else {
                        step.skipped();
                    }
                } else {
                    step.created();
                    imp = new ImportSql(part.getCompanyId(), PartSql.class, response.getId(), part.getId(), DBType.POSTGRESQL);
                    dbServiceSQL.saveEntity(imp);
                }
            }
            return step;
        });
    }

    @Override
    public APIResult<String> adminUploadProducts(AdminUploadProductsRequest request) throws GamaApiException {
        return apiResultService.result(() -> taskQueueService.queueTask(new SyncCompanyTask(request.companyId)));
    }

    @Override
    public APIResult<String> adminAddCustomer(AdminAddCustomerRequest request) throws GamaApiException {
        return apiResultService.execute(() -> {
            auth.setCompanyId(request.companyId);
            auth.setSettings(authSettingsCacheService.get(auth.getCompanyId()));

            IdRequest idRequest = new IdRequest();
            idRequest.setId(request.customerId);

            return uploadCustomer(idRequest);
        });
    }

    @Override
    public APIResult<String> uploadCustomer(IdRequest request) throws GamaApiException {
        return apiResultService.result(() -> {
            final long companyId = auth.getCompanyId();
            final CompanySettings companySettings = Validators.checkNotNull(auth.getSettings(), "No company settings");
            SyncSettings syncSettings = Validators.checkNotNull(companySettings.getSync(), "No Sync settings");

            if (BooleanUtils.isNotTrue(syncSettings.getSyncActive())) throw new GamaException("Sync is not active");
            if (syncSettings.getType() != SyncType.OPENCART_A) throw new GamaException("Sync type is not OpenCart A");
            if (BooleanUtils.isNotTrue(syncSettings.getAbilities().customer().fromGama())) throw new GamaException("Sync customers from Gama is not activated");

            CounterpartySql counterparty = dbServiceSQL.getByIdOrForeignId(CounterpartySql.class, request.getId(), request.getDb());
            if (counterparty.getCompanyId() != auth.getCompanyId()) {
                throw new GamaUnauthorizedException("Wrong company, CounterpartySql id/companyId=" + counterparty.getId() +
                        "/" + counterparty.getCompanyId() +
                        ", companyId=" + auth.getCompanyId());
            }
            String email = getContactsEmail(counterparty);
            if (StringHelper.isEmpty(email)) throw new GamaException("No email");

            taskQueueService.queueTask(new OCUploadCustomerTask(companyId, counterparty.getId(), email));

            return "Done";
        });
    }


    /**
     * Get first email from contacts
     */
    private String getContactsEmail(ICounterparty counterparty) {
        if (counterparty.getContacts() == null) return null;
        for (NameContact nameContact : counterparty.getContacts()) {
            if (nameContact.getContacts() == null) continue;
            for (Contact contact : nameContact.getContacts()) {
                if (contact.getType() == Contact.ContactType.email) {
                    return contact.getContact();
                }
            }
        }
        return null;
    }
}

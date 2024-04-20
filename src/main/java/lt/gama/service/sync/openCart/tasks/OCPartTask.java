package lt.gama.service.sync.openCart.tasks;

import lt.gama.api.response.TaskResponse;
import lt.gama.helpers.*;
import lt.gama.model.dto.entities.PartDto;
import lt.gama.model.mappers.PartSqlMapper;
import lt.gama.model.sql.entities.ImportSql;
import lt.gama.model.sql.entities.PartSql;
import lt.gama.model.sql.entities.id.ImportId;
import lt.gama.model.type.enums.DBType;
import lt.gama.model.type.enums.PartType;
import lt.gama.model.type.sync.SyncSettings;
import lt.gama.service.sync.SyncHttpService;
import lt.gama.service.sync.openCart.SyncOpenCartUtilsService;
import lt.gama.service.sync.openCart.model.OCIdResponse;
import lt.gama.service.sync.openCart.model.OCImportStep;
import lt.gama.service.sync.openCart.model.OCLogin;
import lt.gama.tasks.BaseDeferredTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serial;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * gama-online
 * Created by valdas on 2019-03-06.
 * if Gama parts information updated or there is new parts in Gama,
 * task updates parts information or adds new parts in OpenCart
 * OpenCart part sku = Gama partId or foreignPartId if part was migrated
 */
public class OCPartTask extends BaseDeferredTask {

    @Serial
    private static final long serialVersionUID = -10L;


    private final List<Long> ids;
    private final String api;
    private final String key;
    private final String username;


    @Autowired
    transient protected SyncOpenCartUtilsService syncOpenCartUtilsService;

    @Autowired
    transient protected SyncHttpService syncHttpService;

    @Autowired
    transient protected PartSqlMapper partSqlMapper;


    public OCPartTask(String api, String key, String username, long companyId, List<Long> ids) {
        super(companyId);
        this.api = api;
        this.key = key;
        this.username = username;
        this.ids = ids;
    }

    @Override
    public void execute() {
        if (CollectionsHelper.isEmpty(ids)) {
            log.info(className + ": companyId=" + getCompanyId() + " has no sync parts");
            finish(TaskResponse.success());
            return;
        }

        final String url;
        try {
            SyncSettings syncSettings = Validators.checkNotNull(auth.getSettings().getSync(), "No Sync Settings");
            url = Validators.checkNotNull(syncSettings.getUrl(), "No URL in Sync Settings");

        } catch (NullPointerException | IllegalArgumentException e) {
            log.error(className + ": " + e.getMessage(), e);
            finish(TaskResponse.error(e.getMessage()));
            return;
        }

        Collection<PartDto> parts = dbServiceSQL.queryByIds(PartSql.class, null, ids).getResultList().stream()
                    .map(partSqlMapper::toDto).toList();

        OCLogin login = syncOpenCartUtilsService.login(api, key, username);
        if (isNotValid(login)) {
            log.error(className + ": Can't login with" +
                    " api='" + api + '\'' +
                    " key='" + key + '\'' +
                    " username='" + username + '\'');
            finish(TaskResponse.error("Can't login with api='" + api + '\''));
            return;
        }

        OCImportStep step = new OCImportStep();

        for (PartDto part : parts) {

            if (part.getType() == PartType.SERVICE) {
                step.skipped();
                continue;
            }

            Long ocSku = part.getForeignId() != null ? part.getForeignId() : part.getId();

            int retry = 0;

            while (true) {

                if (isNotValid(login)) break;

                try {
                    OCIdResponse response = syncHttpService.getRequestData(
                            SyncHttpService.HttpMethod.POST,
                            url,
                            Map.of("route", "api/customerproduct/addEditProduct", "token", login.getToken()),
                            SyncHttpService.ContentType.FORM,
                            Map.of(
                                    "archive", BooleanUtils.isTrue(part.getArchive()) ? "1" : "0",
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
                    if (response == null || StringHelper.isEmpty(response.getId())) {
                        if (response != null && ++retry <= 3) {
                            try {
                                Thread.sleep(1000L * retry);
                            } catch (InterruptedException e) {
                                log.error(className + ": " + e.getMessage(), e);
                            }
                            log.info(className + ": Retrying task - " + this);

                            if (retry == 3) {
                                // one last time try login again
                                login = syncOpenCartUtilsService.login(api, key, username);
                                if (isNotValid(login)) {
                                    log.error(className + ": Can't login with" +
                                            " api='" + api + '\'' +
                                            " key='" + key + '\'' +
                                            " username='" + username + '\'');
                                    break;
                                }
                            }
                            continue;   // retry the same operation again
                        }

                        log.error(className + ": Error task - " + this);
                        step.errors();

                    } else {
                        step.exported();
                        ids.remove(part.getId());

                        if (!BooleanUtils.isTrue(part.getArchive())) {
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
                    }

                } catch (Exception e) {
                    log.error(className + ": " + e.getMessage(), e);
                    step.errors();
                }
                break;
            }
        }
        log.info(className + ": companyId=" + getCompanyId() + " sync parts" +
                " total=" + parts.size() +
                " exported=" + step.getExported() +
                " Imports created=" + step.getCreated() +
                " updated=" + step.getUpdated() +
                " skipped=" + step.getSkipped() +
                " errors=" + step.getErrors());

        finish(TaskResponse.success());
    }

    private boolean isNotValid(OCLogin login) {
        return login == null || StringHelper.isEmpty(login.getToken());
    }


    @Override
    public String toString() {
        return "api='" + api + '\'' +
                " key='" + key + '\'' +
                " username='" + username + '\'' +
                ' ' + super.toString();
    }
}

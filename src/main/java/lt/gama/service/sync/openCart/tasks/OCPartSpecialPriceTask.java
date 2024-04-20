package lt.gama.service.sync.openCart.tasks;

import jakarta.persistence.Tuple;
import lt.gama.api.response.TaskResponse;
import lt.gama.helpers.Validators;
import lt.gama.model.sql.entities.PartSql;
import lt.gama.model.type.auth.CompanySettings;
import lt.gama.model.type.sync.SyncSettings;
import lt.gama.service.sync.i.ISyncOpenCartUtilsService;
import lt.gama.service.sync.SyncHttpService;
import lt.gama.service.sync.openCart.model.OCLogin;
import lt.gama.service.sync.openCart.model.OCResponse;
import lt.gama.tasks.BaseDeferredTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serial;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


/**
 * gama-online
 * Created by valdas on 2019-03-06.
 */
public class OCPartSpecialPriceTask extends BaseDeferredTask {

    @Serial
    private static final long serialVersionUID = -1L;


    private final long counterpartyId;
    private final long partId;
    private final BigDecimal amount;
    private final String api;
    private final String key;
    private final String username;


    @Autowired
    transient protected ISyncOpenCartUtilsService syncOpenCartUtilsService;

    @Autowired
    transient protected SyncHttpService syncHttpService;


    public OCPartSpecialPriceTask(String api, String key, String username, long companyId, long counterpartyId, long partId, BigDecimal amount) {
        super(companyId);
        this.api = api;
        this.key = key;
        this.username = username;
        this.counterpartyId = counterpartyId;
        this.partId = partId;
        this.amount = amount;
    }

    @Override
    public void execute() {
        try {
            CompanySettings companySettings = auth.getSettings();
            SyncSettings syncSettings = Validators.checkNotNull(companySettings.getSync(), "No Sync Settings");
            Validators.checkNotNull(syncSettings.getUrl(), "No URL in Sync Settings");

            Tuple tuple = dbServiceSQL.executeAndReturnInTransaction(em -> {
                List<Tuple> result = em.createQuery(
                                "SELECT id AS id, foreignId AS foreignId" +
                                        " FROM " + PartSql.class.getName() + " p" +
                                        " WHERE id = :id" +
                                        " AND companyId = :companyId" +
                                        " AND (p.archive IS null OR p.archive = false)" +
                                        " AND (p.hidden IS null OR p.hidden = false)", Tuple.class)
                        .setParameter("companyId", auth.getCompanyId())
                        .setParameter("id", partId)
                        .getResultList();
                return result != null && result.size() == 1 ? result.get(0) : null;
            });
            String ocSku = (tuple.get("foreignId", Long.class) != null
                    ? tuple.get("foreignId", Long.class)
                    : tuple.get("id", Long.class)).toString();

            OCLogin login = syncOpenCartUtilsService.login(api, key, username);
            if (login == null) {
                log.error(className + ": Can't login with api='" + api + '\'');
                finish(TaskResponse.error("Can't login with api='" + api + '\''));
                return;
            }

            OCResponse response = syncHttpService.getRequestData(
                    SyncHttpService.HttpMethod.POST,
                    syncSettings.getUrl(),
                    Map.of("route", "api/customerproduct/addSpecialPrice", "token", login.getToken()),
                    SyncHttpService.ContentType.FORM,
                    Map.of(
                            "name", String.valueOf(counterpartyId),
                            "sku", ocSku,
                            "partDiscPrice", String.valueOf(amount)),
                    OCResponse.class,
                    login.getSession());

            if (response == null) {
                log.warn(className + ": Empty response " + this);
                finish(TaskResponse.error("Empty response"));
            }

            finish(TaskResponse.success());

        } catch (Exception e) {
            log.error(className + ": " + e.getMessage(), e);
            finish(TaskResponse.error(e.getMessage()));
        }
    }


    public long getCounterpartyId() {
        return counterpartyId;
    }

    public long getPartId() {
        return partId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "counterpartyId='" + counterpartyId + '\'' +
                " partId='" + partId + '\'' +
                " amount='" + amount + '\'' +
                " api='" + api + '\'' +
                " key='" + key + '\'' +
                " username='" + username + '\'' +
                ' ' + super.toString();
    }
}

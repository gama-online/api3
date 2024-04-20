package lt.gama.tasks;

import jakarta.persistence.Tuple;
import lt.gama.api.response.TaskResponse;
import lt.gama.helpers.CollectionsHelper;
import lt.gama.helpers.DateUtils;
import lt.gama.helpers.StringHelper;
import lt.gama.helpers.Validators;
import lt.gama.integrations.vmi.ITaxRefundService;
import lt.gama.integrations.vmi.TaxRefundService;
import lt.gama.integrations.vmi.types.TaxFreeState;
import lt.gama.service.ex.rt.GamaException;
import org.apache.commons.lang3.mutable.MutableInt;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static lt.gama.integrations.vmi.TaxRefundWSHelper.API_ZONE_ID;

/**
 * Will be executed every sunday and thursday at about 2:00.
 * So we need to check data for whole week.
 * But we will check for 1 week to be sure that nothing is missed.
 */
public class TaxFreeSyncTask extends BaseDeferredTask {

    @Autowired
    transient protected TaxRefundService taxRefundService;


    public TaxFreeSyncTask(long companyId) {
        super(companyId);
    }


    @Override
    public void execute() {
        var counter = new MutableInt();
        try {
            LocalDateTime now = DateUtils.now(API_ZONE_ID);
            LocalDateTime from = now.minusDays(7);

            ITaxRefundService.QueryDeclarationsResponse response = taxRefundService.queryTaxFreeDeclarations(from, now);
            if (!response.isSuccess()) {
                if (CollectionsHelper.hasValue(response.getErrors())) {
                    StringBuilder sb = new StringBuilder();
                    response.getErrors().forEach(error -> sb.append(error.getDescription()).append(" | ").append(error.getDetails()).append('\n'));
                    throw new GamaException(sb.toString());
                } else {
                    throw new GamaException("No errors returned");
                }
            }

            if (CollectionsHelper.isEmpty(response.getDeclarations())) {
                log.info(className + ": No declarations");
                return;
            }

            dbServiceSQL.executeInTransaction(entityManager -> response.getDeclarations().forEach(item -> {
                //noinspection unchecked
                ((Stream<Tuple>) entityManager.createNativeQuery("""
                                SELECT i.id AS id, i.tax_free ->> 'state' AS state, i.tax_free ->> 'updatedOn' AS updatedOn
                                FROM invoice i
                                JOIN documents d ON i.id = d.id
                                WHERE d.company_id = :companyId
                                    AND (d.archive IS null OR d.archive = false)
                                    AND jsonb_path_exists(i.tax_free, '$.docHeader.docId ? (@ == $docId)',
                                        jsonb_build_object('docId', :docId))
                                ORDER BY i.id
                                """, Tuple.class)
                        .setParameter("companyId", auth.getCompanyId())
                        .setParameter("docId", item.getDocId())
                        .getResultStream())
                        .flatMap(i -> {
                            try {
                                long invoiceId = i.get("id", BigInteger.class).longValue();
                                TaxFreeState invoiceState = TaxFreeState.from(i.get("state", String.class));
                                LocalDateTime invoiceUpdatedOn = StringHelper.hasValue(i.get("updatedOn", String.class))
                                        ? LocalDateTime.parse(i.get("updatedOn", String.class)) : null;

                                if (invoiceState == TaxFreeState.REFUNDED) {
                                    log.info(MessageFormat.format("Invoice {0} tax free declaration {1} has REFUNDED already", invoiceId, item.getDocId()));
                                    return null;
                                }

                                Validators.checkArgument(invoiceState == TaxFreeState.ASSESSED,
                                        MessageFormat.format("Invoice {0} Wrong Tax Free declaration state: {1}", invoiceId, invoiceState));

                                if (invoiceUpdatedOn != null && !invoiceUpdatedOn.isBefore(item.getStateDate())) {
                                    log.info(MessageFormat.format("Invoice {0} tax free declaration {1} has updated already", invoiceId, item.getDocId()));
                                    return null;
                                }

                                counter.increment();
                                taskQueueService.queueTask(new TaxFreeUpdateTask(getCompanyId(), item.getDocId(), item.getStateDate(), invoiceId));
                                return null;

                            } catch (Exception e) {
                                return Stream.of(e);
                            }
                        })
                        .reduce((e1, e2) -> {
                            e1.addSuppressed(e2);
                            return e1;
                        })
                        .ifPresent(e -> {
                            throw new GamaException(e.getMessage(), e);
                        });

            }));

        } catch (Exception e) {
            log.error(className + ": " + e.getMessage(), e);
            finish(TaskResponse.error(e.getMessage()));
        } finally {
            log.info(className + ": " + counter.intValue() + " tasks added " + this);
        }
    }
}

package lt.gama.tasks;

import lt.gama.api.response.TaskResponse;
import lt.gama.helpers.BigDecimalUtils;
import lt.gama.helpers.Validators;
import lt.gama.integrations.vmi.ITaxRefundService;
import lt.gama.integrations.vmi.types.TaxFreeState;
import lt.gama.model.sql.documents.InvoiceSql;
import lt.gama.model.type.inventory.TaxFree;
import lt.gama.service.ex.rt.GamaException;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Objects;

public class TaxFreeUpdateTask extends BaseDeferredTask {

    @Autowired
    transient protected ITaxRefundService taxRefundService;


    private final String docId;
    private final LocalDateTime timestamp;
    private final long invoiceId;


    public TaxFreeUpdateTask(long companyId, String docId, LocalDateTime timestamp, long invoiceId) {
        super(companyId);
        this.docId = docId;
        this.timestamp = timestamp;
        this.invoiceId = invoiceId;
    }


    @Override
    public void execute() {
        try {
            dbServiceSQL.executeInTransaction(entitymanager -> {

                InvoiceSql invoice = dbServiceSQL.getAndCheck(InvoiceSql.class, invoiceId);

                Validators.checkArgument(invoice.getTaxFree() != null && invoice.getTaxFree().getDocHeader() != null, "No Tax Free declaration found");

                if (invoice.getTaxFree().getState() == TaxFreeState.REFUNDED) {
                    log.info("Invoice tax free declaration has REFUNDED already");
                    return;
                }

                Validators.checkArgument(invoice.getTaxFree().getState() == TaxFreeState.ACCEPTED ||
                                invoice.getTaxFree().getState() == TaxFreeState.ASSESSED,
                        MessageFormat.format("Wrong Tax Free declaration state: {0}", invoice.getTaxFree().getState()));

                if (invoice.getTaxFree().getUpdatedOn() != null && !invoice.getTaxFree().getUpdatedOn().isBefore(timestamp)) {
                    log.info("Invoice tax free declaration has updated already");
                    return;
                }

                Validators.checkArgument(Objects.equals(invoice.getTaxFree().getDocHeader().getDocId(), docId),
                        MessageFormat.format("Wrong Tax Free declaration docId {0}", invoice.getTaxFree().getDocHeader().getDocId()));

                ITaxRefundService.DeclarationInfoResponse response = taxRefundService.getTaxFreeInfoOnExportedGoods(docId);
                if (!response.isSuccess()) {
                    throw new GamaException("Error on getTaxFreeInfoOnExportedGoods");
                }

                TaxFree taxFree = invoice.getTaxFree();

                taxFree.setState(TaxFreeState.ASSESSED);
                taxFree.setUpdatedOn(response.getCustomsVerification().getVerificationDate());
                taxFree.getSalesDoc().setAcceptedVatAmount(null);

                response.getCustomsVerification().getVerifiedGoods().forEach(good -> {
                    var salesDocItem = taxFree.getSalesDoc().getGoods().stream()
                            .filter(item -> item.getSequenceNo() == good.getSequenceNo())
                            .findFirst()
                            .orElseThrow(() -> new GamaException(
                                    MessageFormat.format("The good in sales document with Sequence No {0} not found", good.getSequenceNo())));

                    Validators.checkArgument(Objects.equals(salesDocItem.getQuantity(), good.getQuantity()),
                            MessageFormat.format("Sequence No {0} - Wrong quantity, in document {1}, from customs {2}",
                                    good.getSequenceNo(), salesDocItem.getQuantity(), good.getQuantity()));
                    Validators.checkArgument(Objects.equals(salesDocItem.getTotalAmount(), good.getTotalAmount()),
                            MessageFormat.format("Sequence No {0} - Wrong total amount, in document {1}, from customs {2}",
                                    good.getSequenceNo(), salesDocItem.getTotalAmount(), good.getTotalAmount()));

                    salesDocItem.setAcceptedQuantity(good.getQuantityVerified());
                });

                taxFree.getSalesDoc().getGoods().stream()
                        .filter(good -> BigDecimalUtils.isPositive(good.getAcceptedQuantity()))
                        .forEach(good -> {
                            BigDecimal unitPrice = good.getTaxableAmount()
                                    .divide(good.getQuantity(), 4, RoundingMode.HALF_UP)
                                    .multiply(good.getVatRate()).setScale(4, RoundingMode.HALF_UP)
                                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                            good.setAcceptedVatAmount(unitPrice.multiply(good.getAcceptedQuantity()).setScale(2, RoundingMode.HALF_UP));
                            taxFree.getSalesDoc().setAcceptedVatAmount(BigDecimalUtils.add(taxFree.getSalesDoc().getAcceptedVatAmount(), good.getAcceptedVatAmount()));
                        });
            });

        } catch (Exception e) {
            log.error(className + ": " + e.getMessage(), e);
            finish(TaskResponse.error(e.getMessage()));
        }
    }

    @Override
    public String toString() {
        return "docId='" + docId + '\'' +
                " timestamp=" + timestamp +
                " invoiceId=" + invoiceId +
                ' ' + super.toString();
    }
}

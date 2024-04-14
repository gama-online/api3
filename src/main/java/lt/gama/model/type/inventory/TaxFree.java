package lt.gama.model.type.inventory;

import lt.gama.helpers.CollectionsHelper;
import lt.gama.helpers.StringHelper;
import lt.gama.integrations.vmi.ITaxRefundService;
import lt.gama.integrations.vmi.types.TaxFreeState;
import lt.gama.model.type.inventory.taxfree.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class TaxFree implements Serializable {
    @Serial
    private static final long serialVersionUID = -1L;

    private TaxFreeState state;
    private LocalDateTime updatedOn;
    private List<ITaxRefundService.DeclarationError> errors;

    private DocHeader docHeader;
    private Customer customer;
    private SalesDoc salesDoc;
    private PaymentInfo paymentInfo;

    public void incDocCorrNo() {
        if (docHeader != null) docHeader.incDocCorrNo();
    }

    public boolean hasValues() {
        return docHeader != null && StringHelper.hasValue(docHeader.getDocId()) && docHeader.getCompletionDate() != null
                && customer != null && StringHelper.hasValue(customer.getFirstName()) && StringHelper.hasValue(customer.getLastName())
                && customer.getBirthDate() != null
                && StringHelper.hasValue(customer.getIdDocType()) && StringHelper.hasValue(customer.getIdDocNo())
                && StringHelper.hasValue(customer.getIssuedBy()) && StringHelper.hasValue(customer.getResCountryCode())
                && salesDoc != null && CollectionsHelper.hasValue(salesDoc.getGoods());
    }

    public TaxFreeForQRCode prepareForQRCode() {
        TaxFreeForQRCode taxFree = new TaxFreeForQRCode();

        taxFree.setDocHeader(new DocHeader());
        taxFree.getDocHeader().setDocId(getDocHeader().getDocId());
        taxFree.getDocHeader().setDocCorrNo(getDocHeader().getDocCorrNo());
        taxFree.getDocHeader().setCompletionDate(getDocHeader().getCompletionDate());

        taxFree.setCustomer(new Customer());
        taxFree.getCustomer().setFirstName(getCustomer().getFirstName());
        taxFree.getCustomer().setLastName(getCustomer().getLastName());
        taxFree.getCustomer().setIdDocNo(getCustomer().getIdDocNo());

        taxFree.setGoods(getSalesDoc().getGoods().stream()
                .map(x -> {
                    Good good = new Good();
                    good.setSequenceNo(x.getSequenceNo());
                    good.setDescription(x.getDescription());
                    good.setQuantity(x.getQuantity());
                    good.setUnitOfMeasureCode(x.getUnitOfMeasureCode());
                    good.setUnitOfMeasureOther(x.getUnitOfMeasureOther());
                    good.setTotalAmount(x.getTotalAmount());
                    return good;
                })
                .collect(Collectors.toList()));
        return taxFree;
    }

    // generated

    public TaxFreeState getState() {
        return state;
    }

    public void setState(TaxFreeState state) {
        this.state = state;
    }

    public LocalDateTime getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(LocalDateTime updatedOn) {
        this.updatedOn = updatedOn;
    }

    public List<ITaxRefundService.DeclarationError> getErrors() {
        return errors;
    }

    public void setErrors(List<ITaxRefundService.DeclarationError> errors) {
        this.errors = errors;
    }

    public DocHeader getDocHeader() {
        return docHeader;
    }

    public void setDocHeader(DocHeader docHeader) {
        this.docHeader = docHeader;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public SalesDoc getSalesDoc() {
        return salesDoc;
    }

    public void setSalesDoc(SalesDoc salesDoc) {
        this.salesDoc = salesDoc;
    }

    public PaymentInfo getPaymentInfo() {
        return paymentInfo;
    }

    public void setPaymentInfo(PaymentInfo paymentInfo) {
        this.paymentInfo = paymentInfo;
    }

    @Override
    public String toString() {
        return "TaxFree{" +
                "state=" + state +
                ", updatedOn=" + updatedOn +
                ", errors=" + errors +
                ", docHeader=" + docHeader +
                ", customer=" + customer +
                ", salesDoc=" + salesDoc +
                ", paymentInfo=" + paymentInfo +
                '}';
    }
}

package lt.gama.api.response;

import lt.gama.model.type.GamaBigMoney;

import java.time.LocalDate;
import java.util.UUID;

/**
 * gama-online
 * Created by valdas on 2018-07-16.
 */
public class LastInvoicePriceResponse {

    private LastInvoicePriceDoc doc;

    private LastInvoicePricePart part;

    @SuppressWarnings("unused")
    protected LastInvoicePriceResponse() {}

    public LastInvoicePriceResponse(Long docId, String number, LocalDate date, UUID uuid,
                                    Long partId, GamaBigMoney price, Double discount) {
        this.doc = new LastInvoicePriceDoc(docId, number, date, uuid);
        this.part = new LastInvoicePricePart(partId, price, discount);
    }

    // generated

    public LastInvoicePriceDoc getDoc() {
        return doc;
    }

    public void setDoc(LastInvoicePriceDoc doc) {
        this.doc = doc;
    }

    public LastInvoicePricePart getPart() {
        return part;
    }

    public void setPart(LastInvoicePricePart part) {
        this.part = part;
    }

    @Override
    public String toString() {
        return "LastInvoicePriceResponse{" +
                "doc=" + doc +
                ", part=" + part +
                '}';
    }

    public static class LastInvoicePriceDoc {

        private Long id;

        private String number;

        private LocalDate date;

        private UUID uuid;

        private LastInvoicePriceDoc(Long id, String number, LocalDate date, UUID uuid) {
            this.id = id;
            this.number = number;
            this.date = date;
            this.uuid = uuid;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public UUID getUuid() {
            return uuid;
        }

        public void setUuid(UUID uuid) {
            this.uuid = uuid;
        }
    }

    public static class LastInvoicePricePart {

        private Long id;

        private GamaBigMoney price;

        private Double discount;

        private LastInvoicePricePart(Long id, GamaBigMoney price, Double discount) {
            this.id = id;
            this.price = price;
            this.discount = discount;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public GamaBigMoney getPrice() {
            return price;
        }

        public void setPrice(GamaBigMoney price) {
            this.price = price;
        }

        public Double getDiscount() {
            return discount;
        }

        public void setDiscount(Double discount) {
            this.discount = discount;
        }
    }
}

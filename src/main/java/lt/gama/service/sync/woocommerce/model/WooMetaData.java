package lt.gama.service.sync.woocommerce.model;

/**
 *
 *  "key": "_wcpdf_invoice_number" "value": "LOOOP-000617"
 *  "key": "_wcpdf_invoice_date", "value": "1707313812"
 *  "key": "_wcpdf_invoice_date_formatted", "value": "2024-02-07 16:50:12"
 */
public record WooMetaData(
        int id,
        String key,
        Object value
) {

    public String valueAsString() {
        return value instanceof String s ? s : "";
    }
}

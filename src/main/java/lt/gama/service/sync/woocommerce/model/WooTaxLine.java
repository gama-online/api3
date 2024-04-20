package lt.gama.service.sync.woocommerce.model;

import java.math.BigDecimal;

public record WooTaxLine (
        long id,
        boolean compound,
        String label,
        String rate_code,
        String rate_id,
        BigDecimal rate_percent,
        BigDecimal shipping_tax_total,
        BigDecimal tax_total
) {}

package lt.gama.service.sync.woocommerce.model;

import java.math.BigDecimal;

public record WooOrderTax (
        long id, // tax_rate_id
        BigDecimal subtotal,
        BigDecimal total
) {}

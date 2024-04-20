package lt.gama.service.sync.woocommerce.model;

import java.math.BigDecimal;
import java.util.List;

public record WooShippingLine (
        long id,
        String method_id,
        String method_title,
        List<WooOrderTax> taxes,
        BigDecimal total,
        BigDecimal total_tax
) {}

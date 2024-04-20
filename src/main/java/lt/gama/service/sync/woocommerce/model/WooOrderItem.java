package lt.gama.service.sync.woocommerce.model;

import java.math.BigDecimal;
import java.util.List;

public record WooOrderItem (
        long id,
        String name,
        long product_id,
        String sku,
        BigDecimal price,
        BigDecimal quantity,
        BigDecimal subtotal,
        BigDecimal subtotal_tax,
        BigDecimal total,
        BigDecimal total_tax,
        String tax_class,
        List<WooOrderTax> taxes
) {}

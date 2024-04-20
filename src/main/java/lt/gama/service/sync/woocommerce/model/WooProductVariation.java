package lt.gama.service.sync.woocommerce.model;

import java.math.BigDecimal;
import java.util.List;

public record WooProductVariation (
        List<WooProductVariationAttribute> attributes,
        long id,
        BigDecimal price,
        BigDecimal regular_price,
        String sku,
        String tax_status, // "taxable"
        String tax_class  // "" - default "Standard rate"
) {}

package lt.gama.service.sync.woocommerce.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record WooProduct (
        long id,
        String sku,
        String name,
        String tax_status, // "taxable"
        String tax_class,  // "" - default "Standard rate"
        boolean on_sale,
        boolean downloadable,
        BigDecimal price,
        LocalDateTime date_created,
        LocalDateTime date_created_gmt,
        LocalDateTime date_modified,
        LocalDateTime date_modified_gmt,
        BigDecimal stock_quantity,
        List<Long> variations
) {}

package lt.gama.service.sync.woocommerce.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record WooOrder (
        long id,
        String number,
        LocalDateTime date_created,
        LocalDateTime date_created_gmt,
        LocalDateTime date_modified,
        LocalDateTime date_modified_gmt,
        LocalDateTime date_paid,
        LocalDateTime date_paid_gmt,
        LocalDateTime date_completed,
        LocalDateTime date_completed_gmt,
        WooCustomer billing,
        WooCustomer shipping,
        String customer_note,
        String order_key,
        String payment_method_title,
        String payment_url,
        String currency,
        List<WooOrderItem> line_items,
        boolean prices_include_tax,
        BigDecimal discount_tax,
        BigDecimal discount_total,
        List<WooShippingLine> shipping_lines,
        BigDecimal shipping_tax,
        BigDecimal shipping_total,
        BigDecimal total,
        BigDecimal total_tax,
        String status,
        List<WooMetaData> meta_data
) {}

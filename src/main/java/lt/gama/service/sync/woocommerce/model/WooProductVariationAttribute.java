package lt.gama.service.sync.woocommerce.model;

public record WooProductVariationAttribute (
        long id,
        String name,
        String option   // suffix added to the product name, i.e. name - suffix
) {}

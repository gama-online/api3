package lt.gama.service.sync.woocommerce.model;

public record WooCustomer (
        String email,
        String first_name,
        String last_name,
        String phone,
        String address_1,
        String address_2,
        String city,
        String company,
        String country,
        String postcode,
        String state
) {}

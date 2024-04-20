package lt.gama.service.sync.woocommerce.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public record WooTax (
        long id,
        @JsonProperty("class")
        String tax_class,
        String country,
        Double rate,
        boolean shipping
) implements Serializable {}

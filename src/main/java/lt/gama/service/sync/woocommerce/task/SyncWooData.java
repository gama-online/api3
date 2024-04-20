package lt.gama.service.sync.woocommerce.task;

import lt.gama.model.type.part.VATRate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyncWooData implements Serializable {

    // Map<Country, Map<TaxClass, TaxRate>>
    Map<String, Map<String, Double>> wooTaxes = new HashMap<>();
    List<VATRate> rates;
    List<String> errors = new ArrayList<>();
    List<String> warnings = new ArrayList<>();
    int productsTotal;
    int productsCreated;
    int ordersTotal;
    int ordersCreated;


    public Map<String, Map<String, Double>> getWooTaxes() {
        return wooTaxes;
    }

    public void setWooTaxes(Map<String, Map<String, Double>> wooTaxes) {
        this.wooTaxes = wooTaxes;
    }

    public List<VATRate> getRates() {
        return rates;
    }

    public void setRates(List<VATRate> rates) {
        this.rates = rates;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public int getProductsTotal() {
        return productsTotal;
    }

    public void setProductsTotal(int productsTotal) {
        this.productsTotal = productsTotal;
    }

    public int getProductsCreated() {
        return productsCreated;
    }

    public void setProductsCreated(int productsCreated) {
        this.productsCreated = productsCreated;
    }

    public int getOrdersTotal() {
        return ordersTotal;
    }

    public void setOrdersTotal(int ordersTotal) {
        this.ordersTotal = ordersTotal;
    }

    public int getOrdersCreated() {
        return ordersCreated;
    }

    public void setOrdersCreated(int ordersCreated) {
        this.ordersCreated = ordersCreated;
    }
}

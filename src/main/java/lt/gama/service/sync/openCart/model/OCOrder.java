package lt.gama.service.sync.openCart.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * gama-online
 * Created by valdas on 11/11/2018.
 */
public class OCOrder extends OCResponse {

    private long order_id;

    private String invoice_prefix;

    private long invoice_no;


    private long customer_id;

    private String firstname;

    private String lastname;

    private String email;

    private String telephone;


    private String payment_firstname;

    private String payment_lastname;

    private String payment_company;

    private String payment_address_1;

    private String payment_address_2;

    private String payment_postcode;

    private String payment_city;

    private String payment_country;

    private String payment_iso_code_2;

    private String payment_iso_code_3;


    private String shipping_firstname;

    private String shipping_lastname;

    private String shipping_company;

    private String shipping_address_1;

    private String shipping_address_2;

    private String shipping_postcode;

    private String shipping_city;

    private String shipping_country;

    private String shipping_iso_code_2;

    private String shipping_iso_code_3;

    /**
     * VAT code
     */
    private String payment_taxid1;


    private String payment_method;

    private String shipping_method;


    private String currency_code;

    private BigDecimal currency_value;


    private LocalDateTime date_modified;

    private LocalDateTime date_added;


    private BigDecimal total;


    private List<OCOrderLine> products;

    private List<OCOrderTotal> totals;

    // generated

    public long getOrder_id() {
        return order_id;
    }

    public void setOrder_id(long order_id) {
        this.order_id = order_id;
    }

    public String getInvoice_prefix() {
        return invoice_prefix;
    }

    public void setInvoice_prefix(String invoice_prefix) {
        this.invoice_prefix = invoice_prefix;
    }

    public long getInvoice_no() {
        return invoice_no;
    }

    public void setInvoice_no(long invoice_no) {
        this.invoice_no = invoice_no;
    }

    public long getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(long customer_id) {
        this.customer_id = customer_id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getPayment_firstname() {
        return payment_firstname;
    }

    public void setPayment_firstname(String payment_firstname) {
        this.payment_firstname = payment_firstname;
    }

    public String getPayment_lastname() {
        return payment_lastname;
    }

    public void setPayment_lastname(String payment_lastname) {
        this.payment_lastname = payment_lastname;
    }

    public String getPayment_company() {
        return payment_company;
    }

    public void setPayment_company(String payment_company) {
        this.payment_company = payment_company;
    }

    public String getPayment_address_1() {
        return payment_address_1;
    }

    public void setPayment_address_1(String payment_address_1) {
        this.payment_address_1 = payment_address_1;
    }

    public String getPayment_address_2() {
        return payment_address_2;
    }

    public void setPayment_address_2(String payment_address_2) {
        this.payment_address_2 = payment_address_2;
    }

    public String getPayment_postcode() {
        return payment_postcode;
    }

    public void setPayment_postcode(String payment_postcode) {
        this.payment_postcode = payment_postcode;
    }

    public String getPayment_city() {
        return payment_city;
    }

    public void setPayment_city(String payment_city) {
        this.payment_city = payment_city;
    }

    public String getPayment_country() {
        return payment_country;
    }

    public void setPayment_country(String payment_country) {
        this.payment_country = payment_country;
    }

    public String getPayment_iso_code_2() {
        return payment_iso_code_2;
    }

    public void setPayment_iso_code_2(String payment_iso_code_2) {
        this.payment_iso_code_2 = payment_iso_code_2;
    }

    public String getPayment_iso_code_3() {
        return payment_iso_code_3;
    }

    public void setPayment_iso_code_3(String payment_iso_code_3) {
        this.payment_iso_code_3 = payment_iso_code_3;
    }

    public String getShipping_firstname() {
        return shipping_firstname;
    }

    public void setShipping_firstname(String shipping_firstname) {
        this.shipping_firstname = shipping_firstname;
    }

    public String getShipping_lastname() {
        return shipping_lastname;
    }

    public void setShipping_lastname(String shipping_lastname) {
        this.shipping_lastname = shipping_lastname;
    }

    public String getShipping_company() {
        return shipping_company;
    }

    public void setShipping_company(String shipping_company) {
        this.shipping_company = shipping_company;
    }

    public String getShipping_address_1() {
        return shipping_address_1;
    }

    public void setShipping_address_1(String shipping_address_1) {
        this.shipping_address_1 = shipping_address_1;
    }

    public String getShipping_address_2() {
        return shipping_address_2;
    }

    public void setShipping_address_2(String shipping_address_2) {
        this.shipping_address_2 = shipping_address_2;
    }

    public String getShipping_postcode() {
        return shipping_postcode;
    }

    public void setShipping_postcode(String shipping_postcode) {
        this.shipping_postcode = shipping_postcode;
    }

    public String getShipping_city() {
        return shipping_city;
    }

    public void setShipping_city(String shipping_city) {
        this.shipping_city = shipping_city;
    }

    public String getShipping_country() {
        return shipping_country;
    }

    public void setShipping_country(String shipping_country) {
        this.shipping_country = shipping_country;
    }

    public String getShipping_iso_code_2() {
        return shipping_iso_code_2;
    }

    public void setShipping_iso_code_2(String shipping_iso_code_2) {
        this.shipping_iso_code_2 = shipping_iso_code_2;
    }

    public String getShipping_iso_code_3() {
        return shipping_iso_code_3;
    }

    public void setShipping_iso_code_3(String shipping_iso_code_3) {
        this.shipping_iso_code_3 = shipping_iso_code_3;
    }

    public String getPayment_taxid1() {
        return payment_taxid1;
    }

    public void setPayment_taxid1(String payment_taxid1) {
        this.payment_taxid1 = payment_taxid1;
    }

    public String getPayment_method() {
        return payment_method;
    }

    public void setPayment_method(String payment_method) {
        this.payment_method = payment_method;
    }

    public String getShipping_method() {
        return shipping_method;
    }

    public void setShipping_method(String shipping_method) {
        this.shipping_method = shipping_method;
    }

    public String getCurrency_code() {
        return currency_code;
    }

    public void setCurrency_code(String currency_code) {
        this.currency_code = currency_code;
    }

    public BigDecimal getCurrency_value() {
        return currency_value;
    }

    public void setCurrency_value(BigDecimal currency_value) {
        this.currency_value = currency_value;
    }

    public LocalDateTime getDate_modified() {
        return date_modified;
    }

    public void setDate_modified(LocalDateTime date_modified) {
        this.date_modified = date_modified;
    }

    public LocalDateTime getDate_added() {
        return date_added;
    }

    public void setDate_added(LocalDateTime date_added) {
        this.date_added = date_added;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public List<OCOrderLine> getProducts() {
        return products;
    }

    public void setProducts(List<OCOrderLine> products) {
        this.products = products;
    }

    public List<OCOrderTotal> getTotals() {
        return totals;
    }

    public void setTotals(List<OCOrderTotal> totals) {
        this.totals = totals;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        OCOrder ocOrder = (OCOrder) o;
        return order_id == ocOrder.order_id && invoice_no == ocOrder.invoice_no && customer_id == ocOrder.customer_id && Objects.equals(invoice_prefix, ocOrder.invoice_prefix) && Objects.equals(firstname, ocOrder.firstname) && Objects.equals(lastname, ocOrder.lastname) && Objects.equals(email, ocOrder.email) && Objects.equals(telephone, ocOrder.telephone) && Objects.equals(payment_firstname, ocOrder.payment_firstname) && Objects.equals(payment_lastname, ocOrder.payment_lastname) && Objects.equals(payment_company, ocOrder.payment_company) && Objects.equals(payment_address_1, ocOrder.payment_address_1) && Objects.equals(payment_address_2, ocOrder.payment_address_2) && Objects.equals(payment_postcode, ocOrder.payment_postcode) && Objects.equals(payment_city, ocOrder.payment_city) && Objects.equals(payment_country, ocOrder.payment_country) && Objects.equals(payment_iso_code_2, ocOrder.payment_iso_code_2) && Objects.equals(payment_iso_code_3, ocOrder.payment_iso_code_3) && Objects.equals(shipping_firstname, ocOrder.shipping_firstname) && Objects.equals(shipping_lastname, ocOrder.shipping_lastname) && Objects.equals(shipping_company, ocOrder.shipping_company) && Objects.equals(shipping_address_1, ocOrder.shipping_address_1) && Objects.equals(shipping_address_2, ocOrder.shipping_address_2) && Objects.equals(shipping_postcode, ocOrder.shipping_postcode) && Objects.equals(shipping_city, ocOrder.shipping_city) && Objects.equals(shipping_country, ocOrder.shipping_country) && Objects.equals(shipping_iso_code_2, ocOrder.shipping_iso_code_2) && Objects.equals(shipping_iso_code_3, ocOrder.shipping_iso_code_3) && Objects.equals(payment_taxid1, ocOrder.payment_taxid1) && Objects.equals(payment_method, ocOrder.payment_method) && Objects.equals(shipping_method, ocOrder.shipping_method) && Objects.equals(currency_code, ocOrder.currency_code) && Objects.equals(currency_value, ocOrder.currency_value) && Objects.equals(date_modified, ocOrder.date_modified) && Objects.equals(date_added, ocOrder.date_added) && Objects.equals(total, ocOrder.total) && Objects.equals(products, ocOrder.products) && Objects.equals(totals, ocOrder.totals);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), order_id, invoice_prefix, invoice_no, customer_id, firstname, lastname, email, telephone, payment_firstname, payment_lastname, payment_company, payment_address_1, payment_address_2, payment_postcode, payment_city, payment_country, payment_iso_code_2, payment_iso_code_3, shipping_firstname, shipping_lastname, shipping_company, shipping_address_1, shipping_address_2, shipping_postcode, shipping_city, shipping_country, shipping_iso_code_2, shipping_iso_code_3, payment_taxid1, payment_method, shipping_method, currency_code, currency_value, date_modified, date_added, total, products, totals);
    }

    @Override
    public String toString() {
        return "OCOrder{" +
                "order_id=" + order_id +
                ", invoice_prefix='" + invoice_prefix + '\'' +
                ", invoice_no=" + invoice_no +
                ", customer_id=" + customer_id +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", email='" + email + '\'' +
                ", telephone='" + telephone + '\'' +
                ", payment_firstname='" + payment_firstname + '\'' +
                ", payment_lastname='" + payment_lastname + '\'' +
                ", payment_company='" + payment_company + '\'' +
                ", payment_address_1='" + payment_address_1 + '\'' +
                ", payment_address_2='" + payment_address_2 + '\'' +
                ", payment_postcode='" + payment_postcode + '\'' +
                ", payment_city='" + payment_city + '\'' +
                ", payment_country='" + payment_country + '\'' +
                ", payment_iso_code_2='" + payment_iso_code_2 + '\'' +
                ", payment_iso_code_3='" + payment_iso_code_3 + '\'' +
                ", shipping_firstname='" + shipping_firstname + '\'' +
                ", shipping_lastname='" + shipping_lastname + '\'' +
                ", shipping_company='" + shipping_company + '\'' +
                ", shipping_address_1='" + shipping_address_1 + '\'' +
                ", shipping_address_2='" + shipping_address_2 + '\'' +
                ", shipping_postcode='" + shipping_postcode + '\'' +
                ", shipping_city='" + shipping_city + '\'' +
                ", shipping_country='" + shipping_country + '\'' +
                ", shipping_iso_code_2='" + shipping_iso_code_2 + '\'' +
                ", shipping_iso_code_3='" + shipping_iso_code_3 + '\'' +
                ", payment_taxid1='" + payment_taxid1 + '\'' +
                ", payment_method='" + payment_method + '\'' +
                ", shipping_method='" + shipping_method + '\'' +
                ", currency_code='" + currency_code + '\'' +
                ", currency_value=" + currency_value +
                ", date_modified=" + date_modified +
                ", date_added=" + date_added +
                ", total=" + total +
                ", products=" + products +
                ", totals=" + totals +
                "} " + super.toString();
    }
}

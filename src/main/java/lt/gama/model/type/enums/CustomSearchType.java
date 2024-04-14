package lt.gama.model.type.enums;

import java.io.Serializable;

/**
 * Gama
 * Created by valdas on 15-08-13.
 */
public enum CustomSearchType implements Serializable {

    /*
     * !!! DO NOT CHANGE 'value' - ARE IN DATABASE ALREADY !!!
     */

    ACCOUNT_TYPE    ("a", "ct_accountType"),
    ESTIMATE_TYPE   ("e", "ct_estimateType"),
    EMPLOYEE_TYPE   ("m", "ct_employeeType"),
    PART_SKU        ("k", "ct_partSKU"),
    PART_TYPE       ("p", "ct_partType"),
    REMAINDER_TYPE  ("r", "ct_remainderType"),  // used in Counterparty - remainder type: customer or vendor
    REMAINDER_AD    ("ra", "ct_remainderAD"),   // used in Counterparty - remainder filter by advances or debts only
    DEBT_TYPE       ("s", "ct_debtType"),
    SUM_TYPE        ("t", "ct_sumType"),
    DB_TYPE         ("d", "ct_dbType"),

    ASSET_STATUS    ("u", "ct_assetStatus"),    // value can be the same as COMPANY_STATUS because they do not intersect
    COMPANY_STATUS  ("u", "ct_companyStatus"),  // value can be the same as ASSET_STATUS because they do not intersect

    INVOICE_ADVANCE ("ia", "ct_invoiceAdvance"),
    PURCHASE_ADVANCE ("pa", "ct_purchaseAdvance"),

    INVOICE_TAX_FREE_ID ("tfi", "ct_invoiceTaxFreeId"),
    INVOICE_TAX_FREE_STATE ("tfs", "ct_invoiceTaxFreeState"),

    MANUFACTURER    ("f", "ct_manufacturer"),

    DOC_NR          ("0", "ct_docNr"),          // document number
    DOC_ID          ("1", "ct_docId"),          // document id
    GL_RC           ("2", "ct_glRC"),
    POSITION        ("3", "ct_position"),
    PART_ID         ("4", "ct_partId"),
    BANK_ID         ("5", "ct_bankId"),
    CASH_ID         ("6", "ct_cashId"),
    EMPLOYEE_ACTIVE ("7", "ct_employeeActive"),
    WAREHOUSE_ACTIVE ("8", "ct_warehouseActive"),
    WAREHOUSE_CLOSED ("9", "ct_warehouseClosed"),

    GL_ACCOUNTS     ("A", "ct_glAccounts"),
    BARCODE         ("B", "ct_barcode"),
    CURRENCY        ("C", "ct_currency"),
    BASE_CURRENCY   ("CB", "ct_baseCurrency"),  // is base currency - true/false
    DATE            ("D", "ct_date"),
    EMPLOYEE        ("E", "ct_employee"),
    FINISHED        ("F", "ct_finished"),
    FINISHED_GL     ("FG", "ct_finishedGl"),

    GROUP           ("G", "ct_group"),
    CASH            ("H", "ct_cash"),       // is bank cash operation
    COUNTERPARTY_COM_CODE ("I", "ct_counterpartyComCode"),  // counterparty company code - used in Counterparty
    COUNTERPARTY_VAT_CODE ("IV", "ct_counterpartyVATCode"), // counterparty VAT code - used in Counterparty
    COUNTERPARTY_NAME ("IN", "ct_counterpartyName"),  // counterparty company name - used in Counterparty
    EMPLOYEE_P_CODE ("J", "ct_employeePCode"),              // employee person code - used in Employee
    COST_DOC        ("K", "ct_costDoc"),    // used together with DOC_PART in costs recalculations in Invoice, Inventory, Production documents
    LABEL           ("L", "ct_label"),
    LABEL_TYPE      ("M", "ct_labelType"),
    HIDDEN          ("N", "ct_hidden"),
    ORIGIN_ID       ("O", "ct_originId"),
    COUNTERPARTY    ("P", "ct_counterparty"),
    BANK            ("Q", "ct_bank"),       // bank's account - used in Employee and Counterparty
    REMAINDER       ("R", "ct_remainder"),  // used in Employee, Part, Counterparty, BankAccount, Cash if remainder is not null
//    SUSPENDED       ("S", "ct_suspended"),  // is entity suspended, i.e. non active (counterparty, RC)
    DOC_PART        ("T", "ct_docPart"),    // used together with COST_DOC in costs recalculations in Invoice, Inventory, Production documents
    VAT_ID          ("V", "ct_vat"),        // VAT id
    WAREHOUSE       ("W", "ct_warehouseId"),
    WAREHOUSE_TAG   ("WT", "ct_warehouseTag"),
    WORK_SCHEDULE   ("W", "ct_workSchedule"), // can be the same as WAREHOUSE because never used in the same entity

    ARCHIVE         ("X", "ct_archive"),

    // price list index
    PL_TYPE                 ("p0", "ct_pl_type"),
    PL_PART_LABEL           ("p1", "ct_pl_partLabel"),
    PL_COUNTERPARTY_LABEL   ("p2", "ct_pl_counterpartyLabel"),
    PL_BASE                 ("p4", "ct_pl_base")

    ;

    private final String value;
    private final String field;

    CustomSearchType(String value, String field) {
        this.value = value;
        this.field = field;
    }

    public static CustomSearchType from(String value) {
        if (value != null) {
            for (CustomSearchType t : values()) {
                if (t.value.equals(value)) {
                    return t;
                }
            }
        }
        return null;
    }

    public static CustomSearchType fromField(String value) {
        if (value != null) {
            for (CustomSearchType t : values()) {
                if (t.field.equals(value)) {
                    return t;
                }
            }
        }
        return null;
    }

    public String getField() {
        return field;
    }

    @Override
    public String toString() {
        return value;
    }

}

package lt.gama.model.type.enums;

public enum Permission {

    /*
     * System level privileges
     */

    SYSTEM_COMPANY_R("CR"), // Company read
    SYSTEM_COMPANY_C("CC"), // Company create
    SYSTEM_COMPANY_M("CM"), // Company modify

    /*
     * Company level privileges
     */

    ADMIN("admin"), // Company Administrator - can assign roles, activate and suspend employees accounts and do everything else
    ROLE("r"),      // Role manager - can create and modify roles
	ACCOUNT("a"),   // Accounts manager - can assign roles, activate and suspend employees accounts

    SETTINGS("s"),  // Company settings - can modify company settings

    GL("g"), // Accounting

    EMPLOYEE_R("p_r"), // Employee read
    EMPLOYEE_M("p_m"), // Employee write
    EMPLOYEE_B("p_b"), // Employee balance

    EMPLOYEE_OP_R("po_r"), // Employee operation read
    EMPLOYEE_OP_M("po_m"), // Employee operation write

    PART_R("t_r"), // Product/service read
    PART_M("t_m"), // Product/service write
    PART_B("t_b"), // Product balance
    PART_S("t_s"), // Product cost

    COUNTERPARTY_R("n_r"), // Counterparty read
    COUNTERPARTY_M("n_m"), // Counterparty write
    COUNTERPARTY_B("n_b"), // Counterparty balance/debt

    DOCUMENT_R("d_r"), // Document read
    DOCUMENT_M("d_m"), // Document write

    PURCHASE_R("pur_r"), // Purchase read
    PURCHASE_M ("pur_m"), // Purchase write

    INVOICE_R("inv_r"), // Invoice read
    INVOICE_M ("inv_m"), // Invoice write

    BANK_R("b_r"), // Bank account read
    BANK_M("b_m"), // Bank account write
    BANK_B("b_b"), // Bank account balance

    BANK_OP_R("bo_r"), // Bank operation read
    BANK_OP_M("bo_m"), // Bank operation write

    CASH_R("h_r"), // Cash read
    CASH_M("h_m"), // Cash write
    CASH_B("h_b"), // Cash balance

    CASH_OP_R("ho_r"), // Cash order read
    CASH_OP_M("ho_m"), // Cash order write

    SALARY_R("s_r"),
    SALARY_M("s_m"),
    ;

	private final String value;

    Permission(final String value) {
        this.value = value;
    }

    public static Permission from(String value) {
        if (value != null) {
            for (Permission t : values()) {
                if (t.value.equals(value)) {
                    return t;
                }
            }
        }
        return null;
    }

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {
		return value;
	}

}

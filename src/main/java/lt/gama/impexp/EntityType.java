package lt.gama.impexp;

/**
 * Gama
 * Created by valdas on 15-04-02.
 */
public enum EntityType {

    SQL("-sql"),
    PART("part"),
    COUNTERPARTY("counterparty"),
    GL_ACCOUNT("gl_account"),
    CURRENCY("currency"),
    DOUBLE_ENTRY("double_entry"),
    BANK("bank"),
    CASH("cash"),
    EMPLOYEE("employee"),
    EMPLOYEE_CARD("employeeCard"),
    WAREHOUSE("warehouse"),
    DOCUMENT("document"),
    INVOICE("invoice"),
    ASSET("asset"),
    WORK_SCHEDULE("workSchedule"),
    POSITION("position")
    ;

    private final String name;

    EntityType(final String name) {
        this.name = name;
    }

    public static EntityType from(String name) {
        if (name != null) {
            for (EntityType t : values()) {
                if (t.name.equals(name)) {
                    return t;
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return name;
    }
}

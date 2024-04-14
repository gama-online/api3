package lt.gama.integrations.vmi.types;

public enum PaymentType {
    CASH("1"),
    BANK("2"),
    OTHER("3");

    private final String value;

    PaymentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static PaymentType from(String value) {
        if (value != null) {
            for (PaymentType t : values()) {
                if (t.value.equals(value)) {
                    return t;
                }
            }
        }
        return null;
    }
}

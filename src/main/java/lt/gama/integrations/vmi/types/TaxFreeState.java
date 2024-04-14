package lt.gama.integrations.vmi.types;

public enum TaxFreeState {
    ACCEPTED,
    ACCEPTED_WITH_ERRORS,
    ASSESSED,
    CANCELLED,
    REFUNDED;

    public static TaxFreeState from(String value) {
        if (value != null) {
            if ("ACCEPTED_CORRECT".equalsIgnoreCase(value)) return ACCEPTED;
            if ("ACCEPTED_INCORRECT".equalsIgnoreCase(value)) return ACCEPTED_WITH_ERRORS;

            for (TaxFreeState t : values()) {
                if (t.name().equalsIgnoreCase(value)) {
                    return t;
                }
            }
        }
        return null;
    }

}

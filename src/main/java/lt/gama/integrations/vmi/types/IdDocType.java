package lt.gama.integrations.vmi.types;

public enum IdDocType {
    ID_CARD(2),
    PASSPORT(1);

    private final int value;

    IdDocType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    static public IdDocType from(int value) {
        for (IdDocType t : values()) {
            if (t.value == value) {
                return t;
            }
        }
        return null;
    }

    public static IdDocType from(String value) {
        if (value == null) return null;
        try {
            return from(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

package lt.gama.model.dto.type;

public class TextDto {
    private String value;

    public TextDto() {
    }

    public TextDto(String value) {
        this.value = value;
    }

    // generated

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "TextDto{" +
                "value='" + value + '\'' +
                '}';
    }
}

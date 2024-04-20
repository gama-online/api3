package lt.gama.api.request;


public class MailRequestContact {

    /**
     * CC, BCC or null if normal
     */
    private String type;

    private String name;

    private String email;

    protected MailRequestContact() {
    }

    public MailRequestContact(String email, String name) {
        this.email = email;
        this.name = name;
    }

    // generated

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "MailRequestContact{" +
                "type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}

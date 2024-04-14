package lt.gama.model.type;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class Contact implements Serializable {

	@Serial
    private static final long serialVersionUID = -1L;

	public enum ContactType implements Serializable {
		phone, mobile, fax, email, skype, url, other
	}

	public enum ContactSubtype implements Serializable {
		work, home
	}

	private ContactType type;

	private ContactSubtype subtype;

	private String contact;


	protected Contact() {
	}

	public Contact(ContactType type, ContactSubtype subtype, String contact) {
		this.type = type;
		this.subtype = subtype;
		this.contact = contact;
	}

	// generated

	public ContactType getType() {
		return type;
	}

	public void setType(ContactType type) {
		this.type = type;
	}

	public ContactSubtype getSubtype() {
		return subtype;
	}

	public void setSubtype(ContactSubtype subtype) {
		this.subtype = subtype;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Contact contact1 = (Contact) o;
		return type == contact1.type && subtype == contact1.subtype && Objects.equals(contact, contact1.contact);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, subtype, contact);
	}

	@Override
	public String toString() {
		return "Contact{" +
				"type=" + type +
				", subtype=" + subtype +
				", contact='" + contact + '\'' +
				'}';
	}
}

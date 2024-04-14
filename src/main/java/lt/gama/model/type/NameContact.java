package lt.gama.model.type;

import jakarta.persistence.Embeddable;
import lt.gama.helpers.CollectionsHelper;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

//TODO @Embeddable
public class NameContact implements Serializable {

	@Serial
    private static final long serialVersionUID = -1L;

	private String name;

	@JdbcTypeCode(SqlTypes.JSON)
	private List<Contact> contacts;


	public NameContact() {
	}

	public NameContact(String name, List<Contact> contacts) {
		this.name = name;
		this.contacts = contacts;
	}

	public String getContactByTypes(Contact.ContactType... contactType) {
		var contactTypes = List.of(contactType);
		return CollectionsHelper.streamOf(contacts)
				.filter(contact -> contactTypes.contains(contact.getType()))
				.map(Contact::getContact)
				.findFirst()
				.orElse(null);
	}

	// generated

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Contact> getContacts() {
		return contacts;
	}

	public void setContacts(List<Contact> contacts) {
		this.contacts = contacts;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		NameContact that = (NameContact) o;
		return Objects.equals(name, that.name) && Objects.equals(contacts, that.contacts);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, contacts);
	}

	@Override
	public String toString() {
		return "NameContact{" +
				"name='" + name + '\'' +
				", contacts=" + contacts +
				'}';
	}
}
